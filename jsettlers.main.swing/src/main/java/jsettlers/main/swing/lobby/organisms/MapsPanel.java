package jsettlers.main.swing.lobby.organisms;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jsettlers.graphics.localization.Labels;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.main.swing.lobby.Ui;
import jsettlers.main.swing.lobby.molecules.SearchTextField;
import jsettlers.main.swing.lobby.molecules.ToggleBarPanel;
import jsettlers.main.swing.menu.openpanel.EMapFilter;
import jsettlers.main.swing.menu.openpanel.MapListCellRenderer;

public class MapsPanel extends JPanel {

	private final JList<MapLoader> mapList;
	private final DefaultListModel<MapLoader> model;
	private final SearchTextField searchTextField;
	private final Controller contoller;
	private EMapFilter filter;
	private String query;

	public MapsPanel(Ui ui, Controller controller) {
		this.contoller = controller;
		this.filter = EMapFilter.ALL;
		this.query = "";
		setLayout(new BorderLayout());
		final Box box = Box.createVerticalBox();
		box.add(new ToggleBarPanel<>(Labels.getString("mapfilter.title"), EMapFilter.values(), EMapFilter.ALL, EMapFilter::getName, value -> {
			this.filter = value;
			load();
		}));
		box.add(this.searchTextField = new SearchTextField(value -> {
			this.query = value;
			load();
		}));
		add(box, BorderLayout.NORTH);
		add(new JScrollPane(this.mapList = new JList<>(this.model = new DefaultListModel<>())), BorderLayout.CENTER);
		this.mapList.setCellRenderer(new MapListCellRenderer());
		this.mapList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent event) {
				if (event.getClickCount() == 2) {
					controller.selectMap(mapList.getSelectedValue());
				}
			}
		});
		this.mapList.setOpaque(false);
		load();
	}

	private void load() {
		this.contoller.load(filter, query).thenAccept(this::setMaps);
	}

	public void setMaps(Collection<MapLoader> mapLoaders) {
		model.clear();
		mapLoaders.forEach(model::addElement);
	}

	public interface Controller {
		CompletableFuture<Collection<MapLoader>> load(EMapFilter filter, String query);

		void selectMap(MapLoader mapLoader);
	}
}
