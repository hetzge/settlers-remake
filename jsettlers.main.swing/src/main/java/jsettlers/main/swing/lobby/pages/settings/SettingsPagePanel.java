package jsettlers.main.swing.lobby.pages.settings;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;

import go.graphics.swing.contextcreator.BackendSelector;
import jsettlers.graphics.localization.Labels;
import jsettlers.main.swing.lobby.atoms.Label;
import jsettlers.main.swing.lobby.atoms.MenuButton;
import jsettlers.main.swing.lobby.atoms.SettingsSlider;
import jsettlers.main.swing.lobby.atoms.TextField;
import jsettlers.main.swing.lookandfeel.ELFStyle;

public class SettingsPagePanel extends JPanel {

	private final JTextField playerNameField;
	private final SettingsSlider volumeSlider;
	private final SettingsSlider fpsLimitSlider;
	private final SettingsSlider guiScaleSlider;
	private final BackendSelector backendSelector;

	SettingsPagePanel(SettingsPageController controller) {
		setLayout(new BorderLayout());

		final JPanel settingsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
		settingsPanel.add(new Label(Labels.getString("settings-name")));
		settingsPanel.add(this.playerNameField = new TextField("", true));
		settingsPanel.add(new Label(Labels.getString("settings-volume")));
		settingsPanel.add(this.volumeSlider = new SettingsSlider("%", 0, 100, null));
		settingsPanel.add(new Label(Labels.getString("settings-fps-limit")));
		settingsPanel.add(this.fpsLimitSlider = new SettingsSlider("fps", 0, 240, "timerless redraw"));
		settingsPanel.add(new Label(Labels.getString("settings-backend")));
		settingsPanel.add(this.backendSelector = new BackendSelector());
		settingsPanel.add(new Label(Labels.getString("settings-gui-scale")));
		settingsPanel.add(this.guiScaleSlider = new SettingsSlider("%", 50, 400, "system default"));
		add(settingsPanel, BorderLayout.NORTH);

		final JPanel footerPanel = new JPanel(new GridLayout(1, 0, 20, 20));
		footerPanel.add(new MenuButton(Labels.getString("settings-back"), event -> controller.cancel()));
		footerPanel.add(new MenuButton(Labels.getString("settings-ok"), event -> controller.save()));
		add(footerPanel, BorderLayout.SOUTH);

		this.backendSelector.putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);
	}

	public String getPlayerName() {
		return this.playerNameField.getText();
	}

	public void setPlayerName(String playerName) {
		this.playerNameField.setText(playerName);
	}

	public float getVolume() {
		return this.volumeSlider.getValue() / 100f;
	}

	public void setVolume(float volume) {
		this.volumeSlider.setValue((int) (volume * 100));
	}

	public int getFpsLimit() {
		return this.fpsLimitSlider.getValue();
	}

	public void setFpsLimit(int limit) {
		this.fpsLimitSlider.setValue(limit);
	}

	public String getBackend() {
		return this.backendSelector.getSelectedItem().toString();
	}

	public void setBackend(String backend) {
		this.backendSelector.setSelectedItem(backend);
	}

	public float getGuiScale() {
		return this.guiScaleSlider.getValue() / 100f;
	}

	public void setGuiScale(float scale) {
		this.guiScaleSlider.setValue((int) (scale * 100));
	}
}
