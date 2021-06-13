package jsettlers.main.swing.lobby.organisms;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import jsettlers.graphics.localization.Labels;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.main.swing.JSettlersSwingUtil;
import jsettlers.main.swing.lobby.atoms.ComboBox;
import jsettlers.main.swing.lobby.atoms.ImagePanel;
import jsettlers.main.swing.lobby.atoms.IntegerSpinner;
import jsettlers.main.swing.lobby.atoms.Label;
import jsettlers.network.server.lobby.core.ELobbyResourceAmount;

public class MatchSettingsPanel extends JPanel {

	private final IntegerSpinner peaceTimeIntegerSpinner;
	private final ComboBox<ELobbyResourceAmount> startResourcesDropDown;
	private final Label mapLabel;
	private final ImagePanel mapImage;

	public MatchSettingsPanel(Controller controller) {
		setLayout(new GridBagLayout());
		int y = 0;
		add(this.mapLabel = new Label("..."), createConstraints(y++));
		add(this.mapImage = new ImagePanel(), createConstraints(y++));

		final JPanel settingsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
		settingsPanel.add(new Label(Labels.getString("join-game-panel-peace-time")));
		settingsPanel.add(this.peaceTimeIntegerSpinner = new IntegerSpinner(10, 0, 9999, 1));
		settingsPanel.add(new Label(Labels.getString("join-game-panel-start-resources")));
		settingsPanel.add(this.startResourcesDropDown = new ComboBox<>(ELobbyResourceAmount.values(), ELobbyResourceAmount.MEDIUM_GOODS, it -> Labels.getString("map-start-resources-" + it.name())));
		add(settingsPanel, createConstraints(y++));
		this.mapImage.setDimension(new Dimension(300, 150));
		this.startResourcesDropDown.setPreferredSize(new Dimension(0, 35));
		this.peaceTimeIntegerSpinner.addChangeListener(event -> controller.setPeaceTime(peaceTimeIntegerSpinner.getIntegerValue()));
		this.startResourcesDropDown.addItemListener(event -> controller.setStartResources(startResourcesDropDown.getValue()));
	}

	public void setMapInformation(MapLoader loader) {
		setMapInformation(loader.getMapName(), JSettlersSwingUtil.createBufferedImageFrom(loader));
	}

	public void setMapInformation(String title, BufferedImage image) {
		this.mapLabel.setText("Map: " + title);
		this.mapImage.setImage(image);
	}

	public void setPeaceTime(int minutes) {
		this.peaceTimeIntegerSpinner.setIntegerValue(minutes);
	}

	public void setStartResources(ELobbyResourceAmount amount) {
		JSettlersSwingUtil.set(this.startResourcesDropDown, () -> this.startResourcesDropDown.setSelectedItem(amount));
	}

	private static GridBagConstraints createConstraints(int y) {
		final GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = y;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		return constraints;
	}

	public interface Controller {
		void setPeaceTime(int minutes);

		void setStartResources(ELobbyResourceAmount amount);
	}
}
