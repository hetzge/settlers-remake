package jsettlers.main.swing.lookandfeel.factory;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import jsettlers.main.swing.lookandfeel.ELFStyle;
import jsettlers.main.swing.lookandfeel.ui.TabbedPaneUI;

public class TabbedPaneUiFactory {

	/**
	 * Forward calls
	 */
	public static final ForwardFactory FORWARD = new ForwardFactory();

	/**
	 * This is only a factory so no objects need to be created.
	 */
	private TabbedPaneUiFactory() {
	}

	/**
	 * Create PLAF
	 *
	 * @param component
	 *            Component which need the UI
	 * @return UI
	 */
	public static ComponentUI createUI(JComponent component) {
		Object style = component.getClientProperty(ELFStyle.KEY);
		if (ELFStyle.TABBED_DEFAULT == style) {
			return new TabbedPaneUI();
		}
		return FORWARD.create(component);
	}
}
