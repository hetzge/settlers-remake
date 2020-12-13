package jsettlers.network.server.lobby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jsettlers.network.NetworkConstants;
import jsettlers.network.NetworkConstants.ENetworkKey;
import jsettlers.network.TestUtils;
import jsettlers.network.client.NetworkClient;
import jsettlers.network.client.NetworkClientClockMock;
import jsettlers.network.client.task.TestTaskPacket;
import jsettlers.network.client.task.packets.SyncTasksPacket;
import jsettlers.network.client.task.packets.TaskPacket;
import jsettlers.network.client.time.TimeSyncSenderTimerTask;
import jsettlers.network.client.time.TimeSynchronizationListener;
import jsettlers.network.common.packets.MapInfoPacket;
import jsettlers.network.infrastructure.channel.AsyncChannel;
import jsettlers.network.infrastructure.channel.TestPacket;
import jsettlers.network.infrastructure.channel.TestPacketListener;
import jsettlers.network.infrastructure.channel.listeners.SimpleListener;
import jsettlers.network.infrastructure.channel.ping.PingPacketListener;
import jsettlers.network.infrastructure.log.ConsoleLogger;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerState;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.LevelId;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.core.MatchState;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.User;
import jsettlers.network.server.lobby.core.UserId;

public class LobbyTest {

	private static final LevelId LEVEL_ID = new LevelId("Level");
	private static final String MATCH_NAME = "match-name";

	private AsyncChannel client;
	private AsyncChannel server;
	private Lobby lobby;
	private User userA;
	private User userB;
	private User userC;
	private LobbyDb db;

	@Before
	public void before() throws IOException {
		final AsyncChannel[] channels = TestUtils.setUpAsyncLoopbackChannels();
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
	public void testConnection() throws InterruptedException {
		TestPacketListener listener = new TestPacketListener(NetworkConstants.ENetworkKey.TEST_PACKET);
		client.registerListener(listener);

		TestPacket testPacket = new TestPacket("sdlfjsh", 2324);
		server.sendPacket(NetworkConstants.ENetworkKey.TEST_PACKET, testPacket);

		Thread.sleep(10L);

		assertEquals(1, listener.packets.size());
		assertEquals(testPacket, listener.packets.get(0));
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
		assertThrows(LobbyException.class, () -> db.getActiveMatch(userA.getId()));
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
		assertEquals(userA.getId(), db.getMatches().iterator().next().getPlayers().get(0).getUserId().get());
		assertEquals(userB.getId(), db.getMatches().iterator().next().getPlayers().get(1).getUserId().get());
	}

	@Test
	public void test_create_match_without_join_lobby() {
		assertThrows(LobbyException.class, () -> lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4));
	}

	@Test
	public void test_join_match_without_join_lobby() {
		lobby.joinLobby(userA);
		final MatchId matchId = lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4);
		assertThrows(LobbyException.class, () -> lobby.joinMatch(userB.getId(), matchId));
	}

	@Test
	public void test_join_match_multiple_times() {
		lobby.joinLobby(userA);
		final MatchId matchId = lobby.createMatch(userA.getId(), MATCH_NAME, LEVEL_ID, 4);
		lobby.joinLobby(userB);
		lobby.joinMatch(userB.getId(), matchId);
		lobby.joinMatch(userB.getId(), matchId);
		assertEquals(userB.getId(), db.getActiveMatch(userA.getId()).getPlayers().get(1).getUserId().get());
		assertFalse(db.getActiveMatch(userA.getId()).getPlayers().get(2).getUserId().isPresent());
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
		lobby.updatePlayerType(userA.getId(), otherPlayer.getIndex(), ELobbyPlayerType.HUMAN);
		lobby.updatePlayerCivilisation(userA.getId(), otherPlayer.getIndex(), ELobbyCivilisation.EGYPTIAN);
		lobby.updatePlayerTeam(userA.getId(), otherPlayer.getIndex(), 2);
		lobby.updatePlayerReady(userA.getId(), otherPlayer.getIndex(), true);
		assertNotEquals("Username should not be updateable", "Other", otherPlayer.getName());
		assertEquals(ELobbyCivilisation.EGYPTIAN, otherPlayer.getCivilisation());
		assertEquals("Human players only can join a game. If a player is set to human then it should be set to empty player", ELobbyPlayerType.EMPTY, otherPlayer.getType());
		assertEquals(1, otherPlayer.getIndex());
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
