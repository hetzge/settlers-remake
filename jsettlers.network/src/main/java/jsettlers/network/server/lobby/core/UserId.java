package jsettlers.network.server.lobby.core;

import java.util.Objects;

public final class UserId {

	private final String value;

	public UserId(String value) {
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
		if (!(obj instanceof UserId)) {
			return false;
		}
		UserId other = (UserId) obj;
		return Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return String.format("UserId [value=%s]", value);
	}
}
