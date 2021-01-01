package jsettlers.main.swing.lobby.atoms;

import javax.swing.JLabel;

import jsettlers.main.swing.lookandfeel.ELFStyle;

public class Label extends JLabel {

	public Label(String text) {
		this(text, JLabel.LEFT);
	}

	public Label(String text, int horizontalAlignment) {
		super(text);
		setHorizontalAlignment(horizontalAlignment);
		putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_DYNAMIC);
	}
}
