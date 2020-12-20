package jsettlers.main.swing.lobby;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public class PlayersTable extends JTable {

	public PlayersTable() {
		super(new PlayersTableModel(), new PlayersTableColumnModel());
		setFillsViewportHeight(true);
		setCellSelectionEnabled(false);
		setDragEnabled(false);
	}

	public static interface IPlayersTableListener {

	}

	private static final class PlayersTableModel implements TableModel {

		private final Object[][] model;

		public PlayersTableModel() {
			this.model = new Object[][] {
					{ "AA", Bla.AA, "C", "D", "E" },
					{ "AB", Bla.BB, "C", "D", "E" }
			};
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			this.model[rowIndex][columnIndex] = aValue;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return this.model[rowIndex][columnIndex];
		}

		@Override
		public int getRowCount() {
			return this.model.length;
		}

		@Override
		public String getColumnName(int columnIndex) {
			return new String[] { "1", "2", "3", "4", "5" }[columnIndex];
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return Object.class;
		}

		@Override
		public void addTableModelListener(TableModelListener l) {
		}

		@Override
		public void removeTableModelListener(TableModelListener l) {
		}
	}

	private static final class PlayersTableColumnModel extends DefaultTableColumnModel {
		public PlayersTableColumnModel() {
			addColumn(Utils.createTableColumn(0, "AAA"));
			addColumn(new EnumTableColumn<>("BBB", 1, Bla.class, Bla::name));
		}
	}
}
