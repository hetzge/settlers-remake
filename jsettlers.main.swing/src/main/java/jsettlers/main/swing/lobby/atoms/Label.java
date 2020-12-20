package jsettlers.main.swing.lobby.atoms;

import javax.swing.JLabel;

import jsettlers.main.swing.lookandfeel.ELFStyle;

public class Label extends JLabel {

	public Label(String text) {
		super(text);
		putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_DYNAMIC);

	}
}
