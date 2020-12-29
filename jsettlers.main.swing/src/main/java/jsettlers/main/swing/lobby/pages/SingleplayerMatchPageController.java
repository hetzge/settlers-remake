package jsettlers.main.swing.lobby.pages;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jsettlers.graphics.localization.Labels;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.main.swing.lobby.organisms.MatchSettingsPanel.StartResources;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerState;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.UserId;

public class SingleplayerMatchPageController implements MatchPagePanel.Controller {

	private final MatchPagePanel panel;
	private final List<Player> players;
	private final MapLoader mapLoader;
	private int peaceTimeInMinutes;
	private StartResources startResources;

	public SingleplayerMatchPageController(MapLoader mapLoader) {
		this.mapLoader = mapLoader;
		this.panel = new MatchPagePanel(this);
		this.players = createPlayers(mapLoader);
		this.peaceTimeInMinutes = 0;
		this.startResources = StartResources.MEDIUM;
	}

	@Override
	public MatchPagePanel init() {
		this.panel.setTitle(Labels.getString("join-game-panel-new-single-player-game-title"));
		this.panel.getPlayersPanel().setPlayers(players);
		this.panel.getMatchSettingsPanel().setPeaceTime(peaceTimeInMinutes);
		this.panel.getMatchSettingsPanel().setStartResources(startResources);
		this.panel.getMatchSettingsPanel().setMapInformation(this.mapLoader);
		this.panel.getChatPanel().setVisible(false);
		return this.panel;
	}

	@Override
	public void setType(int index, ELobbyPlayerType type) {
		players.get(index).setType(type);
	}

	@Override
	public void setCivilisation(int index, ELobbyCivilisation civilisation) {
		players.get(index).setCivilisation(civilisation);
	}

	@Override
	public void setTeam(int index, int team) {
		players.get(index).setTeam(team);
	}

	@Override
	public void setReady(int index, boolean ready) {
		players.get(index).setReady(ready);
	}

	@Override
	public void submitMessage(String text) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void setPeaceTime(int minutes) {
		this.peaceTimeInMinutes = minutes;
	}

	@Override
	public void setStartResources(StartResources resources) {
		this.startResources = resources;
	}

	@Override
	public void startMatch() {
		System.out.println("Start match");
	}

	private static List<Player> createPlayers(MapLoader mapLoader) {
		return IntStream.range(0, mapLoader.getMaxPlayers()).mapToObj(SingleplayerMatchPageController::createPlayer).collect(Collectors.toList());
	}

	private static Player createPlayer(int i) {
		if (i == 0) {
			return new Player(i, "Player", new UserId(String.valueOf(i)), ELobbyPlayerState.READY, ELobbyCivilisation.ROMAN, ELobbyPlayerType.HUMAN, i + 1);
		} else {
			return new Player(i, "Player-" + i, new UserId(String.valueOf(i)), ELobbyPlayerState.READY, ELobbyCivilisation.ROMAN, ELobbyPlayerType.AI_EASY, i + 1);
		}
	}
}
