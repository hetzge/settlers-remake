package jsettlers.network.server.lobby.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import jsettlers.network.infrastructure.channel.Channel;
import jsettlers.network.server.lobby.network.MatchPacket;

public final class Lobby {

	// TODO error handling
	// TODO add database layer
	// TODO synchronize

	private final Map<UserId, User> userById;
	private final Map<MatchId, Match> matchById;

	public Lobby() {
		this.userById = new LinkedHashMap<>();
		this.matchById = new LinkedHashMap<>();
	}

	public void join(User user, Channel channel) {
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

	public void joinMatch(UserId userId, MatchId matchId) {
		Optional.ofNullable(userById.get(userId)).ifPresent(user -> {
			Optional.ofNullable(matchById.get(matchId)).ifPresent(match -> {
				// Leave match first if user already contained
				leaveMatch(userId);
				match.findNextHumanPlayerPosition().ifPresent(position -> {
					synchronized (match) {
						// Add player to match
						final Player existingPlayer = match.getPlayers()[position];
						update(match.withPlayer(new Player(userId.getPlayerId(), user.getUsername(), existingPlayer.getCivilisation(), PlayerType.HUMAN, position, existingPlayer.getTeam(), false)));
					}
				});
			});
		});
	}

	public void leaveMatch(UserId userId) {
		final PlayerId playerId = userId.getPlayerId();
		for (Match match : matchById.values()) {
			final Optional<Player> playerOptional = match.contains(playerId);
			if (playerOptional.isPresent()) {
				synchronized (match) {
					// Replace with empty player
					final Player existingPlayer = playerOptional.get();
					update(match.withPlayer(new Player(PlayerId.generate(), "---", existingPlayer.getCivilisation(), PlayerType.EMPTY, existingPlayer.getPosition(), existingPlayer.getTeam(), true)));
					break;
				}
			}
		}
	}

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
		for (Player player : match.getPlayers()) {
			player.getId().getUserId().ifPresent(userId -> {
				Optional.ofNullable(userById.get(userId)).ifPresent(user -> {
					user.getChannel().sendPacket(key, new MatchPacket(match));
				});
			});
		}
	}
}
