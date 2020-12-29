package jsettlers.network.server.lobby;

import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jsettlers.network.NetworkConstants;
import jsettlers.network.NetworkConstants.ENetworkKey;
import jsettlers.network.common.packets.BooleanMessagePacket;
import jsettlers.network.common.packets.ChatMessagePacket;
import jsettlers.network.common.packets.TimeSyncPacket;
import jsettlers.network.infrastructure.channel.Channel;
import jsettlers.network.infrastructure.channel.packet.Packet;
import jsettlers.network.infrastructure.log.LoggerManager;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerState;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.LevelId;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.core.MatchState;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.ResourceAmount;
import jsettlers.network.server.lobby.core.User;
import jsettlers.network.server.lobby.core.UserId;
import jsettlers.network.server.lobby.network.MatchArrayPacket;
import jsettlers.network.server.lobby.network.MatchPacket;
import jsettlers.network.server.lobby.network.PlayerPacket;
import jsettlers.network.server.match.lockstep.TaskCollectingListener;
import jsettlers.network.server.match.lockstep.TaskSendingTimerTask;

public final class Lobby {

	// TODO error handling
	// TODO synchronize
	// TODO control none player via player slot

	private final LobbyDb db;
	private final Map<MatchId, TaskSendingTimerTask> timerTaskByMatchId;

	public Lobby() {
		this(new LobbyDb());
	}

	public Lobby(LobbyDb db) {
		this.db = db;
		this.timerTaskByMatchId = new ConcurrentHashMap<>();
	}

	public void joinLobby(User user) {
		System.out.println("Lobby.join(" + user + ")");
		// If user already exists leave first
		if (db.containsUser(user.getId())) {
			leave(user.getId());
		}
		// Register user in this lobby
		db.setUser(user);
	}

	public void leave(UserId userId) {
		final User user = db.getUser(userId);
		System.out.println("Lobby.leave(" + userId + ")");
		// Leave active match if exists
		leaveMatch(userId);
		// Close and remove channel
		user.getChannel().close();
		db.removeUser(userId);
	}

	public MatchId createMatch(UserId userId, String matchName, LevelId levelId, int maxPlayers) {
		System.out.println("Lobby.createMatch(" + userId + ", " + matchName + ", " + levelId + ", " + maxPlayers + ")");
		final MatchId matchId = MatchId.generate();
		final List<Player> players = IntStream.range(0, maxPlayers)
				.mapToObj(i -> new Player(i, "Player-" + i, null, ELobbyPlayerState.UNKNOWN, ELobbyCivilisation.ROMAN, ELobbyPlayerType.EMPTY, i + 1)).collect(Collectors.toList());
		final Match match = new Match(matchId, matchName, levelId, players, ResourceAmount.HIGH, Duration.ZERO, MatchState.OPENED);
		db.setMatch(match);
		joinMatch(userId, matchId);
		return matchId;
	}

	public void joinMatch(UserId userId, MatchId matchId) {
		final User user = db.getUser(userId);
		final Match match = db.getMatch(matchId);
		// Only join if not already in match
		if (!match.contains(userId)) {
			System.out.println("Lobby.joinMatch(" + userId + ", " + matchId + ")");
			// Leave match if user is part of another match
			leaveMatch(userId);
			// Setup player with user
			final Player player = match.findNextHumanPlayer().orElseThrow(() -> new LobbyException("Failed to join match. No empty slot found."));
			player.setUserId(userId);
			player.setName(user.getUsername());
			player.setType(ELobbyPlayerType.HUMAN);
			player.setState(ELobbyPlayerState.UNKNOWN);
			sendMatchUpdate(ENetworkKey.UPDATE_MATCH, match);
			sendJoinMatch(user, match);
			// Set logger
			user.getChannel().setLogger(match.createLogger());
		}
	}

	public void leaveMatch(UserId userId) {
		if (!db.hasActiveMatch(userId)) {
			return;
		}
		final Match match = db.getActiveMatch(userId);
		System.out.println("Lobby.leaveMatch(" + userId + ")");
		sendKickUser(userId);
		match.getPlayer(userId).ifPresent(player -> {
			if (!player.isHost()) {
				// Update player properties to empty player
				player.setName("---");
				player.setType(ELobbyPlayerType.EMPTY);
				player.setUserId(null);
				player.setState(ELobbyPlayerState.UNKNOWN);
				sendPlayerUpdate(match, player);
			} else {
				// Cancel and remove the timer task
				Optional.ofNullable(timerTaskByMatchId.get(match.getId())).ifPresent(TimerTask::cancel);
				timerTaskByMatchId.remove(match.getId());

				// Leave all other users from the match
				for (UserId id : match.getUserIds()) {
					if (!id.equals(userId)) {
						leaveMatch(id);
					}
				}

				// Remove match from lobby
				db.removeMatch(match.getId());
			}
		});
		final User user = db.getUser(userId);
		// Reset logger
		user.getChannel().setLogger(LoggerManager.ROOT_LOGGER);
		// Remove listener
		user.getChannel().removeListener(ENetworkKey.SYNCHRONOUS_TASK);
	}

	public void startMatch(UserId userId, Timer timer) {
		System.out.println("Lobby.startMatch(" + userId + ")");

		final Match match = db.getActiveMatch(userId);
		if (!match.areAllPlayersReady()) {
			System.out.println("Not all players are ready");
			return; // not all players ready
		}
		if (match.getState() == MatchState.RUNNING) {
			System.out.println("Match already running");
			return; // match already started
		}
		final TaskCollectingListener taskCollectingListener = new TaskCollectingListener();
		final TaskSendingTimerTask taskSendingTimerTask = new TaskSendingTimerTask(match.createLogger(), taskCollectingListener, packet -> {
			sendMatchPacket(match, ENetworkKey.SYNCHRONOUS_TASK, packet, Collections.emptySet());
		});
		timer.schedule(taskSendingTimerTask, NetworkConstants.Client.LOCKSTEP_PERIOD, NetworkConstants.Client.LOCKSTEP_PERIOD / 2 - 2);
		timerTaskByMatchId.put(match.getId(), taskSendingTimerTask);

		final List<UserId> userIds = match.getUserIds();
		for (int i = 0; i < userIds.size(); i++) {

			final Channel channel = db.getUser(userIds.get(i)).getChannel();
			channel.registerListener(taskCollectingListener);

			// needed so that the sending task can adapt to the ping
			channel.setPingUpdateListener(taskSendingTimerTask.getPingListener(i));
		}

		// Set match running
		match.setState(MatchState.RUNNING);
		match.setAiPlayersIngame();
		sendMatchUpdate(ENetworkKey.MATCH_STARTED, match);
	}

	public void setStartFinished(UserId userId, boolean value) {
		final Match match = db.getActiveMatch(userId);
		match.getPlayer(userId).ifPresent(player -> {
			if (value) {
				player.setState(ELobbyPlayerState.INGAME);
				sendPlayerUpdate(match, player);
			}
		});
	}

	public void update(UserId userId, Match matchUpdate) {
		final Match existingMatch = db.getMatch(matchUpdate.getId());
		existingMatch.getPlayer(userId).ifPresent(player -> {
			if (player.isHost()) {
				existingMatch.update(matchUpdate);
			}
			sendMatchUpdate(ENetworkKey.UPDATE_MATCH, existingMatch.withoutPlayers());
		});
	}

	public void updatePlayerType(UserId userId, int playerIndex, ELobbyPlayerType playerType) {
		System.out.println("Lobby.updatePlayerType(" + userId + ", " + playerIndex + ", " + playerType + ")");
		final Player currentPlayer = db.getPlayer(userId);
		final Match match = db.getActiveMatch(userId);
		final Player player = match.getPlayer(playerIndex);
		if (currentPlayer.isHost() || currentPlayer.equals(player)) {
			if (player.getType() != playerType && playerType == ELobbyPlayerType.HUMAN) {
				player.setType(ELobbyPlayerType.EMPTY);
			} else {
				player.setType(playerType);
			}
			// Ai/None players are always ready
			player.setState((playerType.isAi() || playerType == ELobbyPlayerType.NONE) ? ELobbyPlayerState.READY : ELobbyPlayerState.UNKNOWN);
			// user leaves match if set to non human
			if (!player.getType().isHuman()) {
				player.getUserId().ifPresent(this::leaveMatch);
			}
			sendPlayerUpdate(match, player);
		}
	}

	public void updatePlayerReady(UserId userId, int playerIndex, boolean ready) {
		System.out.println("Lobby.updatePlayerReady(" + userId + ", " + playerIndex + ", " + ready + ")");
		final Player currentPlayer = db.getPlayer(userId);
		final Match match = db.getActiveMatch(userId);
		final Player player = match.getPlayer(playerIndex);
		if (currentPlayer.isHost() || currentPlayer.equals(player)) {
			if (player.getType() == ELobbyPlayerType.EMPTY) {
				player.setReady(false);
			} else if (player.getType() == ELobbyPlayerType.NONE) {
				player.setReady(true);
			} else if (player.getType().isAi()) {
				player.setReady(true);
			} else {
				player.setReady(ready);
			}
			sendPlayerUpdate(match, player);
		}
	}

	public void updatePlayerCivilisation(UserId userId, int playerIndex, ELobbyCivilisation civilisation) {
		final Player currentPlayer = db.getPlayer(userId);
		final Match match = db.getActiveMatch(userId);
		final Player player = match.getPlayer(playerIndex);
		if (currentPlayer.isHost() || currentPlayer.equals(player)) {
			player.setCivilisation(civilisation);
			sendPlayerUpdate(match, player);
		}
	}

	public void updatePlayerTeam(UserId userId, int playerIndex, int team) {
		final Player currentPlayer = db.getPlayer(userId);
		final Match match = db.getActiveMatch(userId);
		final Player player = match.getPlayer(playerIndex);
		if (currentPlayer.isHost() || currentPlayer.equals(player)) {
			player.setTeam(team);
			sendPlayerUpdate(match, player);
		}
	}

	private void sendMatchUpdate(ENetworkKey networkKey, Match match) {
		for (UserId userId : match.getUserIds()) {
			final User user = db.getUser(userId);
			user.getChannel().sendPacket(networkKey, new MatchPacket(match));
		}
	}

	private void sendJoinMatch(User user, Match match) {
		user.getChannel().sendPacket(ENetworkKey.JOIN_MATCH, new MatchPacket(match));
	}

	private void sendPlayerUpdate(Match match, Player player) {
		for (UserId userId : match.getUserIds()) {
			final User user = db.getUser(userId);
			user.getChannel().sendPacket(ENetworkKey.UPDATE_PLAYER, new PlayerPacket(player));
		}
	}

	public void sendMatches(UserId userId) {
		final User user = db.getUser(userId);
		user.getChannel().sendPacket(ENetworkKey.UPDATE_MATCHES, getMatchArrayPacket());
	}

	private MatchArrayPacket getMatchArrayPacket() {
		final Match[] matches = db.getMatches().toArray(new Match[0]);
		final MatchArrayPacket packet = new MatchArrayPacket(matches);
		return packet;
	}

	public void sendMatchChatMessage(UserId authorUserId, String message) {
		System.out.println("Lobby.sendMatchChatMessage(" + authorUserId + ", " + message + ")");
		final Match match = db.getActiveMatch(authorUserId);
		sendMatchPacket(match, ENetworkKey.CHAT_MESSAGE, new ChatMessagePacket(authorUserId.getValue(), message), Collections.emptySet());
	}

	public void sendMatchTimeSync(UserId userId, TimeSyncPacket packet) {
		final Match match = db.getActiveMatch(userId);
		final Set<UserId> exclude = new HashSet<>();
		exclude.add(userId);
		sendMatchPacket(match, NetworkConstants.ENetworkKey.TIME_SYNC, packet, exclude);
		timerTaskByMatchId.get(match.getId()).receivedLockstepAcknowledge(packet.getTime() / NetworkConstants.Client.LOCKSTEP_PERIOD);
	}

	private void sendKickUser(UserId userId) {
		db.getUser(userId).getChannel().sendPacket(ENetworkKey.KICK_USER, new BooleanMessagePacket(true));
	}

	private void sendMatchPacket(Match match, ENetworkKey networkKey, Packet packet, Set<UserId> exclude) {
		for (UserId userId : match.getUserIds()) {
			if (!exclude.contains(userId)) {
				final User user = db.getUser(userId);
				user.getChannel().sendPacket(networkKey, packet);
			}
		}
	}
}
