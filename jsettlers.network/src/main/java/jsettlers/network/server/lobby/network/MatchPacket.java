package jsettlers.network.server.lobby.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Objects;

import jsettlers.network.infrastructure.channel.packet.Packet;
import jsettlers.network.server.lobby.core.Civilisation;
import jsettlers.network.server.lobby.core.LevelId;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.core.MatchState;
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
		dos.writeUTF(match.getLevelId().getValue());
		dos.writeInt(match.getPlayers().length);
		for (int i = 0; i < match.getPlayers().length; i++) {
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
		dos.writeUTF(match.getName());
		dos.writeInt(match.getResourceAmount().ordinal());
		dos.writeLong(match.getPeaceTime().toMinutes());
		dos.writeInt(match.getState().ordinal());
	}

	@Override
	public void deserialize(DataInputStream dis) throws IOException {
		final LevelId levelId = new LevelId(dis.readUTF());
		final int playersLength = dis.readInt();
		final Player[] players = new Player[playersLength];
		for (int i = 0; i < playersLength; i++) {
			players[i] = new Player(new PlayerId(dis.readUTF()), dis.readUTF(), Civilisation.VALUES[dis.readInt()], PlayerType.VALUES[dis.readInt()], dis.readInt(), dis.readInt(),
					dis.readBoolean());
		}
		match = new Match(new MatchId(dis.readUTF()), dis.readUTF(), levelId, players, ResourceAmount.VALUES[dis.readInt()], Duration.ofMinutes(dis.readLong()), MatchState.VALUES[dis.readInt()]);
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
