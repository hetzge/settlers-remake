package jsettlers.main.swing.lobby.pages.match;

import jsettlers.main.swing.lobby.organisms.ChatPanel;
import jsettlers.main.swing.lobby.organisms.MatchSettingsPanel;
import jsettlers.main.swing.lobby.organisms.PlayersPanel;

public interface MatchPageController extends PlayersPanel.Controller, ChatPanel.Controller, MatchSettingsPanel.Controller {

	MatchPagePanel init();

	void cancel();

	void startMatch();
}