package jsettlers.main.swing.lobby.atoms;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;

import jsettlers.main.swing.lookandfeel.ELFStyle;

public class IntegerSpinner extends JSpinner {

	private boolean change;

	public IntegerSpinner(int value, int minimum, int maximum, int stepSize) {
		super(new SpinnerNumberModel(value, minimum, maximum, stepSize));
		this.change = true;
		putClientProperty(ELFStyle.KEY, ELFStyle.SPINNER_DEFAULT);
	}

	public void setIntegerValue(int value) {
		this.change = false;
		setValue(value);
		this.change = true;
	}

	public int getIntegerValue() {
		return (int) getValue();
	}

	@Override
	public void addChangeListener(ChangeListener listener) {
		super.addChangeListener(event -> {
			// if listener is an editor always call change listener (otherwise rendered value is not updated)
			final boolean isEditor = listener instanceof DefaultEditor;
			if (this.change || isEditor) {
				listener.stateChanged(event);
			}
		});
	}
}
