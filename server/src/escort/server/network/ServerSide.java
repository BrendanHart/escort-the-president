package escort.server.network;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import escort.common.game.map.MapLoader;
import escort.common.network.Message;
import escort.server.lobby.LobbyManagement;

/**
 * This class is intended for something common among players
 * 
 * @author Kwong Hei Tsang
 *
 */
public class ServerSide {

	private final Server server;
	private final LobbyManagement lobbymgmt;
	private final Map<Integer, Player> players;
	private final Map<String, Integer> nametoid;
	private final Set<String> playerNames;
	private final Object nameslock;
	private int nextplayerid;
	
	private final Map<Integer, Player> protocolswitch;
	private int nextswitchid;
	private final Object switchlock;

	/**
	 * Construct a server side object and create necessary items for the server
	 * @param server The server object managing the connections
	 */
	public ServerSide(Server server) {
		try {
			new MapLoader().load();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.lobbymgmt = new LobbyManagement();
		this.server = server;
		this.players = new ConcurrentHashMap<Integer, Player>();
		this.nametoid = new ConcurrentHashMap<String, Integer>();
		this.playerNames = new HashSet<String>();
		this.nextplayerid = 1;
		this.nameslock = new Object();
		
		this.protocolswitch = new ConcurrentHashMap<Integer, Player>();
		this.nextswitchid = 0;
		this.switchlock = new Object();
	}

	/**
	 * Shut down the server
	 */
	public void shutdownServer() {
		// Notify all players
		for (Player player : this.players.values()) {
			player.getSender().put(new Message(Message.EXIT, new int[] { 1 }, null));
		}

		// shutdown lobby management
		this.lobbymgmt.stopLobbyManagement();

		// shutdown server
		if (this.server != null) {
			this.server.interrupt();
		}
	}

	/**
	 * Get Lobby management
	 * @return the lobby management object
	 */
	public LobbyManagement getLobbyManagement() {
		return this.lobbymgmt;
	}

	/**
	 * Get the players
	 * @return The players
	 */
	public List<Player> getPlayers() {
		synchronized(this.nameslock){
			return new ArrayList<Player>(this.players.values());
		}
	}
	
	/**
	 * Get the player by ID
	 * @param id The player ID
	 * @return The player object
	 */
	public Player getPlayer(int id){
		synchronized(this.nameslock){
			return this.players.get(id);
		}
	}

	/**
	 * Request a player ID
	 * @param name The name of player
	 * @return a new player ID
	 */
	public int requestID(String name, Player player) {
		synchronized (this.nameslock) {
			if (this.playerNames.contains(name)) {
				return -1;
			} else {
				this.players.put(this.nextplayerid, player);
				this.playerNames.add(name);
				this.nametoid.put(name, this.nextplayerid);
				return this.nextplayerid++;
			}
		}
	}

	/**
	 * Get the map mapping player name to player id
	 * @return The player ID
	 */
	public Map<String, Integer> getNameToID() {
		return this.nametoid;
	}

	/**
	 * Remove a player, typically when connection of a player is broken
	 * @param player the player to be removed
	 */
	public void removePlayer(Player player) {
		String playername = player.getPlayerName();
		if(playername != null){
			synchronized(this.nameslock){
				this.players.remove(player.getPlayerID(), player);
				this.nametoid.remove(playername,player.getPlayerID());
				this.playerNames.remove(playername);
			}
		}
	}
	
	/**
	 * Get protocol switch requests
	 * @return The protocol switch requests
	 */
	public Map<Integer, Player> getProtocolSwitch(){
		return this.protocolswitch;
	}
	
	/**
	 * Get the ID of protocol switch request
	 * @param player The player
	 * @return The protocol switch request ID
	 */
	public int putProtocolSwitchRequest(Player player){
		synchronized(this.switchlock){
			this.nextswitchid++;
			this.protocolswitch.put(this.nextswitchid, player);
			return this.nextswitchid;
		}
	}
	
	/**
	 * Remove Connection
	 */
	public void removeConnection(Player player){
		if(player.getControl().protocol().equals("tcp")){
			this.server.removeTCPClient(player.getControl());
		}
	}
}
