package jsettlers.network.server.lobby.core;

public enum MatchState {
	OPENED,
	RUNNING,
	FINISHED;
	
	public static final MatchState[] VALUES = values();
}