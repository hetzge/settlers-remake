package jsettlers.main.swing.lobby;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import jsettlers.main.swing.JSettlersFrame;

public class Ui {

	private final JFrame frame;
	private final ExecutorService executorService;

	public Ui(JFrame frame) {
		this.frame = frame;
		this.executorService = Executors.newSingleThreadExecutor();
	}

	public JSettlersFrame getFrame() {
		// TODO
		return (JSettlersFrame) frame;
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
