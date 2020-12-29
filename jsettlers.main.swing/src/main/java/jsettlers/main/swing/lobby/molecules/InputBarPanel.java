package jsettlers.main.swing.lobby.molecules;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;

import jsettlers.main.swing.lobby.atoms.Button;
import jsettlers.main.swing.lobby.atoms.TextField;

public class InputBarPanel extends JPanel {

	private final InputBarListener listener;
	private final TextField textField;
	private final Button button;

	public InputBarPanel(InputBarListener listener) {
		this.listener = listener;
		setLayout(new BorderLayout());
		add(this.textField = new TextField("", true), BorderLayout.CENTER);
		add(this.button = new Button("Submit"), BorderLayout.EAST);
		this.textField.addKeyListener(new EnterKeyAdapter());
		this.button.addActionListener(event -> onSubmit());
	}

	private void onSubmit() {
		this.listener.submitValue(this.textField.getText());
		this.textField.setText("");
	}

	private final class EnterKeyAdapter extends KeyAdapter {
		@Override
		public void keyReleased(KeyEvent event) {
			if (event.getKeyCode() == KeyEvent.VK_ENTER) {
				onSubmit();
			}
		}
	}

	public interface InputBarListener {
		void submitValue(String value);
	}
}
