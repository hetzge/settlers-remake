package jsettlers.network.server.lobby.core;

import java.util.Objects;

import jsettlers.common.player.ECivilisation;

public final class Player {

	private final PlayerId id;
	private final String name;
	private EPlayerState state;
	private ECivilisation civilisation;
	private PlayerType type;
	private int position;
	private int team;
	private boolean ready;

	public Player(PlayerId id, String name, EPlayerState state, ECivilisation civilisation, PlayerType type, int position, int team, boolean ready) {
		this.id = id;
		this.name = name;
		this.state = state;
		this.civilisation = civilisation;
		this.type = type;
		this.position = position;
		this.team = team;
		this.ready = ready;
	}

	public void set(Player player) {
		setCivilisation(player.civilisation);
		setType(player.type);
		setPosition(player.position);
		setReady(player.ready);
		setTeam(player.team);
	}

	public boolean isHost() {
		return position == 0;
	}

	public PlayerId getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public EPlayerState getState() {
		return state;
	}

	public ECivilisation getCivilisation() {
		return civilisation;
	}

	public PlayerType getType() {
		return type;
	}

	public int getPosition() {
		return position;
	}

	public int getTeam() {
		return team;
	}

	public boolean isReady() {
		return ready;
	}

	public void setCivilisation(ECivilisation civilisation) {
		this.civilisation = civilisation;
	}

	public void setState(EPlayerState state) {
		this.state = state;
	}

	public void setType(PlayerType type) {
		this.type = type;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void setTeam(int team) {
		this.team = team;
	}

	public void setReady(boolean ready) {
		this.ready = ready;
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
		if (!(obj instanceof Player)) {
			return false;
		}
		Player other = (Player) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return String.format("Player [id=%s, name=%s, state=%s, civilisation=%s, type=%s, position=%s, team=%s, ready=%s]", id, name, state, civilisation, type, position, team, ready);
	}
}
