package jsettlers.main.swing.lobby;

import java.awt.Component;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;

import jsettlers.main.swing.JSettlersFrame;
import jsettlers.main.swing.lobby.pages.Page;

public class UiController {

	private final JSettlersFrame frame;
	private final ExecutorService executorService;

	public UiController(JSettlersFrame frame) {
		this.frame = frame;
		this.executorService = Executors.newSingleThreadExecutor();
	}

	public JSettlersFrame getFrame() {
		return frame;
	}

	public void setPage(String title, Component component) {
		this.frame.setNewContentPane(new Page(title, component));
		SwingUtilities.updateComponentTreeUI(this.frame);
	}

	public <T> void async(Supplier<T> request, Consumer<T> handler) {
		this.executorService.submit(() -> {
			final T value = request.get();
			SwingUtilities.invokeLater(() -> {
				handler.accept(value);
			});
		});
	}
}
