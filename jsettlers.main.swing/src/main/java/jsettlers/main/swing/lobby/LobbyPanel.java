package jsettlers.main.swing.lobby;

import java.awt.BorderLayout;

import javax.swing.JPanel;

public class LobbyPanel extends JPanel {

	private final ServersPanel serverListPanel;

	public LobbyPanel(Ui ui, IServerController serverController) {
		super(new BorderLayout());
		this.serverListPanel = new ServersPanel(ui, serverController, new ServerListPanelListener());
		add(this.serverListPanel, BorderLayout.CENTER);
	}

	public ServersPanel getServerListPanel() {
		return this.serverListPanel;
	}

	private class ServerListPanelListener implements ServersPanel.IListener {
		@Override
		public void onServerSelected(ServerModel server) {
			System.out.println("LobbyPanel.ServerListPanelListener.onServerSelected()");
			System.out.println(server.toString());
		}
	}
}
