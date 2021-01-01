package jsettlers.main.swing.lobby.pages;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;

import jsettlers.main.swing.lobby.atoms.Label;
import jsettlers.main.swing.lookandfeel.components.BackgroundPanel;

public class Page extends BackgroundPanel {

	private final JPanel panel;

	public Page(String title, Component component) {
		add(this.panel = new JPanel(new BorderLayout(20, 20)));
		this.panel.add(new Label(title, JLabel.CENTER), BorderLayout.NORTH);
		this.panel.add(component, BorderLayout.CENTER);
	}
}
