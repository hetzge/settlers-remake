package jsettlers.main.swing.lobby.organisms;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import jsettlers.main.swing.JSettlersSwingUtil;
import jsettlers.main.swing.lobby.atoms.ComboBox;
import jsettlers.main.swing.lobby.atoms.Label;
import jsettlers.main.swing.lobby.atoms.TextField;
import jsettlers.main.swing.lobby.atoms.ToggleButton;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.Player;

public class PlayersPanel extends JPanel {

	private final Controller controller;

	public PlayersPanel(Controller controller) {
		this.controller = controller;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	public void setPlayers(Collection<Player> players) {
		removeAll();
		add(new PlayerHeaderPanel());
		for (Player player : players) {
			add(new PlayerPanel(player, controller));
		}
	}

	public PlayerPanel getPlayerPanel(int index) {
		return (PlayerPanel) getComponent(1 + index);
	}

	private void resizeComponents(Container parent) {
		int i = 0;
		for (Component component : parent.getComponents()) {
			final Dimension dimension;
			if (i == 0 || i == 4 || i == 5) {
				dimension = new Dimension(70, 35);
			} else {
				dimension = new Dimension(120, 35);
			}
			component.setSize(dimension);
			component.setPreferredSize(dimension);
			component.setMaximumSize(dimension);
			i++;
		}
	}

	public class PlayerHeaderPanel extends JPanel {
		public PlayerHeaderPanel() {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			add(new Label("Index"));
			add(new Label("Player"));
			add(new Label("Type"));
			add(new Label("Civilisation"));
			add(new Label("Team"));
			add(new Label("Ready"));
			resizeComponents(this);
		}
	}

	public class PlayerPanel extends JPanel {
		private final TextField indexTextField;
		private final TextField playerTextField;
		private final ComboBox<ELobbyPlayerType> typeComboBox;
		private final ComboBox<Enum<?>> civilisationComboBox;
		private final TextField teamTextField;
		private final ToggleButton readyButton;

		public PlayerPanel(Player player, Controller controller) {
			final int index = player.getIndex();
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			Arrays.asList(
					this.indexTextField = new TextField(String.valueOf(index), false),
					this.playerTextField = new TextField(player.getName(), false),
					this.typeComboBox = new ComboBox<>(ELobbyPlayerType.VALUES, ELobbyPlayerType.AI_EASY, Enum::name),
					this.civilisationComboBox = new ComboBox<>(ELobbyCivilisation.VALUES, ELobbyPlayerType.AI_EASY, Enum::name),
					this.teamTextField = new TextField("0", false),
					this.readyButton = new ToggleButton(player.isReady(), ready -> controller.setReady(index, ready)))
					.forEach(this::add);
			this.typeComboBox.addItemListener(event -> controller.setType(index, (ELobbyPlayerType) typeComboBox.getSelectedItem()));
			this.civilisationComboBox.addItemListener(event -> controller.setCivilisation(index, (ELobbyCivilisation) civilisationComboBox.getSelectedItem()));
			this.teamTextField.addActionListener(event -> controller.setTeam(index, Integer.parseInt(teamTextField.getText())));
			setType(player.getType());
			setCivilisation(player.getCivilisation());
			setTeam(player.getTeam());
			setReady(player.isReady());
			resizeComponents(this);
		}

		public void setType(ELobbyPlayerType type) {
			JSettlersSwingUtil.set(this.typeComboBox, () -> this.typeComboBox.setSelectedItem(type));
		}

		public void setCivilisation(ELobbyCivilisation civilisation) {
			JSettlersSwingUtil.set(this.civilisationComboBox, () -> this.civilisationComboBox.setSelectedItem(civilisation));
		}

		public void setTeam(int team) {
			JSettlersSwingUtil.set(this.teamTextField, () -> this.teamTextField.setText(String.valueOf(team)));
		}

		public void setReady(boolean ready) {
			this.readyButton.setState(ready);
		}
	}

	public interface Controller {

		void setType(int index, ELobbyPlayerType type);

		void setCivilisation(int index, ELobbyCivilisation civilisation);

		void setTeam(int index, int team);

		void setReady(int index, boolean ready);
	}
}
