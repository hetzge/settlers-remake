package jsettlers.main.swing.lobby.organisms;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;

import jsettlers.main.swing.lobby.atoms.Button;
import jsettlers.main.swing.lobby.atoms.TextArea;
import jsettlers.main.swing.lobby.atoms.TextField;

public class ChatPanel extends JPanel {

	private final TextArea textArea;
	private final TextField textField;
	private final Button button;
	private ChatListener listener;

	public ChatPanel(ChatListener listener) {
		this.listener = listener;
		setLayout(new BorderLayout(10, 10));
		add(this.textArea = new TextArea("", false), BorderLayout.CENTER);
		final JPanel footerPanel = new JPanel(new BorderLayout());
		footerPanel.add(this.textField = new TextField("", true), BorderLayout.CENTER);
		footerPanel.add(this.button = new Button("Submit"), BorderLayout.EAST);
		add(footerPanel, BorderLayout.SOUTH);
		this.textField.addKeyListener(new EnterKeyAdapter());
		this.button.addActionListener(event -> onSubmit());
	}

	private void onSubmit() {
		this.listener.submitMessage(this.textField.getText());
		this.textField.setText("");
	}

	public void addMessage(String text) {
		this.textArea.append(text);
		this.textArea.append("\n");
	}

	private final class EnterKeyAdapter extends KeyAdapter {
		@Override
		public void keyReleased(KeyEvent event) {
			if (event.getKeyCode() == KeyEvent.VK_ENTER) {
				onSubmit();
			}
		}
	}

	public interface ChatListener {
		void submitMessage(String text);
	}
}
