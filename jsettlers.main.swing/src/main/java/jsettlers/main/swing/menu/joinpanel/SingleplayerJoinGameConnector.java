package jsettlers.main.swing.menu.joinpanel;

import javax.swing.SwingUtilities;

import jsettlers.common.menu.IStartingGame;
import jsettlers.common.player.ECivilisation;
import jsettlers.graphics.localization.Labels;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.main.JSettlersGame;
import jsettlers.main.swing.JSettlersFrame;
import jsettlers.main.swing.menu.joinpanel.slots.PlayerSlot;
import jsettlers.main.swing.settings.SettingsManager;
import jsettlers.network.server.lobby.core.PlayerType;

public final class SingleplayerJoinGameConnector implements IJoinGameConnector {

	private final JSettlersFrame settlersFrame;
	private final MapLoader mapLoader;
	private final JoinGamePanel panel;

	public SingleplayerJoinGameConnector(JSettlersFrame settlersFrame, MapLoader mapLoader) {
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
		this.settlersFrame.showMainMenu();
	}

	@Override
	public void start() {
		final long randomSeed = System.currentTimeMillis();
		final PlayerSetting[] playerSettings = panel.getPlayerSettings();
		final JSettlersGame game = new JSettlersGame(mapLoader, randomSeed, (byte) 0, playerSettings);
		final IStartingGame startingGame = game.start();
		settlersFrame.showStartingGamePanel(startingGame);
	}

	@Override
	public PlayerSlot createPlayerSlot(int index) {
		final PlayerType[] playerTypes = index == 0
				? new PlayerType[] {
						PlayerType.HUMAN,
						PlayerType.AI_VERY_HARD,
						PlayerType.AI_HARD,
						PlayerType.AI_EASY,
						PlayerType.AI_VERY_EASY }
				: new PlayerType[] {
						PlayerType.AI_VERY_HARD,
						PlayerType.AI_HARD,
						PlayerType.AI_EASY,
						PlayerType.AI_VERY_EASY };
		final PlayerSlot playerSlot = new PlayerSlot(this, index, mapLoader.getMaxPlayers(), playerTypes);
		if (index == 0) {
			final SettingsManager settingsManager = SettingsManager.getInstance();
			playerSlot.setPlayerName(settingsManager.getPlayer().getName());
		}
		playerSlot.setReady(true);
		return playerSlot;
	}

	@Override
	public void updatePlayer(int index, PlayerType playerType, ECivilisation civilisation, int team, boolean ready) {
	}

	@Override
	public void sendChatMessage(String message) {
	}
}
