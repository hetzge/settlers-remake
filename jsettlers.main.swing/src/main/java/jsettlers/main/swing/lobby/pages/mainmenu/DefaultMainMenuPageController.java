package jsettlers.main.swing.lobby.pages.mainmenu;

import jsettlers.graphics.localization.Labels;
import jsettlers.main.MultiplayerConnector;
import jsettlers.main.swing.lobby.UiController;
import jsettlers.main.swing.lobby.pages.maps.MapsPagePanel;
import jsettlers.main.swing.lobby.pages.maps.MultiplayerCreateMatchMapsPageController;
import jsettlers.main.swing.lobby.pages.maps.MultiplayerJoinMatchMapsPageController;
import jsettlers.main.swing.lobby.pages.maps.SavegameMapsPageController;
import jsettlers.main.swing.lobby.pages.maps.SingleplayerCreateMatchMapsPageController;
import jsettlers.main.swing.lobby.pages.settings.DefaultSettingsPageController;
import jsettlers.main.swing.settings.ServerEntry;
import jsettlers.main.swing.settings.SettingsManager;
import jsettlers.network.client.IClientConnection;
import jsettlers.network.client.NetworkClient;

public class DefaultMainMenuPageController implements MainMenuPageController {

	private final UiController ui;
	private final ServerEntry serverEntry;

	public DefaultMainMenuPageController(UiController ui, ServerEntry serverEntry) {
		this.ui = ui;
		this.serverEntry = serverEntry;
	}

	@Override
	public void startSingleplayerMatch() {
		ui.setPage(Labels.getString("main-panel-new-single-player-game-button"), new MapsPagePanel(ui, new SingleplayerCreateMatchMapsPageController(ui)));
	}

	@Override
	public void loadSingleplayerMatch() {
		ui.setPage(Labels.getString("start-loadgame"), new MapsPagePanel(ui, new SavegameMapsPageController(ui)));
	}

	@Override
	public void createMultiplayerMatch() {
		final IClientConnection connection = this.serverEntry.getConnection();
		final MultiplayerConnector multiplayerConnector = getConnector();
		final NetworkClient networkClient = (NetworkClient) multiplayerConnector.getNetworkClient();
		if (connection.isConnected()) {
			ui.setPage(Labels.getString("join-game-panel-new-multi-player-game-title"), new MapsPagePanel(ui, new MultiplayerCreateMatchMapsPageController(ui, networkClient)));
		} else {
			ui.showAlert("TODO"); // TODO
		}
	}

	@Override
	public void joinMultiplayerMatch() {
		final IClientConnection connection = this.serverEntry.getConnection();
		final MultiplayerConnector multiplayerConnector = getConnector();
		final NetworkClient networkClient = (NetworkClient) multiplayerConnector.getNetworkClient();
		if (connection.isConnected()) {
			ui.setPage(Labels.getString("join-game-panel-join-multi-player-game-title"), new MapsPagePanel(ui, new MultiplayerJoinMatchMapsPageController(ui, networkClient)));
		} else {
			ui.showAlert("TODO"); // TODO
		}
	}

	@Override
	public void showSettings() {
		this.ui.setPage(Labels.getString("settings-title"), new DefaultSettingsPageController(this.ui).init(SettingsManager.getInstance()));
	}

	@Override
	public void exit() {
		ui.getFrame().exit();
	}

	private MultiplayerConnector getConnector() {
		final IClientConnection connection = serverEntry.getConnection();
		if (!(connection instanceof MultiplayerConnector)) {
			throw new IllegalStateException(String.format("Require %s to create match but is '%s'", MultiplayerConnector.class.getSimpleName(), connection.getClass().getSimpleName()));
		}
		return (MultiplayerConnector) connection;
	}
}
