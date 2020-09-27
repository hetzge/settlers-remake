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
import jsettlers.graphics.localization.Labels;
import jsettlers.logic.map.loading.EMapStartResources;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.player.PlayerSetting;
import jsettlers.main.swing.JSettlersSwingUtil;
import jsettlers.main.swing.lookandfeel.ELFStyle;
import jsettlers.main.swing.lookandfeel.GBC;
import jsettlers.main.swing.lookandfeel.components.BackgroundPanel;
import jsettlers.main.swing.menu.joinpanel.slots.PlayerSlot;
import jsettlers.network.server.lobby.core.Match;
import jsettlers.network.server.lobby.core.Player;
import jsettlers.network.server.lobby.core.PlayerType;
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
	private final JLabel numberOfPlayersLabel = new JLabel();
	private final JComboBox<Integer> numberOfPlayersComboBox = new JComboBox<>();
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
	private final JLabel slotsHeadlineMapSlot = new JLabel();
	private final JLabel slotsHeadlineTeam = new JLabel();
	private final JTextField chatInputField = new JTextField();
	private final JTextArea chatArea = new JTextArea();
	private final JButton sendChatMessageButton = new JButton();
	private final List<PlayerSlot> playerSlots = new ArrayList<>();
	private final IJoinGameConnector connector;

	public JoinGamePanel(IJoinGameConnector connector) {
		this.connector = connector;
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
		settingsLabelPanel.add(numberOfPlayersLabel);
		settingsComboBoxPanel.add(numberOfPlayersComboBox);
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
		numberOfPlayersLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_SHORT);
		startResourcesLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_SHORT);
		peaceTimeLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_SHORT);
		titleLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_HEADER);
		cancelButton.putClientProperty(ELFStyle.KEY, ELFStyle.BUTTON_MENU);
		startGameButton.putClientProperty(ELFStyle.KEY, ELFStyle.BUTTON_MENU);
		slotsHeadlinePlayerNameLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_DYNAMIC);
		slotsHeadlineCivilisation.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_DYNAMIC);
		slotsHeadlineType.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_DYNAMIC);
		slotsHeadlineMapSlot.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_DYNAMIC);
		slotsHeadlineTeam.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_DYNAMIC);
		sendChatMessageButton.putClientProperty(ELFStyle.KEY, ELFStyle.BUTTON_MENU);
		chatInputField.putClientProperty(ELFStyle.KEY, ELFStyle.TEXT_DEFAULT);
		chatArea.putClientProperty(ELFStyle.KEY, ELFStyle.PANEL_DARK);
		startResourcesComboBox.putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);
		numberOfPlayersComboBox.putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);
		peaceTimeTextField.putClientProperty(ELFStyle.KEY, ELFStyle.TEXT_DEFAULT);
		chatArea.putClientProperty(ELFStyle.KEY, ELFStyle.TEXT_DEFAULT);
		SwingUtilities.updateComponentTreeUI(this);
	}

	private void localize() {
		numberOfPlayersLabel.setText(Labels.getString("join-game-panel-number-of-players"));
		startResourcesLabel.setText(Labels.getString("join-game-panel-start-resources"));
		cancelButton.setText(Labels.getString("join-game-panel-cancel"));
		startGameButton.setText(Labels.getString("join-game-panel-start"));
		peaceTimeLabel.setText(Labels.getString("join-game-panel-peace-time"));
		slotsHeadlinePlayerNameLabel.setText(Labels.getString("join-game-panel-player-name"));
		slotsHeadlineCivilisation.setText(Labels.getString("join-game-panel-civilisation"));
		slotsHeadlineType.setText(Labels.getString("join-game-panel-player-type"));
		slotsHeadlineMapSlot.setText(Labels.getString("join-game-panel-map-slot"));
		slotsHeadlineTeam.setText(Labels.getString("join-game-panel-team"));
		sendChatMessageButton.setText(Labels.getString("join-game-panel-send-chat-message"));
	}

	private void addListener() {
		numberOfPlayersComboBox.addActionListener(e -> updateNumberOfPlayerSlots());
		ActionListener sendChatMessageListener = e -> {
			connector.sendChatMessage(chatInputField.getText());
			chatInputField.setText("");
		};
		sendChatMessageButton.addActionListener(sendChatMessageListener);
		chatInputField.addActionListener(sendChatMessageListener);
		cancelButton.addActionListener(e -> connector.cancel());
		startGameButton.addActionListener(e -> connector.start());
	}

	public void setTitle(String title) {
		this.titleLabel.setText(title);
	}

	public void setupHost(boolean isHost) {
		numberOfPlayersComboBox.setEnabled(isHost);
		peaceTimeTextField.setEnabled(isHost);
		startResourcesComboBox.setEnabled(isHost);
		startGameButton.setVisible(true);
	}

	public void setupMatch(Match match) {
		setPeaceTime(match.getPeaceTime());
		setStartResourceAmount(match.getResourceAmount());
		buildPlayerSlots(match);
		updateNumberOfPlayerSlots();
	}

	public void setupPlayer(Player player) {
		final PlayerSlot playerSlot = playerSlots.stream().filter(slot -> slot.getPlayerId().equals(player.getId())).findFirst().get();
		playerSlot.setPlayerName(player.getName());
		playerSlot.setCivilisation(player.getCivilisation());
		playerSlot.setPlayerType(player.getType());
		playerSlot.setTeam((byte) player.getTeam());
		playerSlot.setReady(player.isReady());
	}

	private void setPeaceTime(Duration peaceTime) {
		peaceTimeTextField.setText(String.valueOf(peaceTime.toMinutes()));
	}

	private void setStartResourceAmount(ResourceAmount amount) {
		startResourcesComboBox.setSelectedIndex(amount.ordinal());
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
		resetNumberOfPlayersComboBox(mapLoader);
		buildPlayerSlots(mapLoader);
		updateNumberOfPlayerSlots();
	}

	private void buildPlayerSlots(Match match) {
		playerSlots.clear();
		Player[] players = match.getPlayers();

		for (Player player : players) {
			playerSlots.add(new PlayerSlot(connector, player.getId(), players.length, new PlayerType[] {
					PlayerType.EMPTY,
					PlayerType.NONE,
					PlayerType.HUMAN,
					PlayerType.AI_VERY_HARD,
					PlayerType.AI_HARD,
					PlayerType.AI_EASY,
					PlayerType.AI_VERY_EASY }));
		}
		for (Player player : players) {
			setupPlayer(player);
		}
	}

	private void buildPlayerSlots(MapLoader mapLoader) {
		int maxPlayers = mapLoader.getMaxPlayers();
		System.out.println("JoinGamePanel.buildPlayerSlots(" + maxPlayers + ")");
		playerSlots.clear();
		PlayerSetting[] playerSettings = mapLoader.getFileHeader().getPlayerSettings();
		for (byte i = 0; i < maxPlayers; i++) {
			PlayerSlot playerSlot = this.connector.createPlayerSlot(i);
			PlayerSetting playerSetting = playerSettings[i];
			playerSlots.add(playerSlot);
			playerSlot.setSlot(i);

			if (playerSetting.getTeamId() != null) {
				playerSlot.setTeam(playerSetting.getTeamId());
			} else {
				playerSlot.setTeam(i + 1);
			}

			if (playerSetting.getCivilisation() != null) {
				playerSlot.setCivilisation(playerSetting.getCivilisation());
			}

			if (playerSetting.getPlayerType() != null) {
				playerSlot.setPlayerType(PlayerType.from(playerSetting.getPlayerType()));
			}
		}
	}

	private void resetNumberOfPlayersComboBox(MapLoader mapLoader) {
		numberOfPlayersComboBox.removeAllItems();
		for (int i = 1; i < mapLoader.getMaxPlayers() + 1; i++) {
			numberOfPlayersComboBox.addItem(i);
		}
		numberOfPlayersComboBox.setSelectedIndex(mapLoader.getMaxPlayers() - 1);
	}

	private void updateNumberOfPlayerSlots() {
		if (numberOfPlayersComboBox.getSelectedItem() == null) {
			return;
		}
		playerSlotsPanel.removeAll();
		addPlayerSlotHeadline();
		for (int i = 0; i < playerSlots.size(); i++) {
			playerSlots.get(i).addTo(playerSlotsPanel, i + 1);
		}
		SwingUtilities.updateComponentTreeUI(playerSlotsPanel);
	}

	private void addPlayerSlotHeadline() {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridwidth = 4;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		playerSlotsPanel.add(slotsHeadlinePlayerNameLabel, constraints);
		constraints.gridx = 5;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		playerSlotsPanel.add(slotsHeadlineCivilisation, constraints);
		constraints = new GridBagConstraints();
		constraints.gridx = 7;
		constraints.gridy = 0;
		constraints.gridwidth = 4;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		playerSlotsPanel.add(slotsHeadlineType, constraints);
		constraints = new GridBagConstraints();
		constraints.gridx = 11;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		playerSlotsPanel.add(slotsHeadlineMapSlot, constraints);
		constraints = new GridBagConstraints();
		constraints.gridx = 12;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		playerSlotsPanel.add(slotsHeadlineTeam, constraints);
	}

	public PlayerSetting[] getPlayerSettings() {
		return playerSlots.stream()
				.sorted((playerSlot, otherPlayerSlot) -> playerSlot.getSlot() - otherPlayerSlot.getSlot())
				.map(playerSlot -> {
					PlayerType playerType = playerSlot.getPlayerType();
					if (playerType.getPlayerType() != null) {
						return new PlayerSetting(playerSlot.getPlayerType().getPlayerType(), playerSlot.getCivilisation(), (byte) playerSlot.getTeam());
					} else {
						return new PlayerSetting();
					}
				})
				.toArray(PlayerSetting[]::new);
	}
}
