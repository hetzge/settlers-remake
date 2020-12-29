/*******************************************************************************
 * Copyright (c) 2015 - 2017
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.main.swing.menu.joinpanel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import java8.util.J8Arrays;
import java8.util.Optional;
import jsettlers.common.ai.EPlayerType;
import jsettlers.common.player.ECivilisation;
import jsettlers.graphics.localization.Labels;
import jsettlers.logic.map.loading.EMapStartResources;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.main.swing.JSettlersSwingUtil;
import jsettlers.main.swing.lookandfeel.ELFStyle;
import jsettlers.main.swing.lookandfeel.GBC;
import jsettlers.main.swing.lookandfeel.components.BackgroundPanel;
import jsettlers.main.swing.menu.joinpanel.controller.IJoinGameController;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerState;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.ResourceAmount;

/**
 * Layout:
 * 
 * <pre>
 * +---------------------------------------------------------------+
 * |              titleLabel                                       |
 * +------------------------+--------------------------------------+
 * |                        |       playerSlotsPanel               |
 * |                        +--------------------------------------+
 * | westPanel              |       chatPanel                      |
 * |                        +--------------------------------------+
 * |                        |       southPanel                     |
 * +------------------------+--------------------------------------+
 * </pre>
 * 
 * @author codingberlin
 */
public class JoinGamePanel extends BackgroundPanel {
	private static final long serialVersionUID = -1186791399814385303L;

	private final JLabel titleLabel = new JLabel();
	private final JPanel westPanel = new JPanel();
	private final JPanel mapPanel = new JPanel();
	private final JPanel settingsPanel = new JPanel();
	private final JLabel mapNameLabel = new JLabel();
	private final JLabel mapImage = new JLabel();
	private final JLabel peaceTimeLabel = new JLabel();
	private final JTextField peaceTimeTextField = new JTextField("0");
	private final JLabel startResourcesLabel = new JLabel();
	private final JComboBox<MapStartResourcesUIWrapper> startResourcesComboBox = new JComboBox<>();
	private final JPanel playerSlotsPanel = new JPanel();
	private final JButton cancelButton = new JButton();
	private final JButton startGameButton = new JButton();
	private final JLabel slotsHeadlinePlayerNameLabel = new JLabel();
	private final JLabel slotsHeadlineCivilisation = new JLabel();
	private final JLabel slotsHeadlineType = new JLabel();
	private final JLabel slotsHeadlineTeam = new JLabel();
	private final JTextField chatInputField = new JTextField();
	private final JTextArea chatArea = new JTextArea();
	private final JButton sendChatMessageButton = new JButton();
	private final List<PlayerSlot> playerSlots = new ArrayList<>();
	private final IJoinGameController controller;

	public JoinGamePanel(IJoinGameController controller) {
		this.controller = controller;
		createStructure();
		setStyle();
		localize();
		addListener();
	}

	private void createStructure() {
		titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
		westPanel.setLayout(new BorderLayout());
		westPanel.add(mapPanel, BorderLayout.NORTH);
		JPanel settingsPanelWrapper = new JPanel();
		westPanel.add(settingsPanelWrapper, BorderLayout.CENTER);
		settingsPanelWrapper.add(settingsPanel);
		// settingsPanel.setLayout(new GridLayout(0, 2, 20, 0));
		JPanel settingsLabelPanel = new JPanel();
		settingsLabelPanel.setLayout(new GridLayout(3, 0, 0, 20));
		JPanel settingsComboBoxPanel = new JPanel();
		settingsComboBoxPanel.setLayout(new GridLayout(3, 0, 0, 20));
		settingsPanel.add(settingsLabelPanel);
		settingsPanel.add(settingsComboBoxPanel);
		mapPanel.setLayout(new BorderLayout());
		JPanel mapNameLabelWrapper = new JPanel();
		mapPanel.add(mapNameLabelWrapper, BorderLayout.NORTH);
		mapNameLabelWrapper.add(mapNameLabel);
		mapNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mapPanel.add(mapImage, BorderLayout.CENTER);
		mapImage.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		settingsLabelPanel.add(startResourcesLabel);
		J8Arrays.stream(EMapStartResources.values())
				.map(MapStartResourcesUIWrapper::new)
				.forEach(startResourcesComboBox::addItem);
		startResourcesComboBox.setSelectedIndex(EMapStartResources.HIGH_GOODS.value - 1);
		settingsComboBoxPanel.add(startResourcesComboBox);
		settingsLabelPanel.add(peaceTimeLabel);
		settingsComboBoxPanel.add(peaceTimeTextField);
		sendChatMessageButton.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 15));
		JPanel chatPanel = new JPanel();
		chatPanel.setLayout(new BorderLayout(0, 10));
		JPanel chatInputPanel = new JPanel();
		chatInputPanel.setLayout(new BorderLayout(10, 0));
		chatArea.setEditable(false);
		chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
		chatPanel.add(chatInputPanel, BorderLayout.SOUTH);
		chatInputPanel.add(chatInputField, BorderLayout.CENTER);
		chatInputPanel.add(sendChatMessageButton, BorderLayout.EAST);
		playerSlotsPanel.setLayout(new GridBagLayout());
		playerSlotsPanel.setBorder(new EmptyBorder(20, 25, 20, 20));
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
		cancelButton.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 15));
		southPanel.add(cancelButton);
		startGameButton.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 15));
		southPanel.add(startGameButton);
		JPanel content = new JPanel(new GridBagLayout());
		content.add(titleLabel, new GBC().grid(0, 0).size(2, 1).fillx().insets(0, 0, 30, 0));
		content.add(westPanel, new GBC().grid(0, 1).size(1, 3).filly());
		content.add(new JScrollPane(playerSlotsPanel), new GBC().grid(1, 1).fillx().filly());
		content.add(chatPanel, new GBC().grid(1, 2).fillx().filly().insets(30, 0, 0, 0));
		content.add(southPanel, new GBC().grid(1, 3).fillx().insets(30, 0, 0, 0));
		add(content);
	}

	private void setStyle() {
		mapNameLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_LONG);
		startResourcesLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_SHORT);
		peaceTimeLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_SHORT);
		titleLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_HEADER);
		cancelButton.putClientProperty(ELFStyle.KEY, ELFStyle.BUTTON_MENU);
		startGameButton.putClientProperty(ELFStyle.KEY, ELFStyle.BUTTON_MENU);
		slotsHeadlinePlayerNameLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_DYNAMIC);
		slotsHeadlineCivilisation.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_DYNAMIC);
		slotsHeadlineType.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_DYNAMIC);
		slotsHeadlineTeam.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_DYNAMIC);
		sendChatMessageButton.putClientProperty(ELFStyle.KEY, ELFStyle.BUTTON_MENU);
		chatInputField.putClientProperty(ELFStyle.KEY, ELFStyle.TEXT_DEFAULT);
		chatArea.putClientProperty(ELFStyle.KEY, ELFStyle.PANEL_DARK);
		startResourcesComboBox.putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);
		peaceTimeTextField.putClientProperty(ELFStyle.KEY, ELFStyle.TEXT_DEFAULT);
		chatArea.putClientProperty(ELFStyle.KEY, ELFStyle.TEXT_DEFAULT);
		SwingUtilities.updateComponentTreeUI(this);
	}

	private void localize() {
		startResourcesLabel.setText(Labels.getString("join-game-panel-start-resources"));
		cancelButton.setText(Labels.getString("join-game-panel-cancel"));
		startGameButton.setText(Labels.getString("join-game-panel-start"));
		peaceTimeLabel.setText(Labels.getString("join-game-panel-peace-time"));
		slotsHeadlinePlayerNameLabel.setText(Labels.getString("join-game-panel-player-name"));
		slotsHeadlineCivilisation.setText(Labels.getString("join-game-panel-civilisation"));
		slotsHeadlineType.setText(Labels.getString("join-game-panel-player-type"));
		slotsHeadlineTeam.setText(Labels.getString("join-game-panel-team"));
		sendChatMessageButton.setText(Labels.getString("join-game-panel-send-chat-message"));
	}

	private void addListener() {
		startResourcesComboBox.addActionListener(e -> controller.updateMatch(getPeaceTime(), getStartResourceAmount()));
		peaceTimeTextField.addActionListener(e -> controller.updateMatch(getPeaceTime(), getStartResourceAmount()));
		ActionListener sendChatMessageListener = e -> {
			controller.sendChatMessage(chatInputField.getText());
			chatInputField.setText("");
		};
		sendChatMessageButton.addActionListener(sendChatMessageListener);
		chatInputField.addActionListener(sendChatMessageListener);
		cancelButton.addActionListener(e -> controller.cancel());
		startGameButton.addActionListener(e -> controller.start());
	}

	public void setTitle(String title) {
		this.titleLabel.setText(title);
	}

	public void setupCurrentPlayer(Player player) {
		setupHost(player.isHost());
		for (PlayerSlot playerSlot : playerSlots) {
			if (player.isHost()) {
				playerSlot.enable();
			} else if (playerSlot.getIndex() == player.getIndex()) {
				playerSlot.enableLite();
			} else {
				playerSlot.disable();
			}
		}
	}

	public void setupHost(boolean isHost) {
		peaceTimeTextField.setEnabled(isHost);
		startResourcesComboBox.setEnabled(isHost);
		startGameButton.setVisible(isHost);
	}

	public void setupMatch(Match match) {
		final List<Player> players = match.getPlayers();
		setPeaceTime(match.getPeaceTime());
		setStartResourceAmount(match.getResourceAmount());
		// players can be empty if they should not be updated
		if (!players.isEmpty()) {
			buildPlayerSlots(players);
		}
		updateNumberOfPlayerSlots();
	}

	public void setupPlayer(Player player) {
		final PlayerSlot playerSlot = playerSlots.get(player.getIndex());
		playerSlot.setCivilisation(player.getCivilisation());
		playerSlot.setPlayerType(player.getType());
		playerSlot.setTeam((byte) player.getTeam());
		playerSlot.setReady(player.isReady());
		playerSlot.setState(player.getState());
		playerSlot.setPlayerName(player.getName());
	}

	private void setPeaceTime(Duration peaceTime) {
		JSettlersSwingUtil.set(peaceTimeTextField, () -> peaceTimeTextField.setText(String.valueOf(peaceTime.toMinutes())));
	}

	private Duration getPeaceTime() {
		return Duration.ofMinutes(Long.valueOf(peaceTimeTextField.getText()));
	}

	private void setStartResourceAmount(ResourceAmount amount) {
		JSettlersSwingUtil.set(startResourcesComboBox, () -> startResourcesComboBox.setSelectedIndex(amount.ordinal()));
	}

	private ResourceAmount getStartResourceAmount() {
		return ResourceAmount.VALUES[startResourcesComboBox.getSelectedIndex()];
	}

	public void appendChat(String message) {
		chatArea.append(message + "\n");
	}

	public void setChatVisible(boolean isVisible) {
		chatArea.setVisible(isVisible);
		chatInputField.setVisible(isVisible);
		sendChatMessageButton.setVisible(isVisible);
		chatArea.setText("");
		chatInputField.setText("");
	}

	public void setupMap(MapLoader mapLoader) {
		mapNameLabel.setText(mapLoader.getMapName());
		mapImage.setIcon(new ImageIcon(JSettlersSwingUtil.createBufferedImageFrom(mapLoader)));
		buildPlayerSlots(mapLoader);
		updateNumberOfPlayerSlots();
	}

	private void buildPlayerSlots(MapLoader mapLoader) {
		final PlayerSetting[] playerSettings = mapLoader.getFileHeader().getPlayerSettings();
		final List<Player> players = IntStream.range(0, mapLoader.getMaxPlayers()).mapToObj(i -> {
			final ELobbyCivilisation civilisation = Utils.lobby(Optional.ofNullable(playerSettings[i].getCivilisation()).orElse(ECivilisation.ROMAN));
			final ELobbyPlayerType playerType = Optional.ofNullable(playerSettings[i].getPlayerType()).map(Utils::lobby).orElse(ELobbyPlayerType.AI_EASY);
			final int team = Optional.ofNullable(playerSettings[i].getTeamId()).map(it -> (int) it).orElse(i + 1);
			return new Player(i, "Player-" + i, null, ELobbyPlayerState.READY, civilisation, playerType, team);
		}).collect(Collectors.toList());
		buildPlayerSlots(players);
	}

	private void buildPlayerSlots(List<Player> players) {
		playerSlots.clear();

		for (Player player : players) {
			playerSlots.add(controller.createPlayerSlot(player));
		}
		for (Player player : players) {
			setupPlayer(player);
		}
	}

	private void updateNumberOfPlayerSlots() {
		playerSlotsPanel.removeAll();
		addPlayerSlotHeadline();
		for (int i = 0; i < playerSlots.size(); i++) {
			playerSlots.get(i).addTo(playerSlotsPanel, i + 1);
		}
		SwingUtilities.updateComponentTreeUI(playerSlotsPanel);
	}

	private void addPlayerSlotHeadline() {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = constraints.gridx + constraints.gridwidth + 1;
		constraints.gridy = 0;
		constraints.gridwidth = 4;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		playerSlotsPanel.add(slotsHeadlinePlayerNameLabel, constraints);
		constraints.gridx = constraints.gridx + constraints.gridwidth;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		playerSlotsPanel.add(slotsHeadlineCivilisation, constraints);
		constraints.gridx = constraints.gridx + constraints.gridwidth;
		constraints.gridy = 0;
		constraints.gridwidth = 4;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		playerSlotsPanel.add(slotsHeadlineType, constraints);
		constraints.gridx = constraints.gridx + constraints.gridwidth;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		playerSlotsPanel.add(slotsHeadlineTeam, constraints);
	}

	public PlayerSetting[] getPlayerSettings() {
		return playerSlots.stream()
				.map(playerSlot -> {
					ELobbyPlayerType playerType = playerSlot.getPlayerType();
					EPlayerType ingamePlayerType = Utils.ingame(playerType);
					return new PlayerSetting(ingamePlayerType != null, ingamePlayerType, Utils.ingame(playerSlot.getCivilisation()), (byte) playerSlot.getTeam());
				})
				.toArray(PlayerSetting[]::new);
	}

	public boolean haveAllPlayersStartFinished() {
		return playerSlots.stream().map(PlayerSlot::getState).allMatch(ELobbyPlayerState.INGAME::equals);
	}
}
