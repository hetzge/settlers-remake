package jsettlers.main.swing.lobby.atoms;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.Icon;

/**
 * Icon to display search symbol
 */
public class SearchIcon implements Icon {

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D graphics = (Graphics2D) g;
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		Stroke oldStroke = graphics.getStroke();

		graphics.setColor(Color.LIGHT_GRAY);
		graphics.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		graphics.drawOval(x + 3, y + 3, 12, 12);
		graphics.drawLine(x + 15, y + 15, x + 20, y + 20);

		graphics.setStroke(oldStroke);
	}

	@Override
	public int getIconWidth() {
		return 22;
	}

	@Override
	public int getIconHeight() {
		return 22;
	}
}
