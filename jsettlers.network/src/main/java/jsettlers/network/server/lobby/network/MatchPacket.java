package jsettlers.network.server.lobby.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jsettlers.network.infrastructure.channel.packet.Packet;
import jsettlers.network.server.lobby.core.LevelId;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.core.MatchState;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.ELobbyResourceAmount;

public final class MatchPacket extends Packet {

	private Match match;

	public MatchPacket() {
		this(null);
	}

	public MatchPacket(Match match) {
		this.match = match;
	}

	@Override
	public void serialize(DataOutputStream dos) throws IOException {
		dos.writeUTF(match.getLevelId().getValue());
		dos.writeInt(match.getPlayers().size());
		for (Player player : match.getPlayers()) {
			new PlayerPacket(player).serialize(dos);
		}
		dos.writeUTF(match.getId().getValue());
		dos.writeUTF(match.getName());
		dos.writeInt(match.getResourceAmount().ordinal());
		dos.writeLong(match.getPeaceTime().toMinutes());
		dos.writeInt(match.getState().ordinal());
	}

	@Override
	public void deserialize(DataInputStream dis) throws IOException {
		final LevelId levelId = new LevelId(dis.readUTF());
		final int playersLength = dis.readInt();
		final List<Player> players = new ArrayList<>(playersLength);
		final PlayerPacket playerPacket = new PlayerPacket();
		for (int i = 0; i < playersLength; i++) {
			playerPacket.deserialize(dis);
			players.add(playerPacket.getPlayer());
		}
		this.match = new Match(
				new MatchId(dis.readUTF()),
				dis.readUTF(),
				levelId,
				players,
				ELobbyResourceAmount.VALUES[dis.readInt()],
				Duration.ofMinutes(dis.readLong()),
				MatchState.VALUES[dis.readInt()]);
	}

	public Match getMatch() {
		return match;
	}

	@Override
	public int hashCode() {
		return Objects.hash(match);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MatchPacket)) {
			return false;
		}
		MatchPacket other = (MatchPacket) obj;
		return Objects.equals(match, other.match);
	}

	@Override
	public String toString() {
		return String.format("MatchPacket [match=%s]", match);
	}
}
