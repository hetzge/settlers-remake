package jsettlers.network.server.lobby.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

import jsettlers.network.infrastructure.channel.packet.Packet;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerState;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.UserId;

public class PlayerPacket extends Packet {

	private Player player;

	public PlayerPacket() {
		this(null);
	}

	public PlayerPacket(Player player) {
		this.player = player;
	}

	@Override
	public void serialize(DataOutputStream dos) throws IOException {
		dos.writeInt(player.getIndex());
		dos.writeUTF(player.getName());
		dos.writeBoolean(player.getUserId().isPresent());
		dos.writeUTF(player.getUserId().map(UserId::getValue).orElse(""));
		dos.writeInt(player.getState().ordinal());
		dos.writeInt(player.getCivilisation().ordinal());
		dos.writeInt(player.getType().ordinal());
		dos.writeInt(player.getTeam());
	}

	@Override
	public void deserialize(DataInputStream dis) throws IOException {
		this.player = new Player(
				dis.readInt(),
				dis.readUTF(),
				deserializeUserId(dis),
				ELobbyPlayerState.VALUES[dis.readInt()],
				ELobbyCivilisation.VALUES[dis.readInt()],
				ELobbyPlayerType.VALUES[dis.readInt()],
				dis.readInt());
	}

	private UserId deserializeUserId(DataInputStream dis) throws IOException {
		final boolean hasUserId = dis.readBoolean();
		final String userIdValue = dis.readUTF();
		return hasUserId ? new UserId(userIdValue) : null;
	}

	public Player getPlayer() {
		return player;
	}

	@Override
	public int hashCode() {
		return Objects.hash(player);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof PlayerPacket)) {
			return false;
		}
		PlayerPacket other = (PlayerPacket) obj;
		return Objects.equals(player, other.player);
	}
}
