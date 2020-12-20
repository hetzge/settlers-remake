package jsettlers.main.swing.lobby;

public class ServerModel {
	private final String host;
	private final int port;

	public ServerModel(String address, int port) {
		this.host = address;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	@Override
	public String toString() {
		return String.format("ServerModel [host=%s, port=%s]", host, port);
	}
}
