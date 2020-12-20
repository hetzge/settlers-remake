package jsettlers.main.swing.lobby;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class ServersPanel extends JPanel {

	private final Ui ui;
	private final IServerController controller;
	private final IListener listener;

	private final ServerList serverList;
	private final ServerFormPanel serverFormPanel;

	public ServersPanel(Ui ui, IServerController controller, IListener listener) {
		this.ui = ui;
		this.controller = controller;
		this.listener = listener;

		this.serverList = new ServerList();
		this.serverFormPanel = new ServerFormPanel();

		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(this.serverList);
		add(this.serverFormPanel);

		refreshModel();
	}

	private void refreshModel() {
		this.ui.async(controller::readAll, this.serverList::setServers);
	}

	private class ServerList extends JList<ServerModel> {
		public ServerList() {
			super(new DefaultListModel<>());
			setCellRenderer(new ServerCellRenderer());
			addListSelectionListener(new ServerSelectionListener());
		}

		public void setServers(Collection<ServerModel> servers) {
			final DefaultListModel<ServerModel> listModel = (DefaultListModel<ServerModel>) getModel();
			listModel.removeAllElements();
			servers.forEach(listModel::addElement);
		}

		private class ServerCellRenderer implements ListCellRenderer<ServerModel> {
			@Override
			public Component getListCellRendererComponent(JList<? extends ServerModel> list, ServerModel value, int index, boolean isSelected, boolean cellHasFocus) {
				return new ServerPanel(value);
			}
		}

		private class ServerSelectionListener implements ListSelectionListener {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				if (!event.getValueIsAdjusting()) {
					listener.onServerSelected(getModel().getElementAt(event.getFirstIndex()));
				}
			}
		}
	}

	private class ServerFormPanel extends JPanel {

		private final JTextField hostTextField;
		private final JTextField portTextField;
		private final JButton submitButton;

		public ServerFormPanel() {
			this.hostTextField = new JTextField();
			this.portTextField = new JTextField();
			this.submitButton = new JButton("Submit");
			this.submitButton.addActionListener(this::submit);

			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
			add(this.hostTextField);
			add(this.portTextField);
			add(this.submitButton);
		}

		private void submit(ActionEvent event) {
			final String text = hostTextField.getText();
			final int parseInt = Integer.parseInt(portTextField.getText());
			ServersPanel.this.controller.create(new ServerModel(text, parseInt));
			refreshModel();
		}
	}

	public static interface IListener {
		void onServerSelected(ServerModel server);
	}
}
