package jsettlers.main.swing.lobby.pages.settings;

import jsettlers.main.swing.lobby.UiController;
import jsettlers.main.swing.settings.SettingsManager;

public class DefaultSettingsPageController implements SettingsPageController {

	private final UiController ui;
	private final SettingsPagePanel panel;

	public DefaultSettingsPageController(UiController ui) {
		this.ui = ui;
		this.panel = new SettingsPagePanel(this);
	}

	public SettingsPagePanel init(SettingsManager manager) {
		this.panel.setPlayerName(manager.getPlayer().getName());
		this.panel.setVolume(manager.getVolume());
		this.panel.setFpsLimit(manager.getFpsLimit());
		this.panel.setBackend(manager.getBackend().name());
		this.panel.setGuiScale(manager.getGuiScale());
		return this.panel;
	}

	@Override
	public void save() {
		SettingsManager settingsManager = SettingsManager.getInstance();
		settingsManager.setUserName(panel.getPlayerName());
		settingsManager.setVolume(panel.getVolume());
		settingsManager.setFpsLimit(panel.getFpsLimit());
		settingsManager.setBackend(panel.getBackend());
		settingsManager.setGuiScale(panel.getGuiScale());
		ui.showHomePage();
	}

	@Override
	public void cancel() {
		ui.showHomePage();
	}
}
