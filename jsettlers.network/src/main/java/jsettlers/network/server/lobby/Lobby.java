package jsettlers.network.server.lobby;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jsettlers.common.player.ECivilisation;
import jsettlers.network.NetworkConstants;
import jsettlers.network.NetworkConstants.ENetworkKey;
import jsettlers.network.common.packets.ChatMessagePacket;
import jsettlers.network.common.packets.TimeSyncPacket;
import jsettlers.network.infrastructure.channel.Channel;
import jsettlers.network.infrastructure.channel.packet.Packet;
import jsettlers.network.infrastructure.log.LoggerManager;
import jsettlers.network.server.lobby.core.EPlayerState;
import jsettlers.network.server.lobby.core.LevelId;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.core.MatchState;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.PlayerId;
import jsettlers.network.server.lobby.core.PlayerType;
import jsettlers.network.server.lobby.core.ResourceAmount;
import jsettlers.network.server.lobby.core.User;
import jsettlers.network.server.lobby.core.UserId;
import jsettlers.network.server.lobby.network.MatchArrayPacket;
import jsettlers.network.server.lobby.network.MatchPacket;
import jsettlers.network.server.lobby.network.PlayerPacket;
import jsettlers.network.server.match.EMatchState;
import jsettlers.network.server.match.lockstep.TaskCollectingListener;
import jsettlers.network.server.match.lockstep.TaskSendingTimerTask;

public final class Lobby {

	// TODO error handling
	// TODO add database layer
	// TODO synchronize
	// TODO Left players

	private final LobbyDb db;
	private final Map<MatchId, TaskSendingTimerTask> timerTaskByMatchId;

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
				.mapToObj(i -> new Player(PlayerId.generate(), "Player-" + i, EPlayerState.UNKNOWN, ECivilisation.ROMAN, PlayerType.EMPTY, i, i + 1, false)).collect(Collectors.toList());
		final Match match = new Match(matchId, matchName, levelId, players, ResourceAmount.HIGH, Duration.ZERO, MatchState.OPENED);
		db.setMatch(match);
		joinMatch(userId, matchId);
		return matchId;
	}

	public void joinMatch(UserId userId, MatchId matchId) {
		final User user = db.getUser(userId);
		final Match match = db.getMatch(matchId);
		// Only join if not already in match
		if (!match.contains(userId.getPlayerId())) {
			System.out.println("Lobby.joinMatch(" + userId + ", " + matchId + ")");
			// Leave match if user is part of another match
			leaveMatch(userId);
			match.findNextHumanPlayerPosition().ifPresent(position -> {
				// Add player to match
				final Player existingPlayer = match.getPlayers().get(position);
				match.setPlayerByPosition(new Player(userId.getPlayerId(), user.getUsername(), EPlayerState.UNKNOWN, existingPlayer.getCivilisation(), PlayerType.HUMAN, position,
						existingPlayer.getTeam(), false));
				sendMatchUpdate(ENetworkKey.UPDATE_MATCH, match);
			});
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
		final PlayerId playerId = userId.getPlayerId();
		match.getPlayer(playerId).ifPresent(existingPlayer -> {
			if (!existingPlayer.isHost()) {
				// Replace with empty player
				match.setPlayerByPosition(new Player(PlayerId.generate(), "---", EPlayerState.UNKNOWN, existingPlayer.getCivilisation(), PlayerType.EMPTY, existingPlayer.getPosition(),
						existingPlayer.getTeam(), false));
				sendMatchUpdate(ENetworkKey.UPDATE_MATCH, match);
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
			return; // not all players ready
		}
		if (match.getState() == MatchState.RUNNING) {
			return; // match already started
		}
		final TaskCollectingListener taskCollectingListener = new TaskCollectingListener();
		final TaskSendingTimerTask taskSendingTimerTask = new TaskSendingTimerTask(match.createLogger(), taskCollectingListener, packet -> {
			sendMatchPacket(match, ENetworkKey.SYNCHRONOUS_TASK, packet);
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
		match.getPlayer(userId.getPlayerId()).ifPresent(player -> {
			if (value) {
				player.setState(EPlayerState.INGAME);
				sendPlayerUpdate(match, player);
			}
		});
	}

	public void update(UserId userId, Match matchUpdate) {
		final Match existingMatch = db.getMatch(matchUpdate.getId());
		existingMatch.getPlayer(userId.getPlayerId()).ifPresent(player -> {
			if (player.isHost()) {
				existingMatch.update(matchUpdate);
			}
			sendMatchUpdate(ENetworkKey.UPDATE_MATCH, existingMatch.withoutPlayers());
		});
	}

	public void update(UserId userId, Player updatePlayer) {
		final Match existingMatch = db.getActiveMatch(userId);
		final Player currentPlayer = db.getPlayer(userId);
		System.out.println("Lobby.update(" + userId + ", " + updatePlayer + ")");
		if (updatePlayer.getId().equals(userId.getPlayerId()) || currentPlayer.isHost()) {
			final Player player = db.getPlayer(userId, updatePlayer.getId());

			player.setCivilisation(updatePlayer.getCivilisation());
			player.setTeam(updatePlayer.getTeam());

			final Optional<UserId> playerUserIdOptional = player.getId().getUserId();
			if (player.getType() != updatePlayer.getType() && updatePlayer.getType().isHuman()) {
				player.setReady(false);
			} else if (updatePlayer.getType() == PlayerType.EMPTY) {
				player.setReady(false);
				// user leaves match if set to NONE
				playerUserIdOptional.ifPresent(this::leaveMatch);
			} else if (updatePlayer.getType() == PlayerType.NONE) {
				player.setReady(true);
				// user leaves match if set to NONE
				playerUserIdOptional.ifPresent(this::leaveMatch);
			} else if (updatePlayer.getType().isAi()) {
				player.setReady(true);
			} else {
				player.setReady(updatePlayer.isReady());
			}

			if (player.getType() != updatePlayer.getType() && updatePlayer.getType() == PlayerType.HUMAN) {
				player.setType(PlayerType.EMPTY);
			} else {
				player.setType(updatePlayer.getType());
			}

			sendPlayerUpdate(existingMatch, player);
		}
	}

	private void sendMatchUpdate(ENetworkKey networkKey, Match match) {
		for (UserId userId : match.getUserIds()) {
			final User user = db.getUser(userId);
			user.getChannel().sendPacket(networkKey, new MatchPacket(match));
		}
	}

	private void sendPlayerUpdate(Match match, Player player) {
		for (UserId userId : match.getUserIds()) {
			final User user = db.getUser(userId);
			user.getChannel().sendPacket(ENetworkKey.UPDATE_PLAYER, new PlayerPacket(player));
		}
	}

	public void sendMatches() {
		final MatchArrayPacket packet = getMatchArrayPacket();
		for (User user : db.getUsers()) {
			user.getChannel().sendPacket(ENetworkKey.UPDATE_MATCHES, packet);
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
		sendMatchPacket(match, ENetworkKey.CHAT_MESSAGE, new ChatMessagePacket(authorUserId.getValue(), message));
	}

	public void sendMatchTimeSync(UserId userId, TimeSyncPacket packet) {
		final Match match = db.getActiveMatch(userId);
		sendMatchPacket(match, NetworkConstants.ENetworkKey.TIME_SYNC, packet);
		timerTaskByMatchId.get(match.getId()).receivedLockstepAcknowledge(packet.getTime() / NetworkConstants.Client.LOCKSTEP_PERIOD);
	}

	private void sendMatchPacket(Match match, ENetworkKey networkKey, Packet packet) {
		for (UserId userId : match.getUserIds()) {
			final User user = db.getUser(userId);
			user.getChannel().sendPacket(networkKey, packet);
		}
	}
}
