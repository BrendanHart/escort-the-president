package escort.client.input;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Contains all typed keys, action keys, mouse buttons to be passed around to
 * players and UI Components. This class does not handle bindings.
 * 
 * @author Ahmed Bhallo
 *
 */
public final class Inputs {

	public Inputs() {

	}

	/**
	 * x-coordinate of the mouse
	 */
	public int mouseX = 0;

	/**
	 * y-coordinate of the mouse
	 */
	public int mouseY = 0;

	/**
	 * Left click button
	 */
	public final MouseButton leftClick = new MouseButton("Left Click/Shoot");

	/**
	 * Mouse wheel scroll up
	 */
	public final Key scrollUp = new Key("Scroll Up");

	/**
	 * Mouse wheel scroll down
	 */
	public final Key scrollDown = new Key("Scroll Down");

	/**
	 * Right arrow. Used only for InputField caret position. Not to be used as
	 * in-game controls.
	 */
	public final Key rightArrow = new Key("Right arrow");

	/**
	 * Left arrow. Used only for InputField caret position. Not to be used as
	 * in-game controls.
	 */
	public final Key leftArrow = new Key("Left arrow");

	/**
	 * Typed keys map. <Key, True iff the key is pressed>
	 */
	public final Map<Character, Boolean> typedInput = new ConcurrentHashMap<>();

	/**
	 * Shoot weapon
	 */
	public final Key shoot = new Key("Fire weapon");

	/**
	 * Move up
	 */
	public final Key up = new Key("Move Up");

	/**
	 * Move down
	 */
	public final Key down = new Key("Move Down");

	/**
	 * Move left
	 */
	public final Key left = new Key("Move Left");

	/**
	 * Move right
	 */
	public final Key right = new Key("Move Right");

	/**
	 * Unlock/lock the camera
	 */
	public final Key cameraLock = new Key("Toggle camera lock");

	/**
	 * Cook/release grenade.
	 */
	public final Key grenade = new Key("Grenade action");

	/**
	 * Change to pistol
	 */
	public final Key pistolKey = new Key("Change to Pistol");

	/**
	 * Change to machine gun
	 */
	public final Key mgKey = new Key("Change to MG");

	/**
	 * Change to blast shield
	 */
	public final Key shieldKey = new Key("Change to Shield");

	/**
	 * Scroll weapon scroll up
	 */
	public final Key weaponScrollUp = new Key("Weapon Scroll Up");

	/**
	 * Scroll weapon scroll down
	 */
	public final Key weaponScrollDown = new Key("Weapon Scroll Down");

	/**
	 * Follow a president
	 */
	public final Key followKey = new Key("Follow/unfollow Pres");

	/**
	 * Reload
	 */
	public final Key reload = new Key("Reloading gun");

	/**
	 * Open the ingame settings menu
	 */
	public final Key esc = new Key("Escape");
}
