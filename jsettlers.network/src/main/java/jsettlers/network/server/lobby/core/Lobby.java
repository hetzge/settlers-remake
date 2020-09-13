package jsettlers.network.server.lobby.core;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import jsettlers.network.NetworkConstants;
import jsettlers.network.NetworkConstants.ENetworkKey;
import jsettlers.network.common.packets.ChatMessagePacket;
import jsettlers.network.common.packets.MatchInfoPacket;
import jsettlers.network.common.packets.MatchStartPacket;
import jsettlers.network.infrastructure.channel.Channel;
import jsettlers.network.infrastructure.channel.packet.Packet;
import jsettlers.network.infrastructure.log.Logger;
import jsettlers.network.infrastructure.log.LoggerManager;
import jsettlers.network.server.exceptions.NotAllPlayersReadyException;
import jsettlers.network.server.lobby.network.MatchArrayPacket;
import jsettlers.network.server.lobby.network.MatchPacket;
import jsettlers.network.server.match.EMatchState;
import jsettlers.network.server.match.lockstep.TaskCollectingListener;
import jsettlers.network.server.match.lockstep.TaskSendingTimerTask;
import jsettlers.network.server.packets.ServersideSyncTasksPacket;

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
	private final Map<MatchId, TimerTask> timerTaskByMatchId;

	public Lobby() {
		this.userById = new LinkedHashMap<>();
		this.matchById = new LinkedHashMap<>();
		this.timerTaskByMatchId = new HashMap<>();
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

	public void createMatch(UserId userId, LevelId levelId, int maxPlayers) {
		final MatchId matchId = MatchId.generate();
		update(new Match(matchId, levelId, new Player[maxPlayers], ResourceAmount.HIGH, Duration.ZERO));
		joinMatch(userId, matchId);
	}

	public void joinMatch(UserId userId, MatchId matchId) {
		Optional.ofNullable(userById.get(userId)).ifPresent(user -> {
			Optional.ofNullable(matchById.get(matchId)).ifPresent(match -> {
				synchronized (match) {
					// Leave match first if user already contained
					leaveMatch(userId);
					match.findNextHumanPlayerPosition().ifPresent(position -> {
						// Add player to match
						final Player existingPlayer = match.getPlayers()[position];
						update(match.withPlayer(new Player(userId.getPlayerId(), user.getUsername(), existingPlayer.getCivilisation(), PlayerType.HUMAN, position, existingPlayer.getTeam(), false)));
					});
				}
			});
		});
	}

	public void leaveMatch(UserId userId) {
		getActiveMatch(userId).ifPresent(match -> {
			synchronized (match) {
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

						// Leave all users from the match
						for (UserId id : match.getUserIds()) {
							leaveMatch(id);
						}

						// Remove match from lobby
						matchById.remove(match.getId());
					}
				});
			}
		});
	}

	public void startMatch(UserId userId, Timer timer) {
		// if (state == EMatchState.RUNNING || state == EMatchState.FINISHED) {
		// return; // match already started
		// }
		if (!getActiveMatch(userId).map(Match::areAllPlayersReady).orElse(false)) {
			throw new NotAllPlayersReadyException();
		}
		getActiveMatch(userId).ifPresent(match -> {
			synchronized (match) {
				final Logger logger = match.createLogger();
				final TaskCollectingListener taskCollectingListener = new TaskCollectingListener();
				final TaskSendingTimerTask taskSendingTimerTask = new TaskSendingTimerTask(logger, taskCollectingListener, packet -> {
					sendMatchPacket(match, ENetworkKey.SYNCHRONOUS_TASK, packet);
				});
				timer.schedule(taskSendingTimerTask, NetworkConstants.Client.LOCKSTEP_PERIOD, NetworkConstants.Client.LOCKSTEP_PERIOD / 2 - 2);
				timerTaskByMatchId.put(match.getId(), taskSendingTimerTask);

				for (UserId id : match.getUserIds()) {
					Optional.ofNullable(userById.get(id)).ifPresent(user -> {
						final Channel channel = user.getChannel();
						// TODO unregister listener
						channel.registerListener(taskCollectingListener);
						channel.sendPacket(NetworkConstants.ENetworkKey.MATCH_STARTED, new MatchStartPacket(new MatchInfoPacket(this), 0L));

						// needed so that the sending task can adapt to the ping
						channel.setPingUpdateListener(taskSendingTimerTask.getPingListener(i));
					});
				}
			}
		});
	}

	// TODO add userid and access controll
	// TODO extract to match and player
	public void update(Match match) {
		synchronized (match) {
			if (!matchById.containsKey(match.getId())) {
				matchById.put(match.getId(), match);
			}

			final Match existingMatch = matchById.get(match.getId());

			Match newMatch = existingMatch;
			if (!match.getResourceAmount().equals(existingMatch.getResourceAmount())) {
				newMatch = existingMatch.withResourceAmount(match.getResourceAmount());
			}
			if (!match.getPeaceTime().equals(existingMatch.getPeaceTime())) {
				newMatch = existingMatch.withPeaceTime(match.getPeaceTime());
			}
			final Player[] players = match.getPlayers();
			for (int i = 0; i < players.length; i++) {

				final Player player = players[i];
				final Player existingPlayer = existingMatch.getPlayers()[i];

				if (player.getTeam() != existingPlayer.getTeam()) {
					newMatch = newMatch.withPlayer(existingPlayer.withTeam(player.getTeam()));
				}
				if (player.getType().equals(existingPlayer.getType())) {
					newMatch = newMatch.withPlayer(existingPlayer.withType(player.getType()));
				}
				if (player.getPosition() != existingPlayer.getPosition()) {
					newMatch = newMatch.withPlayer(existingPlayer.withPosition(player.getPosition()));
					newMatch = newMatch.withPlayer(existingMatch.getPlayers()[player.getPosition()].withPosition(existingPlayer.getPosition()));
				}
				if (player.isReady() != existingPlayer.isReady()) {
					newMatch = newMatch.withPlayer(existingPlayer.withReady(player.isReady()));
				}
			}

			matchById.put(match.getId(), newMatch);
			sendMatchUpdate(newMatch);
		}
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
