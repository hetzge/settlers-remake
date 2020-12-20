package jsettlers.main.swing.lobby;

import javax.swing.table.TableColumn;

public final class Utils {

	private Utils() {
	}

	public static TableColumn createTableColumn(int modelIndex, String header) {
		final TableColumn column = new TableColumn(modelIndex);
		column.setHeaderValue(header);
		return column;
	}
}
