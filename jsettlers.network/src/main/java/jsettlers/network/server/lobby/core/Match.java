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
	private ResourceAmount resourceAmount;
	private Duration peaceTime;
	private MatchState state;

	public Match(MatchId id, String name, LevelId levelId, Player[] players, ResourceAmount resourceAmount, Duration peaceTime, MatchState state) {
		this.id = id;
		this.name = name;
		this.levelId = levelId;
		this.players = players;
		this.resourceAmount = resourceAmount;
		this.peaceTime = peaceTime;
		this.state = state;
	}

	public void update(Match match) {
		setPeaceTime(match.peaceTime);
		setResourceAmount(match.resourceAmount);
		setState(match.state);
	}

	public boolean contains(PlayerId playerId) {
		return getPlayer(playerId).isPresent();
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

	public void setPlayerByPosition(Player newPlayer) {
		players[newPlayer.getPosition()] = newPlayer;
	}

	public void setPlayerById(Player newPlayer) {
		getPlayer(newPlayer.getId()).ifPresent(player -> {
			players[player.getPosition()] = newPlayer;
		});
	}

	public void setResourceAmount(ResourceAmount resourceAmount) {
		this.resourceAmount = resourceAmount;
	}

	public void setPeaceTime(Duration peaceTime) {
		this.peaceTime = peaceTime;
	}

	public void setState(MatchState state) {
		this.state = state;
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
