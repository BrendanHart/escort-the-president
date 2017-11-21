package escort.client.ui.menus;

import java.io.IOException;

import escort.client.input.Inputs;
import escort.client.main.Client;
import escort.client.main.Scale;
import escort.client.ui.components.panels.DialogPanel;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.text.TextButton;
import escort.client.ui.components.text.TextLabel;
import escort.client.ui.components.text.TextUtils;
import escort.client.ui.utils.Colors;
import escort.common.network.Message;

/**
 * The main menu. Contains the button to take the client to other menus.
 * 
 * @author Ahmed Bhallo
 *
 */
public class MainMenu extends AbstractMenu {

	private DialogPanel exitWarning;
	private final Client client;

	/**
	 * Instantiates a new Main Menu
	 * 
	 * @param inputs
	 *            The inputs object
	 * @param menuManager
	 *            The menu manager
	 */
	public MainMenu(Inputs inputs, MenuManager menuManager) {
		super(inputs, menuManager, "MAIN MENU");
		initButtons();
		client = menuManager.getClient();
		TextButton cancelButton = new TextButton("Cancel", inputs);
		cancelButton.addListener(e -> client.setDialog(null));
		TextButton acceptButton = new TextButton("Quit", inputs);
		acceptButton.addListener(e -> System.exit(0));
		Panel buttons = new Panel(inputs, 120 * Scale.factor, cancelButton.getHeight());
		buttons.add(cancelButton, 0, 0);
		buttons.add(acceptButton, buttons.getWidth() - acceptButton.getWidth(), 0);
		exitWarning = new DialogPanel(inputs, client.getGameWidth(), client.getGameHeight(), "Are you sure?",
				"The game client will close.", buttons);
		displayBackButton = false;
		Panel firstTimeMessage = TextUtils.wrappedTextLabel(inputs, "Is this your first time? Learn how to play by clicking the button on the left. Famaliarise yourself with the controls in the settings panel.", 32, Colors.DARK_WHITE);
		firstTimeMessage.setBackground(Colors.UI_BG);
		add(firstTimeMessage, getWidth() - firstTimeMessage.getWidth(), 50*Scale.factor);
		
		TextLabel welcome =new TextLabel("", inputs, Colors.DARK_WHITE){
			@Override
			public void update() {
				super.update();
				setText("Welcome, "+menuManager.getClient().getNetworkManager().getUsername()+".",false);
			}
		};
		welcome.setWidth(firstTimeMessage.getWidth());
		welcome.setBackground(Colors.UI_BG);
		add(welcome, getWidth() - welcome.getWidth(), 0);
		
	}

	/**
	 * Initialises the buttons on the menu
	 */
	private void initButtons() {
		final int separation = 15 * Scale.factor;
		Panel container = new Panel(inputs, 0, 0);

		TextLabel serverList = new TextButton("Lobby list", inputs);
		serverList.addListener(e -> menuManager.displayLobbyList());
		container.add(serverList, 0, container.getHeight());
		container.pack();

		TextLabel howToPlay = new TextButton("How to play", inputs);
		container.add(howToPlay, 0, container.getHeight() + separation);
		howToPlay.addListener(e -> menuManager.displayHowToPlay());
		container.pack();

		TextLabel settings = new TextButton("Settings", inputs);
		container.add(settings, 0, container.getHeight() + separation);
		settings.addListener(e -> menuManager.displaySettingsMenu());
		container.pack();

		TextLabel disconnect = new TextButton("Disconnect from server", inputs);
		container.add(disconnect, 0, container.getHeight() + separation);
		disconnect.addListener(e -> leaveServer());
		container.pack();

		TextLabel exit = new TextButton("Exit", inputs);
		container.add(exit, 0, container.getHeight() + separation);
		exit.addListener(e -> displayExitWarning());
		container.pack();

		add(container, 0, center(container).y);
	}

	/**
	 * Called when the leave server button is pressed. Sends a leave request to
	 * the server.
	 */
	private void leaveServer() {
		menuManager.getClient().getNetworkManager().getSender().put(new Message(Message.EXIT, null));
		try {
			menuManager.getClient().getNetworkManager().getMsgControl().close();
		} catch (IOException e) {
		}
		menuManager.displaySetupMenu();
	}

	/**
	 * Called when the exit button is pressed. Shows the exit warning.
	 */
	private void displayExitWarning() {
		client.setDialog(exitWarning);
	}

	@Override
	public void goBack() {
		// Do nothing
	}

}