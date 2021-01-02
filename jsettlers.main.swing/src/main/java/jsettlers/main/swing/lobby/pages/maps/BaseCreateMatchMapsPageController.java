package jsettlers.main.swing.lobby.pages.maps;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.loading.list.MapList;
import jsettlers.main.swing.menu.openpanel.EMapFilter;

public abstract class BaseCreateMatchMapsPageController implements MapsPageController {

	@Override
	public CompletableFuture<Collection<MapLoader>> load(EMapFilter filter, String query) {
		return CompletableFuture.completedFuture(MapList
				.getDefaultList()
				.getFreshMaps()
				.getItems()
				.stream()
				.filter(filter::filter)
				.filter(mapLoader -> MapsPageUtils.isMapLoaderMatchQuery(mapLoader, query))
				.collect(Collectors.toList()));
	}
}
