/*******************************************************************************
 * Copyright (c) 2015
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
package jsettlers.network.client.interfaces;

import java.io.Closeable;
import java.util.List;
import java.util.function.Consumer;

import jsettlers.network.NetworkConstants.ENetworkKey;
import jsettlers.network.client.receiver.IPacketReceiver;
import jsettlers.network.common.packets.MapInfoPacket;
import jsettlers.network.infrastructure.channel.IChannelListener;
import jsettlers.network.infrastructure.channel.reject.RejectPacket;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.core.UserId;

/**
 * This interface defines the methods offered by the client of the network library.
 * 
 * @author Andreas Eberle
 * 
 */
public interface INetworkClient extends Closeable {

	UserId getUserId();

	// LOBBY

	void logIn(String username);

	void sendChatMessage(String message);

	// MATCH

	void queryMatches(Consumer<List<Match>> callback);

	void openNewMatch(String matchName, int maxPlayers, MapInfoPacket mapInfo);

	void joinMatch(MatchId matchId);

	void leaveMatch();

	void startMatch() throws IllegalStateException;

	void updateMatch(Match match);

	void updatePlayerType(int playerType, ELobbyPlayerType playerType2);

	void updatePlayerCivilisation(int playerType, ELobbyCivilisation civilisation);

	void updatePlayerTeam(int playerType, int team);

	void updatePlayerReady(int playerType, boolean ready);

	// INGAME

	void setStartFinished(boolean startFinished);

	void startTimeSynchronization();

	IGameClock getGameClock();

	// CALLBACKS

	void registerListener(IChannelListener listener);

	void removeListener(ENetworkKey key);

	void registerRejectReceiver(IPacketReceiver<RejectPacket> rejectListener);
}
