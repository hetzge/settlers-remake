package jsettlers.main.swing.lobby.atoms;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class IntegerSpinner extends JSpinner {

	public IntegerSpinner(int value, int minimum, int maximum, int stepSize) {
		super(new SpinnerNumberModel(value, minimum, maximum, stepSize));
	}

	public void setIntegerValue(int value) {
		setValue(value);
	}

	public int getIntegerValue() {
		return (int) getValue();
	}
}
