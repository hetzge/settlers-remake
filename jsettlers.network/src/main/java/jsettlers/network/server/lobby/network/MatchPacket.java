package jsettlers.network.server.lobby.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

import jsettlers.network.infrastructure.channel.packet.Packet;
import jsettlers.network.server.lobby.core.Civilisation;
import jsettlers.network.server.lobby.core.Level;
import jsettlers.network.server.lobby.core.LevelId;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.PlayerId;
import jsettlers.network.server.lobby.core.PlayerType;
import jsettlers.network.server.lobby.core.ResourceAmount;

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
		dos.writeUTF(match.getLevel().getId().getValue());
		dos.writeUTF(match.getLevel().getName());
		dos.writeInt(match.getLevel().getNumberOfPlayers());
		for (int i = 0; i < match.getLevel().getNumberOfPlayers(); i++) {
			final Player player = match.getPlayers()[i];
			dos.writeUTF(player.getId().getValue());
			dos.writeUTF(player.getName());
			dos.writeInt(player.getCivilisation().ordinal());
			dos.writeInt(player.getType().ordinal());
			dos.writeInt(player.getPosition());
			dos.writeInt(player.getTeam());
			dos.writeBoolean(player.isReady());
		}
		dos.writeUTF(match.getId().getValue());
		dos.writeInt(match.getResourceAmount().ordinal());
		dos.writeLong(match.getPeaceTime().toMinutes());
	}

	@Override
	public void deserialize(DataInputStream dis) throws IOException {
		Level level = new Level(new LevelId(dis.readUTF()), dis.readUTF(), dis.readInt());
		Player[] players = new Player[level.getNumberOfPlayers()];
		for (int i = 0; i < level.getNumberOfPlayers(); i++) {
			players[i] = new Player(new PlayerId(dis.readUTF()), dis.readUTF(), Civilisation.values()[dis.readInt()], PlayerType.values()[dis.readInt()], dis.readInt(), dis.readInt(),
					dis.readBoolean());
		}
		match = new Match(new MatchId(dis.readUTF()), level, players, ResourceAmount.values()[dis.readInt()], Duration.ofMinutes(dis.readLong()));
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
}
