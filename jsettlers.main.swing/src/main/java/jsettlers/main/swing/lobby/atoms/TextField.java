package jsettlers.main.swing.lobby.atoms;

import javax.swing.JTextField;

import jsettlers.main.swing.lookandfeel.ELFStyle;

public class TextField extends JTextField {

	public TextField(String text, boolean editable) {
		super(text);
		setEditable(editable);
		putClientProperty(ELFStyle.KEY, ELFStyle.TEXT_DEFAULT);
	}
}
