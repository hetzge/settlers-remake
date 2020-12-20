package jsettlers.main.swing.lobby.organisms;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import jsettlers.main.swing.JSettlersSwingUtil;
import jsettlers.main.swing.lobby.atoms.Button;
import jsettlers.main.swing.lobby.atoms.ComboBox;
import jsettlers.main.swing.lobby.atoms.Label;
import jsettlers.main.swing.lobby.atoms.TextField;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerState;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.UserId;

public class PlayersPanel extends JPanel {

	public PlayersPanel(Collection<Player> players, PlayersListener listener) {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(new PlayerHeaderPanel());
		for (Player player : players) {
			add(new PlayerPanel(player, listener));
		}
	}

	public PlayerPanel getPlayerPanel(int index) {
		return (PlayerPanel) getComponent(1 + index);
	}

	private void resizeComponents(Container parent) {
		for (Component component : parent.getComponents()) {
			final Dimension dimension = new Dimension(120, 40);
			component.setSize(dimension);
			component.setPreferredSize(dimension);
			component.setMaximumSize(dimension);
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
		private final Button readyButton;

		public PlayerPanel(Player player, PlayersListener listener) {
			final int index = player.getIndex();
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			Arrays.asList(
					this.indexTextField = new TextField(String.valueOf(index), false),
					this.playerTextField = new TextField(player.getName(), false),
					this.typeComboBox = new ComboBox<>(ELobbyPlayerType.VALUES, ELobbyPlayerType.AI_EASY, Enum::name),
					this.civilisationComboBox = new ComboBox<>(ELobbyCivilisation.VALUES, ELobbyPlayerType.AI_EASY, Enum::name),
					this.teamTextField = new TextField("0", false),
					this.readyButton = new Button("???"))
					.forEach(this::add);
			this.typeComboBox.addItemListener(event -> listener.setType(index, (ELobbyPlayerType) typeComboBox.getSelectedItem()));
			this.civilisationComboBox.addItemListener(event -> listener.setCivilisation(index, (ELobbyCivilisation) civilisationComboBox.getSelectedItem()));
			this.teamTextField.addActionListener(event -> listener.setTeam(index, Integer.parseInt(teamTextField.getText())));
			this.readyButton.addActionListener(event -> listener.setReady(index, !readyButton.isSelected()));
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
			JSettlersSwingUtil.set(this.typeComboBox, () -> this.civilisationComboBox.setSelectedItem(civilisation));
		}

		public void setTeam(int team) {
			JSettlersSwingUtil.set(this.typeComboBox, () -> this.teamTextField.setText(String.valueOf(team)));
		}

		public void setReady(boolean ready) {
			JSettlersSwingUtil.set(this.typeComboBox, () -> {
				this.readyButton.setText(ready ? "YES" : "NO");
				this.readyButton.setSelected(ready);
			});
		}
	}

	public interface PlayersListener {

		void setType(int index, ELobbyPlayerType type);

		void setCivilisation(int index, ELobbyCivilisation civilisation);

		void setTeam(int index, int team);

		void setReady(int index, boolean ready);
	}
}
