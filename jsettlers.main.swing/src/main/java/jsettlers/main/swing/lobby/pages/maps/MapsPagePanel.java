package jsettlers.main.swing.lobby.pages.maps;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import jsettlers.main.swing.lobby.UiController;
import jsettlers.main.swing.lobby.organisms.MapsPanel;

public class MapsPagePanel extends JPanel {

	private final MapsPanel mapsPanel;

	public MapsPagePanel(UiController ui, MapsPageController controller) {
		setLayout(new BorderLayout());
		add(this.mapsPanel = new MapsPanel(ui, controller), BorderLayout.CENTER);
	}

	public MapsPanel getMapsPanel() {
		return mapsPanel;
	}
}
