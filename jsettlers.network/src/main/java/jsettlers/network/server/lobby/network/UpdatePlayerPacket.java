package jsettlers.network.server.lobby.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

import jsettlers.network.infrastructure.channel.packet.Packet;

public class UpdatePlayerPacket extends Packet {

	private int playerIndex;
	private int value;

	public UpdatePlayerPacket() {
		this(0, 0);
	}

	public UpdatePlayerPacket(int playerIndex, boolean value) {
		this(playerIndex, value ? 1 : 0);
	}

	public UpdatePlayerPacket(int playerIndex, int value) {
		this.playerIndex = playerIndex;
		this.value = value;
	}

	@Override
	public void serialize(DataOutputStream dos) throws IOException {
		dos.writeInt(playerIndex);
		dos.writeInt(value);
	}

	@Override
	public void deserialize(DataInputStream dis) throws IOException {
		this.playerIndex = dis.readInt();
		this.value = dis.readInt();
	}

	public int getPlayerIndex() {
		return playerIndex;
	}

	public int getIntegerValue() {
		return value;
	}

	public boolean getBooleanValue() {
		return value > 0;
	}

	@Override
	public int hashCode() {
		return Objects.hash(playerIndex, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof UpdatePlayerPacket)) {
			return false;
		}
		final UpdatePlayerPacket other = (UpdatePlayerPacket) obj;
		return playerIndex == other.playerIndex && value == other.value;
	}

	@Override
	public String toString() {
		return String.format("UpdatePlayerPacket [playerIndex=%s, value=%s]", playerIndex, value);
	}
}
