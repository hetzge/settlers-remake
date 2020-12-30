package jsettlers.main.swing.lobby.molecules;

import java.util.function.Function;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;

import jsettlers.main.swing.lobby.atoms.Label;
import jsettlers.main.swing.lobby.atoms.ToggleButton;

public class ToggleBarPanel<T> extends JPanel {

	public ToggleBarPanel(String labelText, T[] values, T active, Function<T, String> labelFunction, ToggleBarListener<T> listener) {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(new Label(labelText));
		final ButtonGroup group = new ButtonGroup();
		for (final T value : values) {
			final ToggleButton button = new ToggleButton(labelFunction.apply(value));
			button.addActionListener(event -> listener.onToggle(value));
			group.add(button);
			add(button);
			if (value.equals(active)) {
				button.setSelected(true);
			}
		}
	}

	public interface ToggleBarListener<T> {
		void onToggle(T value);
	}
}
