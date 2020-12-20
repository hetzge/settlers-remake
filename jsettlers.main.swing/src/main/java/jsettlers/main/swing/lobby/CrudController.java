package jsettlers.main.swing.lobby;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CrudController<T> implements ICrudController<T> {

	private final List<T> values;

	public CrudController() {
		this.values = new ArrayList<>();
	}

	@Override
	public void create(T value) {
		this.values.add(value);
	}

	@Override
	public Collection<T> readAll() {
		return values;
	}

	@Override
	public void update(T value) {
		final int index = this.values.indexOf(value);
		if (index != -1) {
			this.values.set(index, value);
		}
	}

	@Override
	public void delete(T value) {
		this.values.remove(value);
	}

}
