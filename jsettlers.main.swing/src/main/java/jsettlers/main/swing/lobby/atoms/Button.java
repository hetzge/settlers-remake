package jsettlers.main.swing.lobby.atoms;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import jsettlers.main.swing.lookandfeel.ELFStyle;

public class Button extends JButton {

	public Button(Icon icon) {
		super(icon);
		style();
	}

	public Button(Icon icon, ActionListener listener) {
		super(icon);
		style();
		addActionListener(listener);
	}

	public Button(String text, Icon icon) {
		super(text, icon);
		style();
	}

	public Button(String text, Icon icon, ActionListener listener) {
		super(text, icon);
		style();
		addActionListener(listener);
	}

	public Button(String text) {
		super(text);
		style();
	}

	public Button(String text, ActionListener listener) {
		super(text);
		style();
		addActionListener(listener);
	}

	private void style() {
		putClientProperty(ELFStyle.KEY, ELFStyle.BUTTON_STONE);
	}
}
