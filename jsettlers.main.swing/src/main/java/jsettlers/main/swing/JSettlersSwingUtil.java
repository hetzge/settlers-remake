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
package jsettlers.main.swing;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeListener;

import jsettlers.graphics.image.SingleImage;
import jsettlers.graphics.map.draw.ImageProvider;
import jsettlers.logic.map.loading.MapLoader;
import jsettlers.logic.map.loading.newmap.MapFileHeader;

/**
 * @author Andreas Butti
 * @author codingberlin
 */
public final class JSettlersSwingUtil {

	private JSettlersSwingUtil() {
	}

	public static final Color DISABLE_COLOR = new Color(28, 34, 40, 150);

	public static BufferedImage createBufferedImageFrom(MapLoader mapLoader) {
		short[] data = mapLoader.getImage();
		int xOffset = MapFileHeader.PREVIEW_IMAGE_SIZE;
		BufferedImage img = new BufferedImage(MapFileHeader.PREVIEW_IMAGE_SIZE + xOffset,
				MapFileHeader.PREVIEW_IMAGE_SIZE, BufferedImage.TYPE_INT_ARGB);

		xOffset--;
		for (int y = 0; y < MapFileHeader.PREVIEW_IMAGE_SIZE; y++) {
			for (int x = 0; x < MapFileHeader.PREVIEW_IMAGE_SIZE; x++) {
				int index = y * MapFileHeader.PREVIEW_IMAGE_SIZE + x;
				jsettlers.common.Color c = jsettlers.common.Color.fromShort(data[index]);
				img.setRGB(x + xOffset, y, c.getARGB());
			}
			if (xOffset > 1 && (y % 2 == 0)) {
				xOffset--;
			}
		}

		return img;
	}

	public static Image createImage(int file, int seq, int imagenumber, boolean enabled, int width, int height) {
		BufferedImage readyImage = ((SingleImage) ImageProvider.getInstance().getSettlerSequence(file, seq).getImage(imagenumber, null)).convertToBufferedImage();
		if (!enabled) {
			readyImage = createDisabledImage(readyImage);
		}
		return readyImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
	}

	public static BufferedImage createDisabledImage(BufferedImage image) {
		return JSettlersSwingUtil.dye(image, DISABLE_COLOR);
	}

	/**
	 * code taken from: http://stackoverflow.com/questions/21382966/colorize-a-picture-in-java
	 */
	public static BufferedImage dye(BufferedImage image, Color color) {
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage dyed = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = dyed.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.setComposite(AlphaComposite.SrcAtop);
		g.setColor(color);
		g.fillRect(0, 0, w, h);
		g.dispose();
		return dyed;
	}

	/**
	 * Set without trigger action listener.
	 */
	public static void set(JComboBox<?> component, Runnable runnable) {
		final ActionListener[] actionListeners = component.getActionListeners();
		for (final ActionListener listener : actionListeners) {
			component.removeActionListener(listener);
		}
		final ItemListener[] itemListeners = component.getItemListeners();
		for (final ItemListener listener : itemListeners) {
			component.removeItemListener(listener);
		}
		try {
			runnable.run();
		} finally {
			for (final ActionListener listener : actionListeners) {
				component.addActionListener(listener);
			}
			for (final ItemListener listener : itemListeners) {
				component.addItemListener(listener);
			}
		}
	}

	/**
	 * Set without trigger action listener.
	 */
	public static void set(JTextField component, Runnable runnable) {
		final ActionListener[] listeners = component.getActionListeners();
		for (final ActionListener listener : listeners) {
			component.removeActionListener(listener);
		}
		try {
			runnable.run();
		} finally {
			for (final ActionListener listener : listeners) {
				component.addActionListener(listener);
			}
		}
	}

	/**
	 * Set without trigger action listener.
	 */
	public static void set(JSpinner component, Runnable runnable) {
		final ChangeListener[] listeners = component.getChangeListeners();
		for (final ChangeListener listener : listeners) {
			component.removeChangeListener(listener);
		}
		try {
			runnable.run();
		} finally {
			for (final ChangeListener listener : listeners) {
				component.addChangeListener(listener);
			}
		}
	}

	/**
	 * Set without trigger action listener.
	 */
	public static void set(JButton component, Runnable runnable) {
		final ChangeListener[] listeners = component.getChangeListeners();
		for (final ChangeListener listener : listeners) {
			component.removeChangeListener(listener);
		}
		try {
			runnable.run();
		} finally {
			for (final ChangeListener listener : listeners) {
				component.addChangeListener(listener);
			}
		}
	}
}
