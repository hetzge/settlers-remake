package jsettlers.main.swing.lobby.organisms;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import jsettlers.main.swing.lobby.atoms.TextArea;
import jsettlers.main.swing.lobby.molecules.InputBarPanel;

public class ChatPanel extends JPanel {

	private final Controller controller;
	private final TextArea textArea;

	public ChatPanel(Controller controller) {
		this.controller = controller;
		setLayout(new BorderLayout(10, 10));
		add(new JScrollPane(this.textArea = new TextArea("", false)), BorderLayout.CENTER);
		add(new InputBarPanel(new ChatInputBarListener()), BorderLayout.SOUTH);
	}

	public void addMessage(String text) {
		this.textArea.append(text);
		this.textArea.append("\n");
	}

	private class ChatInputBarListener implements InputBarPanel.InputBarListener {
		@Override
		public void submitValue(String value) {
			controller.submitMessage(value);
		}
	}

	public interface Controller {
		void submitMessage(String text);
	}
}
