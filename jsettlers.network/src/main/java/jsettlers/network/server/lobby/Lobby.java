package jsettlers.network.server.lobby;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import jsettlers.network.NetworkConstants;
import jsettlers.network.NetworkConstants.ENetworkKey;
import jsettlers.network.common.packets.ChatMessagePacket;
import jsettlers.network.common.packets.TimeSyncPacket;
import jsettlers.network.infrastructure.channel.Channel;
import jsettlers.network.infrastructure.channel.packet.Packet;
import jsettlers.network.infrastructure.log.LoggerManager;
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
import jsettlers.network.server.match.lockstep.TaskCollectingListener;
import jsettlers.network.server.match.lockstep.TaskSendingTimerTask;

public final class Lobby {

	// TODO error handling
	// TODO add database layer
	// TODO synchronize
	// TODO player package
	// TODO ChatMessagePacket remove author ?!
	// TODO link ENetworkKey with packet class
	// TODO if (state == EMatchState.RUNNING || state == EMatchState.FINISHED) {
	// TODO Left players

	private final Map<UserId, User> userById;
	private final Map<MatchId, Match> matchById;
	private final Map<MatchId, TaskSendingTimerTask> timerTaskByMatchId;

	public Lobby() {
		this.userById = new ConcurrentHashMap<>();
		this.matchById = new ConcurrentHashMap<>();
		this.timerTaskByMatchId = new ConcurrentHashMap<>();
	}

	public void join(User user) {
		// If user already exists leave first
		if (userById.containsKey(user.getId())) {
			leave(user.getId());
		}
		// Register user in this lobby
		userById.put(user.getId(), user);
	}

	public void leave(UserId userId) {
		Optional.ofNullable(userById.get(userId)).ifPresent(user -> {
			// Leave active match if exists
			leaveMatch(userId);
			// Close and remove channel
			user.getChannel().close();
			userById.remove(userId);
		});
	}

	public void createMatch(UserId userId, String matchName, LevelId levelId, int maxPlayers) {
		final MatchId matchId = MatchId.generate();
		update(new Match(matchId, matchName, levelId, new Player[maxPlayers], ResourceAmount.HIGH, Duration.ZERO, MatchState.OPENED));
		joinMatch(userId, matchId);
	}

	public void joinMatch(UserId userId, MatchId matchId) {
		Optional.ofNullable(userById.get(userId)).ifPresent(user -> {
			Optional.ofNullable(matchById.get(matchId)).ifPresent(match -> {
				// Leave match first if user already contained
				leaveMatch(userId);
				match.findNextHumanPlayerPosition().ifPresent(position -> {
					// Add player to match
					final Player existingPlayer = match.getPlayers()[position];
					update(match.withPlayer(new Player(userId.getPlayerId(), user.getUsername(), existingPlayer.getCivilisation(), PlayerType.HUMAN, position, existingPlayer.getTeam(), false)));
				});
				// Set logger
				user.getChannel().setLogger(match.createLogger());
			});
		});
	}

	public void leaveMatch(UserId userId) {
		getActiveMatch(userId).ifPresent(match -> {
			final PlayerId playerId = userId.getPlayerId();
			match.getPlayer(playerId).ifPresent(existingPlayer -> {
				if (!existingPlayer.isHost()) {
					// Replace with empty player
					update(match
							.withPlayer(new Player(PlayerId.generate(), "---", existingPlayer.getCivilisation(), PlayerType.EMPTY, existingPlayer.getPosition(), existingPlayer.getTeam(), true)));
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
					matchById.remove(match.getId());
				}
			});
			Optional.ofNullable(userById.get(userId)).ifPresent(user -> {
				// Reset logger
				user.getChannel().setLogger(LoggerManager.ROOT_LOGGER);
				// Remove listener
				user.getChannel().removeListener(ENetworkKey.SYNCHRONOUS_TASK);
			});
		});
	}

	public void startMatch(UserId userId, Timer timer) {
		// if (state == EMatchState.RUNNING || state == EMatchState.FINISHED) {
		// return; // match already started
		// }
		if (!getActiveMatch(userId).map(Match::areAllPlayersReady).orElse(false)) {
			return;
		}
		getActiveMatch(userId).ifPresent(match -> {
			final TaskCollectingListener taskCollectingListener = new TaskCollectingListener();
			final TaskSendingTimerTask taskSendingTimerTask = new TaskSendingTimerTask(match.createLogger(), taskCollectingListener, packet -> {
				sendMatchPacket(match, ENetworkKey.SYNCHRONOUS_TASK, packet);
			});
			timer.schedule(taskSendingTimerTask, NetworkConstants.Client.LOCKSTEP_PERIOD, NetworkConstants.Client.LOCKSTEP_PERIOD / 2 - 2);
			timerTaskByMatchId.put(match.getId(), taskSendingTimerTask);

			final List<UserId> userIds = match.getUserIds();
			for (int i = 0; i < userIds.size(); i++) {
				final int index = i;
				final UserId id = userIds.get(i);
				Optional.ofNullable(userById.get(id)).ifPresent(user -> {
					final Channel channel = user.getChannel();
					channel.registerListener(taskCollectingListener);

					// needed so that the sending task can adapt to the ping
					channel.setPingUpdateListener(taskSendingTimerTask.getPingListener(index));
				});
			}

			// Set match running
			update(match.withState(MatchState.RUNNING));
		});
	}

	// TODO add userid and access control
	public void update(Match match) {
		final Match newMatch = matchById.computeIfAbsent(match.getId(), key -> match).update(match);
		matchById.put(match.getId(), newMatch);
		sendMatchUpdate(newMatch);
	}

	private void sendMatchUpdate(Match match) {
		for (UserId userId : match.getUserIds()) {
			Optional.ofNullable(userById.get(userId)).ifPresent(user -> {
				user.getChannel().sendPacket(ENetworkKey.UPDATE_MATCH, new MatchPacket(match));
			});
		}
	}

	public void sendMatches() {
		final MatchArrayPacket packet = getMatchArrayPacket();
		for (User user : userById.values()) {
			user.getChannel().sendPacket(ENetworkKey.UPDATE_MATCHES, packet);
		}
	}

	public void sendMatches(UserId userId) {
		Optional.ofNullable(userById.get(userId)).ifPresent(user -> {
			user.getChannel().sendPacket(ENetworkKey.UPDATE_MATCHES, getMatchArrayPacket());
		});
	}

	private MatchArrayPacket getMatchArrayPacket() {
		final Match[] matches = getMatches().toArray(new Match[0]);
		final MatchArrayPacket packet = new MatchArrayPacket(matches);
		return packet;
	}

	public void sendMatchChatMessage(UserId authorUserId, String message) {
		getActiveMatch(authorUserId).ifPresent(match -> {
			sendMatchPacket(match, ENetworkKey.CHAT_MESSAGE, new ChatMessagePacket(authorUserId.getValue(), message));
		});
	}

	public void sendMatchTimeSync(UserId userId, TimeSyncPacket packet) {
		getActiveMatch(userId).ifPresent(match -> {
			sendMatchPacket(match, NetworkConstants.ENetworkKey.TIME_SYNC, packet);
			timerTaskByMatchId.get(match.getId()).receivedLockstepAcknowledge(packet.getTime() / NetworkConstants.Client.LOCKSTEP_PERIOD);
		});
	}

	private void sendMatchPacket(Match match, ENetworkKey networkKey, Packet packet) {
		for (UserId userId : match.getUserIds()) {
			Optional.ofNullable(userById.get(userId)).ifPresent(user -> {
				user.getChannel().sendPacket(networkKey, packet);
			});
		}
	}

	public Collection<Match> getMatches() {
		return matchById.values();
	}

	private Optional<Match> getActiveMatch(UserId userId) {
		final PlayerId playerId = userId.getPlayerId();
		for (Match match : matchById.values()) {
			if (match.getPlayer(playerId).isPresent()) {
				return Optional.of(match);
			}
		}
		return Optional.empty();
	}

}
