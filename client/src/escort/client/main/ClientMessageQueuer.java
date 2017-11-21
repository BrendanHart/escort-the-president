package escort.client.main;

import java.util.concurrent.ConcurrentLinkedQueue;

import escort.common.game.lobby.LobbyMessageConstants;
import escort.common.network.Message;

/**
 * Contains a queue for Message objects that are parsed and appropriate actions
 * are taken. Game manager and Menu manager are not thread-safe, so this queue
 * is necessary in order to execute commands based on messages received from the
 * receiving thread.
 * 
 * @author Ahmed Bhallo
 * @author Kwong Hei Tsang
 *
 */
public class ClientMessageQueuer {

	/**
	 * The client object
	 */
	private Client client;

	/**
	 * A concurrent queue for messages.
	 */
	private final ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<>();

	/**
	 * Instantiates a new Client Message Queuer object
	 * 
	 * @param client
	 */
	public ClientMessageQueuer(Client client) {
		this.client = client;
	}

	/**
	 * Loops through all messages in the queue and performs appropriate actions.
	 */
	public void update() {
		Message msg = null;
		// Keep dequeueing from the queue until end is reached.
		while ((msg = queue.poll()) != null) {
			if (msg.messageType != Message.UNIT_MOVED) {
			}
			switch (msg.messageType) {
			case Message.KEEP_ALIVE:
				break;
			case Message.PLAYER_REJECT:
				// Our username has been rejected.
				client.getMenuManager().getSetupMenu().rejectUsername();
				break;
			case Message.PLAYER_ACCEPT:
				// Our username has been accepted.
				client.getNetworkManager().playerAccepted(msg.getInts()[0], msg.getStrings()[0]);
				client.getMenuManager().getSetupMenu().resetForm();
				break;
			case Message.LOBBY_CREATED:
				// We have succcessfully created or joined a lobby.
				client.getMenuManager().createDisplayLobbyMenu(msg.getInts()[0], msg.getStrings()[0], msg.getInts()[1]);
				client.getMenuManager().getLobbyMenu().updatePlayerList(new int[] { msg.getInts()[1] },
						new String[] { client.getNetworkManager().getUsername() });
				client.getNetworkManager().setPlayersInLobby(new int[] { msg.getInts()[1] },
						new String[] { client.getNetworkManager().getUsername() });
				client.getMenuManager().getLobbyCreation().restoreToDefault();
				client.getNetworkManager().setInLobby(true);
				break;
			case Message.LOBBY_JOINED:
				// We have succcessfully created or joined a lobby.
				client.getMenuManager().createDisplayLobbyMenu(msg.getInts()[0], msg.getStrings()[0], msg.getInts()[1]);
				client.setDialog(null);
				client.getNetworkManager().setInLobby(true);
				break;
			case Message.GAME_READY:
				// The owner has started the game. The game is ready.
				client.getGameManager().createNewGame(msg.getGameState(), msg.getInts()[0]);
				break;
			case Message.LOBBY_LIST_RESULT:
				// The lobby has returned the list of lobbies.
				client.getMenuManager().getLobbyList().updateList(msg.getInts(), msg.getStrings());
				break;
			case Message.UNIT_MOVED:
				// A unit has moved.
				int unitID = msg.getInts()[0];
				boolean force = msg.getInts()[1] == 1;
				double x = msg.getDoubles()[0];
				double y = msg.getDoubles()[1];
				double dir = msg.getDoubles()[2];
				double xVel = msg.getDoubles()[3];
				double yVel = msg.getDoubles()[4];
				client.getGameManager().updateRemoteUnit(unitID, x, y, dir, xVel, yVel, force);
				break;
			case Message.PRES_FOLLOW:
				// The president is following a unit.
				client.getGameManager().presidentFollow(msg.getInts()[0], msg.getInts()[1]);
				break;
			case Message.PRES_UNFOLLOW:
				// The president has stopped following a unit.
				client.getGameManager().presidentUnfollow(msg.getInts()[0]);
				break;
			case Message.COUNTDOWN_TICK:
				// The countdown counter has changed.
				client.getGameManager().getHUDManager().updateCount(msg.getInts()[0]);
				break;
			case Message.GAME_START:
				// The countdown has reached 0 and the game has started.
				client.getGameManager().startGame();
				break;
			case Message.LOBBY_MESSAGE:
				// Someone has sent a message in the lobby.
				client.getMenuManager().getChat().messageReceived(msg.getStrings()[0], msg.getStrings()[1]);
				client.getNetworkManager().getLobbyMessageCache().add(msg.getStrings()[0] + ": " + msg.getStrings()[1]);
				break;
			case Message.GRENADE_ID:
				// A grenade id has been requested by a unit. That unit is now
				// holding the grenade.
				client.getGameManager().grenadeCreated(msg.getInts()[0], msg.getInts()[1]);
				break;
			case Message.THROW_GRENADE:
				// A unit has thrown a grenade.
				client.getGameManager().grenadeThrown(msg.getInts()[0]);
				break;
			case Message.GRENADE_EXPLODED:
				// Grenade has exploded
				// A unit has taken damage
				client.getGameManager().grenadeExplode(msg.getInts()[0], msg.getInts()[1]);
				// client.getGameManager().getClientUnit().setHP(msg.getDoubles()[0]);
				break;
			case Message.HP_LEFT:
				client.getGameManager().updateHP(msg.getInts()[0], msg.getInts()[1]);
				break;
			case Message.RESPAWN:
				client.getGameManager().respawn(msg.getInts()[0], msg.getInts()[1], msg.getDoubles());
				break;
			case Message.POWERUP_ASSIGNMENT:
				client.getGameManager().assignPowerUps(msg.getInts(), msg.getDoubles());
				break;
			case Message.POWERUP_USED:
				client.getGameManager().powerUpUsed(msg.getInts()[0], msg.getInts()[1]);
				break;
			case Message.PISTOL_BULLET:
				client.getGameManager().pistolBulletCreated(msg.getInts()[0], msg.getInts()[1]);
				break;
			case Message.MG_BULLET:
				client.getGameManager().mgBulletCreated(msg.getInts()[0], msg.getInts()[1]);
				break;
			case Message.GAME_END:
				client.getGameManager().endGame(msg.getInts()[0]);
				break;
			case Message.WEAPON_SWITCH_ACK:
				client.getGameManager().weaponSwitched(msg.getInts()[0], msg.getInts()[1]);
				break;
			case Message.WEAPON_RELOAD_ACK:
				client.getGameManager().weaponReloaded(msg.getInts()[0]);
				break;
			case Message.LOBBY_SETTINGS:
				client.getMenuManager().getLobbyMenu().updateSettings(msg.getInts()[0], msg.getInts()[1],
						msg.getInts()[2], msg.getInts()[3]);
				break;
			case Message.PLAYER_LIST_UPDATE:
				client.getMenuManager().getLobbyMenu().updatePlayerList(msg.getInts(), msg.getStrings());
				client.getNetworkManager().setPlayersInLobby(msg.getInts(), msg.getStrings());
				break;
			case Message.SYSTEM_MESSAGE:
				client.getMenuManager().getChat().systemMessageReceived(msg.getStrings()[0]);
				break;
			case Message.LOBBY_LEFT:
				switch (msg.getInts()[0]) {
				case LobbyMessageConstants.ACTIVE:
					// This has already been accounted for so we do not need to
					// do anything.
					break;
				case LobbyMessageConstants.INACTIVITY:
					client.getMenuManager().getLobbyMenu().kickedForInactivity();
					break;
				case LobbyMessageConstants.KICKED:
					client.getMenuManager().getLobbyMenu().kickedByOwner();
					break;
				case LobbyMessageConstants.MASTER_LEFT:
					client.getMenuManager().getLobbyMenu().ownerLeft();
					break;
				}
				client.getNetworkManager().setInLobby(false);
				client.getNetworkManager().setPlayersInLobby(new int[0], new String[0]);
				client.getNetworkManager().getLobbyMessageCache().clear();
				break;
			case Message.LOBBY_AUTHFAIL:
				switch (msg.getInts()[0]) {
				case LobbyMessageConstants.KICKED_BEFORE:
					client.getMenuManager().getLobbyList().previouslyKicked();
					break;
				case LobbyMessageConstants.WRONG_PASSWORD:
					client.getMenuManager().getLobbyList().invalidPassword();
					break;
				}
				break;
			case Message.SHIELD_HP_LEFT:
				client.getGameManager().updateShieldHP(msg.getInts()[0], msg.getInts()[1]);
				break;
			default:
				break;
			}
		}
		queue.clear();
	}

	/**
	 * Adds a message to the queue.
	 * 
	 * @param msg
	 *            The message to be added.
	 */
	public void add(Message msg) {
		queue.offer(msg);
	}

}
