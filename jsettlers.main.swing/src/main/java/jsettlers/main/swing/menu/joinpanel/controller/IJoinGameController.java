package jsettlers.main.swing.menu.joinpanel.controller;

import java.time.Duration;

import jsettlers.main.swing.menu.joinpanel.JoinGamePanel;
import jsettlers.main.swing.menu.joinpanel.PlayerSlot;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.ResourceAmount;

public interface IJoinGameController {

	JoinGamePanel setup();

	void cancel();

	void start();

	PlayerSlot createPlayerSlot(int index);

	void updatePlayer(int index, ELobbyPlayerType playerType, ELobbyCivilisation civilisation, int team, boolean ready);

	void updateMatch(Duration peaceTime, ResourceAmount startResources);

	void sendChatMessage(String message);
}
