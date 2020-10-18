package jsettlers.main.swing.menu.joinpanel;

import jsettlers.common.player.ECivilisation;
import jsettlers.main.swing.menu.joinpanel.slots.PlayerSlot;
import jsettlers.network.server.lobby.core.PlayerType;

public interface IJoinGameConnector {

	JoinGamePanel setup();

	void cancel();

	void start();

	PlayerSlot createPlayerSlot(int index);

	void updatePlayer(int index, PlayerType playerType, ECivilisation civilisation, int team, boolean ready);

	void sendChatMessage(String message);
}
