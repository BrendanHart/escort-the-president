package escort.client.ui.menus;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import escort.client.graphics.hud.ChatBox;
import escort.client.input.Inputs;
import escort.client.main.Client;
import escort.client.main.Scale;
import escort.client.ui.components.panels.IconPanel;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.text.TextButton;
import escort.client.ui.components.text.TextLabel;
import escort.client.ui.components.text.TextUtils;
import escort.client.ui.menus.howtoplay.HowToMenu;
import escort.client.ui.menus.lobby.LobbyCreation;
import escort.client.ui.menus.lobby.LobbyList;
import escort.client.ui.menus.lobby.LobbyMenu;
import escort.client.ui.menus.settings.SettingsMenu;
import escort.client.ui.utils.Colors;
import escort.client.ui.utils.Fonts;
import escort.common.game.Outcomes;
import escort.common.systime.SystemTime;

/**
 * Manages all menus and the back button. This class displays the menus and
 * renders them.
 * 
 * @author Ahmed Bhallo
 *
 */
public final class MenuManager {

	public Panel menuContainer;

	private SetupMenu setupMenu;
	private MainMenu mainMenu;
	private LobbyList lobbyList;
	private LobbyCreation lobbyCreation;
	private SettingsMenu settingsMenu;
	private HowToMenu howToMenu;

	private AbstractMenu currentMenu;

	private final Client client;
	private final Inputs inputs;

	private int leftMargin, topMargin, bottomMargin;

	private TextButton backButton;
	private TextLabel heading;

	private LobbyMenu lobbyMenu;

	private ChatBox chat;

	private boolean displayingEndMessage;
	private long timeSinceEndGame = -1;
	private static final int MS_TO_DISPLAY_END = 5000;

	private Panel endGamePanel;

	private IconPanel logo;

	public static BufferedImage LOGO;
	public static BufferedImage MENU_BACKGROUND;

	private TextLabel teamA1;

	private Panel topPanel;

	/**
	 * Instantiates a new menu manager
	 * 
	 * @param client
	 *            The client object
	 * @param inputs
	 *            The inputs object
	 */
	public MenuManager(Client client, Inputs inputs) {
		this.client = client;
		this.inputs = inputs;
		init();
		displaySetupMenu();
	}

	/**
	 * Initialises the menu container and all menus. Call this to rebuild the
	 * menus.
	 */
	public void init() {
		leftMargin = 80 * Scale.factor;
		topMargin = 60 * Scale.factor;
		bottomMargin = 10 * Scale.factor;
		logo = new IconPanel(inputs, LOGO, Scale.factor);

		menuContainer = new Panel(inputs, client.getGameWidth(), client.getGameHeight()) {
			@Override
			public void render(Graphics2D g) {
				g.drawImage(MENU_BACKGROUND, 0, 0, getWidth(), getHeight(), null);
				super.render(g);
			}
		};

		setupMenu = new SetupMenu(inputs, this);
		mainMenu = new MainMenu(inputs, this);
		lobbyList = new LobbyList(inputs, this);
		lobbyCreation = new LobbyCreation(inputs, this);
		settingsMenu = new SettingsMenu(inputs, this);
		howToMenu = new HowToMenu(inputs, this);
		if (lobbyMenu != null) {
			int lobbyID = lobbyMenu.getLobbyID();
			String lobbyName = lobbyMenu.getLobbyName();
			int ownerID = lobbyMenu.getOwnerID();
			lobbyMenu = new LobbyMenu(inputs, this, lobbyID, lobbyName, ownerID);
		}
		topPanel = new Panel(inputs, client.getGameWidth() - leftMargin * 2, topMargin - bottomMargin);
		menuContainer.add(topPanel, leftMargin, bottomMargin);
		initMenu();
	}

	/**
	 * Initialises the components on the menus such as the back button and
	 * heading label.
	 */
	private void initMenu() {
		backButton = new TextButton("< Go back", inputs);
		backButton.addListener(e -> currentMenu.goBack());

		heading = new TextLabel("", inputs);
		heading.setPadding(0, 0);
		heading.setFont(Fonts.HEADER);

		teamA1 = new TextLabel("Made with <3 by A1", inputs, Colors.LIGHT_GRAY);
		teamA1.setPadding(0, 0);
	}

	/**
	 * Displays the set up menu
	 */
	public void displaySetupMenu() {
		setCurrentMenu(setupMenu);
	}

	/**
	 * Displays the main menu
	 */
	public void displayMainMenu() {
		setCurrentMenu(mainMenu);
	}

	/**
	 * Displays the lobby list
	 */
	public void displayLobbyList() {
		setCurrentMenu(lobbyList);
	}

	/**
	 * Displays the lobby creation menu
	 */
	public void displayLobbyCreation() {
		setCurrentMenu(lobbyCreation);
	}

	/**
	 * Displays the settings menu
	 */
	public void displaySettingsMenu() {
		setCurrentMenu(settingsMenu);
	}

	/**
	 * Displays the how to play menu
	 */
	public void displayHowToPlay() {
		setCurrentMenu(howToMenu);
	}

	/**
	 * Displays the lobby menu
	 */
	public void displayLobbyMenu() {
		setCurrentMenu(lobbyMenu);
	}

	/**
	 * Creates and displays the lobby menu
	 * 
	 * @param lobbyID
	 *            The lobby id
	 * @param lobbyName
	 *            The name of the lobby
	 * @param ownerID
	 *            The player id of the owner of the lobby
	 */
	public void createDisplayLobbyMenu(int lobbyID, String lobbyName, int ownerID) {
		lobbyMenu = new LobbyMenu(inputs, this, lobbyID, lobbyName, ownerID);
		setCurrentMenu(lobbyMenu);
	}

	/**
	 * Sets the current menu
	 * 
	 * @param menu
	 *            The new menu
	 */
	public void setCurrentMenu(AbstractMenu menu) {
		menuContainer.remove(currentMenu);
		currentMenu = menu;
		menuContainer.add(currentMenu, leftMargin, topMargin);
		heading.setText(menu.getMenuName(), true);
		if (menu.displayBackButton()) {
			topPanel.remove(logo);
			topPanel.add(backButton, 0, topPanel.center(backButton).y);
			topPanel.add(teamA1, topPanel.getWidth() - teamA1.getWidth(), topPanel.center(teamA1).y);
			topPanel.add(heading, topPanel.center(heading));
		} else {
			topPanel.add(logo, topPanel.center(logo));
			topPanel.remove(backButton);
			topPanel.remove(teamA1);
			topPanel.remove(heading);
		}
	}

	/**
	 * Updates the current menu. Calculates when to hide the outcome message.
	 */
	public void update() {
		if (displayingEndMessage) {
			if (SystemTime.milliTime() - timeSinceEndGame >= MS_TO_DISPLAY_END) {
				displayingEndMessage = false;
			}
			endGamePanel.update();
		} else {
			menuContainer.update();
		}
	}

	/**
	 * Renders the outcome message or the menu containers
	 * 
	 * @param g
	 *            The graphics object
	 */
	public void render(Graphics2D g) {
		if (displayingEndMessage) {
			endGamePanel.render(g);
		} else {
			menuContainer.render(g);
		}
	}

	/**
	 * Display the end game message based on the outcome
	 * 
	 * @param outcome
	 *            The outcome
	 */
	public void displayEndGame(int outcome) {
		displayingEndMessage = true;
		endGamePanel = new Panel(inputs, client.getGameWidth(), client.getGameHeight());
		String message = "";
		switch (outcome) {
		case Outcomes.OUTCOME_DRAW:
			message = "Tie!";
			endGamePanel.setBackground(Colors.DARK_GRAY);
			break;
		case Outcomes.OUTCOME_ASSASSIN_WIN:
			message = "Assassins win!";
			endGamePanel.setBackground(Colors.PRESIDENTIAL_RED);
			break;
		case Outcomes.OUTCOME_ESCORT_WIN:
			message = "Escorts win!";
			endGamePanel.setBackground(Colors.PALE_BLUE);
			break;
		case Outcomes.OUTCOME_ERROR:
			message = "An error has occured. We're sorry about that!";
			endGamePanel.setBackground(Colors.DARK_GRAY);
			break;
		}
		TextLabel label = new TextLabel(message, inputs);
		label.setFont(Fonts.HEADER);
		endGamePanel.add(label, endGamePanel.center(label));
		timeSinceEndGame = SystemTime.milliTime();
	}

	/**
	 * Adds a successful notification and displays it. Automatically removes the
	 * notification after 5 seconds.
	 * 
	 * @param notificationText
	 *            The text to be displayed
	 */
	public void displaySuccess(String notificationText) {
		Panel notificationPanel = new Panel(inputs, 300, 400);
		notificationPanel.setBackground(Colors.setAlpha(Colors.GREEN, 200));
		TextLabel success = new TextLabel("Success", inputs);
		Panel text = TextUtils.wrappedTextLabel(inputs, notificationText, 30, Colors.GREEN, true);
		notificationPanel.add(success, notificationPanel.center(success).x, 5 * Scale.factor);
		notificationPanel.add(text, notificationPanel.center(text).x, success.getHeight() + 15 * Scale.factor);
		menuContainer.add(notificationPanel, menuContainer.getWidth() - notificationPanel.getWidth() - 5 * Scale.factor,
				menuContainer.getHeight() - notificationPanel.getHeight() - 5 * Scale.factor);
		new Thread(() -> {
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}
			menuContainer.remove(notificationPanel);
		}).start();
	}

	// Getters and setters //

	public Client getClient() {
		return client;
	}

	public LobbyList getLobbyList() {
		return lobbyList;
	}

	public Panel getMenuContainer() {
		return menuContainer;
	}

	public SetupMenu getSetupMenu() {
		return setupMenu;
	}

	public MainMenu getMainMenu() {
		return mainMenu;
	}

	public LobbyCreation getLobbyCreation() {
		return lobbyCreation;
	}

	public void setChat(ChatBox chat) {
		this.chat = chat;
	}

	public ChatBox getChat() {
		return chat;
	}

	public LobbyMenu getLobbyMenu() {
		return lobbyMenu;
	}

	public int getLeftMargin() {
		return leftMargin;
	}

	public int getTopMargin() {
		return topMargin;
	}

	public int getBottomMargin() {
		return bottomMargin;
	}

	public SettingsMenu getSettingsMenu() {
		return settingsMenu;
	}

	public HowToMenu getHowToMenu() {
		return howToMenu;
	}

}
