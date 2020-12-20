package jsettlers.main.swing.lobby.atoms;

import javax.swing.JTextArea;

import jsettlers.main.swing.lookandfeel.ELFStyle;

public class TextArea extends JTextArea {

	public TextArea(String text,  boolean editable) {
		super(text);
		setEditable(editable);
		putClientProperty(ELFStyle.KEY, ELFStyle.TEXT_DEFAULT);
	}
}
