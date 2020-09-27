package jsettlers.network.server.lobby.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

import jsettlers.common.player.ECivilisation;
import jsettlers.network.infrastructure.channel.packet.Packet;
import jsettlers.network.server.lobby.core.EPlayerState;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.PlayerId;
import jsettlers.network.server.lobby.core.PlayerType;

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
		dos.writeUTF(player.getId().getValue());
		dos.writeUTF(player.getName());
		dos.writeInt(player.getState().ordinal());
		dos.writeInt(player.getCivilisation().ordinal());
		dos.writeInt(player.getType().ordinal());
		dos.writeInt(player.getPosition());
		dos.writeInt(player.getTeam());
		dos.writeBoolean(player.isReady());
	}

	@Override
	public void deserialize(DataInputStream dis) throws IOException {
		this.player = new Player(
				new PlayerId(dis.readUTF()),
				dis.readUTF(),
				EPlayerState.VALUES[dis.readInt()],
				ECivilisation.VALUES[dis.readInt()],
				PlayerType.VALUES[dis.readInt()],
				dis.readInt(),
				dis.readInt(),
				dis.readBoolean());
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
