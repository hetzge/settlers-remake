package jsettlers.network.server.lobby.core;

import java.util.Objects;

public final class Level {

	private final LevelId id;
	private final String name;
	private final int numberOfPlayers;

	public Level(LevelId id, String name, int numberOfPlayers) {
		this.id = id;
		this.name = name;
		this.numberOfPlayers = numberOfPlayers;
	}

	public LevelId getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getNumberOfPlayers() {
		return numberOfPlayers;
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
		if (!(obj instanceof Level)) {
			return false;
		}
		Level other = (Level) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return String.format("Level [id=%s, name=%s, numberOfPlayers=%s]", id, name, numberOfPlayers);
	}
}
