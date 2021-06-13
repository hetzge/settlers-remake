package jsettlers.main.swing.lobby.pages.settings;

import jsettlers.main.swing.lobby.UiController;
import jsettlers.main.swing.lobby.organisms.GeneralSettingsPanel;
import jsettlers.main.swing.settings.SettingsManager;

public class DefaultSettingsPageController implements SettingsPageController {

	private final UiController ui;
	private final SettingsPagePanel panel;

	public DefaultSettingsPageController(UiController ui) {
		this.ui = ui;
		this.panel = new SettingsPagePanel(this);
	}

	public SettingsPagePanel init(SettingsManager manager) {
		final GeneralSettingsPanel generalSettingsPanel = this.panel.getGeneralSettingsPanel();
		generalSettingsPanel.setPlayerName(manager.getPlayer().getName());
		generalSettingsPanel.setVolume(manager.getVolume());
		generalSettingsPanel.setFpsLimit(manager.getFpsLimit());
		generalSettingsPanel.setBackend(manager.getBackend().name());
		generalSettingsPanel.setGuiScale(manager.getGuiScale());
		return this.panel;
	}

	@Override
	public void save() {
		final GeneralSettingsPanel generalSettingsPanel = this.panel.getGeneralSettingsPanel();
		final SettingsManager settingsManager = SettingsManager.getInstance();
		settingsManager.setUserName(generalSettingsPanel.getPlayerName());
		settingsManager.setVolume(generalSettingsPanel.getVolume());
		settingsManager.setFpsLimit(generalSettingsPanel.getFpsLimit());
		settingsManager.setBackend(generalSettingsPanel.getBackend());
		settingsManager.setGuiScale(generalSettingsPanel.getGuiScale());
		ui.showHomePage();
	}

	@Override
	public void cancel() {
		ui.showHomePage();
	}
}
