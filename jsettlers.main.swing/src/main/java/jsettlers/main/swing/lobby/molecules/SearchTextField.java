package jsettlers.main.swing.lobby.molecules;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import jsettlers.graphics.localization.Labels;
import jsettlers.main.swing.lobby.atoms.ClearIcon;
import jsettlers.main.swing.lobby.atoms.SearchIcon;
import jsettlers.main.swing.lookandfeel.ELFStyle;

/**
 * Search Text field
 */
public class SearchTextField extends JTextField {

	private static final long serialVersionUID = 1L;

	private final SearchIcon searchIcon;
	private final ClearIcon clearIcon;
	private final JLabel clearIconLabel;

	public SearchTextField(Consumer<String> listener) {
		this.searchIcon = new SearchIcon();
		this.clearIcon = new ClearIcon();
		setMargin(new Insets(2, searchIcon.getIconWidth() + 4, 2, clearIcon.getIconWidth() + 4));
		add(this.clearIconLabel = new JLabel(clearIcon));
		clearIconLabel.addMouseListener(new SearchTextFieldMouseAdapter());
		clearIconLabel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		getDocument().addDocumentListener(SearchTextFieldDocumentListener(listener));
		putClientProperty(ELFStyle.KEY, ELFStyle.TEXT_DEFAULT);
	}

	@Override
	public void layout() {
		int x = getWidth() - 4 - clearIcon.getIconWidth();
		int y = (this.getHeight() - searchIcon.getIconHeight()) / 2;
		clearIconLabel.setBounds(x, y, clearIcon.getIconWidth(), clearIcon.getIconHeight());
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int x = 4;
		searchIcon.paintIcon(this, g, x, (this.getHeight() - searchIcon.getIconHeight()) / 2);

		// color already set by search icon...
		if (getText().isEmpty()) {
			x += searchIcon.getIconWidth() + 6;
			int y = this.getHeight() - (this.getHeight() - g.getFontMetrics().getHeight()) / 2 - g.getFontMetrics().getDescent();
			g.drawString(Labels.getString("general.search"), x, y);
		}
	}

	private DocumentListener SearchTextFieldDocumentListener(Consumer<String> listener) {
		return new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent event) {
				listener.accept(getText());
			}

			@Override
			public void insertUpdate(DocumentEvent event) {
				listener.accept(getText());
			}

			@Override
			public void changedUpdate(DocumentEvent event) {
				listener.accept(getText());
			}
		};
	}

	private class SearchTextFieldMouseAdapter extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			setText("");
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			clearIcon.setHover(true);
			clearIconLabel.repaint();
		}

		@Override
		public void mouseExited(MouseEvent e) {
			clearIcon.setHover(false);
			clearIconLabel.repaint();
		}
	}
}
