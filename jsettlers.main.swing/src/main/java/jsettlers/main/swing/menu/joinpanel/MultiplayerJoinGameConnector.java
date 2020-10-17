package jsettlers.main.swing.menu.joinpanel;

import javax.swing.SwingUtilities;

import jsettlers.common.ai.EPlayerType;
import jsettlers.common.player.ECivilisation;
import jsettlers.graphics.localization.Labels;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.loading.list.MapList;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.main.JSettlersGame;
import jsettlers.main.swing.JSettlersFrame;
import jsettlers.main.swing.menu.joinpanel.slots.PlayerSlot;
import jsettlers.main.swing.settings.SettingsManager;
import jsettlers.network.NetworkConstants.ENetworkKey;
import jsettlers.network.client.NetworkClient;
import jsettlers.network.client.interfaces.IGameClock;
import jsettlers.network.client.interfaces.INetworkClient;
import jsettlers.network.client.interfaces.INetworkConnector;
import jsettlers.network.client.interfaces.ITaskScheduler;
import jsettlers.network.client.task.ISyncTasksPacketScheduler;
import jsettlers.network.client.task.packets.SyncTasksPacket;
import jsettlers.network.common.packets.ChatMessagePacket;
import jsettlers.network.common.packets.MapInfoPacket;
import jsettlers.network.infrastructure.channel.listeners.SimpleListener;
import jsettlers.network.server.lobby.core.EPlayerState;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.PlayerId;
import jsettlers.network.server.lobby.core.PlayerType;
import jsettlers.network.server.lobby.network.MatchPacket;
import jsettlers.network.server.lobby.network.PlayerPacket;

public final class MultiplayerJoinGameConnector implements IJoinGameConnector {

	private final JSettlersFrame settlersFrame;
	private final INetworkClient client;
	private final MapLoader mapLoader;
	private final String matchId;
	private final JoinGamePanel panel;

	public MultiplayerJoinGameConnector(JSettlersFrame settlersFrame, INetworkClient client, MapLoader mapLoader, String matchId) {
		this.settlersFrame = settlersFrame;
		this.client = client;
		this.mapLoader = mapLoader;
		this.matchId = matchId;
		this.panel = new JoinGamePanel(this);
	}

	@Override
	public JoinGamePanel setup() {
		if (isHost()) {
			new OpenNewGameThread().start();
		} else {
			new JoinGameThread(matchId).start();
		}
		// Setup ui
		SwingUtilities.invokeLater(() -> {
			this.panel.setTitle(Labels.getString("join-game-panel-join-multi-player-game-title"));
			this.panel.setupHost(isHost());
			this.panel.setChatVisible(true);
			this.panel.setupMap(mapLoader);
		});
		// Setup network
		this.client.registerListener(new SimpleListener<>(ENetworkKey.CHAT_MESSAGE, ChatMessagePacket.class, packet -> {
			SwingUtilities.invokeLater(() -> {
				this.panel.appendChat(packet.getAuthorId() + ": " + packet.getMessage());
			});
		}));
		this.client.registerListener(new SimpleListener<>(ENetworkKey.UPDATE_MATCH, MatchPacket.class, packet -> {
			SwingUtilities.invokeLater(() -> {
				final Match match = packet.getMatch();
				this.panel.appendChat(match.toString());
				this.panel.setupMatch(match);
				// Check if player is kicked from match
				if (!match.getUserIds().contains(client.getUserId())) {
					cancel();
				}
			});
		}));
		this.client.registerListener(new SimpleListener<>(ENetworkKey.UPDATE_PLAYER, PlayerPacket.class, packet -> {
			SwingUtilities.invokeLater(() -> {
				this.panel.appendChat(packet.getPlayer().toString());
				this.panel.setupPlayer(packet.getPlayer());
			});
		}));
		this.client.registerListener(new SimpleListener<>(ENetworkKey.MATCH_STARTED, MatchPacket.class, packet -> {
			packet.getMatch().getPlayers().forEach(panel::setupPlayer);
			final Player currentPlayer = packet.getMatch().getPlayer(this.client.getUserId().getPlayerId()).orElseThrow(() -> new IllegalStateException("Current player is not part of the match"));
			final PlayerSetting[] playerSettings = toPlayerSettings(packet);
			final MapLoader mapLoader = MapList.getDefaultList().getMapById(packet.getMatch().getLevelId().getValue());
			final JSettlersGame game = new JSettlersGame(mapLoader, 0L, new GameNetworkConnector(this.client), (byte) currentPlayer.getPosition(), playerSettings);
			this.settlersFrame.showStartingGamePanel(game.start());
			this.client.startGameSynchronization();
		}));
		this.client.registerListener(new SimpleListener<>(ENetworkKey.SYNCHRONOUS_TASK, SyncTasksPacket.class, packet -> {
			// TODO unsafe cast
			((ISyncTasksPacketScheduler) ((NetworkClient) this.client).getGameClock()).scheduleSyncTasksPacket(packet);
		}));
		this.client.registerRejectReceiver(packet -> {
			System.out.println("Received reject packet: rejectedKey: " + packet.getRejectedKey() + " messageid: " + packet.getErrorMessageId());
			SwingUtilities.invokeLater(() -> {
				this.panel.appendChat(Labels.getString("network-message-" + packet.getRejectedKey().name()));
			});
		});
		// TODO player left / kicked
		return this.panel;
	}

	@Override
	public void cancel() {
		this.client.leaveMatch();
		this.settlersFrame.showMainMenu();
	}

	@Override
	public void start() {
		this.client.startMatch();
	}

	@Override
	public PlayerSlot createPlayerSlot(int slot) {
		return new PlayerSlot(this, PlayerId.generate(), mapLoader.getMaxPlayers(), new PlayerType[] {
				PlayerType.EMPTY,
				PlayerType.NONE,
				PlayerType.HUMAN,
				PlayerType.AI_VERY_HARD,
				PlayerType.AI_HARD,
				PlayerType.AI_EASY,
				PlayerType.AI_VERY_EASY });
	}

	@Override
	public void updatePlayer(PlayerId playerId, PlayerType playerType, ECivilisation civilisation, int team, boolean ready) {
		this.client.updatePlayer(new Player(playerId, "", EPlayerState.UNKNOWN, civilisation, playerType, 0, team, ready));
	}

	@Override
	public void sendChatMessage(String message) {
		this.client.sendChatMessage(this.client.getUserId(), message);
	}

	private boolean isHost() {
		return matchId == null;
	}

	private PlayerSetting[] toPlayerSettings(MatchPacket packet) {
		return packet.getMatch().getPlayers().stream().map(player -> {
			final EPlayerType playerType = player.getType().getPlayerType();
			final ECivilisation civilisation = player.getCivilisation();
			final byte team = (byte) player.getTeam();
			if (playerType == null) {
				throw new IllegalStateException(String.format("Failed to start match. Player %s without valid type.", player));
			}
			return new PlayerSetting(playerType, civilisation, team);
		}).toArray(PlayerSetting[]::new);
	}

	private class OpenNewGameThread extends Thread {
		public OpenNewGameThread() {
			super("OpenNewGame");
		}

		@Override
		public void run() {
			final String matchName = mapLoader.getMapName() + "(" + SettingsManager.getInstance().getUserName() + ")";
			final MapInfoPacket mapInfoPacket = new MapInfoPacket(mapLoader.getMapId(), mapLoader.getMapName(), "", "", mapLoader.getMaxPlayers());
			MultiplayerJoinGameConnector.this.client.openNewMatch(matchName, mapLoader.getMaxPlayers(), mapInfoPacket);
		}
	}

	private class JoinGameThread extends Thread {
		private final String matchId;

		public JoinGameThread(String matchId) {
			super("JoinGame");
			this.matchId = matchId;
		}

		@Override
		public void run() {
			MultiplayerJoinGameConnector.this.client.joinMatch(matchId);
		}
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
			this.client.close();
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
