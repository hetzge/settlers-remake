package jsettlers.main.swing.lobby.atoms;

import java.awt.event.ActionListener;

import javax.swing.JButton;

import jsettlers.main.swing.lookandfeel.ELFStyle;

public class MenuButton extends JButton {

	public MenuButton(String text, ActionListener listener) {
		super(text);
		addActionListener(listener);
		putClientProperty(ELFStyle.KEY, ELFStyle.BUTTON_MENU);
	}
}
