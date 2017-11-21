package escort.client.ui.menus.lobby;

import java.io.IOException;

import escort.client.graphics.hud.ChatBox;
import escort.client.input.Inputs;
import escort.client.main.Client;
import escort.client.main.Scale;
import escort.client.network.NetworkManager;
import escort.client.ui.components.Component;
import escort.client.ui.components.ComponentListener;
import escort.client.ui.components.Stepper;
import escort.client.ui.components.panels.DialogPanel;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.panels.ScrollableList;
import escort.client.ui.components.text.TextButton;
import escort.client.ui.components.text.TextLabel;
import escort.client.ui.menus.AbstractMenu;
import escort.client.ui.menus.MenuManager;
import escort.client.ui.utils.Colors;
import escort.common.network.Message;

/**
 * A panel for when the client is inside a lobby. Displays the chat, buttons to
 * settings and how to play, Panel to view/update lobby settings and list of
 * players.
 * 
 * @author Ahmed Bhallo
 *
 */
public class LobbyMenu extends AbstractMenu implements ComponentListener {
	
	private final int separation = 16 * Scale.factor;

	private final int lobbyID;
	private final String lobbyName;
	private final int ownerID;
	private final boolean isOwner;

	private final ChatBox chat;
	private final Panel information;
	private final ScrollableList players;
	private final Panel lobbySettingsPanel;

	private Stepper civilianStepper, policeStepper, assassinStepper, mapStepper;
	private TextButton startButton;
	private DialogPanel exitDialog;

	private final Client client;

	/**
	 * Instantiates a new lobby menu
	 * 
	 * @param inputs
	 *            The inputs object
	 * @param menuManager
	 *            The menu manager
	 * @param lobbyID
	 *            The id of the lobby
	 * @param lobbyName
	 *            The name of the lobby
	 * @param ownerID
	 *            The player id of the owner of the lobby
	 */
	public LobbyMenu(Inputs inputs, MenuManager menuManager, int lobbyID, String lobbyName, int ownerID) {
		super(inputs, menuManager, "LOBBY");
		this.lobbyID = lobbyID;
		this.lobbyName = lobbyName;
		this.ownerID = ownerID;
		this.client = menuManager.getClient();

		// final int playerListHeight = 128 * Scale.factor;

		isOwner = menuManager.getClient().getNetworkManager().getPlayerID() == ownerID;

		chat = new ChatBox(inputs, menuManager.getClient().getNetworkManager(), 25);
		menuManager.setChat(chat);

		players = new ScrollableList(inputs, chat.getWidth(), getHeight() - separation - chat.getHeight());
		players.setBackground(Colors.UI_BG);

		lobbySettingsPanel = new Panel(inputs, getWidth() - chat.getWidth() - separation, chat.getHeight());
		lobbySettingsPanel.setBackground(Colors.UI_BG);

		information = new Panel(inputs, getWidth() - chat.getWidth() - separation, players.getHeight());
		information.setBackground(Colors.UI_BG);

		add(information, chat.getWidth() + separation, 0);
		add(players, 0, 0);
		add(lobbySettingsPanel, chat.getWidth() + separation, players.getHeight() + separation);

		displayOwnerSettings();
		if (!isOwner) {
			disableSettings();
		}
		TextLabel lobbyNameLabel = new TextLabel("Lobby name: " + lobbyName, inputs);
		TextButton openSettings = new TextButton("Open Settings", inputs);
		openSettings.addListener(e -> menuManager.displaySettingsMenu());
		TextButton howTo = new TextButton("Click here to view how to play", inputs);
		howTo.addListener(e -> menuManager.displayHowToPlay());
		howTo.setBorderDefault(null);
		information.add(lobbyNameLabel, information.center(lobbyNameLabel).x, 0);
		information.add(howTo, information.center(howTo));
		information.add(openSettings, information.center(openSettings).x,
				information.getHeight() - openSettings.getHeight());

		initDialog();
		addChat();

		populatePlayerList();
	}

	/**
	 * Initialises the leaving dialog
	 */
	private void initDialog() {
		TextButton rejectButton = new TextButton("Cancel", inputs);
		rejectButton.addListener(e -> menuManager.getClient().setDialog(null));
		TextButton acceptButton = new TextButton("Yes", inputs);
		acceptButton.addListener(e -> exitLobby());
		Panel buttons = new Panel(inputs, 140 * Scale.factor, rejectButton.getHeight());
		buttons.add(rejectButton, 0, 0);
		buttons.add(acceptButton, buttons.getWidth() - acceptButton.getWidth(), 0);
		exitDialog = new DialogPanel(inputs, client.getGameWidth(), client.getGameHeight(),
				"You are about to leave the lobby", "Are you sure you want to leave? " + ((isOwner)
						? "You are the owner so this lobby will be destoryed." : "The game might start without you!"),
				buttons);
	}

	/**
	 * Called to display settings for the owner of the lobby
	 */
	private void displayOwnerSettings() {
		int seperation = 5 * Scale.factor;
		int stepperWidth = 100 * Scale.factor;
		int stepperX = lobbySettingsPanel.getWidth() - stepperWidth;
		int y = 0;

		TextLabel civilianLabel = new TextLabel("Civilians AI: ", inputs);
		lobbySettingsPanel.add(civilianLabel, 0, y);
		civilianStepper = new Stepper(inputs, stepperWidth, 3, new String[] { "0", "1", "2", "3", "4", "5" });
		civilianStepper.addListener(this);
		lobbySettingsPanel.add(civilianStepper, stepperX, y);
		y += civilianLabel.getHeight() + seperation;

		TextLabel policeLabel = new TextLabel("Police AI: ", inputs);
		lobbySettingsPanel.add(policeLabel, 0, y);
		policeStepper = new Stepper(inputs, stepperWidth, 3, new String[] { "0", "1", "2", "3", "4", "5" });
		policeStepper.addListener(this);
		lobbySettingsPanel.add(policeStepper, stepperX, y);
		y += policeLabel.getHeight() + seperation;

		TextLabel assassinLabel = new TextLabel("Assassin AI: ", inputs);
		lobbySettingsPanel.add(assassinLabel, 0, y);
		assassinStepper = new Stepper(inputs, stepperWidth, 3, new String[] { "0", "1", "2", "3", "4", "5" });
		assassinStepper.addListener(this);
		lobbySettingsPanel.add(assassinStepper, stepperX, y);
		y += assassinLabel.getHeight() + seperation;

		TextLabel mapLabel = new TextLabel("Map: ", inputs);
		lobbySettingsPanel.add(mapLabel, 0, y);
		mapStepper = new Stepper(inputs, stepperWidth, 0, new String[] { "Hotel", "University" });
		mapStepper.addListener(this);
		lobbySettingsPanel.add(mapStepper, stepperX, y);
		y += mapLabel.getHeight() + seperation;

		startButton = new TextButton("Start game", inputs);
		startButton.addListener(e -> startGame());
		lobbySettingsPanel.add(startButton, lobbySettingsPanel.center(startButton).x,
				lobbySettingsPanel.getHeight() - startButton.getHeight());
	}

	/**
	 * Disables the settings for non-owners.
	 */
	private void disableSettings() {
		civilianStepper.setEnabled(false);
		assassinStepper.setEnabled(false);
		policeStepper.setEnabled(false);
		mapStepper.setEnabled(false);
		startButton.setEnabled(false);
	}

	/**
	 * Populates the players list from the cached lists
	 */
	private void populatePlayerList() {
		NetworkManager netManager = menuManager.getClient().getNetworkManager();
		updatePlayerList(netManager.getPlayerIDInLobby(), netManager.getPlayerNameInLobby());
	}

	/**
	 * Sends a request to the server to start a game
	 */
	private void startGame() {
		try {
			menuManager.getClient().getNetworkManager().startGame();
		} catch (IOException e) {
		}
	}

	/**
	 * Updates the settings from the server
	 * 
	 * @param mapID
	 *            The new map id
	 * @param numAssassinAI
	 *            The new number of assassin ai
	 * @param numPoliceAI
	 *            The new number of police ai
	 * @param numCivilianAI
	 *            THe new number of civilian ai
	 */
	public void updateSettings(int mapID, int numAssassinAI, int numPoliceAI, int numCivilianAI) {
		if (mapID > 2 || numAssassinAI > 5 || numPoliceAI > 5 || numCivilianAI > 5) {
			return;
		}
		mapStepper.setIndex(mapID);
		assassinStepper.setIndex(numAssassinAI);
		policeStepper.setIndex(numPoliceAI);
		civilianStepper.setIndex(numCivilianAI);
	}

	/**
	 * Called when the lobby settings components have been updated. Sends the
	 * message to the server to have them updated.
	 */
	@Override
	public void componentClicked(Component source) {
		menuManager.getClient().getNetworkManager().getSender()
				.put(new Message(Message.LOBBY_SETTINGS_CHANGE, new int[] { mapStepper.getIndex(),
						assassinStepper.getIndex(), policeStepper.getIndex(), civilianStepper.getIndex() }));
	}

	/**
	 * Updates the player list from the data receives from the server.
	 * 
	 * @param ints
	 *            The array of player id
	 * @param strings
	 *            The array of username
	 */
	public void updatePlayerList(int[] ints, String[] strings) {
		players.clear();
		for (int i = 0; i < ints.length; i++) {
			int playerID = ints[i];
			Panel entry = new Panel(inputs, players.getWidth() - players.getScrollBar().getWidth(), 24 * Scale.factor);
			entry.setBackground(Colors.DARK_WHITE);
			String name = strings[i] + (ownerID == ints[i] ? " (Owner)" : "");
			TextLabel nameLabel = new TextLabel(name, inputs, Colors.LIGHT_BLACK);
			nameLabel.setShadow(false);
			entry.add(nameLabel, 0, entry.center(nameLabel).y);
			players.addEntry(entry);
			if (isOwner && playerID != ownerID) {
				TextButton kickButton = new TextButton("Kick", inputs);
				kickButton.setBorderDefault(Colors.VERY_DARK_GRAY);
				kickButton.setForeground(Colors.VERY_DARK_GRAY);
				kickButton.setShadow(false);
				kickButton.addListener(e -> kickPlayer(playerID));
				entry.add(kickButton, entry.getWidth() - kickButton.getWidth(), entry.center(kickButton).y);
			}
		}
	}

	/**
	 * Sends a message to the server to kick a player with given player id
	 * 
	 * @param playerID
	 *            The player to kick
	 */
	private void kickPlayer(int playerID) {
		menuManager.getClient().getNetworkManager().getSender()
				.put(new Message(Message.LOBBY_KICK_PLAYER, new int[] { playerID }));
	}

	/**
	 * Called when the back button is pressed
	 */
	@Override
	public void goBack() {
		client.setDialog(exitDialog);
	}

	/**
	 * Called when the client leaves the lobby. Notifies the server and displays
	 * the lobby list.
	 */
	private void exitLobby() {
		menuManager.getClient().getNetworkManager().getSender().put(new Message(Message.LOBBY_LEAVE, null));
		goToLobbyList();
		client.getNetworkManager().setInLobby(false);
	}

	/**
	 * Displays the lobby list and removes the modal dialog.
	 */
	private void goToLobbyList() {
		client.setDialog(null);
		menuManager.displayLobbyList();
	}

	/**
	 * Notify the client that they have been kicked for inactivity.
	 */
	public void kickedForInactivity() {
		TextButton okButton = new TextButton("Ok", inputs);
		okButton.addListener(e -> goToLobbyList());
		DialogPanel dialog = new DialogPanel(inputs, client.getGameWidth(), client.getGameHeight(),
				"You are have been kicked for inactivity", "Feel free to start a new lobby.", okButton);
		client.setDialog(dialog);
	}

	/**
	 * Notify the client that they have been kicked by the owner.
	 */
	public void kickedByOwner() {
		TextButton okButton = new TextButton("Ok", inputs);
		okButton.addListener(e -> goToLobbyList());
		DialogPanel dialog = new DialogPanel(inputs, client.getGameWidth(), client.getGameHeight(),
				"You are have been kicked by the owner", "You will be taken to the lobby list menu.", okButton);
		client.setDialog(dialog);
	}

	/**
	 * Notify the client that the owner has left.
	 */
	public void ownerLeft() {
		if (client.isInGame()) {
			goToLobbyList();
			return;
		}
		TextButton okButton = new TextButton("Ok", inputs);
		okButton.addListener(e -> goToLobbyList());
		DialogPanel dialog = new DialogPanel(inputs, client.getGameWidth(), client.getGameHeight(),
				"The owner has left the lobby", "You will be taken to the lobby list menu.", okButton);
		client.setDialog(dialog);
	}

	/**
	 * Adds the chat to the lobby menu
	 */
	public void addChat() {
		add(chat, 0, information.getHeight() + separation);
	}

	/**
	 * @return the lobby id of the current lobby.
	 */
	public int getLobbyID() {
		return lobbyID;
	}

	/**
	 * @return The name of the lobby
	 */
	public String getLobbyName() {
		return lobbyName;
	}

	/**
	 * @return The player id of the owner of the lobby
	 */
	public int getOwnerID() {
		return ownerID;
	}

}