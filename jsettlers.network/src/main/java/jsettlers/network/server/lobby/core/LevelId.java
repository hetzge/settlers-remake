package jsettlers.network.server.lobby.core;

import java.util.Objects;

public final class LevelId {

	private final String value;

	public LevelId(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof LevelId)) {
			return false;
		}
		LevelId other = (LevelId) obj;
		return Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return String.format("LevelId [value=%s]", value);
	}
}
