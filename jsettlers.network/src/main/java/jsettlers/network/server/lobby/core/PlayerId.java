package jsettlers.network.server.lobby.core;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PlayerId {
	private static final String USER_PLAYER_ID_PREFIX = "USER::";

	private final String value;

	public PlayerId(UserId userId) {
		this(USER_PLAYER_ID_PREFIX + userId.getValue());
	}

	public PlayerId(String value) {
		this.value = value;
	}

	public Optional<UserId> getUserId() {
		return value.startsWith(USER_PLAYER_ID_PREFIX) ? Optional.of(new UserId(value.substring(USER_PLAYER_ID_PREFIX.length()))) : Optional.empty();
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

	public static PlayerId generate() {
		return new PlayerId(UUID.randomUUID().toString());
	}
}
