package jsettlers.main.swing.lobby.pages;

import java.awt.BorderLayout;
import java.awt.ComponentOrientation;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import jsettlers.main.swing.lobby.atoms.Button;
import jsettlers.main.swing.lobby.atoms.Label;
import jsettlers.main.swing.lobby.organisms.ChatPanel;
import jsettlers.main.swing.lobby.organisms.MatchSettingsPanel;
import jsettlers.main.swing.lobby.organisms.PlayersPanel;

public class MatchPagePanel extends JPanel {

	private final MatchSettingsPanel matchSettingsPanel;
	private final PlayersPanel playersPanel;
	private final ChatPanel chatPanel;
	private final Label titleLabel;
	private final Button startButton;

	public MatchPagePanel(Controller controller) {
		setLayout(new BorderLayout());
		add(this.titleLabel = new Label("..."), BorderLayout.NORTH);
		add(this.matchSettingsPanel = new MatchSettingsPanel(controller), BorderLayout.WEST);
		final JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(this.playersPanel = new PlayersPanel(controller), BorderLayout.NORTH);
		centerPanel.add(this.chatPanel = new ChatPanel(controller), BorderLayout.SOUTH);
		add(centerPanel, BorderLayout.CENTER);
		final JPanel southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.LINE_AXIS));
		southPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		southPanel.add(this.startButton = new Button("Start"));
		add(southPanel, BorderLayout.SOUTH);
		this.startButton.addActionListener(event -> controller.startMatch());
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
		this.titleLabel.setText(title);
	}

	public interface Controller extends PlayersPanel.Controller, ChatPanel.Controller, MatchSettingsPanel.Controller {

		MatchPagePanel init();

		void startMatch();
	}
}
