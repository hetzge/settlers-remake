/*******************************************************************************
 * Copyright (c) 2015-2018
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
package go.graphics.swing;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import java.awt.BorderLayout;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import go.graphics.DrawmodeListener;
import go.graphics.RedrawListener;
import go.graphics.area.Area;
import go.graphics.event.GOEvent;
import go.graphics.event.GOEventHandlerProvider;
import go.graphics.swing.contextcreator.BackendSelector;
import go.graphics.swing.contextcreator.EBackendType;
import go.graphics.swing.contextcreator.ContextCreator;
import go.graphics.swing.opengl.LWJGLDrawContext;

/**
 * This class lets you embed areas into swing components.
 * 
 * @author michael
 * @author paul
 */
public class AreaContainer extends JPanel implements RedrawListener, GOEventHandlerProvider {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8204496712425576430L;
	protected final Area area;

	private ContextCreator cc;
	private LWJGLDrawContext context;

	/**
	 * creates a new area container
	 * 
	 * @param area
	 *            The area to display
	 */
	public AreaContainer(Area area) {
		this(area, EBackendType.DEFAULT);
	}

	public AreaContainer(Area area, EBackendType backend) {
		this.area = area;
		this.setLayout(new BorderLayout());

		/*if(platform == Platform.LINUX) {
			// linux(x11) only
			// if your screen is flickering try "-Dsun.awt.noerasebackground=true" or System.setProperty("sun.awt.noerasebackground", "true");
			cc = new GLXContextCreator(this);
		} else if(platform == Platform.WINDOWS) {
			cc = new WGLContextCreator(this);
		} else {
			// laggy, slow and creates its own window, but works on osx and linux(wayland) too.
			cc = new GLFWContextCreator(this);
		}*/
		try {
			cc = BackendSelector.createBackend(this, backend);
			cc.init();
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not create opengl context through " + backend.cc_name + "\nPress ok to exit");
			System.exit(1);
		}

		if(cc instanceof DrawmodeListener) {
			area.setDrawmodeListener((DrawmodeListener) cc);
		}


		area.addRedrawListener(this);

	}

	public void resize_gl(int width, int height) {
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		// coordinate system origin at lower left with width and height same as
		// the window
		GL11.glOrtho(0, width, 0, height, -1, 1);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		GL11.glViewport(0, 0, width, height);
		area.setWidth(width);
		area.setHeight(height);

	}

	public void init() {
		context = new LWJGLDrawContext(GL.createCapabilities());
	}

	public void draw() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
		GL11.glLoadIdentity();

		context.startFrame();
		area.drawArea(context);
	}


	/**
	 * Disposes all textures / buffers that were allocated by this context.
	 */
	public void disposeAll() {
		cc.stop();
		if (context != null) {
			context.disposeAll();
		}
		context = null;
	}

	@Override
	public void requestRedraw() {
		cc.repaint();
	}

	/**
	 * Forward the focus call to the Input canvas
	 */
	@Override
	public void requestFocus() {
		cc.requestFocus();
	}

	@Override
	public void handleEvent(GOEvent event) {
		area.handleEvent(event);
	}
}
