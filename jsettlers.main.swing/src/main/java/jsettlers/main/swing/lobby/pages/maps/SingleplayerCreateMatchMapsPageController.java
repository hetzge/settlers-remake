package jsettlers.main.swing.lobby.pages.maps;

import jsettlers.logic.map.loading.MapLoader;
import jsettlers.main.swing.lobby.Ui;

public class SingleplayerCreateMatchMapsPageController extends BaseCreateMatchMapsPageController {

	private final Ui ui;

	public SingleplayerCreateMatchMapsPageController(Ui ui) {
		this.ui = ui;
	}

	@Override
	public void selectMap(MapLoader mapLoader) {
		this.ui.getFrame().showNewSinglePlayerGameMenu(mapLoader);
	}
}
