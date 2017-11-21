package escort.client.input;

/**
 * A key object. This class does not handle bindings. A key object does not care
 * about its binding. This is handles by the InputHandler class. A key only
 * knows whether or not it is pressed.
 * 
 * @author Ahmed Bhallo
 *
 */
public class Key {

	/**
	 * The name of the key.
	 */
	private final String keyName;

	/**
	 * Whether or not the key is pressed.
	 */
	private boolean pressed = false;

	/**
	 * Instantiates a new key object.
	 * 
	 * @param keyName
	 *            The name of the key.
	 */
	public Key(String keyName) {
		this.keyName = keyName;
	}

	/**
	 * Sets the pressed state of the key.
	 * 
	 * @param isPressed
	 *            The new pressedState.
	 */
	public void setPressed(boolean isPressed) {
		this.pressed = isPressed;
	}

	/**
	 * Returns whether or not the key is pressed.
	 * 
	 * @return True iff the key is pressed.
	 */
	public boolean isPressed() {
		return pressed;
	}

	/**
	 * Returns the hashcode of the key. It is computed as the hashcode of the
	 * name of the key.
	 * 
	 * @return The hashcode of the key.
	 */
	public int hashCode() {
		return keyName.hashCode();
	}

	/**
	 * If two keys have the same keyName, they are considered the same key.
	 * 
	 * @return True if the the input object is equal to this one.
	 */
	public boolean equals(Object o) {
		if (!(o instanceof Key)) {
			return false;
		}
		return keyName.equals(((Key) o).keyName);
	}

	/**
	 * Returns the String representation of this key.
	 * 
	 * @return The String representation of this key.
	 */
	public String toString() {
		return "Key object for " + keyName;
	}
}
