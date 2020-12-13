package jsettlers.network.server.lockstep;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

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
import jsettlers.network.common.packets.BooleanMessagePacket;
import jsettlers.network.common.packets.MapInfoPacket;
import jsettlers.network.infrastructure.channel.AsyncChannel;
import jsettlers.network.infrastructure.channel.listeners.SimpleListener;
import jsettlers.network.server.lobby.Lobby;
import jsettlers.network.server.lobby.LobbyDb;
import jsettlers.network.server.lobby.LobbyServerController;
import jsettlers.network.server.lobby.core.User;
import jsettlers.network.server.lobby.core.UserId;

public class ClientServerLockstepTest {

	private LobbyDb db;
	private Lobby lobby;

	@Before
	public void before() throws IOException {
		this.db = new LobbyDb();
		this.lobby = new Lobby(db);
	}

	@Test
	public void test() throws IOException, InterruptedException {

		final AsyncChannel[] channelsA = TestUtils.setUpAsyncLoopbackChannels();
		final AsyncChannel[] channelsB = TestUtils.setUpAsyncLoopbackChannels();

		final LobbyServerController serverController = new LobbyServerController(lobby);
		serverController.setup(channelsA[1]);
		serverController.setup(channelsB[1]);

		final User userA = new User(new UserId("testA"), "testuserA", channelsA[1]);
		final User userB = new User(new UserId("testB"), "testuserB", channelsB[1]);

		final NetworkClientClockMock clockA = new NetworkClientClockMock();
		final NetworkClient clientA = new NetworkClient(channelsA[0], userA.getId());
		clientA.registerListener(new TimeSynchronizationListener(channelsA[0], clockA));
		clientA.registerListener(new SimpleListener<>(ENetworkKey.SYNCHRONOUS_TASK, SyncTasksPacket.class, packet -> {
			clockA.scheduleSyncTasksPacket(packet);
		}));
		clientA.registerListener(new SimpleListener<>(ENetworkKey.MATCH_STARTED, BooleanMessagePacket.class, packet -> {
			new Timer().schedule(new TimeSyncSenderTimerTask(channelsA[0], clockA), 0, NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL);
		}));
		clientA.logIn("AAA");
		clientA.openNewMatch("MATCH", 2, new MapInfoPacket("MAP", "TESTMAP", "AUTHOR", "BOB", 2));
		Thread.sleep(100);
		clientA.updatePlayerReady(0, true);

		final NetworkClientClockMock clockB = new NetworkClientClockMock();
		final NetworkClient clientB = new NetworkClient(channelsB[0], userB.getId());
		clientB.registerListener(new TimeSynchronizationListener(channelsB[0], clockB));
		clientB.registerListener(new SimpleListener<>(ENetworkKey.SYNCHRONOUS_TASK, SyncTasksPacket.class, packet -> {
			clockB.scheduleSyncTasksPacket(packet);
		}));
		clientB.registerListener(new SimpleListener<>(ENetworkKey.MATCH_STARTED, BooleanMessagePacket.class, packet -> {
			new Timer().schedule(new TimeSyncSenderTimerTask(channelsB[0], clockB), 0, NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL);
		}));
		clientB.logIn("BBB");
		clientB.joinMatch(db.getActiveMatch(userA.getId()).getId());
		Thread.sleep(100);
		clientB.updatePlayerReady(1, true);
		Thread.sleep(1000);
		clientA.startMatch();

		Thread.sleep(1000);

		// ----------------

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
}
