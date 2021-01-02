package jsettlers.main.swing.lobby.pages.match;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import jsettlers.common.menu.IStartingGame;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.main.JSettlersGame;
import jsettlers.main.swing.lobby.UiController;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerState;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.ELobbyResourceAmount;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.UserId;

public class SingleplayerMatchPageController implements MatchPageController {

	private final UiController ui;
	private final MatchPagePanel panel;
	private final List<Player> players;
	private final MapLoader mapLoader;
	private int peaceTimeInMinutes;
	private ELobbyResourceAmount resourceAmount;

	public SingleplayerMatchPageController(UiController ui, MapLoader mapLoader) {
		this.ui = ui;
		this.mapLoader = mapLoader;
		this.panel = new MatchPagePanel(this);
		this.players = createPlayers(mapLoader);
		this.peaceTimeInMinutes = 0;
		this.resourceAmount = ELobbyResourceAmount.MEDIUM_GOODS;
	}

	@Override
	public MatchPagePanel init() {
		this.panel.getPlayersPanel().setPlayers(players);
		this.panel.getMatchSettingsPanel().setPeaceTime(peaceTimeInMinutes);
		this.panel.getMatchSettingsPanel().setStartResources(resourceAmount);
		this.panel.getMatchSettingsPanel().setMapInformation(this.mapLoader);
		this.panel.getChatPanel().setVisible(false);
		this.panel.showStartButton(true);
		return this.panel;
	}

	@Override
	public void cancel() {
		ui.showHomePage();
	}

	@Override
	public void setType(int index, ELobbyPlayerType type) {
		players.get(index).setType(type);
		if (type == ELobbyPlayerType.HUMAN) {
			players.get(index).setName("Player");
			for (Player player : players) {
				if (player.getIndex() != index && player.getType() == ELobbyPlayerType.HUMAN) {
					player.setName("Computer " + player.getIndex());
					player.setType(ELobbyPlayerType.AI_EASY);
				}
			}
		}
		// Update ui
		this.panel.getPlayersPanel().setPlayers(this.players);
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
		players.get(index).setReady(true);
		this.panel.getPlayersPanel().getPlayerPanel(index).setReady(true);
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
	public void setStartResources(ELobbyResourceAmount amount) {
		this.resourceAmount = amount;
	}

	@Override
	public void startMatch() {
		final long randomSeed = System.currentTimeMillis();
		final PlayerSetting[] playerSettings = MatchPageUtils.toPlayerSettings(this.players);
		final byte playerId = (byte) this.players.stream().filter(player -> player.getType() == ELobbyPlayerType.HUMAN).findFirst()
				.orElseThrow(() -> new IllegalStateException("Can't start single player game without human player")).getIndex();
		final JSettlersGame game = new JSettlersGame(mapLoader, randomSeed, playerId, playerSettings);
		final IStartingGame startingGame = game.start();
		ui.getFrame().showStartingGamePanel(startingGame);
	}

	private static List<Player> createPlayers(MapLoader mapLoader) {
		return IntStream.range(0, mapLoader.getMaxPlayers()).mapToObj(SingleplayerMatchPageController::createPlayer).collect(Collectors.toList());
	}

	private static Player createPlayer(int i) {
		if (i == 0) {
			return new Player(i, "Player", new UserId(String.valueOf(i)), ELobbyPlayerState.READY, ELobbyCivilisation.ROMAN, ELobbyPlayerType.HUMAN, i + 1);
		} else {
			return new Player(i, "Computer " + i, new UserId(String.valueOf(i)), ELobbyPlayerState.READY, ELobbyCivilisation.ROMAN, ELobbyPlayerType.AI_EASY, i + 1);
		}
	}
}
