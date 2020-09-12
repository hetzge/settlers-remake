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

import jsettlers.common.ai.EPlayerType;
import jsettlers.common.menu.IJoinPhaseMultiplayerGameConnector;
import jsettlers.common.menu.IMultiplayerPlayer;
import jsettlers.common.player.ECivilisation;
import jsettlers.graphics.image.SingleImage;
import jsettlers.graphics.localization.Labels;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.main.swing.JSettlersSwingUtil;
import jsettlers.main.swing.lookandfeel.ELFStyle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * @author codingberlin
 */
public class PlayerSlot {

	public static final int READY_BUTTON_WIDTH = 40;
	public static final int READY_BUTTON_HEIGHT = 25;
	public static final ImageIcon READY_IMAGE = new ImageIcon(getReadyButtonImage(2, 17, 0, true));
	public static final ImageIcon READY_PRESSED_IMAGE = new ImageIcon(getReadyButtonImage(2, 17, 1, true));
	public static final ImageIcon READY_DISABLED_IMAGE = new ImageIcon(getReadyButtonImage(2, 17, 0, false));
	public static final ImageIcon NOT_READY_IMAGE = new ImageIcon(getReadyButtonImage(2, 18, 0, true));
	public static final ImageIcon NOT_READY_PRESSED_IMAGE = new ImageIcon(getReadyButtonImage(2, 18, 1, true));
	public static final ImageIcon NOT_READY_DISABLED_IMAGE = new ImageIcon(getReadyButtonImage(2, 18, 0, false));

	private final JLabel playerNameLabel;
	private final JComboBox<CivilisationUiWrapper> civilisationComboBox;
	private final JComboBox<PlayerTypeUiWrapper> typeComboBox;
	private final JComboBox<Byte> slotComboBox;
	private final JComboBox<Byte> teamComboBox;
	private final JButton readyButton;
	private byte oldSlotValue;
	private ISlotListener slotListener;
	private boolean isAvailable;
	private boolean isReady = true;
	private IJoinPhaseMultiplayerGameConnector gameConnector;

	public PlayerSlot() {
		playerNameLabel = new JLabel();
		civilisationComboBox = new JComboBox<>();
		typeComboBox = new JComboBox<>();
		slotComboBox = new JComboBox<>();
		teamComboBox = new JComboBox<>();
		readyButton = new JButton();
		isAvailable = true;

		setStyle();
		localize();
		addListener();
		initializeComboBoxes();
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

	private void setStyle() {
		readyButton.setSize(new Dimension(READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));
		readyButton.setMaximumSize(new Dimension(READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));
		readyButton.setPreferredSize(new Dimension(READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));
		readyButton.setMinimumSize(new Dimension(READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT));
		playerNameLabel.putClientProperty(ELFStyle.KEY, ELFStyle.LABEL_DYNAMIC);
		teamComboBox.putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);
		slotComboBox.putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);
		typeComboBox.putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);
		civilisationComboBox.putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);
		updateReadyButtonStyle();
	}

	private void updateReadyButtonStyle() {
		if (isReady()) {
			readyButton.setIcon(READY_IMAGE);
			readyButton.setPressedIcon(READY_PRESSED_IMAGE);
			readyButton.setDisabledIcon(READY_DISABLED_IMAGE);
		} else {
			readyButton.setIcon(NOT_READY_IMAGE);
			readyButton.setPressedIcon(NOT_READY_PRESSED_IMAGE);
			readyButton.setDisabledIcon(NOT_READY_DISABLED_IMAGE);
		}
	}

	private void localize() {
	}

	private void addListener() {
		typeComboBox.addActionListener(e -> updateAiPlayerName());
		civilisationComboBox.addActionListener(e -> {
			updateAiPlayerName();

			if (gameConnector != null) {
				gameConnector.setCivilisation(getCivilisation());
			}
		});
		slotComboBox.addActionListener(e -> {
			if (slotListener != null) {
				slotListener.slotHasChanged(oldSlotValue, getSlot());
			}
			oldSlotValue = getSlot();
		});
		readyButton.addActionListener(e -> {
			setReady(!isReady());
			if (gameConnector != null) {
				gameConnector.setReady(isReady());
			}
		});
		teamComboBox.addActionListener(e -> {
			if (gameConnector != null) {
				gameConnector.setTeamId(getTeam());
			}
		});
	}

	private void updateAiPlayerName() {
		if (typeComboBox.getSelectedItem() == null || civilisationComboBox.getSelectedItem() == null) {
			return;
		}

		if (getPlayerType().isAi()) {
			ECivilisation civilisation = getCivilisation();
			if (civilisation != null) {
				setPlayerName(Labels.getString("player-name-" + getCivilisation().name() + "-" + ((PlayerTypeUiWrapper) typeComboBox.getSelectedItem()).getPlayerType().name()));
			} else {
				setPlayerName(Labels.getString("player-name-random"));
			}
		}
	}

	private void initializeComboBoxes() {
		civilisationComboBox.addItem(new CivilisationUiWrapper());
		for (ECivilisation civilisation : ECivilisation.values())
			civilisationComboBox.addItem(new CivilisationUiWrapper(civilisation));
	}
	
	public void set(IMultiplayerPlayer player) {
		setPlayerName(player.getName());
		setPlayerType(EPlayerType.HUMAN, false);
		setTeam(player.getTeamId());
		setReady(player.isReady());
		setCivilisation(player.getCivilisation());
	}

	public void setPlayerName(String playerName) {
		playerNameLabel.setText(playerName);
	}

	public void setPossibleTypes(EPlayerType[] playerTypes) {
		typeComboBox.removeAll();
		Arrays.asList(playerTypes)
				.stream()
				.map(PlayerTypeUiWrapper::new)
				.forEach(typeComboBox::addItem);
	}

	public void setSlotAndTeams(Byte slotAndTeamCount) {
		slotComboBox.removeAllItems();
		teamComboBox.removeAllItems();
		for (byte i = 1; i < slotAndTeamCount + 1; i++) {
			slotComboBox.addItem(i);
			teamComboBox.addItem(i);
		}
	}

	public void setSlot(byte slot) {
		slotComboBox.setSelectedIndex(slot);
		oldSlotValue = slot;
	}

	public void setTeam(byte team) {
		setTeam(team, true);
	}

	public void setTeam(byte team, boolean enabled) {
		teamComboBox.setSelectedIndex(team);
		teamComboBox.setEnabled(enabled);
	}

	public void setAvailable(boolean available) {
		isAvailable = available;
	}

	public byte getSlot() {
		return (byte) slotComboBox.getSelectedIndex();
	}

	public byte getTeam() {
		return (byte) teamComboBox.getSelectedIndex();
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public EPlayerType getPlayerType() {
		return ((PlayerTypeUiWrapper) typeComboBox.getSelectedItem()).getPlayerType();
	}

	public ECivilisation getCivilisation() {
		return ((CivilisationUiWrapper) civilisationComboBox.getSelectedItem()).getCivilisation();
	}

	public void setSlotListener(ISlotListener slotListener) {
		this.slotListener = slotListener;
	}

	public boolean isReady() {
		return isReady;
	}

	public void setReady(boolean ready) {
		isReady = ready;
		updateReadyButtonStyle();
	}

	public void disableAllInputs() {
		slotComboBox.setEnabled(false);
		civilisationComboBox.setEnabled(false);
		teamComboBox.setEnabled(false);
		typeComboBox.setEnabled(false);
	}

	public void setReadyButtonEnabled(boolean isEnabled) {
		readyButton.setEnabled(isEnabled);
	}

	public void setCivilisation(ECivilisation civilisation) {
		for (int i = 0; i < civilisationComboBox.getItemCount(); i++) {
			if (civilisationComboBox.getItemAt(i).getCivilisation() == civilisation) {
				civilisationComboBox.setSelectedIndex(i);
				break;
			}
		}
	}

	public void setPlayerType(EPlayerType playerType, boolean enabled) {
		for (int i = 0; i < typeComboBox.getItemCount(); i++) {
			if (typeComboBox.getItemAt(i).getPlayerType() == playerType) {
				typeComboBox.setSelectedIndex(i);
				break;
			}
		}

		typeComboBox.setEnabled(enabled);
		updateAiPlayerName();

		if (playerType.isAi()) {
			setReady(true);
		}
	}

	public void setGameConnector(IJoinPhaseMultiplayerGameConnector gameConnector) {
		this.gameConnector = gameConnector;
	}

	private static Image getReadyButtonImage(int file, int seq, int imagenumber, boolean imageIsForEnabledState) {
		BufferedImage readyImage = ((SingleImage) ImageProvider.getInstance().getSettlerSequence(file, seq).getImage(imagenumber, null)).convertToBufferedImage();
		if (!imageIsForEnabledState) {
			readyImage = JSettlersSwingUtil.createDisabledImage(readyImage);
		}
		return readyImage.getScaledInstance(READY_BUTTON_WIDTH, READY_BUTTON_HEIGHT, Image.SCALE_SMOOTH);
	}
}
