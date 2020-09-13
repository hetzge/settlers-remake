package jsettlers.network.server.lobby.core;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import jsettlers.network.infrastructure.log.Logger;
import jsettlers.network.infrastructure.log.LoggerManager;

public final class Match {

	private final MatchId id;
	private final LevelId levelId;
	private final Player[] players;
	private final ResourceAmount resourceAmount;
	private final Duration peaceTime;

	public Match(MatchId id, LevelId levelId, Player[] players, ResourceAmount resourceAmount, Duration peaceTime) {
		this.id = id;
		this.levelId = levelId;
		this.players = players;
		this.resourceAmount = resourceAmount;
		this.peaceTime = peaceTime;
	}

	public Optional<Player> getPlayer(PlayerId playerId) {
		for (int i = 0; i < players.length; i++) {
			if (playerId.equals(players[i].getId())) {
				return Optional.of(players[i]);
			}
		}
		return Optional.empty();
	}

	public Optional<Integer> findNextHumanPlayerPosition() {
		for (int i = 0; i < players.length; i++) {
			if (players[i].getType().canBeReplacedWithHuman()) {
				return Optional.of(players[i].getPosition());
			}
		}
		return Optional.empty();
	}

	public boolean areAllPlayersReady() {
		for (Player player : players) {
			if (!player.isReady()) {
				return false;
			}
		}
		return true;
	}

	public Match withPlayer(Player player) {
		Player[] clonedPlayers = players.clone();
		clonedPlayers[player.getPosition()] = player;
		return new Match(id, levelId, clonedPlayers, resourceAmount, peaceTime);
	}

	public Match withResourceAmount(ResourceAmount resourceAmount) {
		return new Match(id, levelId, players, resourceAmount, peaceTime);
	}

	public Match withPeaceTime(Duration peaceTime) {
		return new Match(id, levelId, players, resourceAmount, peaceTime);
	}

	public MatchId getId() {
		return id;
	}

	public LevelId getLevelId() {
		return levelId;
	}

	public Player[] getPlayers() {
		return Arrays.copyOf(players, players.length);
	}

	public List<UserId> getUserIds() {
		return Arrays.asList(players).stream().map(player -> player.getId().getUserId()).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
	}

	public ResourceAmount getResourceAmount() {
		return resourceAmount;
	}

	public Duration getPeaceTime() {
		return peaceTime;
	}

	public Logger createLogger() {
		// TODO name
		return LoggerManager.getMatchLogger(getId().getValue(), getId().getValue());
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
		return String.format("Match [id=%s, levelId=%s, players=%s, resourceAmount=%s, peaceTime=%s]", id, levelId, Arrays.toString(players), resourceAmount, peaceTime);
	}
}
