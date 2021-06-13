package jsettlers.main.swing.lobby.pages.match;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import jsettlers.graphics.localization.Labels;
import jsettlers.main.swing.lobby.atoms.Label;
import jsettlers.main.swing.lobby.molecules.FormButtonsPanel;
import jsettlers.main.swing.lobby.organisms.ChatPanel;
import jsettlers.main.swing.lobby.organisms.MatchSettingsPanel;
import jsettlers.main.swing.lobby.organisms.PlayersPanel;

public class MatchPagePanel extends JPanel {

	private final MatchSettingsPanel matchSettingsPanel;
	private final PlayersPanel playersPanel;
	private final ChatPanel chatPanel;
	private final FormButtonsPanel formButtonsPanel;

	MatchPagePanel(MatchPageController controller) {
		setLayout(new BorderLayout(20, 20));
		final JPanel westPanel = new JPanel();
		westPanel.add(this.matchSettingsPanel = new MatchSettingsPanel(controller), BorderLayout.NORTH);
		add(westPanel, BorderLayout.WEST);
		final Box centerBox = Box.createVerticalBox();
		centerBox.add(new JScrollPane(this.playersPanel = new PlayersPanel(controller)));
		centerBox.add(this.chatPanel = new ChatPanel(controller));
		add(centerBox, BorderLayout.CENTER);
		add(this.formButtonsPanel = new FormButtonsPanel(
				Labels.getString("join-game-panel-start"), controller::startMatch,
				Labels.getString("join-game-panel-cancel"), controller::cancel),
				BorderLayout.SOUTH);
		this.playersPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
	}

	public MatchSettingsPanel getMatchSettingsPanel() {
		return matchSettingsPanel;
	}

	public PlayersPanel getPlayersPanel() {
		return playersPanel;
	}

	public ChatPanel getChatPanel() {
		return chatPanel;
	}

	public void setTitle(String title) {
		add(new Label(title, JLabel.CENTER), BorderLayout.NORTH);
		SwingUtilities.updateComponentTreeUI(this);
	}

	public void showStartButton(boolean visible) {
		this.formButtonsPanel.showSubmitButton(visible);
	}
}
