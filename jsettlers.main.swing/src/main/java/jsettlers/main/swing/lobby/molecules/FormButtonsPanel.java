package jsettlers.main.swing.lobby.molecules;

import java.awt.ComponentOrientation;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import jsettlers.main.swing.lobby.atoms.Button;

public class FormButtonsPanel extends JPanel {

	private final Button submitButton;

	public FormButtonsPanel(String submitText, Runnable onSubmit, String cancelText, Runnable onCancel) {
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		add(this.submitButton = new Button(submitText, event -> onSubmit.run()));
		add(new Button(cancelText, event -> onCancel.run()));
	}

	public void showSubmitButton(boolean visible) {
		this.submitButton.setVisible(visible);
	}
}
