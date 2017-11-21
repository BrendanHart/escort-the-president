package escort.client.ui.menus.settings;

import escort.client.input.Inputs;
import escort.client.main.Client;
import escort.client.main.Scale;
import escort.client.ui.components.panels.DialogPanel;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.text.TextButton;
import escort.client.ui.menus.AbstractMenu;
import escort.client.ui.menus.MenuManager;

/**
 * A menu container for the settings panel
 * 
 * @author Ahmed Bhallo
 *
 */
public class SettingsMenu extends AbstractMenu {

	private DialogPanel leaveWarning;
	private final Client client;

	/**
	 * Instantiates a new menu container for the settings panel
	 * 
	 * @param inputs
	 *            The inputs object
	 * @param menuManager
	 *            The menu manager
	 */
	public SettingsMenu(Inputs inputs, MenuManager menuManager) {
		super(inputs, menuManager, "SETTINGS");
		client = menuManager.getClient();
		SettingsPanel panel = new SettingsPanel(inputs, menuManager.getClient(), getWidth(), getHeight());
		add(panel, 0, 0);
		TextButton rejectButton = new TextButton("Cancel", inputs);
		rejectButton.addListener(e -> client.setDialog(null));
		TextButton acceptButton = new TextButton("I understand", inputs);
		acceptButton.addListener(e -> {
			client.setDialog(null);
			if (menuManager.getClient().getNetworkManager().isInLobby()) {
				menuManager.displayLobbyMenu();
			} else {
				menuManager.displayMainMenu();
			}
		});
		Panel buttons = new Panel(inputs, 200 * Scale.factor, rejectButton.getHeight());
		buttons.add(rejectButton, 0, 0);
		buttons.add(acceptButton, buttons.getWidth() - acceptButton.getWidth(), 0);
		leaveWarning = new DialogPanel(inputs, client.getGameWidth(), client.getGameHeight(), "Are you sure?",
				"Any unsaved changes will be lost", buttons);
	}

	/**
	 * Called when the back button is pressed. Displays the leave warning
	 * message.
	 */
	@Override
	public void goBack() {
		client.setDialog(leaveWarning);
	}

}
