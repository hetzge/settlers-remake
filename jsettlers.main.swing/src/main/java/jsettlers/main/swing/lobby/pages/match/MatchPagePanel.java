package jsettlers.main.swing.lobby.pages.match;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import jsettlers.graphics.localization.Labels;
import jsettlers.main.swing.lobby.atoms.Button;
import jsettlers.main.swing.lobby.atoms.Label;
import jsettlers.main.swing.lobby.organisms.ChatPanel;
import jsettlers.main.swing.lobby.organisms.MatchSettingsPanel;
import jsettlers.main.swing.lobby.organisms.PlayersPanel;

public class MatchPagePanel extends JPanel {

	private final MatchSettingsPanel matchSettingsPanel;
	private final PlayersPanel playersPanel;
	private final ChatPanel chatPanel;
	private final Button cancelButton;
	private final Button startButton;

	MatchPagePanel(Controller controller) {
		setLayout(new BorderLayout(20, 20));
		final JPanel westPanel = new JPanel();
		westPanel.add(this.matchSettingsPanel = new MatchSettingsPanel(controller), BorderLayout.NORTH);
		add(westPanel, BorderLayout.WEST);
		final Box centerBox = Box.createVerticalBox();
		centerBox.add(new JScrollPane(this.playersPanel = new PlayersPanel(controller)));
		centerBox.add(this.chatPanel = new ChatPanel(controller));
		add(centerBox, BorderLayout.CENTER);
		final JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.LINE_AXIS));
		southPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		southPanel.add(this.startButton = new Button(Labels.getString("join-game-panel-start")));
		southPanel.add(this.cancelButton = new Button(Labels.getString("join-game-panel-cancel")));
		add(southPanel, BorderLayout.SOUTH);
		this.playersPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
		this.cancelButton.addActionListener(event -> controller.cancel());
		this.startButton.addActionListener(event -> controller.startMatch());
		this.startButton.setVisible(false);
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
		this.startButton.setVisible(visible);
	}

	public interface Controller extends PlayersPanel.Controller, ChatPanel.Controller, MatchSettingsPanel.Controller {

		MatchPagePanel init();

		void cancel();

		void startMatch();
	}
}
