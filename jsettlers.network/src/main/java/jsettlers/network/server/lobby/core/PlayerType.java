package jsettlers.network.server.lobby.core;

public enum PlayerType {
	NONE,
	EMPTY,
	HUMAN,
	KI_EASY,
	KI_MEDIUM,
	KI_HARD,
	KI_VERY_HARD;
	
	public static final PlayerType[] VALUES = values();

	public boolean canBeReplacedWithHuman() {
		return this != HUMAN && this != NONE;
	}
}
