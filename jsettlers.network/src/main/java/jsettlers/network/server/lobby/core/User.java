package jsettlers.network.server.lobby.core;

import java.util.Objects;

import jsettlers.network.infrastructure.channel.Channel;

public final class User {

	private final UserId id;
	private final String username;
	private final Channel channel;

	public User(UserId id, String username, Channel channel) {
		this.id = id;
		this.username = username;
		this.channel = channel;
	}

	public UserId getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public Channel getChannel() {
		return channel;
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
		if (!(obj instanceof User)) {
			return false;
		}
		User other = (User) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return String.format("User [id=%s, username=%s, channel=%s]", id, username, channel);
	}
}
