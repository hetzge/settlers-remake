package jsettlers.main.swing.lobby;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import jsettlers.graphics.localization.Labels;
import jsettlers.main.swing.JSettlersFrame;
import jsettlers.main.swing.lobby.pages.Page;
import jsettlers.main.swing.lobby.pages.mainmenu.DefaultMainMenuPageController;
import jsettlers.main.swing.lobby.pages.mainmenu.MainMenuPagePanel;
import jsettlers.main.swing.settings.ServerManager;

public class UiController {

	private final JSettlersFrame frame;

	public UiController(JSettlersFrame frame) {
		this.frame = frame;
	}

	public JSettlersFrame getFrame() {
		return frame;
	}

	public void setPage(String title, Component component) {
		this.frame.setNewContentPane(new Page(title, component));
		SwingUtilities.updateComponentTreeUI(this.frame);
	}

	public void showHomePage() {
		setPage("JSettlers", new MainMenuPagePanel(new DefaultMainMenuPageController(this, ServerManager.getInstance().getServers().get(0))));
	}

	public void showAlert(String message) {
		JOptionPane.showMessageDialog(frame, message, Labels.getString("errordlg-header"), JOptionPane.ERROR_MESSAGE);
	}

}
