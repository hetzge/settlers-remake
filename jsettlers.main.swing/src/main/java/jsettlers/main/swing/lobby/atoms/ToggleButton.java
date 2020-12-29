package jsettlers.main.swing.lobby.atoms;

import java.awt.Color;
import java.util.function.Consumer;

import javax.swing.ImageIcon;
import javax.swing.border.EmptyBorder;

import jsettlers.main.swing.JSettlersSwingUtil;
import jsettlers.main.swing.lookandfeel.ELFStyle;

public class ToggleButton extends Button {

	private static final int READY_BUTTON_WIDTH = 40;
	private static final int READY_BUTTON_HEIGHT = 25;
	private static final ImageIcon READY_IMAGE = new ImageIcon(JSettlersSwingUtil.createImage(2, 17, 0, true, READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));
	private static final ImageIcon READY_PRESSED_IMAGE = new ImageIcon(JSettlersSwingUtil.createImage(2, 17, 1, true, READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));
	private static final ImageIcon READY_DISABLED_IMAGE = new ImageIcon(JSettlersSwingUtil.createImage(2, 17, 0, false, READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));
	private static final ImageIcon NOT_READY_IMAGE = new ImageIcon(JSettlersSwingUtil.createImage(2, 18, 0, true, READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));
	private static final ImageIcon NOT_READY_PRESSED_IMAGE = new ImageIcon(JSettlersSwingUtil.createImage(2, 18, 1, true, READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));
	private static final ImageIcon NOT_READY_DISABLED_IMAGE = new ImageIcon(JSettlersSwingUtil.createImage(2, 18, 0, false, READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));

	public ToggleButton(boolean state, Consumer<Boolean> onToggle) {
		super(READY_IMAGE);
		setState(state);
		addActionListener(event -> {
			final boolean selected = !isSelected();
			setState(selected);
			onToggle.accept(selected);
		});
		// unset button style
		putClientProperty(ELFStyle.KEY, "");
		setBorder(new EmptyBorder(0, 0, 0, 0));
		setBorderPainted(false);
		setBackground(new Color(0, 0, 0, 0));
		setFocusPainted(false);
		setContentAreaFilled(false);
	}

	public void setState(boolean state) {
		JSettlersSwingUtil.set(this, () -> {
			setSelected(state);

			if (state) {
				setIcon(READY_IMAGE);
				setPressedIcon(READY_PRESSED_IMAGE);
				setDisabledIcon(READY_DISABLED_IMAGE);
			} else {
				setIcon(NOT_READY_IMAGE);
				setPressedIcon(NOT_READY_PRESSED_IMAGE);
				setDisabledIcon(NOT_READY_DISABLED_IMAGE);
			}
		});
	}
}
