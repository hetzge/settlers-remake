package jsettlers.main.swing.lobby.pages.maps;

import jsettlers.logic.map.loading.MapLoader;
import jsettlers.main.swing.lobby.Ui;
import jsettlers.main.swing.lobby.pages.match.MultiplayerMatchPageController;
import jsettlers.network.client.NetworkClient;

public class MultiplayerCreateMatchMapsPageController extends BaseCreateMatchMapsPageController {

	private final Ui ui;
	private final NetworkClient client;

	public MultiplayerCreateMatchMapsPageController(Ui ui, NetworkClient client) {
		this.ui = ui;
		this.client = client;
	}

	@Override
	public void selectMap(MapLoader mapLoader) {
		MultiplayerMatchPageController.createMatch(ui, client, mapLoader).thenAccept(panel -> {
			// TODO
			// this.ui.getFrame().showJoinGamePanel(panel);
		});
	}
}
