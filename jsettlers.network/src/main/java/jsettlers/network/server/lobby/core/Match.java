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
	private final String name;
	private final LevelId levelId;
	private final Player[] players;
	private final ResourceAmount resourceAmount;
	private final Duration peaceTime;
	private final MatchState state;

	public Match(MatchId id, String name, LevelId levelId, Player[] players, ResourceAmount resourceAmount, Duration peaceTime, MatchState state) {
		this.id = id;
		this.name = name;
		this.levelId = levelId;
		this.players = players;
		this.resourceAmount = resourceAmount;
		this.peaceTime = peaceTime;
		this.state = state;
	}

	public Match update(Match match) {
		Match newMatch = this;
		if (!match.getResourceAmount().equals(newMatch.getResourceAmount())) {
			newMatch = newMatch.withResourceAmount(match.getResourceAmount());
		}
		if (!match.getPeaceTime().equals(newMatch.getPeaceTime())) {
			newMatch = newMatch.withPeaceTime(match.getPeaceTime());
		}
		final Player[] newPlayers = match.getPlayers();
		for (int i = 0; i < newPlayers.length; i++) {
			final Player newPlayer = newPlayers[i];
			final Player oldPlayer = getPlayer(newPlayer.getId()).orElseGet(() -> players[newPlayer.getPosition()]);
			newMatch = newMatch.withPlayer(oldPlayer.update(newPlayer));
		}
		return newMatch;
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
		final Player oldPlayer = getPlayer(player.getId()).orElseGet(() -> players[player.getPosition()]);
		final Player[] clonedPlayers = players.clone();
		clonedPlayers[oldPlayer.getPosition()] = players[player.getPosition()].withPosition(oldPlayer.getPosition());
		clonedPlayers[player.getPosition()] = player;
		return new Match(id, name, levelId, clonedPlayers, resourceAmount, peaceTime, state);
	}

	public Match withResourceAmount(ResourceAmount resourceAmount) {
		return new Match(id, name, levelId, players, resourceAmount, peaceTime, state);
	}

	public Match withPeaceTime(Duration peaceTime) {
		return new Match(id, name, levelId, players, resourceAmount, peaceTime, state);
	}

	public Match withState(MatchState state) {
		return new Match(id, name, levelId, players, resourceAmount, peaceTime, state);
	}

	public MatchId getId() {
		return id;
	}

	public String getName() {
		return name;
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

	public MatchState getState() {
		return state;
	}

	public Logger createLogger() {
		return LoggerManager.getMatchLogger(getId().getValue(), name);
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
		return String.format("Match [id=%s, levelId=%s, players=%s, resourceAmount=%s, peaceTime=%s, state=%s]", id, levelId, Arrays.toString(players), resourceAmount, peaceTime, state);
	}
}
