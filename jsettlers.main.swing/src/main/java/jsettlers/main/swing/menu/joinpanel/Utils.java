package jsettlers.main.swing.menu.joinpanel;

import jsettlers.common.ai.EPlayerType;
import jsettlers.common.player.ECivilisation;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;

public final class Utils {

	private Utils() {
	}

	public static EPlayerType ingame(ELobbyPlayerType playerType) {
		switch (playerType) {
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
	
	public static ELobbyPlayerType lobby(EPlayerType playerType) {
		switch (playerType) {
		case HUMAN:
			return ELobbyPlayerType.HUMAN;
		case AI_EASY:
			return ELobbyPlayerType.AI_EASY;
		case AI_HARD:
			return ELobbyPlayerType.AI_HARD;
		case AI_VERY_EASY:
			return ELobbyPlayerType.AI_VERY_EASY;
		case AI_VERY_HARD:
			return ELobbyPlayerType.AI_VERY_HARD;
		default:
			return null;
		}
	}

	public static ECivilisation ingame(ELobbyCivilisation civilisation) {
		switch (civilisation) {
		case AMAZON:
			return ECivilisation.AMAZON;
		case ASIAN:
			return ECivilisation.ASIAN;
		case EGYPTIAN:
			return ECivilisation.EGYPTIAN;
		case ROMAN:
			return ECivilisation.ROMAN;
		default:
			throw new IllegalStateException();
		}
	}

	public static ELobbyCivilisation lobby(ECivilisation civilisation) {
		switch (civilisation) {
		case AMAZON:
			return ELobbyCivilisation.AMAZON;
		case ASIAN:
			return ELobbyCivilisation.ASIAN;
		case EGYPTIAN:
			return ELobbyCivilisation.EGYPTIAN;
		case ROMAN:
			return ELobbyCivilisation.ROMAN;
		default:
			throw new IllegalStateException();
		}
	}
}
