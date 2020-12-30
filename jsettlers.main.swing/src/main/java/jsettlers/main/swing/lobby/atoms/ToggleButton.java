package jsettlers.main.swing.lobby.atoms;

import javax.swing.JToggleButton;

import jsettlers.main.swing.lookandfeel.ELFStyle;

public class ToggleButton extends JToggleButton {

	public ToggleButton(String text) {
		super(text);
		putClientProperty(ELFStyle.KEY, ELFStyle.TOGGLE_BUTTON_STONE);
	}
}
