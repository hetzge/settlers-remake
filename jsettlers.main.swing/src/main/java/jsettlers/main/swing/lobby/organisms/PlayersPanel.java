package jsettlers.main.swing.lobby.organisms;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import jsettlers.graphics.localization.Labels;
import jsettlers.graphics.map.MapDrawContext;
import jsettlers.main.swing.JSettlersSwingUtil;
import jsettlers.main.swing.lobby.Utils;
import jsettlers.main.swing.lobby.atoms.CheckboxButton;
import jsettlers.main.swing.lobby.atoms.ComboBox;
import jsettlers.main.swing.lobby.atoms.IntegerSpinner;
import jsettlers.main.swing.lobby.atoms.Label;
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
		SwingUtilities.updateComponentTreeUI(this);
	}

	public PlayerPanel getPlayerPanel(int index) {
		return (PlayerPanel) getComponent(1 + index);
	}

	private void resizeComponents(Container parent) {
		int i = 0;
		for (Component component : parent.getComponents()) {
			final Dimension dimension;
			final int height = 35;
			if (i == 0 || i == 4 || i == 5) {
				dimension = new Dimension(70, height);
			} else if (i == 2) {
				dimension = new Dimension(160, height);
			} else {
				dimension = new Dimension(120, height);
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
			add(new Label(""));
			add(new Label(Labels.getString("join-game-panel-player-name")));
			add(new Label(Labels.getString("join-game-panel-player-type")));
			add(new Label(Labels.getString("join-game-panel-civilisation")));
			add(new Label(Labels.getString("join-game-panel-team")));
			add(new Label(""));

			getComponent(0).setVisible(false);
			getComponent(5).setVisible(false);
			resizeComponents(this);
		}
	}

	public class PlayerPanel extends JPanel {
		private final Label indexLabel;
		private final Label playerLabel;
		private final ComboBox<ELobbyPlayerType> typeComboBox;
		private final ComboBox<ELobbyCivilisation> civilisationComboBox;
		private final IntegerSpinner teamSpinner;
		private final CheckboxButton readyButton;

		public PlayerPanel(Player player, Controller controller) {
			final int index = player.getIndex();
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			Arrays.asList(
					this.indexLabel = new Label(String.valueOf(index + 1)),
					this.playerLabel = new Label(player.getName()),
					this.typeComboBox = new ComboBox<>(ELobbyPlayerType.VALUES, ELobbyPlayerType.AI_EASY, type -> Labels.getString("player-type-" + type.name())),
					this.civilisationComboBox = new ComboBox<>(ELobbyCivilisation.VALUES, ELobbyCivilisation.ROMAN, type -> Labels.getString("civilisation-" + type.name())),
					this.teamSpinner = new IntegerSpinner(1, 1, 30, 1),
					this.readyButton = new CheckboxButton(player.isReady(), ready -> controller.setReady(index, ready)))
					.forEach(this::add);
			final jsettlers.common.Color color = MapDrawContext.getPlayerColor((byte) player.getIndex());
			this.indexLabel.setIcon(Utils.createImageIcon(new Color(color.red, color.green, color.blue), 20));
			this.typeComboBox.addItemListener(event -> controller.setType(index, typeComboBox.getValue()));
			this.civilisationComboBox.addItemListener(event -> controller.setCivilisation(index, civilisationComboBox.getValue()));
			this.teamSpinner.addChangeListener(event -> controller.setTeam(index, teamSpinner.getIntegerValue()));
			setType(player.getType());
			setCivilisation(player.getCivilisation());
			setTeam(player.getTeam());
			setReady(player.isReady());
			resizeComponents(this);
		}

		public void setPlayer(String name) {
			this.playerLabel.setText(name);
		}

		public void setType(ELobbyPlayerType type) {
			JSettlersSwingUtil.set(this.typeComboBox, () -> this.typeComboBox.setValue(type));
		}

		public void setCivilisation(ELobbyCivilisation civilisation) {
			JSettlersSwingUtil.set(this.civilisationComboBox, () -> this.civilisationComboBox.setValue(civilisation));
		}

		public void setTeam(int team) {
			this.teamSpinner.setIntegerValue(team);
		}

		public void setReady(boolean ready) {
			this.readyButton.setState(ready);
		}

		public void setEnabled(boolean enabled) {
			this.typeComboBox.setEnabled(enabled);
			this.civilisationComboBox.setEnabled(enabled);
			this.teamSpinner.setEnabled(enabled);
			this.readyButton.setEnabled(enabled);
		}
	}

	public interface Controller {

		void setType(int index, ELobbyPlayerType type);

		void setCivilisation(int index, ELobbyCivilisation civilisation);

		void setTeam(int index, int team);

		void setReady(int index, boolean ready);
	}
}
