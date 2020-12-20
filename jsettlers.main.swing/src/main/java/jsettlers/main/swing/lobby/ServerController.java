package jsettlers.main.swing.lobby;

public class ServerController extends CrudController<ServerModel> implements IServerController {
	public ServerController() {
		create(new ServerModel("localhost", 1234));
		create(new ServerModel("superhost", 3333));
	}
}
