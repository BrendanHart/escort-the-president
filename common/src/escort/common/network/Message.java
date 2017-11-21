package escort.common.network;

import java.io.Serializable;

import escort.common.game.GameState;

/**
 * The command to be sent between the client and server
 * 
 * @author Kwong Hei Tsang
 *
 */
public class Message implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3252803150932018384L;

	// non critical messages:
	// unit_moved (to be renamed to mob_moved)
	// unit_reload
	// unit_weapon_switch

	// protocol control messages, never touch
	public static final int PROTOCOL_SWITCH = 201;
	public static final int PROTOCOL_SWITCH_RESPONSE = 202;
	public static final int PROTOCOL_SWITCH_FIND = 203;
	public static final int PROTOCOL_SWITCH_DONE = 204;
	public static final int MESSAGE_TOO_FREQUENT = 205;

	// common messages
	public static final int EXIT = 1;
	public static final int PRINT = 2;
	public static final int KEEP_ALIVE = 0;
	public static final int LOBBY_MESSAGE = 200;

	// server to client
	public static final int PLAYER_REJECT = 3;
	public static final int PLAYER_ACCEPT = 4;
	public static final int LOBBY_IDINVALID = 5;
	public static final int LOBBY_LIST_RESULT = 6;
	public static final int LOBBY_AUTHFAIL = 7;
	public static final int LOBBY_CREATED = 8;
	public static final int LOBBY_LEFT = 9;
	public static final int LOBBY_JOINED = 10;
	public static final int PLAYER_LIST_UPDATE = 11;
	public static final int PLAYER_LEFT = 12;
	public static final int UNIT_MOVED = 13;
	public static final int PLAYER_MESSAGE = 14;
	public static final int UNIT_WEAPON_SWITCH = 18;
	public static final int UNIT_ROLLED = 19;
	public static final int PRESIDENT_IS_SAFE = 20;
	public static final int GAME_START = 21;
	public static final int MENACE_START = 22;
	public static final int MENACE_END = 23;
	public static final int UNIT_TAKEN_DAMAGE = 24;
	public static final int GAMESTATE_UPDATE = 25;
	public static final int LOBBY_PASSWORD_SET = 26;
	public static final int LOBBY_NAME_INVALID = 27;
	public static final int GAME_END = 28;
	public static final int LOBBY_GAME_ALREADY_STARTED = 29;
	public static final int LOBBY_FULL = 30;
	public static final int MESSAGE_RECEIVED = 31;
	public static final int GAME_READY = 32;
	public static final int COUNTDOWN_TICK = 33;
	public static final int LOBBY_SETTINGS = 34;
	public static final int MOB_MOVED = 37;
	public static final int GRENADE_ID = 38;
	public static final int PRES_FOLLOW = 39;
	public static final int PRES_UNFOLLOW = 40;
	public static final int UNIT_FOLLOW = 41;
	public static final int GRENADE_EXPLODED = 42;
	public static final int UNIT_UNFOLLOW = 43;
	public static final int HP_LEFT = 44;
	public static final int PISTOL_BULLET = 45;
	public static final int MG_BULLET = 46;
	public static final int RESPAWN = 47;
	public static final int WEAPON_RELOAD_ACK = 48;
	public static final int WEAPON_SWITCH_ACK = 49;
	public static final int POWERUP_ASSIGNMENT = 50;
	public static final int POWERUP_USED = 51;
	public static final int SYSTEM_MESSAGE = 52;
	public static final int SHIELD_HP_LEFT = 53;

	// client to server
	public static final int PLAYER_REQUESTID = 1000;
	public static final int LOBBY_LIST = 1001;
	public static final int LOBBY_JOIN = 1002;
	public static final int LOBBY_LEAVE = 1003;
	public static final int LOBBY_NEW = 1004;
	public static final int MOVE = 1005;
	public static final int SEND_MESSAGE = 1006;
	public static final int SHOOT = 1007;
	public static final int THROW_GRENADE = 1008;
	public static final int RELOAD = 1009;
	public static final int ROLL = 1011;
	public static final int LOBBY_SET_PASSWORD = 1012;
	public static final int LOBBY_KICK_PLAYER = 1013;
	public static final int LOBBY_SETTINGS_CHANGE = 1015;
	public static final int REQUEST_GRENADE_ID = 1016;
	public static final int EXPLODE_GRENADE = 1017;
	public static final int SHOOT_GUN = 1018;
	public static final int REQUEST_PISTOL_BULLET = 1019;
	public static final int REQUEST_MG_BULLET = 1020;

	private final double[] doubles;
	private final int[] ints;
	private final String[] strings;
	public final int messageType;
	private GameState gameState;

	/**
	 * Construct a message
	 * 
	 * @param messageType
	 *            The type of the message
	 * @param values
	 *            The values to be sent
	 * @param texts
	 *            The texts to be sent
	 * @param doubles
	 *            The values (double) to be sent
	 */
	public Message(int messageType, int[] values, String[] texts, double[] doubles) {
		this.messageType = messageType;
		this.ints = values;
		this.strings = texts;
		this.doubles = doubles;
	}

	/**
	 * Construct a message without doubles array
	 * 
	 * @param messageType
	 *            The message type
	 * @param values
	 *            The values
	 */
	public Message(int messageType, int[] values) {
		this(messageType, values, null, null);
	}

	/**
	 * Construct a message
	 * 
	 * @param messageType
	 *            The type of the message
	 * @param values
	 *            The values to be sent
	 * @param texts
	 *            The texts to be sent
	 */
	public Message(int messageType, int[] values, String[] texts) {
		this(messageType, values, texts, null);
	}

	/**
	 * Get the texts of this message
	 * 
	 * @return the texts
	 */
	public String[] getStrings() {
		return this.strings;
	}

	/**
	 * Get the values of this message
	 * 
	 * @return The values of this message
	 */
	public int[] getInts() {
		return this.ints;
	}

	/**
	 * Get the doubles values of this message
	 * 
	 * @return the double values of this message
	 */
	public double[] getDoubles() {
		return this.doubles;
	}

	/**
	 * Get the game state
	 * 
	 * @return The game state
	 */
	public GameState getGameState() {
		return gameState;
	}

	/**
	 * Set the game state
	 * 
	 * @param gameState
	 *            the game state
	 */
	public void setGameState(GameState gameState) {
		this.gameState = gameState;
	}

	/**
	 * Get the String representation of the message
	 */
	public String toString() {
		String result = "messageType: " + messageType;
		if (ints != null) {
			result += "\nvalues: [";
			for (int i = 0; i < ints.length; i++) {
				result += ints[i];
				if (i < ints.length - 1) {
					result += ",";
				}
			}
			result += "]";
		}
		if (strings != null) {
			result += "\ntexts: [";
			for (int i = 0; i < strings.length; i++) {
				result += strings[i];
				if (i < strings.length - 1) {
					result += ",";
				}
			}
			result += "]";
		}
		return result;
	}
}
