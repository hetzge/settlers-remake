package jsettlers.main.swing.lookandfeel.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicSpinnerUI;

import jsettlers.main.swing.lookandfeel.ELFStyle;

public class SpinnerUi extends BasicSpinnerUI {

	@Override
	public void installUI(JComponent component) {
		super.installUI(component);
		component.setOpaque(false);
		component.setBorder(BorderFactory.createLineBorder(Color.WHITE));
		component.setFont(UIDefaults.FONT_SMALL);
		((DefaultEditor) ((JSpinner) component).getEditor()).getTextField().putClientProperty(ELFStyle.KEY, ELFStyle.TEXT_DEFAULT);
	}

	@Override
	public void uninstallUI(JComponent component) {
		super.uninstallUI(component);
		component.setOpaque(true);
		component.setBorder(null);
	}

	@Override
	protected Component createNextButton() {
		final JButton button = new ScrollbarUiButton(BasicArrowButton.NORTH, UIDefaults.ARROW_COLOR);
		button.setName("Spinner.nextButton");
		installNextButtonListeners(button);
		return button;
	}

	@Override
	protected Component createPreviousButton() {
		final JButton button = new ScrollbarUiButton(BasicArrowButton.SOUTH, UIDefaults.ARROW_COLOR);
		button.setName("Spinner.previousButton");
		installPreviousButtonListeners(button);
		return button;
	}
}
