package jsettlers.network.server.lobby.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class Lobby {

	private final Map<MatchId, Match> matches;

	public Lobby() {
		this.matches = new LinkedHashMap<>();
	}

	public synchronized void update(Match match) {
		if (!matches.containsKey(match.getId())) {
			matches.put(match.getId(), match);
		}

		final Match existingMatch = matches.get(match.getId());

		Match newMatch = existingMatch;
		if (!match.getResourceAmount().equals(existingMatch.getResourceAmount())) {
			newMatch = existingMatch.withResourceAmount(match.getResourceAmount());
		}
		if (!match.getPeaceTime().equals(existingMatch.getPeaceTime())) {
			newMatch = existingMatch.withPeaceTime(match.getPeaceTime());
		}
		final Player[] players = match.getPlayers();
		for (int i = 0; i < players.length; i++) {
			// TODO hide array

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

		matches.put(match.getId(), newMatch);
	}

	public synchronized Optional<Match> get(MatchId matchId) {
		return Optional.ofNullable(matches.get(matchId));
	}
}
