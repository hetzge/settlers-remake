package jsettlers.network.server.lobby;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java8.util.Optional;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.core.Player;
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
		return Optional.ofNullable(userById.get(userId)).orElseThrow(() -> new LobbyException(String.format("User %s not found", userId)));
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
		return Optional.ofNullable(matchById.get(matchId)).orElseThrow(() -> new LobbyException(String.format("Match %s not found", matchId)));
	}

	public void removeMatch(MatchId matchId) {
		matchById.remove(matchId);
	}

	public boolean hasActiveMatch(UserId userId) {
		return matchById.values().stream().anyMatch(match -> match.contains(userId));
	}

	public Match getActiveMatch(UserId userId) {
		return matchById.values().stream().filter(match -> match.contains(userId)).findFirst().orElseThrow(() -> new LobbyException(String.format("No active match for %s found", userId)));
	}

	public Collection<Match> getMatches() {
		return matchById.values();
	}

	public Player getPlayer(UserId userId) {
		return getActiveMatch(userId).getPlayer(userId).orElseThrow(() -> new LobbyException(String.format("Player with id %s not found", userId)));
	}
}
