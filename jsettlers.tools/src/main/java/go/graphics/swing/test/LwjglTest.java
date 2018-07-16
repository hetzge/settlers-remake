/*******************************************************************************
 * Copyright (c) 2015
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package go.graphics.swing.test;

import go.graphics.EGeometryFormatType;
import go.graphics.EGeometryType;
import go.graphics.GLDrawContext;
import go.graphics.GeometryHandle;
import go.graphics.IllegalBufferException;
import go.graphics.UIPoint;
import go.graphics.area.Area;
import go.graphics.event.GOEvent;
import go.graphics.event.GOModalEventHandler;
import go.graphics.event.command.GOCommandEvent;
import go.graphics.event.mouse.GODrawEvent;
import go.graphics.event.mouse.GOHoverEvent;
import go.graphics.event.mouse.GOPanEvent;
import go.graphics.region.Region;
import go.graphics.region.RegionContent;
import go.graphics.swing.AreaContainer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.swing.JFrame;

/**
 * A lwjgl test class.
 * 
 * @author michael
 */
public class LwjglTest {

	private Area area;

	public static Area generateArea() {
		Region region = new Region(Region.POSITION_CENTER);

		Area area = new Area();
		area.set(region);
		RegionContent content = new TestContent(region);
		region.setContent(content);
		region.addEventHandler(content);
		return area;
	}

	private static class TestContent implements RegionContent {

		private int point_index = 0;
		private float[] pointx = new float[10];
		private float[] pointy = new float[10];
		private final Region region;

		private TestContent(Region region) {
			this.region = region;
		}

		private GOModalEventHandler handler = new GOModalEventHandler() {

			@Override
			public void phaseChanged(GOEvent event) {
			}

			@Override
			public void finished(GOEvent event) {
				eventDataChanged(event);
			}

			@Override
			public void aborted(GOEvent event) {

			}

			@Override
			public void eventDataChanged(GOEvent event) {
				UIPoint point = null;
				if (event instanceof GOHoverEvent) {
					point = ((GOHoverEvent) event).getHoverPosition();
				} else if (event instanceof GODrawEvent) {
					point = ((GODrawEvent) event).getDrawPosition();
				} else if (event instanceof GOPanEvent) {
					point = ((GOPanEvent) event).getPanDistance();
				} else if (event instanceof GOCommandEvent) {
					point = ((GOCommandEvent) event).getCommandPosition();
				}

				synchronized (pointLock) {
					if (point != null && point_index != 10) {
						pointx[point_index] = (float) point.getX();
						pointy[point_index] = (float) point.getY();
						point_index++;
					}
					region.requestRedraw();
				}
			}
		};

		@Override
		public void handleEvent(GOEvent event) {
			event.setHandler(handler);
			handler.eventDataChanged(event);
		}

		private final Object pointLock = new Object();

		private GeometryHandle pointGeometry = null;
		private ByteBuffer bfr = ByteBuffer.allocateDirect(4*2*2).order(ByteOrder.nativeOrder());

		@Override
		public void drawContent(GLDrawContext gl2, int width, int height) {
			gl2.color(1, 1, 1, 1);

			if(point_index < 2) return;

			if(pointGeometry == null) pointGeometry = gl2.generateGeometry(2, EGeometryFormatType.VertexOnly2D, true, null);

			synchronized (pointLock) {
				try {
					for (int i = 1; i != point_index; i++) {
						bfr.asFloatBuffer().put(new float[]{pointx[i - 1], pointy[i - 1], pointx[i], pointy[i]});
						gl2.updateGeometryAt(pointGeometry, 0, bfr);
						gl2.draw2D(pointGeometry, null, EGeometryType.LineStrip, 0, 2);
					}
				} catch (IllegalBufferException ex) {
					ex.printStackTrace();
				}
				pointx[0] = pointx[point_index-1];
				pointy[0] = pointy[point_index-1];
				point_index = 1;
			}
		}
	}

	/**
	 * creates a test window.
	 */
	protected LwjglTest() {
		JFrame window = new JFrame("Test");
		area = generateArea();

		AreaContainer content = new AreaContainer(area) {
			@Override
			public void draw() {
				area.drawArea(context);
			}
		};
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		window.add(content);
		window.setSize(500, 500);
		window.setVisible(true);
	}

	/**
	 * Starts the test program.
	 * 
	 * @param args
	 *            The arguments
	 */
	public static void main(String[] args) {
		new LwjglTest();
	}
}
