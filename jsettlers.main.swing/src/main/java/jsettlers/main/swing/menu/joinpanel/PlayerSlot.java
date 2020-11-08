/*******************************************************************************
 * Copyright (c) 2015
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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jsettlers.common.player.ECivilisation;
import jsettlers.graphics.image.SingleImage;
import jsettlers.graphics.localization.Labels;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.main.swing.JSettlersSwingUtil;
import jsettlers.main.swing.lookandfeel.ELFStyle;
import jsettlers.main.swing.menu.joinpanel.controller.IJoinGameController;
import jsettlers.network.server.lobby.core.ELobbyCivilisation;
import jsettlers.network.server.lobby.core.ELobbyPlayerState;
import jsettlers.network.server.lobby.core.ELobbyPlayerType;

public class PlayerSlot {

	public static final int READY_BUTTON_WIDTH = 40;
	public static final int READY_BUTTON_HEIGHT = 25;
	public static final ImageIcon READY_IMAGE = new ImageIcon(getReadyButtonImage(2, 17, 0, true));
	public static final ImageIcon READY_PRESSED_IMAGE = new ImageIcon(getReadyButtonImage(2, 17, 1, true));
	public static final ImageIcon READY_DISABLED_IMAGE = new ImageIcon(getReadyButtonImage(2, 17, 0, false));
	public static final ImageIcon NOT_READY_IMAGE = new ImageIcon(getReadyButtonImage(2, 18, 0, true));
	public static final ImageIcon NOT_READY_PRESSED_IMAGE = new ImageIcon(getReadyButtonImage(2, 18, 1, true));
	public static final ImageIcon NOT_READY_DISABLED_IMAGE = new ImageIcon(getReadyButtonImage(2, 18, 0, false));

	private final IJoinGameController controller;
	private final int index;
	private boolean ready;
	private ELobbyPlayerState state;

	private final JLabel indexLabel;
	private final JLabel playerNameLabel;
	private final JComboBox<ELobbyCivilisation> civilisationComboBox;
	private final JComboBox<ELobbyPlayerType> typeComboBox;
	private final JComboBox<Integer> teamComboBox;
	private final JButton readyButton;

	public PlayerSlot(IJoinGameController controller, int index, int totalSlots, ELobbyPlayerType[] playerTypes) {
		this.controller = controller;
		this.index = index;

		// components
		this.indexLabel = new JLabel(String.valueOf(index + 1));
		this.playerNameLabel = new JLabel();
		this.civilisationComboBox = new JComboBox<>();
		this.typeComboBox = new JComboBox<>();
		this.typeComboBox.removeAll();
		this.teamComboBox = new JComboBox<>();
		this.readyButton = new JButton();

		// style
		readyButton.setSize(new Dimension(READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));
		readyButton.setMaximumSize(new Dimension(READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));
		readyButton.setPreferredSize(new Dimension(READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));
		readyButton.setMinimumSize(new Dimension(READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));
		playerNameLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_DYNAMIC);
		teamComboBox.putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);
		indexLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_DYNAMIC);
		typeComboBox.putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);
		civilisationComboBox.putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);

		// init
		for (ELobbyCivilisation civilisation : ELobbyCivilisation.values()) {
			civilisationComboBox.addItem(civilisation);
		}
		for (int i = 1; i < totalSlots + 1; i++) {
			teamComboBox.addItem(i);
		}
		for (ELobbyPlayerType playerType : playerTypes) {
			this.typeComboBox.addItem(playerType);
		}

		// listener
		typeComboBox.addActionListener(event -> {
			updatePlayerName();
			controller.updatePlayerType(index, getPlayerType());
		});
		civilisationComboBox.addActionListener(event -> {
			updatePlayerName();
			controller.updatePlayerCivilisation(index, getCivilisation());
		});
		readyButton.addActionListener(event -> {
			setReady(!isReady());
			controller.updatePlayerReady(index, isReady());
		});
		teamComboBox.addActionListener(event -> {
			controller.updatePlayerTeam(index, getTeam());
		});

		updatePlayerName();
	}

	public void addTo(JPanel panel, int row) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = constraints.gridx + constraints.gridwidth;
		constraints.gridy = row + 1;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(indexLabel, constraints);
		constraints.gridx = constraints.gridx + constraints.gridwidth;
		constraints.gridy = row + 1;
		constraints.gridwidth = 4;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(playerNameLabel, constraints);
		constraints.gridx = constraints.gridx + constraints.gridwidth;
		constraints.gridy = row + 1;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(civilisationComboBox, constraints);
		constraints.gridx = constraints.gridx + constraints.gridwidth;
		constraints.gridy = row + 1;
		constraints.gridwidth = 4;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(typeComboBox, constraints);
		constraints.gridx = constraints.gridx + constraints.gridwidth;
		constraints.gridy = row + 1;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(teamComboBox, constraints);
		constraints.gridx = constraints.gridx + constraints.gridwidth;
		constraints.gridy = row + 1;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(readyButton, constraints);
	}

	public ELobbyPlayerState getState() {
		return state;
	}

	public int getIndex() {
		return index;
	}

	public int getTeam() {
		return teamComboBox.getSelectedIndex() + 1;
	}

	public ELobbyPlayerType getPlayerType() {
		return (ELobbyPlayerType) typeComboBox.getSelectedItem();
	}

	public ELobbyCivilisation getCivilisation() {
		return (ELobbyCivilisation) civilisationComboBox.getSelectedItem();
	}

	public boolean isReady() {
		return ready;
	}

	public void setState(ELobbyPlayerState state) {
		this.state = state;
	}

	public void setPlayerName(String playerName) {
		playerNameLabel.setText(playerName);
		updatePlayerName();
	}

	private void updatePlayerName() {
		if (getPlayerType().isEmpty()) {
			playerNameLabel.setText("...");
		} else if (getPlayerType().isAi()) {
			ELobbyCivilisation civilisation = getCivilisation();
			if (civilisation != null) {
				playerNameLabel.setText(Labels.getString("player-name-" + getCivilisation().name() + "-" + getPlayerType().name()));
			} else {
				playerNameLabel.setText(Labels.getString("player-name-random"));
			}
		}
	}

	public void setTeam(int team) {
		if (team == 0) {
			throw new IllegalArgumentException("Team can not be less then 1");
		}
		JSettlersSwingUtil.set(teamComboBox, () -> teamComboBox.setSelectedIndex(team - 1));
	}

	public void setReady(boolean ready) {
		this.ready = ready;
		if (ready) {
			readyButton.setIcon(READY_IMAGE);
			readyButton.setPressedIcon(READY_PRESSED_IMAGE);
			readyButton.setDisabledIcon(READY_DISABLED_IMAGE);
		} else {
			readyButton.setIcon(NOT_READY_IMAGE);
			readyButton.setPressedIcon(NOT_READY_PRESSED_IMAGE);
			readyButton.setDisabledIcon(NOT_READY_DISABLED_IMAGE);
		}
	}

	public void setCivilisation(ELobbyCivilisation civilisation) {
		for (int i = 0; i < civilisationComboBox.getItemCount(); i++) {
			if (civilisationComboBox.getItemAt(i) == civilisation) {
				final int index = i;
				JSettlersSwingUtil.set(civilisationComboBox, () -> civilisationComboBox.setSelectedIndex(index));
				updatePlayerName();
				break;
			}
		}
	}

	public void setPlayerType(ELobbyPlayerType playerType) {
		for (int i = 0; i < typeComboBox.getItemCount(); i++) {
			if (typeComboBox.getItemAt(i) == playerType) {
				final int index = i;
				JSettlersSwingUtil.set(typeComboBox, () -> typeComboBox.setSelectedIndex(index));
				updatePlayerName();
				if (playerType.isAi()) {
					setReady(true);
				}
				break;
			}
		}
	}

	public void enable() {
		civilisationComboBox.setEnabled(true);
		teamComboBox.setEnabled(true);
		typeComboBox.setEnabled(true);
		readyButton.setEnabled(true);
	}

	public void enableLite() {
		civilisationComboBox.setEnabled(true);
		teamComboBox.setEnabled(true);
		typeComboBox.setEnabled(false);
		readyButton.setEnabled(true);
	}

	public void disable() {
		civilisationComboBox.setEnabled(false);
		teamComboBox.setEnabled(false);
		typeComboBox.setEnabled(false);
		readyButton.setEnabled(false);
	}

	private static Image getReadyButtonImage(int file, int seq, int imagenumber, boolean imageIsForEnabledState) {
		BufferedImage readyImage = ((SingleImage) ImageProvider.getInstance().getSettlerSequence(file, seq).getImage(imagenumber, null)).convertToBufferedImage();
		if (!imageIsForEnabledState) {
			readyImage = JSettlersSwingUtil.createDisabledImage(readyImage);
		}
		return readyImage.getScaledInstance(READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT, Image.SCALE_SMOOTH);
	}
}
