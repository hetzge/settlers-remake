package jsettlers.main.swing.lobby.organisms;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import jsettlers.logic.map.loading.MapLoader;
import jsettlers.main.swing.JSettlersSwingUtil;
import jsettlers.main.swing.lobby.atoms.ComboBox;
import jsettlers.main.swing.lobby.atoms.ImagePanel;
import jsettlers.main.swing.lobby.atoms.IntegerSpinner;
import jsettlers.main.swing.lobby.atoms.Label;
import jsettlers.network.server.lobby.core.ResourceAmount;

public class MatchSettingsPanel extends JPanel {

	private final IntegerSpinner peaceTimeIntegerSpinner;
	private final ComboBox<ResourceAmount> startResourcesDropDown;
	private final Label mapLabel;
	private final ImagePanel mapImage;

	public MatchSettingsPanel(Controller controller) {
		setLayout(new GridBagLayout());
		int y = 0;
		add(this.mapLabel = new Label("..."), createConstraints(y++));
		add(this.mapImage = new ImagePanel(), createConstraints(y++));
		add(new Label("Peace time"), createConstraints(y++));
		add(this.peaceTimeIntegerSpinner = new IntegerSpinner(10, 0, 9999, 1), createConstraints(y++));
		add(new Label("Start resources"), createConstraints(y++));
		add(this.startResourcesDropDown = new ComboBox<>(ResourceAmount.values(), ResourceAmount.MEDIUM, Enum::name), createConstraints(y++));
		this.mapImage.setDimension(new Dimension(300, 150));
		this.peaceTimeIntegerSpinner.addChangeListener(event -> controller.setPeaceTime(peaceTimeIntegerSpinner.getIntegerValue()));
		this.startResourcesDropDown.addItemListener(event -> controller.setStartResources((ResourceAmount) event.getItem()));
	}

	public void setMapInformation(MapLoader loader) {
		setMapInformation(loader.getMapName(), JSettlersSwingUtil.createBufferedImageFrom(loader));
	}

	public void setMapInformation(String title, BufferedImage image) {
		this.mapLabel.setText("Map: " + title);
		this.mapImage.setImage(image);
	}

	public void setPeaceTime(int minutes) {
		JSettlersSwingUtil.set(this.peaceTimeIntegerSpinner, () -> this.peaceTimeIntegerSpinner.setIntegerValue(minutes));
	}

	public void setStartResources(ResourceAmount amount) {
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

		void setStartResources(ResourceAmount amount);
	}
}
