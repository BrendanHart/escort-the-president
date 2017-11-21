package escort.client.ui.menus.lobby;

import escort.client.input.Inputs;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.text.TextLabel;
import escort.client.ui.utils.Colors;

/**
 * An entry of the lobby list. Contains information of whether or not the lobby
 * is in game, has a password, the amount of players, the name of the lobby, and
 * the owner.
 * 
 * @author Ahmed Bhallo
 *
 */
public class LobbyListEntry extends Panel {

	public static final int MAX_PLAYERS = 15;

	private final LobbyList lobbyList;
	public final String name;
	public final String owner;
	public final int lobbyID;
	public final int players;
	public final boolean locked;
	public final boolean inGame;

	private boolean isSelected;

	/**
	 * Instantiates a new lobby list entry
	 * 
	 * @param inputs
	 *            The inputs object
	 * @param lobbyList
	 *            The lobby list object
	 * @param name
	 *            THe name of the lobby
	 * @param owner
	 *            The player id of the owner
	 * @param lobbyID
	 *            The lobby id
	 * @param players
	 *            The number of players in the lobby
	 * @param locked
	 *            Whether or not the lobby is password protected
	 * @param inGame
	 *            Whether or not the lobby is in a game
	 */
	public LobbyListEntry(Inputs inputs, LobbyList lobbyList, String name, String owner, int lobbyID, int players,
			boolean locked, boolean inGame) {
		super(inputs, 0, 0);
		this.lobbyList = lobbyList;
		this.name = name;
		this.owner = owner;
		this.lobbyID = lobbyID;
		this.players = players;
		this.locked = locked;
		this.inGame = inGame;

		TextLabel nameField = new TextLabel(name, inputs, Colors.DARK_WHITE);
		add(nameField, lobbyList.LOBBY_NAME_X, 0);

		TextLabel ownerField = new TextLabel(owner, inputs, Colors.DARK_WHITE);
		add(ownerField, lobbyList.OWNER_X, 0);

		TextLabel playersField = new TextLabel(players + "/" + MAX_PLAYERS, inputs, Colors.DARK_WHITE);
		add(playersField, lobbyList.PLAYERS_X, 0);

		TextLabel lockedField = new TextLabel(locked ? "Yes" : "No", inputs, Colors.DARK_WHITE);
		add(lockedField, lobbyList.LOCKED_X, 0);

		TextLabel inGameField = new TextLabel(inGame ? "Yes" : "No", inputs, Colors.DARK_WHITE);
		add(inGameField, lobbyList.IN_GAME_X, 0);
		pack();
		setWidth(lobbyList.getWidth() - lobbyList.getList().getScrollBar().getWidth());
	}

	/**
	 * Detects if the entry has been pressed. Updates colors based on selection.
	 */
	@Override
	public void update() {
		super.update();

		if (lobbyList.getList().contentDown()) {
			setSelected(isDown());
		}

		if (isSelected()) {
			setBackground(Colors.LIGHT_BLUE);
		} else {
			setBackground(Colors.GRAY);
		}
	}

	/**
	 * Sets whether or not this entry is selected
	 * 
	 * @param isSelected
	 *            Whether or not this entry is selected
	 */
	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
		if (isSelected) {
			lobbyList.setSelectedLobbyID(lobbyID);
		}
	}

	/**
	 * @return True iff this entry is selected
	 */
	public boolean isSelected() {
		return isSelected;
	}

	/**
	 * @return The ID of this lobby.
	 */
	public int getLobbyID() {
		return lobbyID;
	}

}
