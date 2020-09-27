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
package jsettlers.main.swing.menu.joinpanel.slots;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import jsettlers.main.swing.menu.joinpanel.IJoinGameConnector;
import jsettlers.network.server.lobby.core.PlayerId;
import jsettlers.network.server.lobby.core.PlayerType;

public class PlayerSlot {

	public static final int READY_BUTTON_WIDTH = 40;
	public static final int READY_BUTTON_HEIGHT = 25;
	public static final ImageIcon READY_IMAGE = new ImageIcon(getReadyButtonImage(2, 17, 0, true));
	public static final ImageIcon READY_PRESSED_IMAGE = new ImageIcon(getReadyButtonImage(2, 17, 1, true));
	public static final ImageIcon READY_DISABLED_IMAGE = new ImageIcon(getReadyButtonImage(2, 17, 0, false));
	public static final ImageIcon NOT_READY_IMAGE = new ImageIcon(getReadyButtonImage(2, 18, 0, true));
	public static final ImageIcon NOT_READY_PRESSED_IMAGE = new ImageIcon(getReadyButtonImage(2, 18, 1, true));
	public static final ImageIcon NOT_READY_DISABLED_IMAGE = new ImageIcon(getReadyButtonImage(2, 18, 0, false));

	private final IJoinGameConnector connector;
	private final PlayerId playerId;
	private boolean ready;
	
	private final JLabel playerNameLabel;
	private final JComboBox<ECivilisation> civilisationComboBox;
	private final JComboBox<PlayerType> typeComboBox;
	private final JComboBox<Byte> slotComboBox;
	private final JComboBox<Byte> teamComboBox;
	private final JButton readyButton;

	public PlayerSlot(IJoinGameConnector connector, PlayerId playerId, int totalSlots, PlayerType[] playerTypes) {
		this.connector = connector;
		this.playerId = playerId;
		
		// components
		this.playerNameLabel = new JLabel();
		this.civilisationComboBox = new JComboBox<>();
		this.typeComboBox = new JComboBox<>();
		this.typeComboBox.removeAll();
		this.slotComboBox = new JComboBox<>();
		this.teamComboBox = new JComboBox<>();
		this.readyButton = new JButton();

		// style
		readyButton.setSize(new Dimension(READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));
		readyButton.setMaximumSize(new Dimension(READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));
		readyButton.setPreferredSize(new Dimension(READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));
		readyButton.setMinimumSize(new Dimension(READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));
		playerNameLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_DYNAMIC);
		teamComboBox.putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);
		slotComboBox.putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);
		typeComboBox.putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);
		civilisationComboBox.putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);

		// init
		for (ECivilisation civilisation : ECivilisation.values()) {
			civilisationComboBox.addItem(civilisation);
		}
		for (byte i = 1; i < totalSlots + 1; i++) {
			slotComboBox.addItem(i);
			teamComboBox.addItem(i);
		}
		for (PlayerType playerType : playerTypes) {
			this.typeComboBox.addItem(playerType);
		}

		// listener
		typeComboBox.addActionListener(this::onChange);
		civilisationComboBox.addActionListener(this::onChange);
		slotComboBox.addActionListener(this::onChange);
		readyButton.addActionListener(event -> {
			setReady(!isReady());
			this.onChange(event);
		});
		teamComboBox.addActionListener(this::onChange);
	}

	public void addTo(JPanel panel, int row) {
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = row + 1;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(readyButton, constraints);
		constraints.gridx = 1;
		constraints.gridy = row + 1;
		constraints.gridwidth = 4;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(playerNameLabel, constraints);
		constraints.gridx = 5;
		constraints.gridy = row + 1;
		constraints.gridwidth = 2;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(civilisationComboBox, constraints);
		constraints = new GridBagConstraints();
		constraints.gridx = 7;
		constraints.gridy = row + 1;
		constraints.gridwidth = 4;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(typeComboBox, constraints);
		constraints = new GridBagConstraints();
		constraints.gridx = 11;
		constraints.gridy = row + 1;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(slotComboBox, constraints);
		constraints = new GridBagConstraints();
		constraints.gridx = 12;
		constraints.gridy = row + 1;
		constraints.gridwidth = 1;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(teamComboBox, constraints);
	}

	private void onChange(ActionEvent event) {
		sendPlayerUpdate();
		updateAiPlayerName();
	}

	private void sendPlayerUpdate() {
		connector.updatePlayer(playerId, getPlayerType(), getCivilisation(), getTeam(), isReady());
	}

	private void updateAiPlayerName() {
		if (getPlayerType().isAi()) {
			ECivilisation civilisation = getCivilisation();
			if (civilisation != null) {
				setPlayerName(Labels.getString("player-name-" + getCivilisation().name() + "-" + getPlayerType().name()));
			} else {
				setPlayerName(Labels.getString("player-name-random"));
			}
		}
	}

	public PlayerId getPlayerId() {
		return playerId;
	}

	public int getSlot() {
		return slotComboBox.getSelectedIndex();
	}

	public int getTeam() {
		return teamComboBox.getSelectedIndex() + 1;
	}

	public PlayerType getPlayerType() {
		return (PlayerType) typeComboBox.getSelectedItem();
	}

	public ECivilisation getCivilisation() {
		return (ECivilisation) civilisationComboBox.getSelectedItem();
	}

	public boolean isReady() {
		return ready;
	}

	public void setPlayerName(String playerName) {
		playerNameLabel.setText(playerName);
	}

	public void setTeam(int team) {
		if (team == 0) {
			throw new IllegalArgumentException("Team can not be less then 1");
		}
		set(teamComboBox, () -> teamComboBox.setSelectedIndex(team - 1));
	}

	public void setSlot(int slot) {
		set(slotComboBox, () -> slotComboBox.setSelectedIndex(slot));
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

	public void setCivilisation(ECivilisation civilisation) {
		for (int i = 0; i < civilisationComboBox.getItemCount(); i++) {
			if (civilisationComboBox.getItemAt(i) == civilisation) {
				final int index = i;
				set(civilisationComboBox, () -> civilisationComboBox.setSelectedIndex(index));
				updateAiPlayerName();
				break;
			}
		}
	}

	public void setPlayerType(PlayerType playerType) {
		for (int i = 0; i < typeComboBox.getItemCount(); i++) {
			if (typeComboBox.getItemAt(i) == playerType) {
				final int index = i;
				set(typeComboBox, () -> typeComboBox.setSelectedIndex(index));
				updateAiPlayerName();
				if (playerType.isAi()) {
					setReady(true);
				}
				break;
			}
		}
	}

	public void enable() {
		slotComboBox.setEnabled(true);
		civilisationComboBox.setEnabled(true);
		teamComboBox.setEnabled(true);
		typeComboBox.setEnabled(true);
		readyButton.setEnabled(true);
	}

	public void disable() {
		slotComboBox.setEnabled(false);
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

	private static void set(JComboBox<?> component, Runnable runnable) {
		final ActionListener[] listeners = component.getActionListeners();
		for (final ActionListener listener : listeners) {
			component.removeActionListener(listener);
		}
		try {
			runnable.run();
		} finally {
			for (final ActionListener listener : listeners)
				component.addActionListener(listener);
		}
	}

}
