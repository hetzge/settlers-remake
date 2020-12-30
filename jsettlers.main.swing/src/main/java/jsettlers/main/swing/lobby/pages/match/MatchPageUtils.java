package jsettlers.main.swing.lobby.pages.match;

import java.util.Collection;

import jsettlers.common.ai.EPlayerType;
import jsettlers.common.player.ECivilisation;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.main.swing.menu.joinpanel.Utils;
import jsettlers.network.server.lobby.core.Player;

public final class MatchPageUtils {

	private MatchPageUtils() {
	}

	public static PlayerSetting[] toPlayerSettings(Collection<Player> players) {
		return players.stream().map(player -> {
			final EPlayerType playerType = Utils.ingame(player.getType());
			final boolean available = playerType != null;
			final ECivilisation civilisation = Utils.ingame(player.getCivilisation());
			final byte team = (byte) player.getTeam();
			return new PlayerSetting(available, playerType, civilisation, team);
		}).toArray(PlayerSetting[]::new);
	}
}
