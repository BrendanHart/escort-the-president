package escort.common.game.lobby;

/**
 * Validate whether the name is valid
 * 
 * @author Kwong Hei Tsang
 * @author Ahmed Bhallo
 *
 */
public class NameValidation {

	public static final int PLAYER_MAX_LENGTH = 12;
	public static final int LOBBY_MAX_LENGTH = 20;

	/**
	 * Checks if a username is valid. Valid if length is less than or equal to
	 * the max length, greater than 0, and alphanumeric.
	 * 
	 * @param name
	 *            The name to check
	 * @return True iff the username is valid.
	 */
	public static boolean validatePlayer(String name) {
		if (name.length() > PLAYER_MAX_LENGTH || name.equals("")) {
			return false;
		}
		return isAlphanumeric(name);
	}

	/**
	 * Checks if a lobby name is valid. Valid if length is less than or equal to
	 * the max length, greater than 0, and alphanumeric.
	 * 
	 * @param name
	 *            The name to check
	 * @return True iff the lobby ame is valid.
	 */
	public static boolean validateLobby(String name) {
		if (name.length() > LOBBY_MAX_LENGTH || name.equals("")) {
			return false;
		}
		return isAlphanumeric(name);
	}

	/**
	 * Determines if an input only contains alphanumeric characters.
	 * 
	 * @param text
	 *            The text to check
	 * @return True iff the input text is alphanumeric
	 */
	private static boolean isAlphanumeric(String text) {
		for (int i = 0; i < text.length(); i++) {
			if (!Character.isLetterOrDigit(text.charAt(i))) {
				return false;
			}
		}
		return true;
	}
}
