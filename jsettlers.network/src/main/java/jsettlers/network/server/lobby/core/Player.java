package jsettlers.network.server.lobby.core;

import java.util.Objects;
import java.util.Optional;

public final class Player {

	private final int index;
	private String name;
	private UserId userId;
	private ELobbyPlayerState state;
	private ELobbyCivilisation civilisation;
	private ELobbyPlayerType type;
	private int team;
	private boolean ready;

	public Player(int index, String name, UserId userId, ELobbyPlayerState state, ELobbyCivilisation civilisation, ELobbyPlayerType type, int team, boolean ready) {
		this.index = index;
		this.name = name;
		this.userId = userId;
		this.state = state;
		this.civilisation = civilisation;
		this.type = type;
		this.team = team;
		this.ready = ready;
	}

	public void set(Player player) {
		setCivilisation(player.civilisation);
		setType(player.type);
		setReady(player.ready);
		setTeam(player.team);
	}

	public boolean isHost() {
		return index == 0;
	}
	
	public boolean isUser(UserId userId) {
		return getUserId().filter(userId::equals).isPresent();
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public Optional<UserId> getUserId() {
		return Optional.ofNullable(userId);
	}

	public ELobbyPlayerState getState() {
		return state;
	}

	public ELobbyCivilisation getCivilisation() {
		return civilisation;
	}

	public ELobbyPlayerType getType() {
		return type;
	}

	public int getTeam() {
		return team;
	}

	public boolean isReady() {
		return ready;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUserId(UserId userId) {
		this.userId = userId;
	}

	public void setCivilisation(ELobbyCivilisation civilisation) {
		this.civilisation = civilisation;
	}

	public void setState(ELobbyPlayerState state) {
		this.state = state;
	}

	public void setType(ELobbyPlayerType type) {
		this.type = type;
	}

	public void setTeam(int team) {
		this.team = team;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
	}

	@Override
	public int hashCode() {
		return Objects.hash(index);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Player)) {
			return false;
		}
		Player other = (Player) obj;
		return index == other.index;
	}

	@Override
	public String toString() {
		return String.format("Player [index=%s, name=%s, userId=%s, state=%s, civilisation=%s, type=%s, team=%s, ready=%s]", index, name, userId, state, civilisation, type, team, ready);
	}
}
