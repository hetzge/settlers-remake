package jsettlers.network.server.lobby.core;

import java.util.Objects;
import java.util.UUID;

public final class MatchId {

	private final String value;

	public MatchId(String value) {
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
		if (!(obj instanceof MatchId)) {
			return false;
		}
		MatchId other = (MatchId) obj;
		return Objects.equals(value, other.value);
	}

	@Override
	public String toString() {
		return String.format("MatchId [value=%s]", value);
	}

	public static MatchId generate() {
		return new MatchId(UUID.randomUUID().toString());
	}
}
