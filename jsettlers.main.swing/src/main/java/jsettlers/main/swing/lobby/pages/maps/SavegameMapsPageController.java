package jsettlers.main.swing.lobby.pages.maps;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import jsettlers.common.menu.IStartingGame;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.loading.list.MapList;
import jsettlers.logic.map.loading.newmap.MapFileHeader;
import jsettlers.logic.map.loading.savegame.SavegameLoader;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.main.JSettlersGame;
import jsettlers.main.swing.lobby.UiController;
import jsettlers.main.swing.menu.openpanel.EMapFilter;

public class SavegameMapsPageController implements MapsPageController {

	private final UiController ui;

	public SavegameMapsPageController(UiController ui) {
		this.ui = ui;
	}

	@Override
	public CompletableFuture<Collection<MapLoader>> load(EMapFilter filter, String query) {
		return CompletableFuture.completedFuture(MapList
				.getDefaultList()
				.getSavedMaps()
				.getItems()
				.stream()
				.filter(filter::filter)
				.filter(mapLoader -> MapsPageUtils.isMapLoaderMatchQuery(mapLoader, query))
				.collect(Collectors.toList()));
	}

	@Override
	public void selectMap(MapLoader mapLoader) {
		if (mapLoader instanceof SavegameLoader) {
			final SavegameLoader savegameLoader = (SavegameLoader) mapLoader;
			final MapFileHeader mapFileHeader = savegameLoader.getFileHeader();
			final PlayerSetting[] playerSettings = mapFileHeader.getPlayerSettings();
			final byte playerId = mapFileHeader.getPlayerId();
			final JSettlersGame game = new JSettlersGame(savegameLoader, -1, playerId, playerSettings);
			final IStartingGame startingGame = game.start();
			this.ui.getFrame().showStartingGamePanel(startingGame);
		} else {
			throw new IllegalStateException("Map loader is not a SavegameLoader: " + mapLoader);
		}
	}
}
