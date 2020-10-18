package jsettlers.network.server.lobby;

public class LobbyException extends RuntimeException {
	public LobbyException(String message, Throwable cause) {
		super(message, cause);
	}

	public LobbyException(String message) {
		super(message);
	}

	public LobbyException(Throwable cause) {
		super(cause);
	}
}