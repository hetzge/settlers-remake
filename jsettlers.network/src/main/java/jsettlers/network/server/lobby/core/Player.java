package jsettlers.network.server.lobby.core;

import java.util.Objects;

public final class Player {

	private final PlayerId id;
	private final String name;
	private final Civilisation civilisation;
	private final PlayerType type;
	private final int position;
	private final int team;
	private final boolean ready;

	public Player(PlayerId id, String name, Civilisation civilisation, PlayerType type, int position, int team, boolean ready) {
		this.id = id;
		this.name = name;
		this.civilisation = civilisation;
		this.type = type;
		this.position = position;
		this.team = team;
		this.ready = ready;
	}

	public Player withCivilisation(Civilisation civilisation) {
		return new Player(id, name, civilisation, type, position, team, ready);
	}

	public Player withType(PlayerType type) {
		return new Player(id, name, civilisation, type, position, team, ready);
	}

	public Player withPosition(int position) {
		return new Player(id, name, civilisation, type, position, position, ready);
	}

	public Player withTeam(int team) {
		return new Player(id, name, civilisation, type, team, team, ready);
	}

	public Player withReady(boolean ready) {
		return new Player(id, name, civilisation, type, position, team, ready);
	}

	public PlayerId getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Civilisation getCivilisation() {
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
		return String.format("Player [id=%s, name=%s, civilisation=%s, type=%s, position=%s, team=%s, ready=%s]", id, name, civilisation, type, position, team, ready);
	}
}
