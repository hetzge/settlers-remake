package jsettlers.main.swing.lobby;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Function;

import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import jsettlers.main.swing.lobby.atoms.ComboBox;

public class EnumTableColumn<T extends Enum<T>> extends TableColumn {

	private final Class<T> clazz;
	private final Function<T, String> labelFunction;

	public EnumTableColumn(String header, int modelIndex, Class<T> clazz, Function<T, String> labelFunction) {
		super(modelIndex);
		this.clazz = clazz;
		this.labelFunction = labelFunction;
		setHeaderValue(header);
		setCellRenderer(new EnumTableCellRenderer(clazz, labelFunction));
	}

	private final class EnumTableCellRenderer implements TableCellRenderer {

		private final Class<T> clazz;
		private final Function<T, String> labelFunction;

		private EnumTableCellRenderer(Class<T> clazz, Function<T, String> labelFunction) {
			this.clazz = clazz;
			this.labelFunction = labelFunction;
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			final T[] values = EnumSet.allOf(clazz).toArray((T[]) Array.newInstance(clazz, 0));
			Arrays.sort(values, (a, b) -> labelFunction.apply(a).compareTo(labelFunction.apply(b)));
			final ComboBox<T> comboBox = new ComboBox<>(values, (T) value, labelFunction);
			comboBox.addActionListener(new EnumTableActionListener(comboBox, column, row, table));
			return comboBox;
		}

		private final class EnumTableActionListener implements ActionListener {
			private final JComboBox<T> comboBox;
			private final int column;
			private final int row;
			private final JTable table;

			private EnumTableActionListener(JComboBox<T> comboBox, int column, int row, JTable table) {
				this.comboBox = comboBox;
				this.column = column;
				this.row = row;
				this.table = table;
			}

			@Override
			public void actionPerformed(ActionEvent event) {
				table.setValueAt(comboBox.getSelectedItem(), row, column);
			}
		}
	}
}
