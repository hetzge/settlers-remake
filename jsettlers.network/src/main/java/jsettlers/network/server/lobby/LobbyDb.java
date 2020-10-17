package jsettlers.network.server.lobby;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java8.util.Optional;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.PlayerId;
import jsettlers.network.server.lobby.core.User;
import jsettlers.network.server.lobby.core.UserId;

public final class LobbyDb {
	private final Map<UserId, User> userById;
	private final Map<MatchId, Match> matchById;

	public LobbyDb() {
		this.userById = new ConcurrentHashMap<>();
		this.matchById = new ConcurrentHashMap<>();
	}

	public void setUser(User user) {
		this.userById.put(user.getId(), user);
	}

	public User getUser(UserId userId) {
		return Optional.ofNullable(userById.get(userId)).orElseThrow(() -> new LobbyDbException("User not found"));
	}

	public Collection<User> getUsers() {
		return userById.values();
	}

	public boolean containsUser(UserId userId) {
		return userById.containsKey(userId);
	}

	public void removeUser(UserId userId) {
		userById.remove(userId);
	}

	public void setMatch(Match match) {
		this.matchById.put(match.getId(), match);
	}

	public Match getMatch(MatchId matchId) {
		return Optional.ofNullable(matchById.get(matchId)).orElseThrow(() -> new LobbyDbException("Match not found"));
	}

	public void removeMatch(MatchId matchId) {
		matchById.remove(matchId);
	}

	public boolean hasActiveMatch(UserId userId) {
		final PlayerId playerId = userId.getPlayerId();
		for (Match match : matchById.values()) {
			if (match.contains(playerId)) {
				return true;
			}
		}
		return false;
	}

	public Match getActiveMatch(UserId userId) {
		final PlayerId playerId = userId.getPlayerId();
		for (Match match : matchById.values()) {
			if (match.contains(playerId)) {
				return match;
			}
		}
		throw new LobbyDbException("No active match for user found");
	}

	public Collection<Match> getMatches() {
		return matchById.values();
	}

	public Player getPlayer(UserId userId) {
		return getActiveMatch(userId).getPlayer(userId.getPlayerId()).orElseThrow(() -> new LobbyDbException("Player not found"));
	}

	public Player getPlayer(UserId currentUserId, PlayerId playerId) {
		return getActiveMatch(currentUserId).getPlayer(playerId).orElseThrow(() -> new LobbyDbException("Player not found"));
	}

	public static class LobbyDbException extends RuntimeException {
		public LobbyDbException(String message, Throwable cause) {
			super(message, cause);
		}

		public LobbyDbException(String message) {
			super(message);
		}

		public LobbyDbException(Throwable cause) {
			super(cause);
		}
	}
}
