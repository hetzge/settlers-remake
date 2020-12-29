/*******************************************************************************
 * Copyright (c) 2015 - 2017
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.network.client;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import jsettlers.network.NetworkConstants;
import jsettlers.network.NetworkConstants.ENetworkKey;
import jsettlers.network.client.interfaces.IGameClock;
import jsettlers.network.client.interfaces.INetworkClient;
import jsettlers.network.client.interfaces.ITaskScheduler;
import jsettlers.network.client.receiver.IPacketReceiver;
import jsettlers.network.client.task.packets.TaskPacket;
import jsettlers.network.client.time.TimeSyncSenderTimerTask;
import jsettlers.network.client.time.TimeSynchronizationListener;
import jsettlers.network.common.packets.BooleanMessagePacket;
import jsettlers.network.common.packets.ChatMessagePacket;
import jsettlers.network.common.packets.IdPacket;
import jsettlers.network.common.packets.MapInfoPacket;
import jsettlers.network.common.packets.OpenNewMatchPacket;
import jsettlers.network.common.packets.PlayerInfoPacket;
import jsettlers.network.infrastructure.channel.AsyncChannel;
import jsettlers.network.infrastructure.channel.GenericDeserializer;
import jsettlers.network.infrastructure.channel.IChannelListener;
import jsettlers.network.infrastructure.channel.listeners.SimpleListener;
import jsettlers.network.infrastructure.channel.packet.EmptyPacket;
import jsettlers.network.infrastructure.channel.packet.Packet;
import jsettlers.network.infrastructure.channel.reject.RejectPacket;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.core.UserId;
import jsettlers.network.server.lobby.network.MatchArrayPacket;
import jsettlers.network.server.lobby.network.MatchPacket;
import jsettlers.network.server.lobby.network.UpdatePlayerPacket;
import jsettlers.network.synchronic.timer.NetworkTimer;

/**
 * The {@link NetworkClient} class offers an interface to the servers methods. All methods of the {@link NetworkClient} class will never block. All calls to the server are done by an asynchronous
 * Thread.
 * 
 * @author Andreas Eberle
 * 
 */
public class NetworkClient implements ITaskScheduler, INetworkClient {

	private final AsyncChannel channel;
	private final Timer timer;
	private final INetworkClientClock clock;
	private final UserId userId;

	public NetworkClient(AsyncChannel channel, UserId userId) {
		this.channel = channel;
		this.userId = userId;
		this.timer = new Timer("NetworkClientTimer");
		this.clock = new NetworkTimer();

		channel.setChannelClosedListener(this::close);
		if (!channel.isStarted()) {
			channel.start();
		}
	}

	@Override
	public void close() {
		timer.cancel();
		channel.close();
		clock.stopExecution();
	}

	@Override
	public UserId getUserId() {
		return userId;
	}

	// LOBBY

	@Override
	public CompletableFuture<Void> logIn(String username) {
		final CompletableFuture<Void> future = new CompletableFuture<Void>();
		channel.registerListener(new SimpleListener<>(ENetworkKey.IDENTIFY_USER, PlayerInfoPacket.class, packet -> future.complete(null)));
		channel.sendPacketAsync(ENetworkKey.IDENTIFY_USER, new PlayerInfoPacket(userId.getValue(), username, false));
		return future;
	}

	@Override
	public void sendChatMessage(String message) throws IllegalStateException {
		channel.sendPacketAsync(ENetworkKey.CHAT_MESSAGE, new ChatMessagePacket(userId.getValue(), message));
	}

	// MATCH

	@Override
	public void queryMatches(Consumer<List<Match>> callback) {
		channel.registerListener(new SimpleListener<>(ENetworkKey.UPDATE_MATCHES, MatchArrayPacket.class, packet -> {
			callback.accept(Arrays.asList(packet.getMatches()));
		}));
		channel.sendPacketAsync(ENetworkKey.QUERY_MATCHES, new BooleanMessagePacket(true));
	}

	@Override
	public void openNewMatch(String matchName, int maxPlayers, MapInfoPacket mapInfo) {
		channel.sendPacketAsync(ENetworkKey.REQUEST_OPEN_NEW_MATCH, new OpenNewMatchPacket(matchName, maxPlayers, mapInfo, 0L));
	}

	@Override
	public void joinMatch(MatchId matchId) {
		channel.sendPacketAsync(ENetworkKey.REQUEST_JOIN_MATCH, new IdPacket(matchId.getValue()));
	}

	@Override
	public void leaveMatch() {
		channel.sendPacketAsync(ENetworkKey.REQUEST_LEAVE_MATCH, new EmptyPacket());
	}

	@Override
	public void startMatch() throws IllegalStateException {
		channel.sendPacketAsync(ENetworkKey.REQUEST_START_MATCH, new EmptyPacket());
	}

	@Override
	public void updateMatch(Match match) {
		channel.sendPacket(ENetworkKey.UPDATE_MATCH, new MatchPacket(match));
	}

	@Override
	public void updatePlayerType(int playerIndex, ELobbyPlayerType playerType) {
		channel.sendPacket(ENetworkKey.UPDATE_PLAYER_TYPE, new UpdatePlayerPacket(playerIndex, playerType.ordinal()));
	}

	@Override
	public void updatePlayerCivilisation(int playerIndex, ELobbyCivilisation civilisation) {
		channel.sendPacket(ENetworkKey.UPDATE_PLAYER_CIVILISATION, new UpdatePlayerPacket(playerIndex, civilisation.ordinal()));
	}

	@Override
	public void updatePlayerTeam(int playerIndex, int team) {
		channel.sendPacket(ENetworkKey.UPDATE_PLAYER_TEAM, new UpdatePlayerPacket(playerIndex, team));
	}

	@Override
	public void updatePlayerReady(int playerIndex, boolean ready) {
		channel.sendPacket(ENetworkKey.UPDATE_PLAYER_READY, new UpdatePlayerPacket(playerIndex, ready));
	}

	// INGAME

	@Override
	public void setStartFinished(boolean startFinished) throws IllegalStateException {
		channel.sendPacketAsync(ENetworkKey.CHANGE_START_FINISHED, new BooleanMessagePacket(startFinished));
	}

	@Override
	public void startTimeSynchronization() {
		channel.registerListener(new TimeSynchronizationListener(channel, clock));
		timer.schedule(new TimeSyncSenderTimerTask(channel, clock), 0, NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL);
		channel.initPinging();
	}

	@Override
	public IGameClock getGameClock() {
		return clock;
	}

	@Override
	public void scheduleTask(TaskPacket task) {
		channel.sendPacketAsync(ENetworkKey.SYNCHRONOUS_TASK, task);
	}

	// CALLBACKS

	@Override
	public void registerListener(IChannelListener listener) {
		channel.registerListener(listener);
	}

	@Override
	public void removeListener(ENetworkKey key) {
		channel.removeListener(key);
	}

	@Override
	public void registerRejectReceiver(IPacketReceiver<RejectPacket> rejectListener) {
		channel.registerListener(generateDefaultListener(NetworkConstants.ENetworkKey.REJECT_PACKET, RejectPacket.class, rejectListener));
	}

	private <T extends Packet> DefaultClientPacketListener<T> generateDefaultListener(ENetworkKey key, Class<T> classType,
			IPacketReceiver<T> listener) {
		return new DefaultClientPacketListener<>(key, new GenericDeserializer<>(classType), listener);
	}
}
