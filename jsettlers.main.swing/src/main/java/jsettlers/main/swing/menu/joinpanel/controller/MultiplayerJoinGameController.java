package jsettlers.main.swing.menu.joinpanel.controller;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import javax.swing.SwingUtilities;

import jsettlers.common.ai.EPlayerType;
import jsettlers.common.player.ECivilisation;
import jsettlers.graphics.localization.Labels;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.loading.list.MapList;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.main.JSettlersGame;
import jsettlers.main.swing.JSettlersFrame;
import jsettlers.main.swing.menu.joinpanel.JoinGamePanel;
import jsettlers.main.swing.menu.joinpanel.PlayerSlot;
import jsettlers.main.swing.menu.joinpanel.Utils;
import jsettlers.main.swing.settings.SettingsManager;
import jsettlers.network.NetworkConstants;
import jsettlers.network.NetworkConstants.ENetworkKey;
import jsettlers.network.client.NetworkClient;
import jsettlers.network.client.interfaces.IGameClock;
import jsettlers.network.client.interfaces.INetworkClient;
import jsettlers.network.client.interfaces.INetworkConnector;
import jsettlers.network.client.interfaces.ITaskScheduler;
import jsettlers.network.client.task.ISyncTasksPacketScheduler;
import jsettlers.network.client.task.packets.SyncTasksPacket;
import jsettlers.network.common.packets.BooleanMessagePacket;
import jsettlers.network.common.packets.ChatMessagePacket;
import jsettlers.network.common.packets.MapInfoPacket;
import jsettlers.network.infrastructure.channel.listeners.SimpleListener;
import jsettlers.network.infrastructure.channel.reject.RejectPacket;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.LevelId;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.core.MatchState;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.ResourceAmount;
import jsettlers.network.server.lobby.network.MatchPacket;
import jsettlers.network.server.lobby.network.PlayerPacket;

public final class MultiplayerJoinGameController implements IJoinGameController {

	private final JSettlersFrame settlersFrame;
	private final INetworkClient client;
	private final MapLoader mapLoader;
	private final MatchId matchId;
	private final JoinGamePanel panel;

	private MultiplayerJoinGameController(JSettlersFrame settlersFrame, INetworkClient client, MapLoader mapLoader, MatchId matchId) {
		this.settlersFrame = settlersFrame;
		this.client = client;
		this.mapLoader = mapLoader;
		this.matchId = matchId;
		this.panel = new JoinGamePanel(this);
	}

	@Override
	public JoinGamePanel setup() {

		// Setup ui
		SwingUtilities.invokeLater(() -> {
			this.panel.setTitle(Labels.getString("join-game-panel-join-multi-player-game-title"));
			this.panel.setChatVisible(true);
			this.panel.setupMap(mapLoader);
		});
		// Setup network
		this.client.registerListener(new SimpleListener<>(ENetworkKey.CHAT_MESSAGE, ChatMessagePacket.class, packet -> {
			onChatMessage(packet);
		}));
		this.client.registerListener(new SimpleListener<>(ENetworkKey.UPDATE_MATCH, MatchPacket.class, packet -> {
			onMatchUpdate(packet);
		}));
		this.client.registerListener(new SimpleListener<>(ENetworkKey.UPDATE_PLAYER, PlayerPacket.class, packet -> {
			onPlayerUpdate(packet);
		}));
		this.client.registerListener(new SimpleListener<>(ENetworkKey.KICK_USER, BooleanMessagePacket.class, packet -> {
			onKick();
		}));
		this.client.registerListener(new SimpleListener<>(ENetworkKey.MATCH_STARTED, MatchPacket.class, packet -> {
			onMatchStarted(packet);
		}));
		this.client.registerListener(new SimpleListener<>(ENetworkKey.SYNCHRONOUS_TASK, SyncTasksPacket.class, packet -> {
			onSynchronousTask(packet);
		}));
		this.client.registerRejectReceiver(packet -> {
			onReject(packet);
		});
		return this.panel;
	}

	private void onMatchUpdate(MatchPacket packet) {
		SwingUtilities.invokeLater(() -> {
			final Match match = packet.getMatch();
			this.panel.appendChat(match.toString());
			this.panel.setupMatch(match);
			for (Player player : match.getPlayers()) {
				if (player.isUser(client.getUserId())) {
					this.panel.setupCurrentPlayer(player);
				}
			}
		});
	}

	private void onPlayerUpdate(PlayerPacket packet) {
		SwingUtilities.invokeLater(() -> {
			final Player player = packet.getPlayer();
			this.panel.appendChat(player.toString());
			this.panel.setupPlayer(player);
			if (player.isUser(client.getUserId())) {
				this.panel.setupCurrentPlayer(player);
			}
		});
	}

	private void onChatMessage(ChatMessagePacket packet) {
		SwingUtilities.invokeLater(() -> {
			this.panel.appendChat(packet.getAuthorId() + ": " + packet.getMessage());
		});
	}

	private void onKick() {
		SwingUtilities.invokeLater(() -> {
			cancel();
		});
	}

	private void onMatchStarted(MatchPacket packet) {
		this.client.removeListener(NetworkConstants.ENetworkKey.MATCH_STARTED);
		packet.getMatch().getPlayers().forEach(panel::setupPlayer);
		final Player currentPlayer = packet.getMatch().getPlayer(this.client.getUserId()).orElseThrow(() -> new IllegalStateException("Current player is not part of the match"));
		final PlayerSetting[] playerSettings = toPlayerSettings(packet);
		final MapLoader mapLoader = MapList.getDefaultList().getMapById(packet.getMatch().getLevelId().getValue());
		final JSettlersGame game = new JSettlersGame(mapLoader, 0L, new GameNetworkConnector(this.client), (byte) currentPlayer.getIndex(), playerSettings);
		this.settlersFrame.showStartingGamePanel(game.start());
		this.client.startTimeSynchronization();
	}

	private void onSynchronousTask(SyncTasksPacket packet) {
		// TODO unsafe cast
		((ISyncTasksPacketScheduler) ((NetworkClient) this.client).getGameClock()).scheduleSyncTasksPacket(packet);
	}

	private void onReject(RejectPacket packet) {
		System.out.println("Received reject packet: rejectedKey: " + packet.getRejectedKey() + " messageid: " + packet.getErrorMessageId());
		SwingUtilities.invokeLater(() -> {
			this.panel.appendChat(Labels.getString("network-message-" + packet.getRejectedKey().name()));
		});
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
		this.settlersFrame.showMainMenu();
	}

	@Override
	public void start() {
		this.client.startMatch();
	}

	@Override
	public PlayerSlot createPlayerSlot(Player player) {
		return new PlayerSlot(this, player.getIndex(), mapLoader.getMaxPlayers(), ELobbyPlayerType.VALUES);
	}

	@Override
	public void updatePlayerType(int playerIndex, ELobbyPlayerType playerType) {
		this.client.updatePlayerType(playerIndex, playerType);
	}

	@Override
	public void updatePlayerCivilisation(int playerIndex, ELobbyCivilisation civilisation) {
		this.client.updatePlayerCivilisation(playerIndex, civilisation);
	}

	@Override
	public void updatePlayerTeam(int playerIndex, int team) {
		this.client.updatePlayerTeam(playerIndex, team);
	}

	@Override
	public void updatePlayerReady(int playerIndex, boolean ready) {
		this.client.updatePlayerReady(playerIndex, ready);
	}

	@Override
	public void updateMatch(Duration peaceTime, ResourceAmount startResources) {
		System.out.println("MultiplayerJoinGameConnector.updateMatch(" + matchId + ")");
		this.client.updateMatch(new Match(matchId, "", new LevelId(mapLoader.getMapId()), Collections.emptyList(), startResources, peaceTime, MatchState.OPENED));
	}

	@Override
	public void sendChatMessage(String message) {
		this.client.sendChatMessage(message);
	}

	private PlayerSetting[] toPlayerSettings(MatchPacket packet) {
		return packet.getMatch().getPlayers().stream().map(player -> {
			final EPlayerType playerType = Utils.ingame(player.getType());
			final ECivilisation civilisation = Utils.ingame(player.getCivilisation());
			final byte team = (byte) player.getTeam();
			if (playerType == null) {
				throw new IllegalStateException(String.format("Failed to start match. Player %s without valid type.", player));
			}
			return new PlayerSetting(playerType, civilisation, team);
		}).toArray(PlayerSetting[]::new);
	}

	public static CompletableFuture<JoinGamePanel> joinMatch(JSettlersFrame settlersFrame, INetworkClient client, MapLoader mapLoader, MatchId matchId) {
		return create(settlersFrame, client, mapLoader, matchId);
	}

	public static CompletableFuture<JoinGamePanel> createMatch(JSettlersFrame settlersFrame, INetworkClient client, MapLoader mapLoader) {
		return create(settlersFrame, client, mapLoader, null);
	}

	private static CompletableFuture<JoinGamePanel> create(JSettlersFrame settlersFrame, INetworkClient client, MapLoader mapLoader, MatchId matchId) {
		final boolean isHost = matchId == null;
		final CompletableFuture<JoinGamePanel> future = new CompletableFuture<>();
		client.registerListener(new SimpleListener<>(ENetworkKey.JOIN_MATCH, MatchPacket.class, packet -> {
			System.out.println("MultiplayerJoinGameController.create() B");
			client.removeListener(ENetworkKey.JOIN_MATCH);
			final MultiplayerJoinGameController controller = new MultiplayerJoinGameController(settlersFrame, client, mapLoader, packet.getMatch().getId());
			future.complete(controller.setup());
			controller.onMatchUpdate(packet);
		}));
		CompletableFuture.runAsync(() -> {
			System.out.println("MultiplayerJoinGameController.create() A");
			if (isHost) {
				final String matchName = mapLoader.getMapName() + "(" + SettingsManager.getInstance().getUserName() + ")";
				final MapInfoPacket mapInfoPacket = new MapInfoPacket(mapLoader.getMapId(), mapLoader.getMapName(), "", "", mapLoader.getMaxPlayers());
				client.openNewMatch(matchName, mapLoader.getMaxPlayers(), mapInfoPacket);
			} else {
				client.joinMatch(matchId);
			}
		});
		return future;
	}

	private class GameNetworkConnector implements INetworkConnector {

		private final INetworkClient client;

		public GameNetworkConnector(INetworkClient client) {
			this.client = client;
		}

		@Override
		public ITaskScheduler getTaskScheduler() {
			return (ITaskScheduler) this.client; // TODO
		}

		@Override
		public IGameClock getGameClock() {
			return this.client.getGameClock();
		}

		@Override
		public void shutdown() {
			try {
				this.client.close();
			} catch (IOException exception) {
				throw new IllegalStateException(exception);
			}
		}

		@Override
		public void setStartFinished(boolean startFinished) {
			this.client.setStartFinished(startFinished);
		}

		@Override
		public boolean haveAllPlayersStartFinished() {
			return panel.haveAllPlayersStartFinished();
		}
	}
}
