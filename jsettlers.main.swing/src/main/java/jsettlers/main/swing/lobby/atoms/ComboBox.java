package jsettlers.main.swing.lobby.atoms;

import java.util.Arrays;
import java.util.Objects;
import java.util.Vector;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.swing.JComboBox;

import jsettlers.main.swing.lookandfeel.ELFStyle;

public class ComboBox<T> extends JComboBox<ComboBox.Item<T>> {
	private final Function<T, String> labelFunction;

	public ComboBox(T[] values, T value, Function<T, String> labelFunction) {
		super(new Vector<>(Arrays.stream(values).map(it -> new Item<>(it, labelFunction)).collect(Collectors.toList())));
		this.labelFunction = labelFunction;
		setSelectedItem(value);
		putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);
	}

	public T getValue() {
		return ((Item<T>) getSelectedItem()).value;
	}

	public void setValue(T value) {
		setSelectedItem(new Item<T>(value, labelFunction));
	}

	public static class Item<T> {
		private final T value;
		private final Function<T, String> labelFunction;

		public Item(T value, Function<T, String> labelFunction) {
			this.value = value;
			this.labelFunction = labelFunction;
		}

		public T getValue() {
			return value;
		}

		@Override
		public int hashCode() {
			return Objects.hash(value);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Item)) {
				return false;
			}
			final Item<?> other = (Item<?>) obj;
			return Objects.equals(value, other.value);
		}

		@Override
		public String toString() {
			return labelFunction.apply(value);
		}
	}
}
