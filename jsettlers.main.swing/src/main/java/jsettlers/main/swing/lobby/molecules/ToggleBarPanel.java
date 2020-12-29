package jsettlers.main.swing.lobby.molecules;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Function;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

import jsettlers.main.swing.lobby.atoms.Button;
import jsettlers.main.swing.lobby.atoms.Label;

public class ToggleBarPanel<T> extends JPanel {

	private static final LineBorder DISABLED_BORDER = new LineBorder(Color.BLACK);
	private static final LineBorder ENABLED_BORDER = new LineBorder(Color.RED);

	public ToggleBarPanel(T[] values, T active, Function<T, String> labelFunction, ToggleBarListener<T> listener) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(new Label("Filter:"));
		for (T value : values) {
			final Button button = new Button(labelFunction.apply(value));
			button.addActionListener(new ToggleButtonActionListener(value, listener));
			add(button);
		}
	}

	private class ToggleButtonActionListener implements ActionListener {

		private final T value;
		private final ToggleBarListener<T> listener;

		public ToggleButtonActionListener(T value, ToggleBarListener<T> listener) {
			this.value = value;
			this.listener = listener;
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			for (Component component : getComponents()) {
				if (component instanceof Button) {
					final Button otherButton = (Button) component;
					otherButton.setBorder(event.getSource() != otherButton ? DISABLED_BORDER : ENABLED_BORDER);
				}
			}
			listener.onToggle(value);
		}
	}

	public interface ToggleBarListener<T> {
		void onToggle(T value);
	}
}
