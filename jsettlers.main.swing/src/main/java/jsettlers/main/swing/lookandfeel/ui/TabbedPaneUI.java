package jsettlers.main.swing.lookandfeel.ui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import jsettlers.main.swing.lobby.atoms.Button;

public class TabbedPaneUI extends BasicTabbedPaneUI {

	@Override
	public void installUI(JComponent component) {
		super.installUI(component);

		final int count = component.getComponentCount();
		System.out.println(count + "  ---");
		for (int i = 0; i < count; i++) {
			System.out.println(i );
			((JTabbedPane) component).setBackgroundAt(i, Color.BLUE);

		}
		((JTabbedPane) component).setForeground(Color.RED);
		((JTabbedPane) component).setBackground(Color.RED);
		for (Component c : ((JTabbedPane) component).getComponents()) {
			System.out.println(c);
		}
	}
}
