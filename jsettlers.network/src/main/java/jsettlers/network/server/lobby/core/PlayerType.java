package jsettlers.network.server.lobby.core;

public enum PlayerType {
	NONE,
	EMPTY,
	HUMAN,
	KI_EASY,
	KI_MEDIUM,
	KI_HARD,
	KI_VERY_HARD;

	public boolean canBeReplacedWithHuman() {
		return this != HUMAN && this != NONE;
	}
}
