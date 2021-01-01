package jsettlers.main.swing.lookandfeel.factory;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import jsettlers.main.swing.lookandfeel.ELFStyle;
import jsettlers.main.swing.lookandfeel.ui.SpinnerUi;

public class SpinnerUiFactory {

	/**
	 * Forward calls
	 */
	public static final ForwardFactory FORWARD = new ForwardFactory();

	/**
	 * This is only a factory so no objects need to be created.
	 */
	private SpinnerUiFactory() {
	}

	/**
	 * Create PLAF
	 * 
	 * @param component
	 *            Component which need the UI
	 * @return UI
	 */
	public static ComponentUI createUI(JComponent component) {
		if (ELFStyle.SPINNER_DEFAULT == component.getClientProperty(ELFStyle.KEY)) {
			return new SpinnerUi();
		}
		return FORWARD.create(component);
	}
}
