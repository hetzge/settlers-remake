package jsettlers.main.swing.lobby;

import java.awt.BorderLayout;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import jsettlers.common.resources.ResourceManager;
import jsettlers.main.swing.lobby.organisms.ChatPanel;
import jsettlers.main.swing.lobby.organisms.PlayersPanel;
import jsettlers.main.swing.lobby.organisms.PlayersPanel.PlayerPanel;
import jsettlers.main.swing.lookandfeel.JSettlersLookAndFeel;
import jsettlers.main.swing.lookandfeel.JSettlersLookAndFeelExecption;
import jsettlers.main.swing.resources.SwingResourceProvider;
import jsettlers.main.swing.settings.SettingsManager;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerState;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.UserId;

public class LobbyFrame extends JFrame {
	public static void main(String[] args) throws JSettlersLookAndFeelExecption, IOException {
		ResourceManager.setProvider(new SwingResourceProvider());
		SettingsManager.setup(args);
		JSettlersLookAndFeel.install();

		SwingUtilities.invokeLater(() -> {
			final JFrame frame = new JFrame("Lobby");
			frame.setSize(500, 500);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setVisible(true);
			frame.setLayout(new BorderLayout(10, 10));
			// frame.add(new LobbyPanel(new Ui(frame), new ServerController()), BorderLayout.CENTER);

			final List<Player> players = Arrays.asList(
					new Player(0, "Player 1", new UserId("123"), ELobbyPlayerState.UNKNOWN, ELobbyCivilisation.ROMAN, ELobbyPlayerType.HUMAN, 1, false),
					new Player(1, "Player 2", new UserId("234"), ELobbyPlayerState.UNKNOWN, ELobbyCivilisation.ROMAN, ELobbyPlayerType.HUMAN, 1, true));

			final PlayersPanel[] playersPanelPointer = new PlayersPanel[1];
			{
				final PlayersPanel playersPanel = new PlayersPanel(players, new PlayersPanel.PlayersListener() {

					@Override
					public void setType(int index, ELobbyPlayerType type) {
						playersPanelPointer[0].getPlayerPanel(index).setType(type);
					}

					@Override
					public void setTeam(int index, int team) {
						playersPanelPointer[0].getPlayerPanel(index).setTeam(team);
					}

					@Override
					public void setReady(int index, boolean ready) {
						playersPanelPointer[0].getPlayerPanel(index).setReady(ready);
					}

					@Override
					public void setCivilisation(int index, ELobbyCivilisation civilisation) {
						playersPanelPointer[0].getPlayerPanel(index).setCivilisation(civilisation);
					}
				});
				playersPanelPointer[0] = playersPanel;
			}

			final ChatPanel[] chatPanelPointer = new ChatPanel[1];
			{
				final ChatPanel chatPanel = new ChatPanel(new ChatPanel.ChatListener() {

					@Override
					public void submitMessage(String text) {
						chatPanelPointer[0].addMessage(text);
					}
				});
				chatPanelPointer[0] = chatPanel;
			}

			final JScrollPane scrollPane = new JScrollPane(chatPanelPointer[0]);
			frame.add(scrollPane, BorderLayout.CENTER);
			SwingUtilities.updateComponentTreeUI(frame);
		});
	}
}
