package jsettlers.main.swing.menu.joinpanel;

import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JTextField;

final class SwingUtils {

	private SwingUtils() {
	}

	/**
	 * Set without trigger action listener.
	 */
	public static void set(JComboBox<?> component, Runnable runnable) {
		final ActionListener[] listeners = component.getActionListeners();
		for (final ActionListener listener : listeners) {
			component.removeActionListener(listener);
		}
		try {
			runnable.run();
		} finally {
			for (final ActionListener listener : listeners) {
				component.addActionListener(listener);
			}
		}
	}
	

	/**
	 * Set without trigger action listener.
	 */
	public static void set(JTextField component, Runnable runnable) {
		final ActionListener[] listeners = component.getActionListeners();
		for (final ActionListener listener : listeners) {
			component.removeActionListener(listener);
		}
		try {
			runnable.run();
		} finally {
			for (final ActionListener listener : listeners) {
				component.addActionListener(listener);
			}
		}
	}
}
