package jsettlers.main.swing.lobby.pages;

import javax.swing.SwingUtilities;

import jsettlers.graphics.localization.Labels;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.main.swing.lobby.Ui;
import jsettlers.main.swing.lobby.organisms.MatchSettingsPanel.StartResources;
import jsettlers.network.NetworkConstants.ENetworkKey;
import jsettlers.network.client.NetworkClient;
import jsettlers.network.client.task.packets.SyncTasksPacket;
import jsettlers.network.common.packets.BooleanMessagePacket;
import jsettlers.network.common.packets.ChatMessagePacket;
import jsettlers.network.infrastructure.channel.listeners.SimpleListener;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.network.MatchPacket;
import jsettlers.network.server.lobby.network.PlayerPacket;

public class MultiplayerMatchPageController implements MatchPagePanel.Controller {

	private final Ui ui;
	private final MatchPagePanel panel;
	private final NetworkClient client;
	private final MatchId matchId;
	private  final MapLoader mapLoader;

	public MultiplayerMatchPageController(Ui ui, NetworkClient client, MatchId matchId, MapLoader mapLoader) {
		this.ui = ui;
		this.panel = new MatchPagePanel(this);
		this.client = client;
		this.matchId = matchId;
		this.mapLoader = mapLoader;
	}

	@Override
	public MatchPagePanel init() {
		// Setup ui
		this.panel.setTitle(Labels.getString("join-game-panel-join-multi-player-game-title"));
		this.panel.getChatPanel().setVisible(true);
		this.panel.getMatchSettingsPanel().setMapInformation(this.mapLoader);
//		// Setup network
//		this.client.registerListener(new SimpleListener<>(ENetworkKey.CHAT_MESSAGE, ChatMessagePacket.class, packet -> {
//			onChatMessage(packet);
//		}));
//		this.client.registerListener(new SimpleListener<>(ENetworkKey.UPDATE_MATCH, MatchPacket.class, packet -> {
//			onMatchUpdate(packet);
//		}));
//		this.client.registerListener(new SimpleListener<>(ENetworkKey.UPDATE_PLAYER, PlayerPacket.class, packet -> {
//			onPlayerUpdate(packet);
//		}));
//		this.client.registerListener(new SimpleListener<>(ENetworkKey.KICK_USER, BooleanMessagePacket.class, packet -> {
//			onKick();
//		}));
//		this.client.registerListener(new SimpleListener<>(ENetworkKey.MATCH_STARTED, MatchPacket.class, packet -> {
//			onMatchStarted(packet);
//		}));
//		this.client.registerListener(new SimpleListener<>(ENetworkKey.SYNCHRONOUS_TASK, SyncTasksPacket.class, packet -> {
//			onSynchronousTask(packet);
//		}));
//		this.client.registerRejectReceiver(packet -> {
//			onReject(packet);
//		});
		return panel;
	}

	@Override
	public void setType(int index, ELobbyPlayerType type) {
		this.client.updatePlayerType(index, type);
	}

	@Override
	public void setCivilisation(int index, ELobbyCivilisation civilisation) {
		this.client.updatePlayerCivilisation(index, civilisation);
	}

	@Override
	public void setTeam(int index, int team) {
		this.client.updatePlayerTeam(index, team);
	}

	@Override
	public void setReady(int index, boolean ready) {
		this.client.updatePlayerReady(index, ready);
	}

	@Override
	public void submitMessage(String text) {
		this.client.sendChatMessage(text);
	}

	@Override
	public void setPeaceTime(int minutes) {
		// this.client.updateMatch(new Match(id, name, levelId, players, resourceAmount, peaceTime, state));
	}

	@Override
	public void setStartResources(StartResources resources) {
		// this.client.updateMatch(new Match(id, name, levelId, players, resourceAmount, peaceTime, state));
	}

	@Override
	public void startMatch() {
		System.out.println("");
	}
}
