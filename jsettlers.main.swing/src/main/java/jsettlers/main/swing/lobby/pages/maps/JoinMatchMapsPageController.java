package jsettlers.main.swing.lobby.pages.maps;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import jsettlers.logic.map.loading.MapLoader;
import jsettlers.main.datatypes.JoinableGame;
import jsettlers.main.swing.lobby.Ui;
import jsettlers.main.swing.lobby.pages.match.MultiplayerMatchPageController;
import jsettlers.main.swing.menu.mainmenu.NetworkGameMapLoader;
import jsettlers.main.swing.menu.openpanel.EMapFilter;
import jsettlers.network.client.NetworkClient;
import jsettlers.network.server.lobby.core.MatchId;

public class JoinMatchMapsPageController implements MapsPagePanel.Controller {

	private final Ui ui;
	private final NetworkClient client;

	public JoinMatchMapsPageController(Ui ui, NetworkClient client) {
		this.ui = ui;
		this.client = client;
	}

	@Override
	public CompletableFuture<Collection<MapLoader>> load(EMapFilter filter, String query) {
		final CompletableFuture<Collection<MapLoader>> future = new CompletableFuture<>();
		this.client.queryMatches(matches -> {
			SwingUtilities.invokeLater(() -> {
				future.complete(matches
						.stream()
						.map(JoinableGame::new)
						.map(NetworkGameMapLoader::new)
						.filter(filter::filter)
						.filter(mapLoader -> MapsPageUtils.isMapLoaderMatchQuery(mapLoader, query))
						.collect(Collectors.toList()));
			});
		});
		return future;
	}

	@Override
	public void selectMap(MapLoader mapLoader) {
		if (mapLoader instanceof NetworkGameMapLoader) {
			final NetworkGameMapLoader networkGameMapLoader = (NetworkGameMapLoader) mapLoader;
			final MatchId matchId = new MatchId(networkGameMapLoader.getJoinableGame().getId());
			MultiplayerMatchPageController.joinMatch(ui, client, mapLoader, matchId).thenAccept(panel -> {
				// TODO
				// this.ui.getFrame().showJoinGamePanel(panel);
			});
		} else {
			throw new IllegalStateException("Map loader is not a NetworkGameMapLoader");
		}
	}
}
