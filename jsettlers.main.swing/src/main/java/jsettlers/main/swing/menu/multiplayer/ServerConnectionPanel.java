/*******************************************************************************
 * Copyright (c) 2020
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.main.swing.menu.multiplayer;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import jsettlers.common.menu.IMultiplayerConnector;
import jsettlers.graphics.localization.Labels;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.loading.list.MapList;
import jsettlers.main.MultiplayerConnector;
import jsettlers.main.datatypes.JoinableGame;
import jsettlers.main.swing.JSettlersFrame;
import jsettlers.main.swing.menu.joinpanel.controller.MultiplayerJoinGameController;
import jsettlers.main.swing.menu.mainmenu.NetworkGameMapLoader;
import jsettlers.main.swing.menu.openpanel.OpenPanel;
import jsettlers.main.swing.settings.ServerEntry;
import jsettlers.network.client.IClientConnection;
import jsettlers.network.client.interfaces.INetworkClient;
import jsettlers.network.server.lobby.core.MatchId;

public class ServerConnectionPanel extends JPanel {

	private final OpenPanel openSinglePlayerPanel;
	private final JTabbedPane root;
	private final ServerEntry entry;
	private final JSettlersFrame settlersFrame;
	private Timer timer;
	private RemoteMapDirectoryPanel maps;
	private OpenPanel newMatch;
	private OpenPanel joinMatch;

	public ServerConnectionPanel(ServerEntry entry, Runnable leave, JSettlersFrame settlersFrame, OpenPanel openSinglePlayerPanel) {
		this.openSinglePlayerPanel = openSinglePlayerPanel;
		this.root = new JTabbedPane();
		this.settlersFrame = settlersFrame;
		this.timer = new Timer();
		this.maps = null;
		this.newMatch = null;
		this.joinMatch = null;
		this.entry = entry;

		JTextArea logText = new JTextArea();
		logText.setEditable(false);
		entry.setConnectionLogListener(logText::setText);
		root.addTab(Labels.getString("multiplayer-log-title"), new JScrollPane(logText));
		root.addTab(Labels.getString("multiplayer-log-settings"), new EditServerEntryPanel(leave, () -> root.setSelectedIndex(0), entry));

		setLayout(new BorderLayout());
		add(root, BorderLayout.CENTER);
	}

	@Override
	public void addNotify() {
		super.addNotify();
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				updateMatches();
			}
		}, 0L, 3000L);
	}

	private void updateMatches() {
		if (newMatch != null) {
			getConnector().getNetworkClient().queryMatches(matches -> {
				if (newMatch != null) {
					newMatch.setMapLoaders(matches.stream().map(JoinableGame::new).map(NetworkGameMapLoader::new).collect(Collectors.toList()));
				}
			});
		}
	}

	@Override
	public void removeNotify() {
		super.removeNotify();
		timer.cancel();
	}

	public void update() {
		IClientConnection connection = entry.getConnection();
		boolean conMaps = connection.getMaps("/") != null;
		int i = 1;
		if (!conMaps && maps != null) {
			root.removeTabAt(i);
			maps = null;
		} else if (conMaps && maps == null) {
			root.insertTab(Labels.getString("multiplayer-mapslist-title"), null, maps = new RemoteMapDirectoryPanel(connection, openSinglePlayerPanel), null, i);
		}
		if (maps != null) {
			maps.update();
			i++;
		}

		boolean conMatch = connection.isConnected() && connection instanceof IMultiplayerConnector;
		if (!conMatch && newMatch != null) {
			root.removeTabAt(i);
			root.removeTabAt(i + 1);
			newMatch = null;
			joinMatch = null;
		} else if (conMatch && newMatch == null) {
			newMatch = new OpenPanel(MapList.getDefaultList().getFreshMaps().getItems(), this::createMatch);
			SwingUtilities.updateComponentTreeUI(newMatch);
			root.insertTab(Labels.getString("multiplayer-newmatch-title"), null, newMatch, null, i);

			joinMatch = new OpenPanel(Collections.emptyList(), this::joinMatch);
			SwingUtilities.updateComponentTreeUI(joinMatch);

			root.insertTab(Labels.getString("multiplayer-joinmatch-title"), null, joinMatch, null, i + 1);
		}

		if (newMatch != null) {
			i += 2;
		}
	}

	public void createMatch(MapLoader mapLoader) {
		final MultiplayerConnector multiplayerConnector = getConnector();
		final INetworkClient networkClient = multiplayerConnector.getNetworkClient();
		MultiplayerJoinGameController
				.createMatch(settlersFrame, networkClient, mapLoader)
				.thenAccept(panel -> SwingUtilities.invokeLater(() -> settlersFrame.showJoinGamePanel(panel)));
	}

	public void joinMatch(MapLoader mapLoader) {
		final MultiplayerConnector multiplayerConnector = getConnector();
		if (!(mapLoader instanceof NetworkGameMapLoader)) {
			throw new IllegalStateException(String.format("Require %s to create match but is '%s'", NetworkGameMapLoader.class.getSimpleName(), mapLoader.getClass().getSimpleName()));
		}
		final NetworkGameMapLoader networkGameMapLoader = (NetworkGameMapLoader) mapLoader;
		final INetworkClient networkClient = multiplayerConnector.getNetworkClient();
		MultiplayerJoinGameController
				.joinMatch(settlersFrame, networkClient, MapList.getDefaultList().getMapById(networkGameMapLoader.getMapId()), new MatchId(networkGameMapLoader.getJoinableGame().getId()))
				.thenAccept(panel -> SwingUtilities.invokeLater(() -> settlersFrame.showJoinGamePanel(panel)));
	}

	private MultiplayerConnector getConnector() {
		final IClientConnection connection = entry.getConnection();
		if (!(connection instanceof MultiplayerConnector)) {
			throw new IllegalStateException(String.format("Require %s to create match but is '%s'", MultiplayerConnector.class.getSimpleName(), connection.getClass().getSimpleName()));
		}
		return (MultiplayerConnector) connection;
	}
}
