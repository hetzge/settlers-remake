package jsettlers.main.swing.lobby.pages.settings;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import jsettlers.graphics.localization.Labels;
import jsettlers.main.swing.lobby.molecules.FormButtonsPanel;
import jsettlers.main.swing.lobby.organisms.GeneralSettingsPanel;
import jsettlers.main.swing.menu.multiplayer.EditServerEntryPanel;

public class SettingsPagePanel extends JPanel {

	private final GeneralSettingsPanel generalSettingsPanel;

	SettingsPagePanel(SettingsPageController controller) {
		setLayout(new BorderLayout());

		add(this.generalSettingsPanel = new GeneralSettingsPanel(), BorderLayout.NORTH);
		// TODO
		add(new EditServerEntryPanel(() -> {}), BorderLayout.CENTER);
		add(new FormButtonsPanel(
				Labels.getString("settings-ok"), controller::save,
				Labels.getString("settings-back"), controller::cancel),
				BorderLayout.SOUTH);
	}

	public GeneralSettingsPanel getGeneralSettingsPanel() {
		return generalSettingsPanel;
	}
}
