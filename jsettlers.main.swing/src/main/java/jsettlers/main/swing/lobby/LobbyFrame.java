package jsettlers.main.swing.lobby;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import jsettlers.common.resources.ResourceManager;
import jsettlers.common.resources.SettlersFolderChecker;
import jsettlers.common.resources.SettlersFolderChecker.SettlersFolderInfo;
import jsettlers.graphics.image.reader.DatFileUtils;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.logic.map.loading.MapLoadException;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.loading.list.DirectoryMapLister.ListedMapFile;
import jsettlers.logic.map.loading.original.OriginalMapLoader;
import jsettlers.main.swing.lobby.organisms.MapsPanel;
import jsettlers.main.swing.lookandfeel.JSettlersLookAndFeel;
import jsettlers.main.swing.lookandfeel.JSettlersLookAndFeelExecption;
import jsettlers.main.swing.menu.openpanel.EMapFilter;
import jsettlers.main.swing.resources.SwingResourceProvider;
import jsettlers.main.swing.settings.SettingsManager;

public class LobbyFrame extends JFrame {
	public static void main(String[] args) throws JSettlersLookAndFeelExecption, IOException {

		final SettlersFolderInfo settlersFolders = SettlersFolderChecker.checkSettlersFolder(new File("/home/hetzge/.wine/drive_c/GOG Games/Settlers 3 Ultimate/"));
		final String settlersVersionId = DatFileUtils.generateOriginalVersionId(settlersFolders.gfxFolder);
		ImageProvider.setLookupPath(settlersFolders.gfxFolder, settlersVersionId);

		ResourceManager.setProvider(new SwingResourceProvider());
		SettingsManager.setup(args);
		JSettlersLookAndFeel.install();

		SwingUtilities.invokeLater(() -> {
			final JFrame frame = new JFrame("Lobby");
			frame.setSize(500, 500);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
			frame.setLayout(new BorderLayout(10, 10));
			// frame.add(new LobbyPanel(new Ui(frame), new ServerController()), BorderLayout.CENTER);

			OriginalMapLoader mapLoader;
			try {
				mapLoader = new OriginalMapLoader(new ListedMapFile(new File("/home/hetzge/.wine/drive_c/GOG Games/Settlers 3 Ultimate/Map/SINGLE/Pirates.map")));
				// final JScrollPane scrollPane = new JScrollPane(new SingleplayerMatchPageController(new Ui(frame), mapLoader).init());
				final JScrollPane scrollPane = new JScrollPane(new MapsPanel(new Ui(frame), new MapsPanel.Controller() {

					@Override
					public CompletableFuture<Collection<MapLoader>> load(EMapFilter filter, String query) {
						System.out.println("LobbyFrame.main(...).new Controller() {...}.load()");
						return CompletableFuture.completedFuture(Arrays.asList(mapLoader));
					}

					@Override
					public void selectMap(MapLoader mapLoader) {
						System.out.println("LobbyFrame.main(...).new Controller() {...}.selectMap()");
					}
				}));
				frame.add(scrollPane, BorderLayout.CENTER);
				SwingUtilities.updateComponentTreeUI(frame);
			} catch (MapLoadException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		});
	}
}
