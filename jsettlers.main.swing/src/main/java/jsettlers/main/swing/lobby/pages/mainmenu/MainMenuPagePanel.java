package jsettlers.main.swing.lobby.pages.mainmenu;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import jsettlers.graphics.localization.Labels;
import jsettlers.main.swing.lobby.atoms.Label;
import jsettlers.main.swing.lobby.atoms.MenuButton;

public class MainMenuPagePanel extends JPanel {

	public MainMenuPagePanel(MainMenuPageController controller) {
		setLayout(new GridBagLayout());
		final JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(new Label(""));
		panel.add(new MenuButton(Labels.getString("main-panel-new-single-player-game-button"), event -> controller.startSingleplayerMatch()));
		panel.add(new Label(""));
		panel.add(new MenuButton(Labels.getString("start-loadgame"), event -> controller.loadSingleplayerMatch()));
		panel.add(new Label(""));
		panel.add(new MenuButton(Labels.getString("join-game-panel-new-multi-player-game-title"), event -> controller.createMultiplayerMatch()));
		panel.add(new Label(""));
		panel.add(new MenuButton(Labels.getString("join-game-panel-join-multi-player-game-title"), event -> controller.joinMultiplayerMatch()));
		panel.add(new Label(""));
		panel.add(new MenuButton(Labels.getString("settings-title"), event -> controller.showSettings()));
		panel.add(new Label(""));
		panel.add(new MenuButton(Labels.getString("main-panel-exit-button"), event -> controller.exit()));
		panel.add(new Label(""));
		add(panel);
		for (Component component : panel.getComponents()) {
			if (component instanceof MenuButton) {
				final int width = 350;
				final int height = 75;
				component.setSize(new Dimension(width, height));
				component.setMinimumSize(new Dimension(width, height));
				component.setMaximumSize(new Dimension(width, height));
				component.setPreferredSize(new Dimension(width, height));
			} else if (component instanceof Label) {
				final int width = 350;
				final int height = 10;
				component.setSize(new Dimension(width, height));
				component.setMinimumSize(new Dimension(width, height));
				component.setMaximumSize(new Dimension(width, height));
				component.setPreferredSize(new Dimension(width, height));
			}
		}
	}
}
