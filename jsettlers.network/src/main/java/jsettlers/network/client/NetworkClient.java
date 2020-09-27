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

import java.io.IOException;
import java.util.Timer;

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
import jsettlers.network.common.packets.IntegerMessagePacket;
import jsettlers.network.common.packets.MapInfoPacket;
import jsettlers.network.common.packets.MatchInfoUpdatePacket;
import jsettlers.network.common.packets.MatchStartPacket;
import jsettlers.network.common.packets.OpenNewMatchPacket;
import jsettlers.network.common.packets.PlayerInfoPacket;
import jsettlers.network.infrastructure.channel.AsyncChannel;
import jsettlers.network.infrastructure.channel.GenericDeserializer;
import jsettlers.network.infrastructure.channel.IChannelListener;
import jsettlers.network.infrastructure.channel.packet.EmptyPacket;
import jsettlers.network.infrastructure.channel.packet.Packet;
import jsettlers.network.infrastructure.channel.reject.RejectPacket;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.UserId;
import jsettlers.network.server.lobby.network.MatchArrayPacket;
import jsettlers.network.server.lobby.network.PlayerPacket;
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

	public NetworkClient(AsyncChannel channel, UserId userId) throws IOException {
		this.channel = channel;
		this.userId = userId;
		channel.setChannelClosedListener(() -> {
			close();
		});

		this.timer = new Timer("NetworkClientTimer");
		this.clock = new NetworkTimer();

		if (!channel.isStarted()) {
			channel.start();
		}
	}

	@Override
	public void logIn(String name, IPacketReceiver<MatchArrayPacket> matchesReceiver) throws IllegalStateException {
		channel.registerListener(generateDefaultListener(ENetworkKey.UPDATE_MATCHES, MatchArrayPacket.class, matchesReceiver));
		channel.sendPacketAsync(ENetworkKey.IDENTIFY_USER, new PlayerInfoPacket(userId.getValue(), name, false));
	}

	@Override
	public void openNewMatch(String matchName, int maxPlayers, MapInfoPacket mapInfo, long randomSeed, IPacketReceiver<MatchStartPacket> matchStartedListener,
			IPacketReceiver<MatchInfoUpdatePacket> matchInfoUpdatedListener, IPacketReceiver<ChatMessagePacket> chatMessageReceiver) throws IllegalStateException {
		channel.sendPacketAsync(NetworkConstants.ENetworkKey.REQUEST_OPEN_NEW_MATCH, new OpenNewMatchPacket(matchName, maxPlayers, mapInfo, randomSeed));
	}

	@Override
	public void joinMatch(String matchId, IPacketReceiver<MatchStartPacket> matchStartedListener,
			IPacketReceiver<MatchInfoUpdatePacket> matchInfoUpdatedListener, IPacketReceiver<ChatMessagePacket> chatMessageReceiver)
			throws IllegalStateException {
		channel.sendPacketAsync(NetworkConstants.ENetworkKey.REQUEST_JOIN_MATCH, new IdPacket(matchId));
	}

	@Override
	public void leaveMatch() {
		channel.sendPacketAsync(NetworkConstants.ENetworkKey.REQUEST_LEAVE_MATCH, new EmptyPacket());
	}

	@Override
	public void startMatch() throws IllegalStateException {
		channel.sendPacketAsync(NetworkConstants.ENetworkKey.REQUEST_START_MATCH, new EmptyPacket());
	}

	@Override
	public void setReadyState(boolean ready) throws IllegalStateException {
		channel.sendPacketAsync(NetworkConstants.ENetworkKey.CHANGE_READY_STATE, new BooleanMessagePacket(ready));
	}

	@Override
	public void setCivilisation(int civilisation) throws IllegalStateException {
		channel.sendPacketAsync(NetworkConstants.ENetworkKey.CHANGE_CIVILISATION, new IntegerMessagePacket(civilisation));
	}

	@Override
	public void setTeamId(byte teamId) throws IllegalStateException {
		channel.sendPacketAsync(NetworkConstants.ENetworkKey.CHANGE_TEAM, new IntegerMessagePacket(teamId));
	}

	@Override
	public void setStartFinished(boolean startFinished) throws IllegalStateException {
		channel.sendPacketAsync(NetworkConstants.ENetworkKey.CHANGE_START_FINISHED, new BooleanMessagePacket(startFinished));
	}

	@Override
	public void sendChatMessage(UserId userId, String message) throws IllegalStateException {
		channel.sendPacketAsync(NetworkConstants.ENetworkKey.CHAT_MESSAGE, new ChatMessagePacket(userId.getValue(), message));
	}

	@Override
	public void registerRejectReceiver(IPacketReceiver<RejectPacket> rejectListener) {
		channel.registerListener(generateDefaultListener(NetworkConstants.ENetworkKey.REJECT_PACKET, RejectPacket.class, rejectListener));
	}

	@Override
	public void scheduleTask(TaskPacket task) {
		channel.sendPacketAsync(NetworkConstants.ENetworkKey.SYNCHRONOUS_TASK, task);
	}

	private <T extends Packet> DefaultClientPacketListener<T> generateDefaultListener(ENetworkKey key, Class<T> classType,
			IPacketReceiver<T> listener) {
		return new DefaultClientPacketListener<>(key, new GenericDeserializer<>(classType), listener);
	}

	@Override
	public void close() {
		timer.cancel();
		channel.close();
		clock.stopExecution();
	}

	void identifiedUserEvent() {
		channel.removeListener(NetworkConstants.ENetworkKey.IDENTIFY_USER);
	}

	@Override
	public void startGameSynchronization() {
		channel.removeListener(NetworkConstants.ENetworkKey.MATCH_STARTED);
		startTimeSynchronization();
		channel.initPinging();
	}

	private void startTimeSynchronization() {
		channel.registerListener(new TimeSynchronizationListener(channel, clock));
		TimeSyncSenderTimerTask timeSyncSender = new TimeSyncSenderTimerTask(channel, clock);
		timer.schedule(timeSyncSender, 0, NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL);
	}

	@Override
	public IGameClock getGameClock() {
		return clock;
	}

	@Override
	public UserId getUserId() {
		return userId;
	}

	@Override
	public int getRoundTripTimeInMs() {
		return channel.getRoundTripTime().getRtt();
	}

	@Override
	public void openNewMatch(String matchName, int maxPlayers, MapInfoPacket mapInfo) {
		channel.sendPacketAsync(NetworkConstants.ENetworkKey.REQUEST_OPEN_NEW_MATCH, new OpenNewMatchPacket(matchName, maxPlayers, mapInfo, 0L));
	}

	@Override
	public void joinMatch(String matchId) {
		channel.sendPacketAsync(NetworkConstants.ENetworkKey.REQUEST_JOIN_MATCH, new IdPacket(matchId));
	}

	@Override
	public void registerListener(IChannelListener listener) {
		channel.registerListener(listener);
	}

	@Override
	public void removeListener(ENetworkKey key) {
		channel.removeListener(key);
	}

	@Override
	public void updatePlayer(Player player) {
		channel.sendPacket(ENetworkKey.UPDATE_PLAYER, new PlayerPacket(player));
	}
}
