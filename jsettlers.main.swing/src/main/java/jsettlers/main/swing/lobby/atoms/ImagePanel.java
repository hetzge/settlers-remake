package jsettlers.main.swing.lobby.atoms;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import jsettlers.main.swing.lookandfeel.ELFStyle;

public class ImagePanel extends JPanel {

	private BufferedImage image;

	public ImagePanel() {
		this(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB));
	}

	public ImagePanel(BufferedImage image) {
		this.image = image;
		putClientProperty(ELFStyle.KEY, ELFStyle.PANEL_DRAW_BG_CUSTOM);
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public void setDimension(Dimension dimension) {
		setSize(dimension);
		setMaximumSize(dimension);
		setPreferredSize(dimension);
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		graphics.drawImage(this.image, 0, 0, (int) getPreferredSize().getWidth(), (int) getPreferredSize().getHeight(), this);
	}
}
