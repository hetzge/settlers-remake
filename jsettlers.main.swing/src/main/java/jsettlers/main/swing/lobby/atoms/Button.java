package jsettlers.main.swing.lobby.atoms;

import javax.swing.Icon;
import javax.swing.JButton;

import jsettlers.main.swing.lookandfeel.ELFStyle;

public class Button extends JButton {

	public Button(Icon icon) {
		super(icon);
		style();
	}

	public Button(String text, Icon icon) {
		super(text, icon);
		style();
	}

	public Button(String text) {
		super(text);
		style();
	}

	private void style() {
		putClientProperty(ELFStyle.KEY, ELFStyle.BUTTON_STONE);
	}
}
