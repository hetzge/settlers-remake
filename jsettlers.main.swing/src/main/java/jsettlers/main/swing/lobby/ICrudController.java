package jsettlers.main.swing.lobby;

import java.util.Collection;

public interface ICrudController<T> {
	void create(T value);

	Collection<T> readAll();

	void update(T value);

	void delete(T value);
}
