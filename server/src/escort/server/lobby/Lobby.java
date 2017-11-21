package escort.server.lobby;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import escort.common.systime.SystemTime;
import escort.server.network.Player;

/**
 * This represents a lobby
 * 
 * @author Kwong Hei Tsang
 *
 */
public class Lobby {

	private String password;
	private final int lobbyid;
	private String lobbyName;
	private final LobbySettings settings;
	private final Set<Player> playerlist;
	private final Player master;
	private final Object playerlistlock;
	private boolean open;
	private final Map<Player, Long> lastKickedTime;
	private long lastActiveTime;
	private boolean started;
	private final Object gameStatusLock;
	private final Object passwordLock;
	private final Object settingsLock;

	/**
	 * Construct a lobby object
	 * 
	 * @param lobbyid
	 * @param lobbyName
	 * @param password
	 *            The password of the lobby
	 */
	public Lobby(Player player, int lobbyid, String lobbyName, String password) {
		this.password = password;
		this.lobbyName = lobbyName;
		this.lobbyid = lobbyid;
		this.playerlist = new HashSet<Player>();
		this.playerlist.add(player);
		this.master = player;
		this.playerlistlock = new Object();
		this.open = true;
		this.lastKickedTime = new ConcurrentHashMap<Player, Long>();
		this.lastActiveTime = SystemTime.milliTime();
		this.started = false;
		this.gameStatusLock = new Object();
		this.passwordLock = new Object();
		this.settingsLock = new Object();
		this.settings = new LobbySettings();
	}

	/**
	 * Obtain the password of the lobby
	 * 
	 * @return The password
	 */
	public String getPassword() {
		synchronized (this.passwordLock) {
			return this.password;
		}
	}

	/**
	 * Change the password of the lobby
	 * 
	 * @param password
	 */
	public void setPassword(String password) {
		synchronized (this.passwordLock) {
			this.password = password;
		}
	}

	/**
	 * Get the name of the lobby
	 * 
	 * @return The name of the lobby
	 */
	public String getName() {
		return this.lobbyName;
	}

	/**
	 * Get the lobby ID
	 * 
	 * @return the lobby ID
	 */
	public int getID() {
		return this.lobbyid;
	}

	/**
	 * Get the master
	 * 
	 * @return the master
	 */
	public Player getMaster() {
		return this.master;
	}

	/**
	 * Add a player to the lobby
	 * 
	 * @param player
	 *            The player to be added
	 * @throws KickedException
	 *             When the player was kick recently
	 * @throws LobbyFullException
	 *             When the lobby full
	 */
	public int addPlayer(Player player){
		synchronized (this.playerlistlock) {
			if (this.lastKickedTime.get(player) != null && SystemTime.milliTime()
					- this.lastKickedTime.get(player) < LobbyConfiguration.KICK_PREVENT_JOIN) {
				// Player kicked recently
				return 2;
			}

			if (this.playerlist.size() >= LobbyConfiguration.MAX_PLAYERS) {
				// Game is too large
				return 1;
			}

			this.playerlist.add(player);
			
			return 0;
		}
	}

	/**
	 * Remove the specifed player from the lobby
	 * 
	 * @param player
	 *            The specified player
	 */
	public void removePlayer(Player player) {
		synchronized (this.playerlistlock) {
			this.playerlist.remove(player);
		}
	}

	/**
	 * Get the list of players in the lobby
	 * 
	 * @return The list of player in the lobby
	 */
	public List<Player> getPlayers() {
		List<Player> players = null;

		// construct a new list to return
		synchronized (this.playerlistlock) {
			players = new ArrayList<Player>(this.playerlist);
		}

		return players;
	}

	/**
	 * Close the lobby
	 */
	public void closeLobby() {
		synchronized (this.gameStatusLock) {
			this.open = false;
		}
	}

	/**
	 * Check if the lobby is open
	 * 
	 * @return Whether the lobby is open
	 */
	public boolean isOpen() {
		synchronized (this.gameStatusLock) {
			return this.open;
		}
	}

	/**
	 * Start the game, set the game state to true, but it does not create a game
	 * object automatically
	 */
	public void startGame() {
		synchronized (this.gameStatusLock) {
			this.started = true;
		}
	}

	/**
	 * End a game, set the game state to false
	 */
	public void endGame() {
		synchronized (this.gameStatusLock) {
			this.lastActiveTime = SystemTime.milliTime();
			this.started = false;
		}
	}

	/**
	 * Kick a player from a lobby
	 * 
	 * @param player
	 *            The specific player
	 */
	public void kickPlayer(Player player) {
		this.lastKickedTime.put(player, SystemTime.milliTime());
	}

	/**
	 * Get the time when lobby is created or last end of the game with this
	 * lobby
	 * 
	 * @return
	 */
	public long getLastActiveTime() {
		synchronized (this.gameStatusLock) {
			return this.lastActiveTime;
		}
	}

	/**
	 * Check if the lobby has a game playing in
	 * 
	 * @return whether the game is started, just the state
	 */
	public boolean isStarted() {
		synchronized (this.gameStatusLock) {
			return this.started;
		}
	}

	/**
	 * Configure the map
	 * @param map The specific map
	 */
	public void set(int map, int numAssassinsAI, int numPoliceAI, int numCivilianAI){
		synchronized(this.settingsLock){
			this.settings.mapID = map;
			this.settings.numAssassinsAI = numAssassinsAI;
			this.settings.numPoliceAI = numPoliceAI;
			this.settings.numCivilianAI = numCivilianAI;
		}
	}
	
	/**
	 * Get the map information
	 * @return The id of the map
	 */
	public int getMap(){
		synchronized(this.settingsLock){
			return this.settings.mapID;
		}
	}
	
	/**
	 * Get a copy of the lobby settings
	 * @return a copy of the settings
	 */
	public LobbySettings getSettings(){
		synchronized(this.settingsLock){
			return this.settings.copy();
		}
	}
}
