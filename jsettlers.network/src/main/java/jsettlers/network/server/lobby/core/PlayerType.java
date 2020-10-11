package jsettlers.network.server.lobby.core;

import jsettlers.common.ai.EPlayerType;

public enum PlayerType {
	NONE,
	EMPTY,
	HUMAN,
	AI_EASY,
	AI_VERY_EASY,
	AI_HARD,
	AI_VERY_HARD;

	public static final PlayerType[] VALUES = values();

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

	public EPlayerType getPlayerType() {
		switch (this) {
		case HUMAN:
			return EPlayerType.HUMAN;
		case AI_EASY:
			return EPlayerType.AI_EASY;
		case AI_HARD:
			return EPlayerType.AI_HARD;
		case AI_VERY_EASY:
			return EPlayerType.AI_VERY_EASY;
		case AI_VERY_HARD:
			return EPlayerType.AI_VERY_HARD;
		case EMPTY:
		case NONE:
		default:
			return null;
		}
	}

	public static PlayerType from(EPlayerType playerType) {
		switch (playerType) {
		case AI_EASY:
			return AI_EASY;
		case AI_HARD:
			return AI_HARD;
		case AI_VERY_EASY:
			return AI_VERY_EASY;
		case AI_VERY_HARD:
			return AI_VERY_HARD;
		case HUMAN:
			return HUMAN;
		default:
			return NONE;
		}
	}
}
