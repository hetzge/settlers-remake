package jsettlers.main.swing.menu.joinpanel.controller;

import java.time.Duration;

import javax.swing.SwingUtilities;

import jsettlers.common.ai.EPlayerType;
import jsettlers.common.menu.IStartingGame;
import jsettlers.graphics.localization.Labels;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.main.JSettlersGame;
import jsettlers.main.swing.JSettlersFrame;
import jsettlers.main.swing.menu.joinpanel.JoinGamePanel;
import jsettlers.main.swing.menu.joinpanel.PlayerSlot;
import jsettlers.main.swing.settings.SettingsManager;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.ELobbyResourceAmount;
import jsettlers.network.server.lobby.core.Player;

public final class SingleplayerJoinGameController implements IJoinGameController {

	private final JSettlersFrame settlersFrame;
	private final MapLoader mapLoader;
	private final JoinGamePanel panel;

	public SingleplayerJoinGameController(JSettlersFrame settlersFrame, MapLoader mapLoader) {
		this.settlersFrame = settlersFrame;
		this.mapLoader = mapLoader;
		this.panel = new JoinGamePanel(this);
	}

	@Override
	public JoinGamePanel setup() {
		// Setup ui
		SwingUtilities.invokeLater(() -> {
			this.panel.setTitle(Labels.getString("join-game-panel-new-single-player-game-title"));
			this.panel.setupHost(true);
			this.panel.setChatVisible(false);
			this.panel.setupMap(mapLoader);
		});
		return this.panel;
	}

	@Override
	public void cancel() {
		// this.settlersFrame.showMainMenu();
	}

	@Override
	public void start() {
		final long randomSeed = System.currentTimeMillis();
		final PlayerSetting[] playerSettings = panel.getPlayerSettings();
		final byte playerId = firstHumanPlayerId(playerSettings);
		final JSettlersGame game = new JSettlersGame(mapLoader, randomSeed, playerId, playerSettings);
		final IStartingGame startingGame = game.start();
		settlersFrame.showStartingGamePanel(startingGame);
	}

	private byte firstHumanPlayerId(final PlayerSetting[] playerSettings) {
		byte playerId = (byte) 0;
		for (int i = 0; i < playerSettings.length; i++) {
			if (playerSettings[i].getPlayerType() == EPlayerType.HUMAN) {
				return playerId;
			} else {
				playerId++;
			}
		}
		return (byte) 0;
	}

	@Override
	public PlayerSlot createPlayerSlot(Player player) {
		final int index = player.getIndex();
		final ELobbyPlayerType[] playerTypes = index == 0
				? new ELobbyPlayerType[] {
						ELobbyPlayerType.HUMAN }
				: new ELobbyPlayerType[] {
						ELobbyPlayerType.NONE,
						ELobbyPlayerType.AI_VERY_HARD,
						ELobbyPlayerType.AI_HARD,
						ELobbyPlayerType.AI_EASY,
						ELobbyPlayerType.AI_VERY_EASY };
		final PlayerSlot playerSlot = new PlayerSlot(this, index, mapLoader.getMaxPlayers(), playerTypes);
		if (index == 0) {
			final SettingsManager settingsManager = SettingsManager.getInstance();
			playerSlot.setPlayerName(settingsManager.getPlayer().getName());
		}
		playerSlot.setReady(true);
		return playerSlot;
	}

	@Override
	public void updatePlayerType(int playerIndex, ELobbyPlayerType playerType) {
	}

	@Override
	public void updatePlayerCivilisation(int playerIndex, ELobbyCivilisation civilisation) {
	}

	@Override
	public void updatePlayerTeam(int playerIndex, int team) {
	}

	@Override
	public void updatePlayerReady(int playerIndex, boolean ready) {
	}

	@Override
	public void updateMatch(Duration peaceTime, ELobbyResourceAmount startResources) {
	}

	@Override
	public void sendChatMessage(String message) {
	}

}
