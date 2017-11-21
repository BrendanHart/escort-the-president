package escort.server.network;
import java.io.IOException;
import java.security.SecureRandom;
import escort.common.game.lobby.LobbyMessageConstants;
import escort.common.game.lobby.NameValidation;
import escort.common.network.MalformedMessageException;
import escort.common.network.Message;
import escort.common.network.MessageControl;
import escort.common.systime.SystemTime;
import escort.server.game.Game;
import escort.server.lobby.LobbyConfiguration;

/**
 * This thread handles a player
 * @author Kwong Hei Tsang
 */
public class Player extends Thread {
	
	private final MessageControl control;
	private final ServerSide ss;
	private boolean initialized;
	private int playerID;
	private String playername;
	private final PlayerSender sender;
	private int lobbyID;
	private final Object gamestatusLock;
	private Game game;
	private int unitID = -1;
	
	//protocol switching attributes
	private Player anotherclient;
	private final byte[] authkey;
	private boolean primary = true;
	private final Thread refreshThread;
	
	// message count in last 10 seconds
	private long laststarttime;
	private int messagecount;
	private static final int MAX_MSG_IN_10_S = 400;
	
	// chat message throttle
	private long laststarttimechat;
	private int chatcount;
	
	/**
	 * Construct a player object
	 * @param control The message control
	 * @param ss The server side object for items shared among players
	 */
	public Player(MessageControl control, ServerSide ss) {
		super("Player");
		this.control = control;
		this.ss = ss;
		this.initialized = false;
		this.sender = new PlayerSender(this);
		this.lobbyID = -1;
		this.playerID = -1;
		this.playername = null;
		this.gamestatusLock = new Object();
		this.game = null;
		
		//another session
		this.anotherclient = null;
		this.authkey = new byte[32];
		this.refreshThread = new Thread(() -> refresh(), "Refreshing thread");
		
		//initialize laststarttime and message count
		this.laststarttime = SystemTime.milliTime();
		this.messagecount = 0;
		
		//initialize laststarttimechat and chat count
		this.laststarttimechat = SystemTime.milliTime();
		this.chatcount = 0;
	}
	
	/**
	 * Get the message control
	 * @return The message control
	 */
	public MessageControl getControl() {
		return this.control;
	}
	
	/**
	 * The method running by this thread
	 */
	@Override
	public void run() {
		this.sender.start();
		boolean running = true;
		try {
			while (running) {
				// read the message
				Message command = null;
				try {
					command = this.control.receiveMessage();
				} catch (MalformedMessageException e) {
				}
				
				
				if (!this.sender.isAlive()) {
					// There is a problem with the sender thread
					// break the loop
					break;
				}
				
				// Get the time since last counting
				long time = SystemTime.milliTime() - this.laststarttime;
				if(time > 10000){
					// restart the cycle
					this.laststarttime = SystemTime.milliTime();
					this.messagecount = 1;
				}else if(++this.messagecount > MAX_MSG_IN_10_S){
					// message is too frequent
					this.getSender().put(new Message(Message.MESSAGE_TOO_FREQUENT, null, null));
					this.getSender().put(new Message(Message.EXIT, null, null));
					if(this.anotherclient != null){
						// interrupt the other thread as well
						this.anotherclient.interrupt();
					}
					break;
				}
				
				if (command == null) {
					// something wrong with the received message
				}else if (command.messageType == Message.PROTOCOL_SWITCH){
					// Doing protocol switch
					this.protocolSwitchRequest();
				}else if (command.messageType == Message.PROTOCOL_SWITCH_FIND){
					if(command.getInts().length == 1 && command.getStrings().length == 1 && command.getStrings()[0] != null){
						//check message
						Player player = this.ss.getProtocolSwitch().get(command.getInts()[0]);
						//Get player
						if(player != null){
							player.setAnotherClient(command.getInts()[0],this, command.getStrings()[0]);
						}
					}
				} else if (command.messageType == Message.KEEP_ALIVE) {
					// System.out.println("Keepalive received");
				} else if (command.messageType == Message.EXIT) {
					break;
				} else if (command.messageType == Message.PRINT && command.getStrings() != null) {
					System.out.println("Received print request: ");
					for (String values : command.getStrings()) {
						System.out.println(values);
					}
				} else if (command.messageType == Message.LOBBY_LIST) {
					// request the list of lobbies
					Message lobbylist = this.ss.getLobbyManagement().getLobbyListReturnMessage();
					this.control.sendMessage(lobbylist);
				} else if (command.messageType == Message.LOBBY_NEW) {
					// Create a new lobby
					createLobby(command);
				} else if (command.messageType == Message.LOBBY_JOIN) {
					// Join lobby
					joinLobby(command);
				} else if (command.messageType == Message.LOBBY_LEAVE) {
					// Leave lobby
					leaveLobby();
				} else if (command.messageType == Message.LOBBY_SET_PASSWORD) {
					setLobbyPassword(command);
				} else if (command.messageType == Message.LOBBY_KICK_PLAYER) {
					kickPlayer(command);
				} else if (command.messageType == Message.PLAYER_REQUESTID) {
					// request player name and id
					requestID(command);
				} else if (command.messageType == Message.LOBBY_MESSAGE) {
					boolean process = true;
					long chattime = SystemTime.milliTime();
					if(chattime - this.laststarttimechat > 1000){
						// restart a cycle
						this.laststarttimechat = chattime;
						this.chatcount = 1;
					}else if(++this.chatcount > LobbyConfiguration.MAX_CHAT_IN_S){
						// Do nothing, chat too frequent
						process = false;
					}
					
					if (process && this.primary && command.getStrings() != null && command.getStrings().length > 1 && command.getStrings()[1] != null) {
						// send message. we will ignore their first parameter
						// (username)
						this.ss.getLobbyManagement().sendMessage(this, command.getStrings()[1]);
					}
				} else if (command.messageType == Message.GAME_START) {
					this.ss.getLobbyManagement().startGame(this);
				} else if (command.messageType == Message.LOBBY_SETTINGS_CHANGE) {
					if (command.getInts() != null && command.getInts().length == 4) {
						this.ss.getLobbyManagement().set(this, command.getInts()[0], command.getInts()[1], command.getInts()[2], command.getInts()[3]);
					}
				} else {
					//pass to game logic
					passToGameLogic(command);
				}
				if (this.isInterrupted()) {
					break;
				}
			}
		} catch (IOException | ClassNotFoundException e) {
		} finally {
			// close the control as required
			try {
				this.control.close();
			} catch (IOException e) {
			}
		}
		// delete player
		if(this.primary)
			this.ss.removePlayer(this);
		// Leave lobby if needed
		if (this.lobbyID > 0 && this.primary) {
			this.leaveLobby();
		}
		// leave game if needed
		Game game = this.getGame();
		if (game != null && this.primary) {
			game.playerConnectionBroken(this);
		}
		
		//remove connection
		this.ss.removeConnection(this);
		
		//interrupt thread for refreshing another connections
		if(this.refreshThread.isAlive()){
			this.refreshThread.interrupt();
		}
		// Do general leaving procedure, interrupt the sender
		this.sender.interrupt();
		// System.out.println("Player exited.");
	}
	
	// pass command to game logic
	private void passToGameLogic(Message command){
		if(command.getInts() == null || command.getInts().length == 0){
			return;
		}
		
		command.getInts()[0] = this.getUnitID();
		// See if game is empty
		Game game = this.getGame();
		if (game != null) {
			game.getQueuer().add(command);
		}
	}
	
	/**
	 * Request a user ID
	 * @param command The message command
	 */
	private void requestID(Message command) {
		// check if the command is properly formed
		boolean good = command.getStrings() != null && command.getStrings().length == 1
				&& command.getStrings()[0] != null && !this.initialized && this.primary;
		if (!good) {
			return;
		}
		// check if the name is valid
		if (!NameValidation.validatePlayer(command.getStrings()[0])) {
			this.sender.put(new Message(Message.PLAYER_REJECT, new int[] { 0 }, null));
			return;
		}
		// check if the name is available and request player id
		int result = this.ss.requestID(command.getStrings()[0], this);
		if (result == -1) {
			this.sender.put(new Message(Message.PLAYER_REJECT, new int[] { 1 }, null));
			return;
		}
		// name accepted, set id and name
		this.playername = command.getStrings()[0];
		this.playerID = result;
		this.initialized = true;
		this.setName("Player ID: " + this.playerID + ", Name: " + this.playername);
		// notify the client
		this.sender.put(new Message(Message.PLAYER_ACCEPT, new int[] { result }, new String[] { this.playername }));
	}
	
	/**
	 * Create a lobby
	 * @param command The command to create the lobby
	 */
	public void createLobby(Message command) {
		// check if the command is properly formed
		boolean good = command.getStrings() != null && command.getStrings().length == 2
				&& command.getStrings()[0] != null && command.getStrings()[1] != null && this.initialized;
		if (!good) {
			return;
		}
		this.ss.getLobbyManagement().createLobby(this, command.getStrings()[0], command.getStrings()[1]);
	}
	
	/**
	 * Join a lobby
	 * @param command The command to join a lobby
	 */
	public void joinLobby(Message command) {
		// check if the command is properly formed
		boolean good = command.getStrings() != null && command.getStrings().length == 1
				&& command.getStrings()[0] != null && command.getInts() != null && command.getInts().length == 1
				&& this.initialized;
		if (!good) {
			return;
		}
		// Join lobby
		this.ss.getLobbyManagement().joinLobby(this, command.getInts()[0], command.getStrings()[0]);
	}
	
	/**
	 * Leave the joined lobby
	 */
	public void leaveLobby() {
		this.ss.getLobbyManagement().leaveLobby(this, LobbyMessageConstants.ACTIVE);
	}
	
	/**
	 * Get the player ID of this player
	 * @return The player ID of this player
	 */
	public int getPlayerID() {
		return this.playerID;
	}
	
	/**
	 * Get the sender thread of the player
	 * @return The sender thread of the player
	 */
	public PlayerSender getSender() {
		return this.sender;
	}
	
	/**
	 * Get the name of this player
	 * @return The name of this player
	 */
	public String getPlayerName() {
		return this.playername;
	}
	
	/**
	 * Get lobby ID
	 * @return The name of this lobby ID, -1 when not joined to any lobby
	 */
	public int getLobbyID() {
		synchronized (this.gamestatusLock) {
			return this.lobbyID;
		}
	}
	
	/**
	 * Set the lobby ID of this player
	 * @param lobbyID The lobby joined
	 */
	public void setLobbyID(int lobbyID) {
		synchronized (this.gamestatusLock) {
			this.lobbyID = lobbyID;
		}
	}
	
	/**
	 * Set the game object of this player
	 * @param game The game object
	 */
	public void setGame(Game game) {
		synchronized (this.gamestatusLock) {
			if(this.anotherclient != null){
				this.anotherclient.setGame(game);
			}
			this.game = game;
		}
	}
	
	/**
	 * Get the game object
	 * @return The game object
	 */
	public Game getGame() {
		synchronized (this.gamestatusLock) {
			return this.game;
		}
	}
	
	/**
	 * Get the unit ID of the player
	 * @return the unit of the player
	 */
	public int getUnitID() {
		return unitID;
	}
	
	/**
	 * Set Unit ID of the player
	 * @param unitID the unit of the player
	 */
	public void setUnitID(int unitID) {
		if(this.anotherclient != null){
			this.anotherclient.setUnitID(unitID);
		}
		this.unitID = unitID;
	}
	
	/**
	 * Set the password of the lobby
	 * @param command The command for the operation
	 */
	public void setLobbyPassword(Message command) {
		boolean good = command.getStrings().length == 1 && command.getStrings()[0] != null && this.initialized;
		if (!good) {
			return;
		}
		this.ss.getLobbyManagement().setPassword(this, command.getStrings()[0]);
	}
	
	/**
	 * Kick a player
	 * @param command The command kicking the player
	 */
	public void kickPlayer(Message command) {
		boolean good = command.getInts().length == 1 && this.initialized;
		if (!good) {
			return;
		}
		this.ss.getLobbyManagement().kickPlayer(this, this.ss.getPlayer(command.getInts()[0]));
	}
	
	/**
	 * Create a protocol switch request
	 */
	private void protocolSwitchRequest(){
		//generate secure key
		SecureRandom rand = new SecureRandom();
		rand.nextBytes(this.authkey);
		
		//request protocol switch ID
		int id = this.ss.putProtocolSwitchRequest(this);
		
		//send the key to the client
		this.getSender().put(new Message(Message.PROTOCOL_SWITCH_RESPONSE,new int[]{id},
				new String[]{new String(this.authkey)}));
	}
	
	/**
	 * Configure the other client session
	 * @param id The protocol switch request ID
	 * @param anotherclient The other server receiver of the player
	 * @param auth The authentication key
	 */
	private void setAnotherClient(int id, Player anotherclient, String auth){
		//authentication
		if(this == anotherclient || anotherclient.initialized || !auth.equals(new String(this.authkey))){
			//authentication failure or something went wrong
			return;
		}
		
		// so far so good
		this.anotherclient = anotherclient;
		this.getSender().setAnotherSender(anotherclient.getSender());
		this.anotherclient.primary = false;
		if(!this.refreshThread.isAlive()){
			this.refreshThread.start();
		}
		this.getSender().put(new Message(Message.PROTOCOL_SWITCH_DONE,null,null));
		this.ss.getProtocolSwitch().remove(id,this);
	}
	
	/**
	 * Refresh another client connection's information
	 */
	public void refresh(){
		try{
			while(true){
				//Set another sender and another client
				if(this.anotherclient != null){
					this.anotherclient.playerID = this.playerID;
					this.anotherclient.setGame(this.getGame());
					this.anotherclient.setUnitID(this.getUnitID());
				}
				
				Thread.sleep(1000);
			}
		}catch(InterruptedException e){
		}
		
	}
}