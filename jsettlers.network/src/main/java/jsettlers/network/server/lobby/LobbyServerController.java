package jsettlers.network.server.lobby;

import java.io.IOException;
import java.util.Timer;
import java.util.function.Consumer;

import jsettlers.network.NetworkConstants;
import jsettlers.network.NetworkConstants.ENetworkKey;
import jsettlers.network.common.packets.ChatMessagePacket;
import jsettlers.network.common.packets.IdPacket;
import jsettlers.network.common.packets.OpenNewMatchPacket;
import jsettlers.network.common.packets.PlayerInfoPacket;
import jsettlers.network.common.packets.TimeSyncPacket;
import jsettlers.network.infrastructure.channel.Channel;
import jsettlers.network.infrastructure.channel.GenericDeserializer;
import jsettlers.network.infrastructure.channel.listeners.PacketChannelListener;
import jsettlers.network.infrastructure.channel.packet.EmptyPacket;
import jsettlers.network.infrastructure.channel.packet.Packet;
import jsettlers.network.server.lobby.core.LevelId;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.core.User;
import jsettlers.network.server.lobby.core.UserId;
import jsettlers.network.server.lobby.network.MatchPacket;
import jsettlers.network.server.lobby.network.PlayerPacket;
import jsettlers.network.server.match.MatchesListSendingTimerTask;

public final class LobbyServerController {

	private final Lobby lobby;
	private final Timer sendMatchesTimer;
	private final Timer matchesTaskTimer;

	public LobbyServerController(Lobby lobby) {
		this.lobby = lobby;
		this.sendMatchesTimer = new Timer("SendMatchesListTimer", true);
		this.matchesTaskTimer = new Timer("MatchesTaskDistributionTimer", true);
	}

	public synchronized void start() {
		sendMatchesTimer.schedule(new MatchesListSendingTimerTask(lobby), 0, NetworkConstants.Server.OPEN_MATCHES_SEND_INTERVAL_MS);
	}

	public synchronized void stop() {
		sendMatchesTimer.cancel();
		matchesTaskTimer.cancel();
	}

	public void setup(Channel channel) {
		channel.registerListener(new Listener<>(ENetworkKey.IDENTIFY_USER, PlayerInfoPacket.class, packet -> {
			final User user = new User(new UserId(packet.getId()), packet.getName(), channel);
			lobby.joinLobby(user);
			setup(user);
		}));
	}

	private void setup(User user) {
		final Channel channel = user.getChannel();
		channel.setChannelClosedListener(() -> {
			lobby.leave(user.getId());
		});
		channel.registerListener(new Listener<>(ENetworkKey.REQUEST_OPEN_NEW_MATCH, OpenNewMatchPacket.class, packet -> {
			lobby.createMatch(user.getId(), packet.getMatchName(), new LevelId(packet.getMapInfo().getId()), packet.getMaxPlayers());
		}));
		channel.registerListener(new Listener<>(ENetworkKey.REQUEST_JOIN_MATCH, IdPacket.class, packet -> {
			lobby.joinMatch(user.getId(), new MatchId(packet.getId()));
		}));
		channel.registerListener(new Listener<>(ENetworkKey.REQUEST_LEAVE_MATCH, EmptyPacket.class, packet -> {
			lobby.leaveMatch(user.getId());
		}));
		channel.registerListener(new Listener<>(ENetworkKey.REQUEST_START_MATCH, EmptyPacket.class, packet -> {
			lobby.startMatch(user.getId(), matchesTaskTimer);
		}));
		channel.registerListener(new Listener<>(ENetworkKey.CHAT_MESSAGE, ChatMessagePacket.class, packet -> {
			lobby.sendMatchChatMessage(user.getId(), packet.getMessage());
		}));
		channel.registerListener(new Listener<>(ENetworkKey.UPDATE_MATCH, MatchPacket.class, packet -> {
			lobby.update(user.getId(), packet.getMatch());
		}));
		channel.registerListener(new Listener<>(ENetworkKey.UPDATE_PLAYER, PlayerPacket.class, packet -> {
			lobby.update(user.getId(), packet.getPlayer());
		}));
		channel.registerListener(new Listener<>(ENetworkKey.TIME_SYNC, TimeSyncPacket.class, packet -> {
			lobby.sendMatchTimeSync(user.getId(), packet);
		}));
	}

	private static class Listener<T extends Packet> extends PacketChannelListener<T> {

		private final Consumer<T> consumer;

		public Listener(ENetworkKey key, Class<T> packetClass, Consumer<T> consumer) {
			super(key, new GenericDeserializer<T>(packetClass));
			this.consumer = consumer;
		}

		@Override
		protected void receivePacket(ENetworkKey key, T packet) throws IOException {
			consumer.accept(packet);
		}
	}
}
