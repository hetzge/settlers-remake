package jsettlers.network.server.lobby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jsettlers.common.player.ECivilisation;
import jsettlers.network.NetworkConstants;
import jsettlers.network.TestUtils;
import jsettlers.network.NetworkConstants.ENetworkKey;
import jsettlers.network.client.NetworkClient;
import jsettlers.network.client.NetworkClientClockMock;
import jsettlers.network.client.task.TestTaskPacket;
import jsettlers.network.client.task.packets.SyncTasksPacket;
import jsettlers.network.client.task.packets.TaskPacket;
import jsettlers.network.client.time.TimeSyncSenderTimerTask;
import jsettlers.network.client.time.TimeSynchronizationListener;
import jsettlers.network.common.packets.MapInfoPacket;
import jsettlers.network.infrastructure.channel.AsyncChannel;
import jsettlers.network.infrastructure.channel.Channel;
import jsettlers.network.infrastructure.channel.TestPacket;
import jsettlers.network.infrastructure.channel.TestPacketListener;
import jsettlers.network.infrastructure.channel.listeners.SimpleListener;
import jsettlers.network.server.lobby.core.ELobbyPlayerState;
import jsettlers.network.server.lobby.core.LevelId;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.core.MatchState;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.User;
import jsettlers.network.server.lobby.core.UserId;
import jsettlers.network.synchronic.timer.NetworkTimer;

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
	public void testTimeSynchronization() throws IllegalStateException, InterruptedException, IOException {
		NetworkClientClockMock clock1 = new NetworkClientClockMock(200);
		NetworkClientClockMock clock2 = new NetworkClientClockMock(210);

		client.registerListener(new TimeSynchronizationListener(client, clock1));
		new Timer().schedule(new TimeSyncSenderTimerTask(client, clock1), 0, NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL);
		client.initPinging();

		server.registerListener(new TimeSynchronizationListener(server, clock2));
		new Timer().schedule(new TimeSyncSenderTimerTask(server, clock2), 0, NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL);
		server.initPinging();

		Thread.sleep(NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL + 20L); // wait for 1 synchronizations
		assertEquals(0, clock1.popAdjustmentEvents().size()); // no adjustments should have happened, because the clocks are almost sync
		assertEquals(0, clock2.popAdjustmentEvents().size());

		clock1.setTime(2056); // put clock1 forward

		Thread.sleep(3L * NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL + 20L); // wait for 3 synchronizations
		int diff = Math.abs(clock1.getTime() - clock2.getTime());
		assertTrue("diff is to high: " + diff, diff < NetworkConstants.Client.TIME_SYNC_TOLERATED_DIFFERENCE);
		assertTrue(clock1.popAdjustmentEvents().size() > 0);
		assertEquals(0, clock2.popAdjustmentEvents().size());

		clock2.setTime(423423); // put clock2 forward

		Thread.sleep(6L * NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL + 20L); // wait for 6 synchronizations
		diff = Math.abs(clock2.getTime() - clock1.getTime());
		assertTrue("diff is to high: " + diff, diff < NetworkConstants.Client.TIME_SYNC_TOLERATED_DIFFERENCE);
		assertTrue(clock2.popAdjustmentEvents().size() > 0);
		assertEquals(0, clock1.popAdjustmentEvents().size());
	}

	@Test
	public void test() throws IOException, InterruptedException {

		final AsyncChannel[] channelsA = TestUtils.setUpAsyncLoopbackChannels();
		final AsyncChannel[] channelsB = TestUtils.setUpAsyncLoopbackChannels();

		final LobbyServerController serverController = new LobbyServerController(lobby);
		serverController.setup(channelsA[1]);
		serverController.setup(channelsB[1]);

		Thread.sleep(1000);

		NetworkClientClockMock clockA = new NetworkClientClockMock();
		final NetworkClient clientA = new NetworkClient(channelsA[0], userA.getId());
		clientA.registerListener(new SimpleListener<>(ENetworkKey.SYNCHRONOUS_TASK, SyncTasksPacket.class, packet -> {
			clockA.scheduleSyncTasksPacket(packet);
		}));
		clientA.logIn("AAA");
		clientA.openNewMatch("MATCH", 2, new MapInfoPacket("MAP", "TESTMAP", "AUTHOR", "BOB", 2));
		Thread.sleep(100);
		clientA.updatePlayer(new Player(0, "", userA.getId(), ELobbyPlayerState.UNKNOWN, ECivilisation.ROMAN, ELobbyPlayerType.HUMAN, 1, true));

		NetworkClientClockMock clockB = new NetworkClientClockMock();
		final NetworkClient clientB = new NetworkClient(channelsB[0], userB.getId());
		clientB.registerListener(new SimpleListener<>(ENetworkKey.SYNCHRONOUS_TASK, SyncTasksPacket.class, packet -> {
			clockB.scheduleSyncTasksPacket(packet);
		}));
		clientB.logIn("BBB");
		clientB.joinMatch(db.getActiveMatch(userA.getId()).getId());
		Thread.sleep(100);
		clientB.updatePlayer(new Player(1, "", userB.getId(), ELobbyPlayerState.UNKNOWN, ECivilisation.ROMAN, ELobbyPlayerType.HUMAN, 2, true));

		clientA.startMatch();

		Thread.sleep(1000);

		clientA.registerListener(new TimeSynchronizationListener(channelsA[0], clockA));
		new Timer().schedule(new TimeSyncSenderTimerTask(channelsA[0], clockA), 0, NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL);

		clientB.registerListener(new TimeSynchronizationListener(channelsB[0], clockB));
		new Timer().schedule(new TimeSyncSenderTimerTask(channelsB[0], clockB), 0, NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL);

		Thread.sleep(2 * NetworkConstants.Client.LOCKSTEP_PERIOD); // After two lockstep periods, there must be two locksteps.
		assertEquals(NetworkConstants.Client.LOCKSTEP_DEFAULT_LEAD_STEPS, clockA.getAllowedLockstep());
		assertEquals(NetworkConstants.Client.LOCKSTEP_DEFAULT_LEAD_STEPS, clockB.getAllowedLockstep());

		// After more than LOCKSTEP_DEFAULT_LEAD_STEPS periods, the lockstep counter must wait, to prevent it from running away.
		Thread.sleep((2 + NetworkConstants.Client.LOCKSTEP_DEFAULT_LEAD_STEPS) * NetworkConstants.Client.LOCKSTEP_PERIOD);
		assertEquals(NetworkConstants.Client.LOCKSTEP_DEFAULT_LEAD_STEPS, clockA.getAllowedLockstep());
		assertEquals(NetworkConstants.Client.LOCKSTEP_DEFAULT_LEAD_STEPS, clockB.getAllowedLockstep());

		// Submit a task
		TestTaskPacket testTask = new TestTaskPacket("dsfsdf", 2342, (byte) -23);
		clientB.scheduleTask(testTask);

		Thread.sleep(2L * NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL);

		// The task must not have been submitted to the clients yet, because the lockstep is blocked.
		assertEquals(0, clockA.popBufferedTasks().size());
		assertEquals(0, clockB.popBufferedTasks().size());

		// Now let one clock continue one lockstep period.
		clockA.setTime(NetworkConstants.Client.LOCKSTEP_PERIOD + NetworkConstants.Client.TIME_SYNC_TOLERATED_DIFFERENCE + 10);

		Thread.sleep(NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL + 40L);

		List<TaskPacket> packets1 = clockA.popBufferedTasks();
		assertEquals(1, packets1.size());
		assertEquals(testTask, packets1.get(0));
		List<TaskPacket> packets2 = clockB.popBufferedTasks();
		assertEquals(1, packets2.size());
		assertEquals(testTask, packets2.get(0));

		Thread.sleep(2 * NetworkConstants.Client.LOCKSTEP_PERIOD); // Wait two more lockstep periods and check the run away protection again
		assertEquals(NetworkConstants.Client.LOCKSTEP_DEFAULT_LEAD_STEPS + 1, clockA.getAllowedLockstep());
		assertEquals(NetworkConstants.Client.LOCKSTEP_DEFAULT_LEAD_STEPS + 1, clockB.getAllowedLockstep());

	}

	@Test
	public void testSyncTasksDistribution() throws IllegalStateException, InterruptedException {

		client1.openNewMatch("TestMatch", 4, new MapInfoPacket("", "", "", "", 4), 34L, null, null, null);

		Thread.sleep(150L);
		assertEquals(ELobbyPlayerState.IN_MATCH, client1.getState());

		MatchInfoPacket matchInfo = client1.getMatchInfo();

		client2.joinMatch(matchInfo.getId(), null, null, null);

		Thread.sleep(50L);
		assertEquals(ELobbyPlayerState.IN_MATCH, client2.getState());

		client1.setReadyState(true);
		client2.setReadyState(true);
		Thread.sleep(30L);
		client2.startMatch();

		Thread.sleep(30 + NetworkConstants.Client.LOCKSTEP_PERIOD); // Ensure that both clients are in a running match.
		assertEquals(ELobbyPlayerState.IN_RUNNING_MATCH, client1.getState());
		assertEquals(ELobbyPlayerState.IN_RUNNING_MATCH, client2.getState());

		Thread.sleep(2 * NetworkConstants.Client.LOCKSTEP_PERIOD); // After two lockstep periods, there must be two locksteps.
		assertEquals(NetworkConstants.Client.LOCKSTEP_DEFAULT_LEAD_STEPS, clock1.getAllowedLockstep());
		assertEquals(NetworkConstants.Client.LOCKSTEP_DEFAULT_LEAD_STEPS, clock2.getAllowedLockstep());

		// After more than LOCKSTEP_DEFAULT_LEAD_STEPS periods, the lockstep counter must wait, to prevent it from running away.
		Thread.sleep((2 + NetworkConstants.Client.LOCKSTEP_DEFAULT_LEAD_STEPS) * NetworkConstants.Client.LOCKSTEP_PERIOD);
		assertEquals(NetworkConstants.Client.LOCKSTEP_DEFAULT_LEAD_STEPS, clock1.getAllowedLockstep());
		assertEquals(NetworkConstants.Client.LOCKSTEP_DEFAULT_LEAD_STEPS, clock2.getAllowedLockstep());

		// Submit a task
		TestTaskPacket testTask = new TestTaskPacket("dsfsdf", 2342, (byte) -23);
		client2.scheduleTask(testTask);

		Thread.sleep(2L * NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL);

		// The task must not have been submitted to the clients yet, because the lockstep is blocked.
		assertEquals(0, clock1.popBufferedTasks().size());
		assertEquals(0, clock2.popBufferedTasks().size());

		// Now let one clock continue one lockstep period.
		clock1.setTime(NetworkConstants.Client.LOCKSTEP_PERIOD + NetworkConstants.Client.TIME_SYNC_TOLERATED_DIFFERENCE + 10);

		Thread.sleep(NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL + 40L);

		List<TaskPacket> packets1 = clock1.popBufferedTasks();
		assertEquals(1, packets1.size());
		assertEquals(testTask, packets1.get(0));
		List<TaskPacket> packets2 = clock2.popBufferedTasks();
		assertEquals(1, packets2.size());
		assertEquals(testTask, packets2.get(0));

		Thread.sleep(2 * NetworkConstants.Client.LOCKSTEP_PERIOD); // Wait two more lockstep periods and check the run away protection again
		assertEquals(NetworkConstants.Client.LOCKSTEP_DEFAULT_LEAD_STEPS + 1, clock1.getAllowedLockstep());
		assertEquals(NetworkConstants.Client.LOCKSTEP_DEFAULT_LEAD_STEPS + 1, clock2.getAllowedLockstep());
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
		lobby.update(userA.getId(), new Player(otherPlayer.getIndex(), "Other", null, ELobbyPlayerState.UNKNOWN, ECivilisation.EGYPTIAN, ELobbyPlayerType.HUMAN, 2, true));
		assertNotEquals("Username should not be updateable", "Other", otherPlayer.getName());
		assertEquals(ECivilisation.EGYPTIAN, otherPlayer.getCivilisation());
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
