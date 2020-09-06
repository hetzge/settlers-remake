package jsettlers.network.server.lobby.core;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

public final class Match {

	private final MatchId id;
	private final Level level;
	private final Player[] players;
	private final ResourceAmount resourceAmount;
	private final Duration peaceTime;

	public Match(MatchId id, Level level, Player[] players, ResourceAmount resourceAmount, Duration peaceTime) {
		this.id = id;
		this.level = level;
		this.players = players;
		this.resourceAmount = resourceAmount;
		this.peaceTime = peaceTime;
	}

	public Match withPlayer(Player player) {
		Player[] clonedPlayers = players.clone();
		clonedPlayers[player.getPosition()] = player;
		return new Match(id, level, clonedPlayers, resourceAmount, peaceTime);
	}

	public Match withResourceAmount(ResourceAmount resourceAmount) {
		return new Match(id, level, players, resourceAmount, peaceTime);
	}

	public Match withPeaceTime(Duration peaceTime) {
		return new Match(id, level, players, resourceAmount, peaceTime);
	}

	public MatchId getId() {
		return id;
	}

	public Level getLevel() {
		return level;
	}

	public Player[] getPlayers() {
		return players;
	}

	public ResourceAmount getResourceAmount() {
		return resourceAmount;
	}

	public Duration getPeaceTime() {
		return peaceTime;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Match)) {
			return false;
		}
		Match other = (Match) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return String.format("Match [id=%s, level=%s, players=%s, resourceAmount=%s, peaceTime=%s]", id, level, Arrays.toString(players), resourceAmount, peaceTime);
	}
}
