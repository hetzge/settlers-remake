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

	@Before
	public void before() throws IOException {
		final Channel[] channels = TestUtils.setUpLoopbackChannels();
		this.client = channels[0];
		this.server = channels[1];

		this.lobby = new Lobby();
		this.userA = new User(new UserId("testA"), "testuserA", server);
		this.userB = new User(new UserId("testB"), "testuserB", server);
	}

	@After
	public void after() {
		this.client.close();
		this.server.close();
	}

	@Test
	public void test_join() {
		lobby.joinLobby(userA);
		assertTrue(lobby.getUsers().contains(userA));
	}

	@Test
	public void test_join_multiple_times() {
		lobby.joinLobby(userA);
		lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4);
		lobby.joinLobby(userA);
		assertFalse("should left match when rejoin", lobby.getActiveMatch(userA.getId()).isPresent());
	}

	@Test
	public void test_leave() {
		lobby.joinLobby(userA);
		lobby.leave(userA.getId());
		assertFalse(lobby.getUsers().contains(userA));
	}

	@Test
	public void test_create_match() {
		lobby.joinLobby(userA);
		lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4);
		assertFalse(lobby.getMatches().isEmpty());
	}

	@Test
	public void test_join_match() {
		final MatchId matchId = lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4);
		lobby.joinLobby(userA);
		lobby.joinMatch(userA.getId(), matchId);
		lobby.joinLobby(userB);
		lobby.joinMatch(userB.getId(), matchId);
		assertEquals(userA.getId().getPlayerId(), lobby.getMatches().iterator().next().getPlayers()[0].getId());
		assertEquals(userB.getId().getPlayerId(), lobby.getMatches().iterator().next().getPlayers()[1].getId());
	}

	@Test
	public void test_join_match_without_join_lobby() {
		final MatchId matchId = lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4);
		lobby.joinMatch(userB.getId(), matchId);
		assertNotEquals(userB.getId().getPlayerId(), lobby.getMatches().iterator().next().getPlayers()[0].getId());
	}

	@Test
	public void test_join_match_multiple_times() {
		final MatchId matchId = lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4);
		lobby.joinLobby(userB);
		lobby.joinMatch(userB.getId(), matchId);
		lobby.joinMatch(userB.getId(), matchId);
		assertEquals(userB.getId().getPlayerId(), lobby.getMatches().iterator().next().getPlayers()[0].getId());
		assertNotEquals(userB.getId().getPlayerId(), lobby.getMatches().iterator().next().getPlayers()[1].getId());
	}

	@Test
	public void test_join_another_match() {
		final MatchId matchIdA = lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4);
		final MatchId matchIdB = lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4);

		lobby.joinLobby(userB);
		lobby.joinMatch(userB.getId(), matchIdA);
		lobby.joinMatch(userB.getId(), matchIdB);

		assertEquals(matchIdB, lobby.getActiveMatch(userB.getId()).get().getId());
		assertEquals(1, lobby.getMatches().size());
		assertEquals(matchIdB, lobby.getMatches().iterator().next().getId());
	}

	@Test
	public void test_active_match() {
		lobby.joinLobby(new User(userA.getId(), "testuser", server));
		assertFalse(lobby.getActiveMatch(userA.getId()).isPresent());
		lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4);
		assertTrue(lobby.getActiveMatch(userA.getId()).isPresent());
	}

	@Test
	public void test_startMatch() {
		lobby.joinLobby(userA);
		lobby.joinLobby(userB);
		final MatchId matchId = lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 2);
		lobby.joinMatch(userB.getId(), matchId);
		final Match match = lobby.getActiveMatch(userA.getId()).get();
		match.getPlayers()[0].setReady(true);
		match.getPlayers()[1].setReady(true);
		lobby.startMatch(userA.getId(), new Timer());
		assertEquals(MatchState.RUNNING, match.getState());
	}

	@Test
	public void test_update_player() {
		lobby.joinLobby(userA);
		lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 1);
		lobby.update(userA.getId(), new Player(userA.getId().getPlayerId(), "Other", EPlayerState.UNKNOWN, ECivilisation.EGYPTIAN, PlayerType.HUMAN, 0, 2, true));
		final Player player = lobby.getActiveMatch(userA.getId()).get().getPlayer(userA.getId().getPlayerId()).get();
		assertEquals(userA.getId().getPlayerId(), player.getId());
		assertEquals("Other", player.getName());
		assertEquals(ECivilisation.EGYPTIAN, player.getCivilisation());
		assertEquals(PlayerType.HUMAN, player.getType());
		assertEquals(0, player.getPosition());
		assertEquals(2, player.getTeam());
		assertEquals(true, player.isReady());
	}
}
