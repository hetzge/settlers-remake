package jsettlers.network.server.lobby.core;

public enum ELobbyPlayerType {
	NONE,
	EMPTY,
	HUMAN,
	AI_EASY,
	AI_VERY_EASY,
	AI_HARD,
	AI_VERY_HARD;

	public static final ELobbyPlayerType[] VALUES = values();

	public boolean canBeReplacedWithHuman() {
		return this == EMPTY;
	}

	public boolean isAi() {
		return !isHuman() && !isEmpty();
	}

	public boolean isHuman() {
		return this == HUMAN;
	}

	public boolean isEmpty() {
		return this == NONE || this == EMPTY;
	}
}
