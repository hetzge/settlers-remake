package jsettlers.main.swing.lobby;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

public final class Utils {

	private Utils() {
	}

	public static ImageIcon createImageIcon(Color color, int size) {
		final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		final Graphics2D graphics = image.createGraphics();
		graphics.setPaint(color);
		graphics.fillRect(0, 0, size, size);
		return new ImageIcon(image);
	}
}
