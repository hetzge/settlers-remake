package jsettlers.network.server.lobby.core;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
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
	private final List<Player> players;
	private ResourceAmount resourceAmount;
	private Duration peaceTime;
	private MatchState state;

	public Match(MatchId id, String name, LevelId levelId, List<Player> players, ResourceAmount resourceAmount, Duration peaceTime, MatchState state) {
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

	public Match withoutPlayers() {
		return new Match(id, name, levelId, Collections.emptyList(), resourceAmount, peaceTime, state);
	}

	public boolean contains(UserId userId) {
		return getPlayer(userId).isPresent();
	}

	public Player getPlayer(int index) {
		return players.get(index);
	}

	public Optional<Player> getPlayer(UserId userId) {
		return this.players.stream().filter(player -> player.getUserId().map(userId::equals).orElse(false)).findFirst();
	}

	public Optional<Player> findNextHumanPlayer() {
		return players.stream().filter(player -> player.getType().canBeReplacedWithHuman()).findFirst();
	}

	public boolean areAllPlayersReady() {
		for (Player player : players) {
			if (!player.isReady()) {
				return false;
			}
		}
		return true;
	}

	public void setAiPlayersIngame() {
		players.stream().filter(p -> p.getType().isAi()).forEach(p -> p.setState(EPlayerState.INGAME));
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

	public List<Player> getPlayers() {
		return new ArrayList<>(players);
	}

	public List<UserId> getUserIds() {
		return players.stream().map(Player::getUserId).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
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
		players.set(newPlayer.getIndex(), newPlayer);
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
		return String.format("Match [id=%s, name=%s, levelId=%s, players=%s, resourceAmount=%s, peaceTime=%s, state=%s]", id, name, levelId, players, resourceAmount, peaceTime, state);
	}
}
