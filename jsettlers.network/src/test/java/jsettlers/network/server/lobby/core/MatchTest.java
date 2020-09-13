package jsettlers.network.server.lobby.core;

import static org.junit.Assert.*;

import java.time.Duration;

import org.junit.Test;

public class MatchTest {

	@Test
	public void test_swap_position_a() {
		// Given
		final Player[] players = new Player[2];
		players[0] = new Player(PlayerId.generate(), "Player A", Civilisation.ROMAN, PlayerType.HUMAN, 0, 0, false);
		players[1] = new Player(PlayerId.generate(), "Player B", Civilisation.ROMAN, PlayerType.HUMAN, 1, 0, false);
		final Match match = new Match(MatchId.generate(), "test-match", new LevelId("test-level"), players, ResourceAmount.HIGH, Duration.ZERO, MatchState.OPENED);

		// When
		final Match newMatch = match.update(match.withPlayer(players[0].withPosition(1)));

		// Then
		assertEquals(players[1].getId(), newMatch.getPlayers()[0].getId());
		assertEquals(players[0].getId(), newMatch.getPlayers()[1].getId());
	}

	@Test
	public void test_swap_position_b() {
		// Given
		final Player[] players = new Player[3];
		players[0] = new Player(PlayerId.generate(), "Player A", Civilisation.ROMAN, PlayerType.HUMAN, 0, 0, false);
		players[1] = new Player(PlayerId.generate(), "Player B", Civilisation.ROMAN, PlayerType.HUMAN, 1, 0, false);
		players[2] = new Player(PlayerId.generate(), "Player C", Civilisation.ROMAN, PlayerType.HUMAN, 2, 0, false);
		final Match match = new Match(MatchId.generate(), "test-match", new LevelId("test-level"), players, ResourceAmount.HIGH, Duration.ZERO, MatchState.OPENED);

		// When
		final Match newMatchA = match.update(match.withPlayer(players[2].withPosition(0)));
		final Match newMatchB = newMatchA.update(newMatchA.withPlayer(newMatchA.getPlayers()[0].withPosition(1)));

		// Then
		assertEquals(players[0].getId(), newMatchB.getPlayers()[2].getId());
		assertEquals(players[1].getId(), newMatchB.getPlayers()[0].getId());
		assertEquals(players[2].getId(), newMatchB.getPlayers()[1].getId());
	}

	@Test
	public void test_update_swap() {
		// Given
		final PlayerId idA = PlayerId.generate();
		final PlayerId idB = PlayerId.generate();
		final PlayerId idC = PlayerId.generate();
		final Player[] playersA = new Player[] {
				new Player(idA, "Player A", Civilisation.ROMAN, PlayerType.HUMAN, 0, 0, false),
				new Player(idB, "Player B", Civilisation.ROMAN, PlayerType.HUMAN, 1, 0, false),
				new Player(idC, "Player C", Civilisation.ROMAN, PlayerType.HUMAN, 2, 0, false)
		};
		final Match matchA = new Match(MatchId.generate(), "test-match", new LevelId("test-level"), playersA, ResourceAmount.HIGH, Duration.ZERO, MatchState.OPENED);
		final Player[] playersB = new Player[] {
				new Player(idA, "Player A", Civilisation.ROMAN, PlayerType.HUMAN, 0, 0, false),
				new Player(idC, "Player C", Civilisation.ROMAN, PlayerType.HUMAN, 1, 0, false),
				new Player(idB, "Player B", Civilisation.ROMAN, PlayerType.HUMAN, 2, 0, false)
		};
		final Match matchB = new Match(MatchId.generate(), "test-match", new LevelId("test-level"), playersB, ResourceAmount.HIGH, Duration.ZERO, MatchState.OPENED);

		// When
		final Match matchC = matchA.update(matchB);

		// Then
		assertEquals(idA, matchC.getPlayers()[0].getId());
		assertEquals(idB, matchC.getPlayers()[2].getId());
		assertEquals(idC, matchC.getPlayers()[1].getId());
	}

}
