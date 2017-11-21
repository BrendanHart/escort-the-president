package escort.client.ui.menus.lobby;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import escort.client.input.Inputs;
import escort.client.main.Client;
import escort.client.main.Scale;
import escort.client.ui.components.ToggleButton;
import escort.client.ui.components.panels.DialogPanel;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.panels.ScrollableList;
import escort.client.ui.components.text.InputField;
import escort.client.ui.components.text.TextButton;
import escort.client.ui.components.text.TextLabel;
import escort.client.ui.menus.AbstractMenu;
import escort.client.ui.menus.MenuManager;
import escort.client.ui.utils.Colors;
import escort.common.network.Message;
import escort.common.systime.SystemTime;

/**
 * The list of all lobbies. Automatically refreshes the list every 3 seconds by
 * sending a request to the server.
 * 
 * @author Ahmed Bhallo
 *
 */
public class LobbyList extends AbstractMenu {

	private final Panel columns;
	private final ScrollableList list;
	private final Panel buttons;

	private final Inputs inputs;

	public final int LOBBY_NAME_X = 0;
	public final int OWNER_X = LOBBY_NAME_X + 140 * Scale.factor;
	public final int PLAYERS_X = OWNER_X + 90 * Scale.factor;
	public final int LOCKED_X = PLAYERS_X + 55 * Scale.factor;
	public final int IN_GAME_X = LOCKED_X + 55 * Scale.factor;

	private static final int LOBBY_LIST_REFRESH_MS = 3000;
	private long timeSinceLastUpdate = -1;

	private final Map<Integer, LobbyListEntry> entries = new ConcurrentHashMap<>();
	private final Client client;
	private InputField passwordInputField;
	private DialogPanel passwordDialog;

	private int selectedLobbyID = -1;

	private boolean hideFull = false;
	private boolean hideLocked = false;
	private boolean hideInGame = false;

	/**
	 * Instnatiates a new lobby list menu
	 * 
	 * @param inputs
	 *            The inputs object
	 * @param menuManager
	 *            The menu manager
	 */
	public LobbyList(Inputs inputs, MenuManager menuManager) {
		super(inputs, menuManager, "LOBBY LIST");
		this.client = menuManager.getClient();
		this.inputs = inputs;

		columns = new Panel(inputs, getWidth(), 24 * Scale.factor);
		initColumnHeadings();
		add(columns, 0, 0);

		buttons = new Panel(inputs, getWidth(), 40 * Scale.factor);
		initButtons();

		list = new ScrollableList(inputs, getWidth(), getHeight() - buttons.getHeight() - columns.getHeight());
		list.setBackground(Colors.VERY_LIGHT_GRAY);
		add(list, 0, columns.getHeight());

		add(buttons, 0, columns.getHeight() + list.getHeight());
		initPasswordDialog();
	}

	/**
	 * Initialises the column headings and adds them to the columns container
	 */
	private void initColumnHeadings() {
		columns.setBackground(Colors.BLUE);

		TextLabel lobbyNameCol = new TextLabel("Lobby name", inputs);
		columns.add(lobbyNameCol, LOBBY_NAME_X, columns.center(lobbyNameCol).y);

		TextLabel ownerCol = new TextLabel("Owner", inputs);
		columns.add(ownerCol, OWNER_X, columns.center(ownerCol).y);

		TextLabel playersCol = new TextLabel("Players", inputs);
		columns.add(playersCol, PLAYERS_X, columns.center(playersCol).y);

		TextLabel lockedCol = new TextLabel("Locked", inputs);
		columns.add(lockedCol, LOCKED_X, columns.center(lockedCol).y);

		TextLabel ingameCol = new TextLabel("In game?", inputs);
		columns.add(ingameCol, IN_GAME_X, columns.center(ingameCol).y);
	}

	/**
	 * Initialises the buttons at the bottom.
	 */
	private void initButtons() {
		int seperation = 12 * Scale.factor;
		int x = 0;
		TextLabel newLobbyButton = new TextButton("Create new", inputs);
		newLobbyButton.addListener(e -> menuManager.displayLobbyCreation());
		buttons.add(newLobbyButton, x, buttons.center(newLobbyButton).y);
		x += newLobbyButton.getWidth() + seperation;
		TextLabel join = new TextButton("Join", inputs) {
			@Override
			public void update() {
				super.update();
				if (getSelectedLobbyID() == -1) {
					setEnabled(false);
					return;
				}
				LobbyListEntry entry = entries.get(getSelectedLobbyID());
				if (entry == null) {
					setEnabled(false);
					return;
				}
				boolean joinable = !entry.inGame && entry.players <= LobbyListEntry.MAX_PLAYERS;
				setEnabled(joinable);
			}
		};
		join.addListener(e -> joinSelectedLobby());
		buttons.add(join, x, buttons.center(join).y);
		x += join.getWidth() + seperation;
		ToggleButton hideFullButton = new ToggleButton(inputs, "Hide full");
		hideFullButton.addListener(e -> {
			hideFull = !hideFull;
			requestRefresh();
		});
		buttons.add(hideFullButton, x, buttons.center(hideFullButton).y);
		x += hideFullButton.getWidth() + seperation;
		ToggleButton hideLockedButton = new ToggleButton(inputs, "Hide locked");
		hideLockedButton.addListener(e -> {
			hideLocked = !hideLocked;
			requestRefresh();
		});
		buttons.add(hideLockedButton, x, buttons.center(hideLockedButton).y);
		x += hideLockedButton.getWidth() + seperation;
		ToggleButton hideIngameButton = new ToggleButton(inputs, "Hide in game");
		hideIngameButton.addListener(e -> {
			hideInGame = !hideInGame;
			requestRefresh();
		});
		buttons.add(hideIngameButton, x, buttons.center(hideIngameButton).y);
		x += hideIngameButton.getWidth() + seperation;
	}

	/**
	 * Initialises the password dialog for when the client connects to a locked
	 * lobby.
	 */
	private void initPasswordDialog() {
		passwordInputField = new InputField(inputs, 20);
		passwordInputField.setPromptText("Enter the password");
		passwordInputField.addListener(e -> connectWithPassword());
		Panel passwordContent = new Panel(inputs, 180 * Scale.factor, 50 * Scale.factor);
		TextButton rejectButton = new TextButton("Cancel", inputs);
		rejectButton.addListener(e -> menuManager.getClient().setDialog(null));
		TextButton acceptButton = new TextButton("Join", inputs);
		acceptButton.addListener(e -> connectWithPassword());
		Panel buttons = new Panel(inputs, passwordContent.getWidth(), rejectButton.getHeight());
		buttons.add(rejectButton, 0, 0);
		buttons.add(acceptButton, buttons.getWidth() - acceptButton.getWidth(), 0);
		passwordContent.add(passwordInputField, passwordContent.center(passwordInputField).x, 0);
		passwordContent.add(buttons, 0, passwordContent.getHeight() - buttons.getHeight());
		passwordDialog = new DialogPanel(inputs, client.getGameWidth(), client.getGameHeight(),
				"This lobby is password protected", "Enter the password for this lobby.", passwordContent);
	}

	/**
	 * Called when the client connects to a locked lobby
	 */
	private void connectWithPassword() {
		menuManager.getClient().getNetworkManager().getSender().put(new Message(Message.LOBBY_JOIN,
				new int[] { selectedLobbyID }, new String[] { passwordInputField.getText() }));
		passwordInputField.setText("");
	}

	/**
	 * Called when the client joins a selected lobby
	 */
	private void joinSelectedLobby() {
		LobbyListEntry entry = entries.get(selectedLobbyID);
		if (entry.locked) {
			client.setDialog(passwordDialog);
		} else {
			menuManager.getClient().getNetworkManager().getSender()
					.put(new Message(Message.LOBBY_JOIN, new int[] { selectedLobbyID }, new String[] { "" }));
		}
	}

	/**
	 * Requests a refresh every 3 seconds of the lobby list
	 */
	@Override
	public void update() {
		super.update();
		if (SystemTime.milliTime() - timeSinceLastUpdate >= LOBBY_LIST_REFRESH_MS) {
			requestRefresh();
		}
	}

	/**
	 * Sends a message to the server to refresh the lobby list
	 */
	private void requestRefresh() {
		menuManager.getClient().getNetworkManager().getSender().put(new Message(Message.LOBBY_LIST, null, null));
	}

	/**
	 * Updates the list based on the result from the server
	 * 
	 * @param listUpdateValues
	 *            The lobby integer values
	 * @param listUpdateTexts
	 *            The lobby names
	 */
	public void updateList(int[] listUpdateValues, String[] listUpdateTexts) {
		int selectedIndex = getSelectedLobbyID();
		int yOffset = list.getYOffset();
		entries.clear();
		list.clear();
		for (int i = 0; i < listUpdateTexts.length / 2; i++) {
			String lobbyName = listUpdateTexts[i * 2];
			String lobbyOwner = listUpdateTexts[i * 2 + 1];
			int lobbyID = listUpdateValues[i * 4];
			boolean passwordProtected = listUpdateValues[i * 4 + 1] != 0;
			boolean started = listUpdateValues[i * 4 + 2] != 0;
			int numPlayers = listUpdateValues[i * 4 + 3];
			if (hideFull && numPlayers >= LobbyListEntry.MAX_PLAYERS) {
				continue;
			}
			if (hideLocked && passwordProtected) {
				continue;
			}
			if (hideInGame && started) {
				continue;
			}
			LobbyListEntry entry = new LobbyListEntry(inputs, this, lobbyName, lobbyOwner, lobbyID, numPlayers,
					passwordProtected, started);
			entries.put(lobbyID, entry);
			list.addEntry(entry);
		}
		setSelectedLobbyEntry(selectedIndex);
		list.setYOffset(yOffset);
		timeSinceLastUpdate = SystemTime.milliTime();
	}

	/**
	 * Sets the selected lobby index
	 * 
	 * @param The
	 *            lobby index
	 */
	private void setSelectedLobbyEntry(int i) {
		entries.values().forEach(entry -> entry.setSelected(false));
		LobbyListEntry prevSelected = entries.get(i);

		if (prevSelected != null) {
			prevSelected.setSelected(true);
		} else {
			selectedLobbyID = -1;
		}
	}

	/**
	 * Sets the selected lobby id
	 * 
	 * @param selectedLobbyID
	 *            The new selected lobby id
	 */
	public void setSelectedLobbyID(int selectedLobbyID) {
		this.selectedLobbyID = selectedLobbyID;
	}

	/**
	 * @return The selected lobby id
	 */
	public int getSelectedLobbyID() {
		return selectedLobbyID;
	}

	/**
	 * @return The scrollable list of lobbies
	 */
	public ScrollableList getList() {
		return list;
	}

	/**
	 * Called when the back button is pressed.
	 */
	@Override
	public void goBack() {
		menuManager.displayMainMenu();
	}

	/**
	 * Displayed when the user has been kicked by a lobby owner and tries to
	 * connect again.
	 */
	public void previouslyKicked() {
		TextButton okButton = new TextButton("Ok", inputs);
		okButton.addListener(e -> client.setDialog(null));
		DialogPanel dialog = new DialogPanel(inputs, client.getGameWidth(), client.getGameHeight(), "Kicked from lobby",
				"You have previously been kicked by the owner of this lobby.", okButton);
		client.setDialog(dialog);
	}

	/**
	 * Displayed when the user enters an invalid password for a lobby.
	 */
	public void invalidPassword() {
		TextButton okButton = new TextButton("Ok", inputs);
		okButton.addListener(e -> client.setDialog(null));
		DialogPanel dialog = new DialogPanel(inputs, client.getGameWidth(), client.getGameHeight(),
				"Invalid lobby password", "The password you have entered is incorrect.", okButton);
		client.setDialog(dialog);
	}
}