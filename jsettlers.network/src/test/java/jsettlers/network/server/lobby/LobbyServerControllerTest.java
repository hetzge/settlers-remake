package jsettlers.network.server.lobby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Timer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import jsettlers.network.NetworkConstants;
import jsettlers.network.TestUtils;
import jsettlers.network.client.NetworkClient;
import jsettlers.network.client.NetworkClientClockMock;
import jsettlers.network.client.time.TimeSyncSenderTimerTask;
import jsettlers.network.client.time.TimeSynchronizationListener;
import jsettlers.network.infrastructure.channel.AsyncChannel;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerState;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.LevelId;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.MatchId;
import jsettlers.network.server.lobby.core.MatchState;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.ResourceAmount;
import jsettlers.network.server.lobby.core.User;
import jsettlers.network.server.lobby.core.UserId;

public class LobbyServerControllerTest {

	private AsyncChannel clientChannelA;
	private AsyncChannel serverChannelA;
	private AsyncChannel clientChannelB;
	private AsyncChannel serverChannelB;
	private LobbyDb db;
	private Lobby lobby;
	private User userA;
	private User userB;
	private Timer timer;
	private NetworkClient networkClient;
	private LobbyServerController controller;

	@Before
	public void before() throws IOException {
		{
			final AsyncChannel[] channels = TestUtils.setUpAsyncLoopbackChannels();
			this.clientChannelA = channels[0];
			this.serverChannelA = channels[1];
		}

		{
			final AsyncChannel[] channels = TestUtils.setUpAsyncLoopbackChannels();
			this.clientChannelB = channels[0];
			this.serverChannelB = channels[1];
		}

		this.db = new LobbyDb();
		this.lobby = new Lobby(db);
		this.userA = new User(new UserId("testA"), "testuserA", serverChannelA);
		this.userB = new User(new UserId("testB"), "testuserB", serverChannelB);

		db.setUser(userA);
		db.setUser(userB);
		db.setMatch(new Match(new MatchId("test"), "test", new LevelId("test"),
				Arrays.asList(
						new Player(0, userA.getUsername(), userA.getId(), ELobbyPlayerState.UNKNOWN, ELobbyCivilisation.ROMAN, ELobbyPlayerType.HUMAN, 0),
						new Player(1, userB.getUsername(), userB.getId(), ELobbyPlayerState.UNKNOWN, ELobbyCivilisation.ROMAN, ELobbyPlayerType.HUMAN, 0)),
				ResourceAmount.HIGH, Duration.ofMinutes(0),
				MatchState.OPENED));

		this.timer = new Timer();
		this.networkClient = new NetworkClient(clientChannelA, userA.getId());
		this.controller = new LobbyServerController(lobby);
	}

	@After
	public void after() {
		this.timer.cancel();
		this.networkClient.close();
		this.controller.stop();
	}

	@Test
	public void testTimeSynchronization() throws IllegalStateException, InterruptedException, IOException {

		controller.setup(new User(userA.getId(), userA.getUsername(), serverChannelA));
		controller.setup(new User(userB.getId(), userB.getUsername(), serverChannelB));

		networkClient.startMatch();
		Thread.sleep(1000L);

		NetworkClientClockMock clock1 = new NetworkClientClockMock(200);
		NetworkClientClockMock clock2 = new NetworkClientClockMock(210);

		clientChannelA.registerListener(new TimeSynchronizationListener(clientChannelA, clock1));
		clientChannelB.registerListener(new TimeSynchronizationListener(clientChannelB, clock2));

		timer.schedule(new TimeSyncSenderTimerTask(clientChannelA, clock1), 0, NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL);
		timer.schedule(new TimeSyncSenderTimerTask(clientChannelB, clock2), 0, NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL);

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
		Thread.sleep(7L * NetworkConstants.Client.TIME_SYNC_SEND_INTERVALL + 20L); // wait for 7 synchronizations
		diff = Math.abs(clock2.getTime() - clock1.getTime());
		assertTrue("diff is to high: " + diff, diff < NetworkConstants.Client.TIME_SYNC_TOLERATED_DIFFERENCE);
		assertTrue(clock2.popAdjustmentEvents().size() > 0);
		assertEquals(0, clock1.popAdjustmentEvents().size());
	}
}
