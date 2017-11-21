package escort.server.lobby;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import escort.common.game.lobby.LobbyMessageConstants;
import escort.common.game.lobby.NameValidation;
import escort.common.network.Message;
import escort.common.systime.SystemTime;
import escort.server.game.Game;
import escort.server.network.Player;

/**
 * For managing lobbies
 * @author Kwong Hei Tsang
 */
public class LobbyManagement {

	// maps lobby id to lobby
	private final Map<Integer, Lobby> lobbies;
	private final Object lobbiesLock;
	private int lobbycount;
	private final Thread lobbysweeper;

	/**
	 * Create a lobby management object
	 */
	public LobbyManagement() {
		this.lobbies = new ConcurrentHashMap<Integer, Lobby>();
		this.lobbiesLock = new Object();
		this.lobbycount = 0;
		this.lobbysweeper = new Thread(() -> lobbySweeper(), "LobbySweeper");
		this.lobbysweeper.start();
	}

	/**
	 * Create a new lobby
	 * @param player The player
	 * @param name Lobby name
	 * @param password The password of the new lobby
	 */
	public void createLobby(Player player, String name, String password) {
		// check if player is free
		if (player.getLobbyID() > 0) {
			return;
		}

		// check if name is valid
		if (!NameValidation.validateLobby(name)) {
			player.getSender().put(new Message(Message.LOBBY_NAME_INVALID, null, null));
			return;
		}

		// create the lobby
		Lobby newLobby;
		synchronized (this.lobbiesLock) {
			this.lobbycount++;
			newLobby = new Lobby(player, this.lobbycount, name, password);
			this.lobbies.put(this.lobbycount, newLobby);
			player.setLobbyID(this.lobbycount);
		}

		// notify the client
		player.getSender()
				.put(new Message(Message.LOBBY_CREATED,
						new int[] { newLobby.getID(), newLobby.getMaster().getPlayerID() },
						new String[] { newLobby.getName() }));
	}

	/**
	 * Get the list of lobby
	 * @return The list of lobby
	 */
	public List<Lobby> getLobbyList() {
		// compute lobby list
		List<Lobby> lobbylists;
		synchronized (this.lobbiesLock) {
			lobbylists = new ArrayList<Lobby>(this.lobbies.values());
		}

		return lobbylists;
	}

	/**
	 * Player should call this method instead of getLobbyList()
	 * @throws IOException
	 * @return The message to be sent to the client when requesting lobby list
	 */
	public Message getLobbyListReturnMessage() {
		// get lobby list
		List<Lobby> lobbylists = this.getLobbyList();

		// extract ids and lobby names
		int[] values = new int[4 * lobbylists.size()];
		String[] texts = new String[2 * lobbylists.size()];
		int valuei = 0;
		int texti = 0;
		for (int i = 0; i < lobbylists.size(); i++) {
			Lobby lobby = lobbylists.get(i);
			// lobbyid
			values[valuei++] = lobby.getID();
			// islocked
			if (!lobby.getPassword().equals("")) {
				values[valuei++] = 1;
			} else {
				values[valuei++] = 0;
			}
			// ingame
			if (lobby.isStarted()) {
				values[valuei++] = 1;
			} else {
				values[valuei++] = 0;
			}
			// number of players
			values[valuei++] = lobby.getPlayers().size();

			// lobbyname and master's name
			texts[texti++] = lobby.getName();
			texts[texti++] = lobby.getMaster().getPlayerName();
		}

		// reply to the client
		return new Message(Message.LOBBY_LIST_RESULT, values, texts);
	}

	/**
	 * Join a specific lobby
	 * @param player The player
	 * @param lobbyid The lobby ID
	 * @param password The password of the lobby for authentication
	 */
	public void joinLobby(Player player, int lobbyid, String password) {
		// check if player has already joined a lobby, making sure the request
		// is valid
		if (player.getLobbyID() > 0) {
			return;
		}

		// Find the lobby
		Lobby lobby = null;
		synchronized (this.lobbiesLock) {
			lobby = this.lobbies.get(lobbyid);
		}

		// Check if the lobby exists and open
		if (lobby == null || !lobby.isOpen()) {
			player.getSender().put(new Message(Message.LOBBY_IDINVALID, null, null));
			return;
		}

		// Check if the lobby has already started
		if (lobby.isStarted()) {
			player.getSender().put(new Message(Message.LOBBY_GAME_ALREADY_STARTED, null, null));
			return;
		}

		// Join the lobby
		if (!password.equals(lobby.getPassword())) {
			// authentication failure
			player.getSender()
					.put(new Message(Message.LOBBY_AUTHFAIL, new int[] { LobbyMessageConstants.WRONG_PASSWORD }, null));
			return;
		}
		
		// authentication successful, join the lobby
		int result = lobby.addPlayer(player);
		switch (result){
			case 0:
				// Joined successfully
				break;
			case 2:
				// kicked within specified period, rejoin not allowed
				player.getSender().put(new Message(Message.LOBBY_AUTHFAIL, new int[] { LobbyMessageConstants.KICKED_BEFORE }, null));
				return;
			case 1:
				// lobby is full
				player.getSender().put(new Message(Message.LOBBY_FULL, null, null));
				return;
			default:
				return;
		}
		
		// Lobby joined successfully
		player.setLobbyID(lobby.getID());
		player.getSender().put(new Message(Message.LOBBY_JOINED,
				new int[] { lobby.getID(), lobby.getMaster().getPlayerID() }, new String[] { lobby.getName() }));
		
		// Send lobby settings
		LobbySettings settings = lobby.getSettings();
		player.getSender().put(new Message(Message.LOBBY_SETTINGS, new int[]{settings.mapID, settings.numAssassinsAI, settings.numPoliceAI, settings.numCivilianAI}));
		
		// update player list
		this.updatePlayers(lobby);
	}
	
	/**
	 * send player list to players
	 * @param lobby The lobby
	 */
	private void updatePlayers(Lobby lobby){
		// create player joined message and notify all players
		List<Player> players = lobby.getPlayers();
		int[] ids = new int[players.size()];
		String[] names = new String[players.size()];
		for (int i = 0; i < players.size(); i++) {
			ids[i] = players.get(i).getPlayerID();
			names[i] = players.get(i).getPlayerName();
		}
		Message playerjoinedmsg = new Message(Message.PLAYER_LIST_UPDATE, ids, names);
		for (Player eachplayer : players) {
			eachplayer.getSender().put(playerjoinedmsg);
		}
	}

	/**
	 * Leave the lobby
	 * @param player The player
	 * @param reason The reason of leaving the lobby, as specifed in LobbyMessageConstants
	 */
	public void leaveLobby(Player player, int reason) {
		// Get lobby
		Lobby lobby = null;
		synchronized (this.lobbiesLock) {
			lobby = this.lobbies.get(player.getLobbyID());
		}

		// lobby ID not valid or not actually joined any lobby
		if (lobby == null) {
			return;
		}

		// Check if this is master
		if (player == lobby.getMaster()) {
			// this is master, close lobby

			// remove all other players
			player.setLobbyID(-1);
			lobby.closeLobby();
			for (Player eachplayer : lobby.getPlayers()) {
				if (eachplayer != player) {
					leaveLobby(eachplayer, LobbyMessageConstants.MASTER_LEFT);
				}
			}

			// remove master
			lobby.removePlayer(player);
			player.getSender().put(new Message(Message.LOBBY_LEFT, new int[] { reason }, null));

			// close the lobby
			synchronized (this.lobbiesLock) {
				this.lobbies.remove(lobby.getID());
			}
			return;
		}

		// this is not master, single player leave
		lobby.removePlayer(player);
		player.getSender().put(new Message(Message.LOBBY_LEFT, new int[] { reason }, null));
		player.setLobbyID(-1);
		this.updatePlayers(lobby);

	}

	/**
	 * Configure the password of the lobby
	 * @param player The player invoking this request
	 * @param password The new password of the lobby
	 */
	public void setPassword(Player player, String password) {
		Lobby lobby = null;
		synchronized (this.lobbiesLock) {
			lobby = this.lobbies.get(player.getLobbyID());
		}

		// if lobby ID is invalid
		if (lobby == null) {
			return;
		}

		// Change the password
		if (lobby.getMaster() == player) {
			lobby.setPassword(password);
			player.getSender().put(new Message(Message.LOBBY_PASSWORD_SET, null, null));
		}
	}

	/**
	 * Kick a player
	 * @param master The master invoking this request
	 * @param target The player to be kick from the lobby
	 */
	public void kickPlayer(Player master, Player target) {
		Lobby lobby = null;
		synchronized (this.lobbiesLock) {
			lobby = this.lobbies.get(master.getLobbyID());
		}

		// if lobby ID or target player is invalid
		if (lobby == null || target == null) {
			return;
		}

		// Check if both are in the same lobby and master is really master and
		// game not started
		if (master.getLobbyID() != target.getLobbyID() || lobby.getMaster() != master || lobby.isStarted()) {
			return;
		}

		// Kick the player
		lobby.kickPlayer(target);
		leaveLobby(target, LobbyMessageConstants.KICKED);
	}

	/**
	 * Start the game
	 * @param master The master invoking this request
	 */
	public void startGame(Player master) {
		Lobby lobby = null;
		synchronized (this.lobbiesLock) {
			lobby = this.lobbies.get(master.getLobbyID());
		}

		// Only master can start the game
		if (lobby == null || lobby.getMaster() != master || master.getGame() != null || lobby.isStarted()) {
			return;
		}
		lobby.startGame();
		
		//Construct the game logic and start the countdown
		final List<Player> players = lobby.getPlayers();
		final int lobbyID = lobby.getID();
		Game game = new Game(players, this, lobbyID, lobby.getSettings());
		final long starttime = SystemTime.milliTime() + LobbyConfiguration.START_COUNT_DOWN * 1000;
		(new Thread(() -> countDown(players, starttime, game), "Lobby Countdown: " + lobby.getID())).start();
	}

	/**
	 * The method for count down before the game really starts
	 * @param players The list of players in the game
	 * @param starttime The time when game is starting
	 * @param game The game object
	 */
	private void countDown(List<Player> players, long starttime, Game game) {
		int lastcount = LobbyConfiguration.START_COUNT_DOWN + 1;
		while (true) {
			long currentTime = SystemTime.milliTime();
			int count = (int) ((starttime - currentTime) / 1000);

			if (count <= 0) {
				// start game
				break;
			}

			if (count != lastcount) {
				lastcount = count;
				Message countmsg = new Message(Message.COUNTDOWN_TICK, new int[] { count }, null);
				for (Player player : players) {
					player.getSender().put(countmsg);
				}
			}

			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}

		// send start message
		Message startmsg = new Message(Message.GAME_START, null, null);
		for (Player player : players) {
			player.getSender().put(startmsg);
		}
		game.startGame();
	}

	/**
	 * End a game
	 * @param game The game object
	 * @param outcome The outcome of the game
	 */
	public void endGame(Game game, int outcome) {
		Lobby lobby = null;

		// notify end game
		Message endmsg = new Message(Message.GAME_END, new int[] { outcome });
		for (Player player : game.getPlayers()) {
			player.getSender().put(endmsg);
			player.setGame(null);
		}

		// Set the lobby playing state to false
		synchronized (this.lobbiesLock) {
			lobby = this.lobbies.get(game.getLobbyID());
		}
		if (lobby != null) {
			lobby.endGame();
		}
	}

	/**
	 * Send a message to other players in the lobby
	 * @param player The sender
	 * @param message The message
	 */
	public void sendMessage(Player player, String message) {
		Lobby lobby = null;
		synchronized (this.lobbiesLock) {
			lobby = this.lobbies.get(player.getLobbyID());
		}

		// Generate the message
		Message command = new Message(Message.LOBBY_MESSAGE, null, new String[] { player.getPlayerName(), message });

		if (lobby != null) {
			// Obtain player list from lobby
			for (Player eachplayer : lobby.getPlayers()) {
				eachplayer.getSender().put(command);
			}
		} else {
			// fall back to game logic
			Game game = player.getGame();
			if (game != null) {
				for (Player eachplayer : game.getPlayers()) {
					eachplayer.getSender().put(command);
				}
			}
		}
	}

	/**
	 * Configure the lobby
	 * @param player The master of the lobby
	 * @param newmap The new map ID
	 * @param numAssassinsAI The new number of assassins AI
	 * @param numPoliceAI The new number of police AI
	 * @param numCiviliansAI The new number of civilians AI
	 */
	public void set(Player player, int map, int numAssassinsAI, int numPoliceAI, int numCivilianAI) {
		Lobby lobby = null;
		synchronized (this.lobbiesLock) {
			lobby = this.lobbies.get(player.getLobbyID());
		}

		// Have to be master
		if (lobby == null || lobby.getMaster() != player) {
			return;
		}

		// configure the lobby
		numAssassinsAI = Math.min(numAssassinsAI, LobbyConfiguration.MAX_NUM_ASSASSINS_AI);
		numPoliceAI = Math.min(numPoliceAI, LobbyConfiguration.MAX_NUM_POLICE_AI);
		numCivilianAI = Math.min(numCivilianAI, LobbyConfiguration.MAX_NUM_CIVILIAN_AI);
		lobby.set(map, numAssassinsAI, numPoliceAI, numCivilianAI);

		// Notify each player
		Message msg = new Message(Message.LOBBY_SETTINGS, new int[] { map, numAssassinsAI, numPoliceAI, numCivilianAI }, null);
		for (Player eachplayer : lobby.getPlayers()) {
			eachplayer.getSender().put(msg);
		}
	}

	/**
	 * The method called by the thread removing inactive lobbies exceeding
	 * inactive time limit
	 */
	private void lobbySweeper() {
		long loopstarttime;
		long processtime;

		try {
			while (true) {
				loopstarttime = SystemTime.milliTime();

				// remove inactive lobbies
				List<Lobby> lobbies = getLobbyList();
				for (Lobby lobby : lobbies) {
					if (!lobby.isStarted() && SystemTime.milliTime() - lobby.getLastActiveTime() > LobbyConfiguration.MAX_INACTIVE_PERIOD) {
						new Thread(() -> leaveLobby(lobby.getMaster(), LobbyMessageConstants.INACTIVITY)).start();
					}
				}

				processtime = SystemTime.milliTime() - loopstarttime;
				if (processtime < LobbyConfiguration.SWEEPER_PERIOD) {
					Thread.sleep(LobbyConfiguration.SWEEPER_PERIOD - processtime);
				} else if (Thread.currentThread().isInterrupted()) {
					break;
				}

			}
		} catch (InterruptedException e) {
		}
		System.out.println("Lobby management stopped.");
	}

	/**
	 * Stop the lobby management by stopping the required threads under this
	 * lobbymanagement
	 */
	public void stopLobbyManagement() {
		this.lobbysweeper.interrupt();
	}
}
