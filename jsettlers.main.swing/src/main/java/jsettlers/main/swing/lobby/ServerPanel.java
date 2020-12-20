package jsettlers.main.swing.lobby;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class ServerPanel extends JPanel {

	public ServerPanel(ServerModel server) {
		add(new JLabel(server.getHost() + ":" + server.getPort()));
	}
}
