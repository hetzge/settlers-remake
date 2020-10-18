package jsettlers.main.swing.menu.joinpanel;

import java.time.Duration;

import jsettlers.common.player.ECivilisation;
import jsettlers.network.server.lobby.core.PlayerType;
import jsettlers.network.server.lobby.core.ResourceAmount;

public interface IJoinGameConnector {

	JoinGamePanel setup();

	void cancel();

	void start();

	PlayerSlot createPlayerSlot(int index);

	void updatePlayer(int index, PlayerType playerType, ECivilisation civilisation, int team, boolean ready);

	void updateMatch(Duration peaceTime, ResourceAmount startResources);

	void sendChatMessage(String message);
}
