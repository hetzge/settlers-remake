package jsettlers.main.swing.lobby.pages.maps;

import jsettlers.graphics.localization.Labels;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.main.swing.lobby.UiController;
import jsettlers.main.swing.lobby.pages.match.MultiplayerMatchPageController;
import jsettlers.network.client.NetworkClient;

public class MultiplayerCreateMatchMapsPageController extends BaseCreateMatchMapsPageController {

	private final UiController ui;
	private final NetworkClient client;

	public MultiplayerCreateMatchMapsPageController(UiController ui, NetworkClient client) {
		this.ui = ui;
		this.client = client;
	}

	@Override
	public void selectMap(MapLoader mapLoader) {
		MultiplayerMatchPageController.createMatch(ui, client, mapLoader).thenAccept(panel -> {
			this.ui.setPage(Labels.getString("join-game-panel-new-multi-player-game-title"), panel);
		});
	}
}
