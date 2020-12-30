package jsettlers.network.server.lobby;

import java.util.Timer;

import jsettlers.network.NetworkConstants.ENetworkKey;
import jsettlers.network.common.packets.BooleanMessagePacket;
import jsettlers.network.common.packets.ChatMessagePacket;
import jsettlers.network.common.packets.IdPacket;
import jsettlers.network.common.packets.IntegerMessagePacket;
import jsettlers.network.common.packets.OpenNewMatchPacket;
import jsettlers.network.common.packets.PlayerInfoPacket;
import jsettlers.network.common.packets.TimeSyncPacket;
import jsettlers.network.infrastructure.channel.Channel;
import jsettlers.network.infrastructure.channel.listeners.SimpleListener;
import jsettlers.network.infrastructure.channel.packet.EmptyPacket;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.LevelId;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.core.ResourceAmount;
import jsettlers.network.server.lobby.core.User;
import jsettlers.network.server.lobby.core.UserId;
import jsettlers.network.server.lobby.network.MatchPacket;
import jsettlers.network.server.lobby.network.UpdatePlayerPacket;

public final class LobbyServerController {

	private final Lobby lobby;
	private final Timer sendMatchesTimer;
	private final Timer matchesTaskTimer;

	public LobbyServerController(Lobby lobby) {
		this.lobby = lobby;
		this.sendMatchesTimer = new Timer("SendMatchesListTimer", true);
		this.matchesTaskTimer = new Timer("MatchesTaskDistributionTimer", true);
	}

	public synchronized void stop() {
		sendMatchesTimer.cancel();
		matchesTaskTimer.cancel();
	}

	public void setup(Channel channel) {
		channel.registerListener(new SimpleListener<>(ENetworkKey.IDENTIFY_USER, PlayerInfoPacket.class, packet -> {
			final User user = new User(new UserId(packet.getId()), packet.getName(), channel);
			lobby.joinLobby(user);
			setup(user);
			// send packet back to client as confirmation
			channel.sendPacket(ENetworkKey.IDENTIFY_USER, packet);
		}));
	}

	void setup(User user) {
		final Channel channel = user.getChannel();
		channel.setChannelClosedListener(() -> {
			lobby.leave(user.getId());
		});
		channel.registerListener(new SimpleListener<>(ENetworkKey.QUERY_MATCHES, BooleanMessagePacket.class, packet -> {
			lobby.sendMatches(user.getId());
		}));
		channel.registerListener(new SimpleListener<>(ENetworkKey.REQUEST_OPEN_NEW_MATCH, OpenNewMatchPacket.class, packet -> {
			lobby.createMatch(user.getId(), packet.getMatchName(), new LevelId(packet.getMapInfo().getId()), packet.getMaxPlayers());
		}));
		channel.registerListener(new SimpleListener<>(ENetworkKey.REQUEST_JOIN_MATCH, IdPacket.class, packet -> {
			lobby.joinMatch(user.getId(), new MatchId(packet.getId()));
		}));
		channel.registerListener(new SimpleListener<>(ENetworkKey.REQUEST_LEAVE_MATCH, EmptyPacket.class, packet -> {
			lobby.leaveMatch(user.getId());
		}));
		channel.registerListener(new SimpleListener<>(ENetworkKey.REQUEST_START_MATCH, EmptyPacket.class, packet -> {
			lobby.startMatch(user.getId(), matchesTaskTimer);
		}));
		channel.registerListener(new SimpleListener<>(ENetworkKey.CHAT_MESSAGE, ChatMessagePacket.class, packet -> {
			lobby.sendMatchChatMessage(user.getId(), packet.getMessage());
		}));
		channel.registerListener(new SimpleListener<>(ENetworkKey.UPDATE_MATCH, MatchPacket.class, packet -> {
			lobby.update(user.getId(), packet.getMatch());
		}));
		channel.registerListener(new SimpleListener<>(ENetworkKey.UPDATE_MATCH_PEACE_TIME, IntegerMessagePacket.class, packet -> {
			lobby.updateMatchPeaceTime(user.getId(), packet.getValue());
		}));
		channel.registerListener(new SimpleListener<>(ENetworkKey.UPDATE_MATCH_START_RESOURCE_AMOUNT, IntegerMessagePacket.class, packet -> {
			lobby.updateMatchStartResourceAmount(user.getId(), ResourceAmount.VALUES[packet.getValue()]);
		}));
		channel.registerListener(new SimpleListener<>(ENetworkKey.UPDATE_PLAYER_TYPE, UpdatePlayerPacket.class, packet -> {
			lobby.updatePlayerType(user.getId(), packet.getPlayerIndex(), ELobbyPlayerType.VALUES[packet.getIntegerValue()]);
		}));
		channel.registerListener(new SimpleListener<>(ENetworkKey.UPDATE_PLAYER_CIVILISATION, UpdatePlayerPacket.class, packet -> {
			lobby.updatePlayerCivilisation(user.getId(), packet.getPlayerIndex(), ELobbyCivilisation.VALUES[packet.getIntegerValue()]);
		}));
		channel.registerListener(new SimpleListener<>(ENetworkKey.UPDATE_PLAYER_TEAM, UpdatePlayerPacket.class, packet -> {
			lobby.updatePlayerTeam(user.getId(), packet.getPlayerIndex(), packet.getIntegerValue());
		}));
		channel.registerListener(new SimpleListener<>(ENetworkKey.UPDATE_PLAYER_READY, UpdatePlayerPacket.class, packet -> {
			lobby.updatePlayerReady(user.getId(), packet.getPlayerIndex(), packet.getBooleanValue());
		}));
		channel.registerListener(new SimpleListener<>(ENetworkKey.TIME_SYNC, TimeSyncPacket.class, packet -> {
			lobby.sendMatchTimeSync(user.getId(), packet);
		}));
		channel.registerListener(new SimpleListener<>(ENetworkKey.CHANGE_START_FINISHED, BooleanMessagePacket.class, packet -> {
			lobby.setStartFinished(user.getId(), packet.getValue());
		}));
	}
}
