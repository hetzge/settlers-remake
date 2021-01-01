package jsettlers.main.swing.lobby.pages.maps;

import jsettlers.logic.map.loading.MapLoader;
import jsettlers.main.swing.lobby.UiController;

public class SingleplayerCreateMatchMapsPageController extends BaseCreateMatchMapsPageController {

	private final UiController ui;

	public SingleplayerCreateMatchMapsPageController(UiController ui) {
		this.ui = ui;
	}

	@Override
	public void selectMap(MapLoader mapLoader) {
		this.ui.getFrame().showNewSinglePlayerGameMenu(mapLoader);
	}
}
