package jsettlers.network.server.lobby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Timer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jsettlers.common.player.ECivilisation;
import jsettlers.network.TestUtils;
import jsettlers.network.infrastructure.channel.Channel;
import jsettlers.network.server.lobby.LobbyDb.LobbyDbException;
import jsettlers.network.server.lobby.core.EPlayerState;
import jsettlers.network.server.lobby.core.LevelId;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.core.MatchState;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.PlayerType;
import jsettlers.network.server.lobby.core.User;
import jsettlers.network.server.lobby.core.UserId;

public class LobbyTest {

	private static final LevelId LEVEL_ID = new LevelId("Level");
	private static final String MATCH_NAME = "match-name";

	private Channel client;
	private Channel server;
	private Lobby lobby;
	private User userA;
	private User userB;
	private User userC;
	private LobbyDb db;

	@Before
	public void before() throws IOException {
		final Channel[] channels = TestUtils.setUpLoopbackChannels();
		this.client = channels[0];
		this.server = channels[1];

		this.db = new LobbyDb();
		this.lobby = new Lobby(db);
		this.userA = new User(new UserId("testA"), "testuserA", server);
		this.userB = new User(new UserId("testB"), "testuserB", server);
		this.userC = new User(new UserId("testC"), "testuserC", server);
	}

	@After
	public void after() {
		this.client.close();
		this.server.close();
	}

	@Test
	public void test_join() {
		lobby.joinLobby(userA);
		assertTrue(db.getUsers().contains(userA));
	}

	@Test
	public void test_join_multiple_times() {
		lobby.joinLobby(userA);
		lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4);
		lobby.joinLobby(userA);

		// should left match when rejoin
		assertThrows(LobbyDbException.class, () -> db.getActiveMatch(userA.getId()));
	}

	@Test
	public void test_leave() {
		lobby.joinLobby(userA);
		lobby.leave(userA.getId());
		assertFalse(db.getUsers().contains(userA));
	}

	@Test
	public void test_create_match() {
		lobby.joinLobby(userA);
		lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4);
		assertFalse(db.getMatches().isEmpty());
	}

	@Test
	public void test_join_match() {
		lobby.joinLobby(userA);
		lobby.joinLobby(userB);
		final MatchId matchId = lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4);
		lobby.joinMatch(userA.getId(), matchId);
		lobby.joinMatch(userB.getId(), matchId);
		assertEquals(userA.getId().getPlayerId(), db.getMatches().iterator().next().getPlayers().get(0).getId());
		assertEquals(userB.getId().getPlayerId(), db.getMatches().iterator().next().getPlayers().get(1).getId());
	}

	@Test
	public void test_create_match_without_join_lobby() {
		assertThrows(LobbyDbException.class, () -> lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4));
	}

	@Test
	public void test_join_match_without_join_lobby() {
		lobby.joinLobby(userA);
		final MatchId matchId = lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4);
		assertThrows(LobbyDbException.class, () -> lobby.joinMatch(userB.getId(), matchId));
	}

	@Test
	public void test_join_match_multiple_times() {
		lobby.joinLobby(userA);
		final MatchId matchId = lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4);
		lobby.joinLobby(userB);
		lobby.joinMatch(userB.getId(), matchId);
		lobby.joinMatch(userB.getId(), matchId);
		assertEquals(userB.getId().getPlayerId(), db.getActiveMatch(userA.getId()).getPlayers().get(1).getId());
		assertNotEquals(userB.getId().getPlayerId(), db.getActiveMatch(userA.getId()).getPlayers().get(2).getId());
	}

	@Test
	public void test_join_another_match() {
		lobby.joinLobby(userA);
		lobby.joinLobby(userB);
		final MatchId matchIdA = lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4);
		final MatchId matchIdB = lobby.createMatch(userB.getId(), MATCH_NAME, LEVEL_ID, 4);

		lobby.joinLobby(userC);
		lobby.joinMatch(userC.getId(), matchIdA);
		lobby.joinMatch(userC.getId(), matchIdB);

		assertEquals(matchIdB, db.getActiveMatch(userC.getId()).getId());
		assertEquals(2, db.getMatches().size());
	}

	@Test
	public void test_active_match() {
		lobby.joinLobby(new User(userA.getId(), "testuser", server));
		assertFalse(db.hasActiveMatch(userA.getId()));
		lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4);
		assertTrue(db.hasActiveMatch(userA.getId()));
	}

	@Test
	public void test_startMatch() {
		lobby.joinLobby(userA);
		lobby.joinLobby(userB);
		final MatchId matchId = lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 2);
		lobby.joinMatch(userB.getId(), matchId);
		final Match match = db.getActiveMatch(userA.getId());
		match.getPlayers().get(0).setReady(true);
		match.getPlayers().get(1).setReady(true);
		lobby.startMatch(userA.getId(), new Timer());
		assertEquals(MatchState.RUNNING, match.getState());
	}

	@Test
	public void test_update_player() {
		lobby.joinLobby(userA);
		final Match match = db.getMatch(lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 2));
		final Player otherPlayer = match.getPlayers().get(1);
		lobby.update(userA.getId(), new Player(otherPlayer.getId(), "Other", EPlayerState.UNKNOWN, ECivilisation.EGYPTIAN, PlayerType.HUMAN, 0, 2, true));
		assertNotEquals("Username should not be updateable", "Other", otherPlayer.getName());
		assertEquals(ECivilisation.EGYPTIAN, otherPlayer.getCivilisation());
		assertEquals("Human players only can join a game. If a player is set to human then it should be set to empty player", PlayerType.EMPTY, otherPlayer.getType());
		assertEquals(1, otherPlayer.getPosition());
		assertEquals(2, otherPlayer.getTeam());
		assertEquals(false, otherPlayer.isReady());
	}

	private static <T extends Throwable> void assertThrows(Class<T> throwableClass, Runnable runnable) {
		try {
			runnable.run();
			assertTrue(false);
		} catch (Throwable exception) {
			assertEquals(throwableClass, exception.getClass());
		}
	}
}
