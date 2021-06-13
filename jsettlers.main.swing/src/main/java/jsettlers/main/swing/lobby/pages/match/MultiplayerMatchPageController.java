package jsettlers.main.swing.lobby.pages.match;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.swing.SwingUtilities;

import jsettlers.graphics.localization.Labels;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.loading.list.MapList;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.main.JSettlersGame;
import jsettlers.main.swing.lobby.UiController;
import jsettlers.main.swing.lobby.organisms.MatchSettingsPanel;
import jsettlers.main.swing.lobby.organisms.PlayersPanel.PlayerPanel;
import jsettlers.main.swing.settings.SettingsManager;
import jsettlers.network.NetworkConstants;
import jsettlers.network.NetworkConstants.ENetworkKey;
import jsettlers.network.client.NetworkClient;
import jsettlers.network.client.interfaces.IGameClock;
import jsettlers.network.client.interfaces.INetworkConnector;
import jsettlers.network.client.interfaces.ITaskScheduler;
import jsettlers.network.client.task.ISyncTasksPacketScheduler;
import jsettlers.network.client.task.packets.SyncTasksPacket;
import jsettlers.network.common.packets.BooleanMessagePacket;
import jsettlers.network.common.packets.ChatMessagePacket;
import jsettlers.network.common.packets.MapInfoPacket;
import jsettlers.network.infrastructure.channel.listeners.SimpleListener;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerState;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.ELobbyResourceAmount;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.network.MatchPacket;
import jsettlers.network.server.lobby.network.PlayerPacket;

public class MultiplayerMatchPageController implements MatchPageController {

	private final UiController ui;
	private final MatchPagePanel panel;
	private final NetworkClient client;
	private final MatchId matchId;
	private final MapLoader mapLoader;
	private final boolean host;
	private Player[] players;

	public MultiplayerMatchPageController(UiController ui, NetworkClient client, MatchId matchId, MapLoader mapLoader, boolean host) {
		this.ui = ui;
		this.panel = new MatchPagePanel(this);
		this.client = client;
		this.matchId = matchId;
		this.mapLoader = mapLoader;
		this.host = host;
		this.players = new Player[0];
	}

	@Override
	public MatchPagePanel init() {
		// Setup ui
		this.panel.getChatPanel().setVisible(true);
		this.panel.getMatchSettingsPanel().setMapInformation(this.mapLoader);
		// Setup network
		this.client.registerListener(new SimpleListener<>(ENetworkKey.CHAT_MESSAGE, ChatMessagePacket.class, packet -> {
			this.panel.getChatPanel().addMessage(packet.toString());
			this.panel.getChatPanel().addMessage(packet.getMessage());
		}));
		this.client.registerListener(new SimpleListener<>(ENetworkKey.UPDATE_MATCH, MatchPacket.class, packet -> {
			this.panel.getChatPanel().addMessage(packet.toString());
			updateMatch(packet.getMatch());
		}));
		this.client.registerListener(new SimpleListener<>(ENetworkKey.UPDATE_PLAYER, PlayerPacket.class, packet -> {
			this.panel.getChatPanel().addMessage(packet.toString());
			final Player player = packet.getPlayer();
			final int index = player.getIndex();
			this.players[index] = player;
			final PlayerPanel playerPanel = this.panel.getPlayersPanel().getPlayerPanel(index);
			playerPanel.setCivilisation(player.getCivilisation());
			playerPanel.setPlayer(player.getName());
			playerPanel.setReady(player.getState() == ELobbyPlayerState.READY);
			playerPanel.setTeam(player.getTeam());
			playerPanel.setType(player.getType());
			playerPanel.setEnabled(canEditPlayer(player));
			playerPanel.setReadyEnabled(host || isCurrentPlayer(player));
			updateStartButton();
		}));
		this.client.registerListener(new SimpleListener<>(ENetworkKey.KICK_USER, BooleanMessagePacket.class, packet -> {
			this.panel.getChatPanel().addMessage(packet.toString());
			cancel();
		}));
		this.client.registerListener(new SimpleListener<>(ENetworkKey.MATCH_STARTED, MatchPacket.class, packet -> {
			this.panel.getChatPanel().addMessage(packet.toString());
			this.client.removeListener(NetworkConstants.ENetworkKey.MATCH_STARTED);
			final Player currentPlayer = packet.getMatch().getPlayer(this.client.getUserId()).orElseThrow(() -> new IllegalStateException("Current player is not part of the match"));
			final PlayerSetting[] playerSettings = MatchPageUtils.toPlayerSettings(packet.getMatch().getPlayers());
			final MapLoader mapLoader = MapList.getDefaultList().getMapById(packet.getMatch().getLevelId().getValue());
			final JSettlersGame game = new JSettlersGame(mapLoader, 0L, new GameNetworkConnector(), (byte) currentPlayer.getIndex(), playerSettings);
			ui.getFrame().showStartingGamePanel(game.start());
			this.client.startTimeSynchronization();
		}));
		this.client.registerListener(new SimpleListener<>(ENetworkKey.SYNCHRONOUS_TASK, SyncTasksPacket.class, packet -> {
			this.panel.getChatPanel().addMessage(packet.toString());
			((ISyncTasksPacketScheduler) this.client.getGameClock()).scheduleSyncTasksPacket(packet);
		}));
		this.client.registerRejectReceiver(packet -> {
			this.panel.getChatPanel().addMessage(packet.toString());
			this.panel.getChatPanel().addMessage(Labels.getString("network-message-" + packet.getRejectedKey().name()));
		});
		return panel;
	}

	private void updateMatch(Match match) {
		final List<Player> players = match.getPlayers();
		final MatchSettingsPanel matchSettingsPanel = this.panel.getMatchSettingsPanel();
		matchSettingsPanel.setPeaceTime((int) match.getPeaceTime().toMinutes());
		matchSettingsPanel.setStartResources(match.getResourceAmount());
		this.panel.setTitle(match.getName());
		if (!players.isEmpty()) {
			this.players = players.toArray(new Player[0]);
			this.panel.getPlayersPanel().setPlayers(players);
			for (Player player : players) {
				this.panel.getPlayersPanel().getPlayerPanel(player.getIndex()).setEnabled(canEditPlayer(player));
			}
		}
		updateStartButton();
	}

	private boolean canEditPlayer(Player player) {
		return host || (isCurrentPlayer(player) && !player.isReady());
	}

	private boolean isCurrentPlayer(Player player) {
		return this.client.getUserId().equals(player.getUserId().orElse(null));
	}

	private void updateStartButton() {
		final boolean areAllPlayersReady = Arrays.stream(MultiplayerMatchPageController.this.players).allMatch(Player::isReady);
		this.panel.showStartButton(host && areAllPlayersReady);
	}

	@Override
	public void cancel() {
		this.client.removeListener(ENetworkKey.JOIN_MATCH);
		this.client.removeListener(ENetworkKey.CHAT_MESSAGE);
		this.client.removeListener(ENetworkKey.UPDATE_MATCH);
		this.client.removeListener(ENetworkKey.UPDATE_PLAYER);
		this.client.removeListener(ENetworkKey.KICK_USER);
		this.client.removeListener(ENetworkKey.MATCH_STARTED);
		this.client.removeListener(ENetworkKey.SYNCHRONOUS_TASK);
		this.client.leaveMatch();
		ui.showHomePage();
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
		this.client.updateMatchPeaceTime(minutes);
	}

	@Override
	public void setStartResources(ELobbyResourceAmount amount) {
		this.client.updateMatchStartResourceAmount(amount);
	}

	@Override
	public void startMatch() {
		this.client.startMatch();
	}

	/* --- FACTORIES --- */

	public static CompletableFuture<MatchPagePanel> joinMatch(UiController ui, NetworkClient client, MapLoader mapLoader, MatchId matchId) {
		return create(ui, client, mapLoader, matchId);
	}

	public static CompletableFuture<MatchPagePanel> createMatch(UiController ui, NetworkClient client, MapLoader mapLoader) {
		return create(ui, client, mapLoader, null);
	}

	private static CompletableFuture<MatchPagePanel> create(UiController ui, NetworkClient client, MapLoader mapLoader, MatchId matchId) {
		final boolean isHost = matchId == null;
		final CompletableFuture<MatchPagePanel> future = new CompletableFuture<>();
		client.registerListener(new SimpleListener<>(ENetworkKey.JOIN_MATCH, MatchPacket.class, packet -> {
			client.removeListener(ENetworkKey.JOIN_MATCH);
			SwingUtilities.invokeLater(() -> {
				final Match match = packet.getMatch();
				final MultiplayerMatchPageController controller = new MultiplayerMatchPageController(ui, client, match.getId(), mapLoader, isHost);
				future.complete(controller.init());
				controller.updateMatch(match);
			});
		}));
		CompletableFuture.runAsync(() -> {
			if (isHost) {
				final String matchName = mapLoader.getMapName() + " [" + SettingsManager.getInstance().getUserName() + "]";
				final MapInfoPacket mapInfoPacket = new MapInfoPacket(mapLoader.getMapId(), mapLoader.getMapName(), "", "", mapLoader.getMaxPlayers());
				client.openNewMatch(matchName, mapLoader.getMaxPlayers(), mapInfoPacket);
			} else {
				client.joinMatch(matchId);
			}
		});
		return future;
	}

	private class GameNetworkConnector implements INetworkConnector {

		@Override
		public ITaskScheduler getTaskScheduler() {
			return (ITaskScheduler) MultiplayerMatchPageController.this.client; // TODO
		}

		@Override
		public IGameClock getGameClock() {
			return MultiplayerMatchPageController.this.client.getGameClock();
		}

		@Override
		public void shutdown() {
			MultiplayerMatchPageController.this.client.close();
		}

		@Override
		public void setStartFinished(boolean startFinished) {
			MultiplayerMatchPageController.this.client.setStartFinished(startFinished);
		}

		@Override
		public boolean haveAllPlayersStartFinished() {
			return Arrays.stream(MultiplayerMatchPageController.this.players)
					.filter(player -> player.getType().isHuman())
					.map(Player::getState)
					.allMatch(ELobbyPlayerState.INGAME::equals);
		}
	}
}
