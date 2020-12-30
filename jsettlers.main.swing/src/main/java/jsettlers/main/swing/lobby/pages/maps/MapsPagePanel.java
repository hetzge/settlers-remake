package jsettlers.main.swing.lobby.pages.maps;

import javax.swing.JPanel;

import jsettlers.main.swing.lobby.Ui;
import jsettlers.main.swing.lobby.organisms.MapsPanel;

public class MapsPagePanel extends JPanel {

	private final MapsPanel mapsPanel;

	public MapsPagePanel(Ui ui, Controller controller) {
		add(this.mapsPanel = new MapsPanel(ui, controller));
	}

	public MapsPanel getMapsPanel() {
		return mapsPanel;
	}

	public interface Controller extends MapsPanel.Controller {
	}
}
