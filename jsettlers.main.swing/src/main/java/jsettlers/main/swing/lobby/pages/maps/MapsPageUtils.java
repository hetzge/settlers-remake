package jsettlers.main.swing.lobby.pages.maps;

import java.util.Locale;

import jsettlers.logic.map.loading.MapLoader;

public final class MapsPageUtils {

	private MapsPageUtils() {
	}

	public static boolean isMapLoaderMatchQuery(MapLoader mapLoader, String query) {
		if (query.isEmpty()) {
			return true;
		}
		final String lowerCaseQuery = query.toLowerCase(Locale.ENGLISH);
		if (mapLoader.getMapName().toLowerCase(Locale.ENGLISH).contains(lowerCaseQuery)) {
			return true;
		}
		if (mapLoader.getDescription().toLowerCase(Locale.ENGLISH).contains(lowerCaseQuery)) {
			return true;
		}
		return mapLoader.getMapId().toLowerCase(Locale.ENGLISH).contains(lowerCaseQuery);
	}
}
