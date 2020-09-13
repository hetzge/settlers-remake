package jsettlers.network.server.lobby;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java8.util.Optional;
import jsettlers.network.TestUtils;
import jsettlers.network.infrastructure.channel.Channel;
import jsettlers.network.server.lobby.core.LevelId;
import jsettlers.network.server.lobby.core.User;
import jsettlers.network.server.lobby.core.UserId;

public class LobbyTest {

	private Channel client;
	private Channel server;

	@Before
	public void setUp() throws IOException {
		final Channel[] channels = TestUtils.setUpLoopbackChannels();
		this.client = channels[0];
		this.server = channels[1];
	}

	@After
	public void tearDown() {
		this.client.close();
		this.server.close();
	}

	@Test
	public void test() {
		final Lobby lobby = new Lobby();
		lobby.join(new User(new UserId("test"), "testuser", server));
		lobby.createMatch(new UserId("test"), "test-match", new LevelId("test-level"), 4);
		lobby.leave(new UserId("test"));
	}
	
	@Test
	public void test_active_match() {
		final Lobby lobby = new Lobby();
		lobby.join(new User(new UserId("test"), "testuser", server));
		lobby.createMatch(new UserId("test"), "test-match", new LevelId("test-level"), 4);
		
		assertTrue(lobby.getActiveMatch(new UserId("test")).isPresent());
	}

}
