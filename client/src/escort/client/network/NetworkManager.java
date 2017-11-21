package escort.client.network;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import escort.client.main.Client;
import escort.client.network.protocols.ClientGeneric;
import escort.client.network.protocols.ClientTCP;
import escort.common.network.Message;
import escort.common.network.MessageControl;

/**
 * Establishes connection to the server and starts the sending and receiving
 * threads based on the message control object received.
 * 
 * @author Ahmed Bhallo
 *
 */
public class NetworkManager {

	/**
	 * Using a mixture of TCP AND UDP to connect.
	 */
	private static final int MIXED_METHOD = 0;

	/**
	 * Using only TCP to connect.
	 */
	private static final int TCP_METHOD = 1;

	/**
	 * The client object.
	 */
	private Client client;

	/**
	 * Receives messages from the server.
	 */
	private ClientReceiver receiver;

	/**
	 * Sends messages to the server.
	 */
	private ClientSender sender;

	/**
	 * Used to facilitate communication between client and server.
	 */
	private MessageControl msgControl;

	/**
	 * This player's playerID.
	 */
	private int playerID;

	/**
	 * This player's username.
	 */
	private String username;

	private int[] playerIDInLobby = new int[0];
	private String[] playerNameInLobby = new String[0];
	private final List<String> lobbyMessageCache = new ArrayList<>();
	private boolean inLobby = false;

	/**
	 * Instantiates a new Network Manager object. Does not connect until
	 * establish connection is called.
	 * 
	 * @param client
	 *            The client object
	 */
	public NetworkManager(Client client) {
		this.client = client;
	}

	/**
	 * Establishes a connection to a desired server and port. Takes in the
	 * client's desired username. It is expected that the username has been
	 * validated by the client already. The server will reject this connection
	 * if the username is not valid. The desired connection method is speicified
	 * by the client. Throws an IOException if connection is not established.
	 * 
	 * @param username
	 *            The client's desired username.
	 * @param serverName
	 *            The client's server name destination.
	 * @param port
	 *            The desired port number.
	 * @param connMethod
	 *            The dessired connection method
	 * @throws IOException
	 *             Thrown when connection cannot be established by the server,
	 *             or if the server rejects this connection.
	 */
	public void establishConnection(String username, String serverName, int port, int connMethod) throws IOException {
		switch (connMethod) {
		case MIXED_METHOD:
			msgControl = ClientGeneric.getMessageControl(client, serverName, port);
			break;
		case TCP_METHOD:
			msgControl = ClientTCP.getMessageControl(client, serverName, port);
			break;
		default:
			System.err.println("Unimplemented connection method");
			break;
		}

		if (msgControl == null) {
			throw new IOException("Could not establish connection");
		}

		// Create and start the receiving thread.
		receiver = new ClientReceiver(this, msgControl);
		new Thread(receiver).start();

		// Create and start the sending thread.
		sender = new ClientSender(this, msgControl);
		new Thread(sender).start();

		// Sends a message to the server to request to join.
		msgControl.sendMessage(new Message(Message.PLAYER_REQUESTID, null, new String[] { username }));
	}

	/**
	 * Called when the connection has been accepted by the server.
	 * 
	 * @param playerID
	 *            This client's player ID.
	 * @param username
	 *            This client's username.
	 */
	public void playerAccepted(int playerID, String username) {
		this.playerID = playerID;
		this.username = username;
		// Valid connection and username. Display the main menu now.
		client.getMenuManager().displayMainMenu();
	}

	/**
	 * Sends a request to the server to create a lobby
	 * @param name The name of the lobby
	 * @param password The password of the lobby
	 * @throws IOException
	 */
	public void createLobby(String name, String password) throws IOException {
		msgControl.sendMessage(new Message(Message.LOBBY_NEW, null, new String[] { name, password }));
	}

	/**
	 * Sends a reuquest to the server to start a game
	 * @throws IOException
	 */
	public void startGame() throws IOException {
		msgControl.sendMessage(new Message(Message.GAME_START, null, null));
	}

	/**
	 * Get the username of this client.
	 * 
	 * @return The username of this client.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Gets the player id of this client.
	 * 
	 * @return The player id of this client.
	 */
	public int getPlayerID() {
		return playerID;
	}

	/**
	 * Gets the client object of this client.
	 * 
	 * @return The client object of this client.
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * 5 Gets the client sender object.
	 * 
	 * @return The client sending object.
	 */
	public ClientSender getSender() {
		return sender;
	}

	/**
	 * Sets whether or not the client is in a lobby
	 * 
	 * @param inLobby
	 *            Whether the client is in a lobby
	 */
	public void setInLobby(boolean inLobby) {
		this.inLobby = inLobby;
	}

	/**
	 * @return True iff the client is in a lobby
	 */
	public boolean isInLobby() {
		return inLobby;
	}

	/**
	 * @return array of player id in lobby
	 */
	public int[] getPlayerIDInLobby() {
		return playerIDInLobby;
	}

	/**
	 * @return array of player names in lobby
	 */
	public String[] getPlayerNameInLobby() {
		return playerNameInLobby;
	}

	/**
	 * Sets arrays of players in lobby
	 * 
	 * @param ints
	 *            array of player id in lobby
	 * @param strings
	 *            array of player name in lobby
	 */
	public void setPlayersInLobby(int[] ints, String[] strings) {
		playerIDInLobby = ints;
		playerNameInLobby = strings;
	}

	/**
	 * @return stored messages list
	 */
	public List<String> getLobbyMessageCache() {
		return lobbyMessageCache;
	}

	/**
	 * @return the message control object.	
	 */
	public MessageControl getMsgControl() {
		return msgControl;
	}

}
