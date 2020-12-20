package jsettlers.main.swing.lobby.atoms;

import java.awt.Component;
import java.util.function.Function;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import jsettlers.main.swing.lookandfeel.ELFStyle;

public class ComboBox<T> extends JComboBox<T> {
	private Function<T, String> labelFunction;

	public ComboBox(T[] values, T value, Function<T, String> labelFunction) {
		super(values);
		this.labelFunction = labelFunction;
		setRenderer(new EnumListCellRenderer());
		setSelectedItem(value);
		putClientProperty(ELFStyle.KEY, ELFStyle.COMBOBOX);
	}

	private final class EnumListCellRenderer implements ListCellRenderer<T> {
		@Override
		public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected, boolean cellHasFocus) {
			return new JLabel(labelFunction.apply(value));
		}
	}
}