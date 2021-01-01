package jsettlers.main.swing.menu.joinpanel.controller;

import java.time.Duration;

import jsettlers.main.swing.menu.joinpanel.JoinGamePanel;
import jsettlers.main.swing.menu.joinpanel.PlayerSlot;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.ELobbyResourceAmount;
import jsettlers.network.server.lobby.core.Player;

public interface IJoinGameController {

	JoinGamePanel setup();

	void cancel();

	void start();

	PlayerSlot createPlayerSlot(Player player);

	void updateMatch(Duration peaceTime, ELobbyResourceAmount startResources);

	void sendChatMessage(String message);

	void updatePlayerType(int playerIndex, ELobbyPlayerType playerType);

	void updatePlayerCivilisation(int playerIndex, ELobbyCivilisation civilisation);

	void updatePlayerTeam(int playerIndex, int team);

	void updatePlayerReady(int playerIndex, boolean ready);
}
