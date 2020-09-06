package jsettlers.network.server.lobby.core;

import java.util.Objects;

public final class PlayerId {

	private final String value;

	public PlayerId(String value) {
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
		if (!(obj instanceof PlayerId)) {
			return false;
		}
		PlayerId other = (PlayerId) obj;
		return Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return String.format("PlayerId [value=%s]", value);
	}
}
