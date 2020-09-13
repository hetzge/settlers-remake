package jsettlers.network.server.lobby.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

import jsettlers.network.infrastructure.channel.packet.Packet;
import jsettlers.network.server.lobby.core.Match;

public class MatchArrayPacket extends Packet {

	private Match[] matches;

	public MatchArrayPacket() {
		this(new Match[0]);
	}

	public MatchArrayPacket(Match[] matches) {
		this.matches = matches;
	}

	@Override
	public void serialize(DataOutputStream dos) throws IOException {
		dos.writeInt(matches.length);
		for (Match match : matches) {
			new MatchPacket(match).serialize(dos);
		}
	}

	@Override
	public void deserialize(DataInputStream dis) throws IOException {
		int length = dis.readInt();
		this.matches = new Match[length];
		for (int i = 0; i < matches.length; i++) {
			final MatchPacket matchPacket = new MatchPacket();
			matchPacket.deserialize(dis);
			matches[i] = matchPacket.getMatch();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(matches);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MatchArrayPacket)) {
			return false;
		}
		MatchArrayPacket other = (MatchArrayPacket) obj;
		return Arrays.equals(matches, other.matches);
	}
}
