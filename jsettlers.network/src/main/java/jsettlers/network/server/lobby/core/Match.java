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
		Match copy = this;
		if (!match.getResourceAmount().equals(copy.getResourceAmount())) {
			copy = copy.withResourceAmount(match.getResourceAmount());
		}
		if (!match.getPeaceTime().equals(copy.getPeaceTime())) {
			copy = copy.withPeaceTime(match.getPeaceTime());
		}
		final Player[] players = match.getPlayers();
		for (int i = 0; i < players.length; i++) {

			final Player player = players[i];
			final Player existingPlayer = getPlayers()[i];

			if (player.getTeam() != existingPlayer.getTeam()) {
				copy = copy.withPlayer(existingPlayer.withTeam(player.getTeam()));
			}
			if (player.getType().equals(existingPlayer.getType())) {
				copy = copy.withPlayer(existingPlayer.withType(player.getType()));
			}
			if (player.getPosition() != existingPlayer.getPosition()) {
				copy = copy.withPlayer(existingPlayer.withPosition(player.getPosition()));
				copy = copy.withPlayer(getPlayers()[player.getPosition()].withPosition(existingPlayer.getPosition()));
			}
			if (player.isReady() != existingPlayer.isReady()) {
				copy = copy.withPlayer(existingPlayer.withReady(player.isReady()));
			}
		}
		return copy;
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
