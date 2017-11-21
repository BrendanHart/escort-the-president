package escort.client.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.Map;

import escort.client.main.Client;
import escort.client.properties.PropertyManager;

/**
 * Detects input pressed and handles an input object. Creates a Map for mapping
 * key presses to in-game action keys.
 * 
 * @author Ahmed Bhallo
 *
 */
public class InputHandler implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {

	public static final int SCROLL_DOWN_KEY = -10;
	public static final int SCROLL_UP_KEY = -11;
	public static final int LEFT_MOUSE_KEY = -12;
	public static final int MIDDLE_MOUSE_KEY = -13;
	public static final int RIGHT_MOUSE_KEY = -14;

	/**
	 * The map of key code to key object used for key bindings.
	 */
	private Map<Integer, Key> mappings = new HashMap<>();

	/**
	 * The client object.
	 */
	private Client client;

	/**
	 * The inputs object containing key objects.
	 */
	private Inputs inputs;

	/**
	 * Instantiates the Input Handler. Initialises bindings based on user
	 * preferences. If the user has no preferences, initialises default
	 * bindings.
	 * 
	 * @param client
	 *            The client object.
	 * @param inputs
	 *            The inputs object.
	 */
	public InputHandler(Client client, Inputs inputs) {
		this.client = client;
		this.inputs = inputs;
	}

	public void reloadBindings() {
		mappings.clear();
		PropertyManager prop = client.getProperties();
		updateBinding(inputs.up, prop.getInt(PropertyManager.MOVE_UP_KEY));
		updateBinding(inputs.down, prop.getInt(PropertyManager.MOVE_DOWN_KEY));
		updateBinding(inputs.left, prop.getInt(PropertyManager.MOVE_LEFT_KEY));
		updateBinding(inputs.right, prop.getInt(PropertyManager.MOVE_RIGHT_KEY));
		updateBinding(inputs.cameraLock, prop.getInt(PropertyManager.CAMERA_LOCK_KEY));
		updateBinding(inputs.grenade, prop.getInt(PropertyManager.GRENADE_KEY));
		updateBinding(inputs.followKey, prop.getInt(PropertyManager.FOLLOW_KEY));
		updateBinding(inputs.reload, prop.getInt(PropertyManager.RELOAD_KEY));
		updateBinding(inputs.pistolKey, prop.getInt(PropertyManager.PISTOL_SWITCH_KEY));
		updateBinding(inputs.mgKey, prop.getInt(PropertyManager.MG_SWITCH_KEY));
		updateBinding(inputs.shieldKey, prop.getInt(PropertyManager.SHIELD_SWITCH_KEY));
		updateBinding(inputs.shoot, prop.getInt(PropertyManager.SHOOT_KEY));
		updateBinding(inputs.weaponScrollUp, prop.getInt(PropertyManager.WEAPON_SCROLL_UP_KEY));
		updateBinding(inputs.weaponScrollDown, prop.getInt(PropertyManager.WEAPON_SCROLL_DOWN_KEY));
		// updateBinding(inputs.weaponScrollUp,
		// prop.getInt(PropertyManager.WEAPON_SCROLL_UP_KEY));
		// updateBinding(inputs.weaponScrollDown,
		// prop.getInt(PropertyManager.WEAPON_SCROLL_DOWN_KEY));
	}

	/**
	 * Initialises a binding key. If the key object already exists in the map,
	 * it is replaced.
	 * 
	 * @param key
	 *            The key to be binded
	 * @param keyCodeBinding
	 *            The new key press
	 */
	public void updateBinding(Key key, int keyCodeBinding) {
		mappings.put(keyCodeBinding, key);
	}

	/**
	 * Toggle a key with a given boolean from the binding map based on the key
	 * code.
	 */
	private void toggleKey(int keyCode, boolean pressed) {
		// Get the key fom the map and if it exists, update it's pressed state.
		Key key = mappings.get(keyCode);
		if (key != null) {
			key.setPressed(pressed);
		}
	}

	/**
	 * Releases all typed keys, and scroll keys.
	 */
	public void releaseTypedAndScroll() {
		// Release all typed input keys.
		inputs.typedInput.keySet().forEach(key -> inputs.typedInput.put(key, false));

		// Release scroll up and scroll down keys.
		inputs.scrollUp.setPressed(false);
		inputs.scrollDown.setPressed(false);
	}

	/**
	 * Releases every key.
	 */
	public void releaseAll() {
		// Release All types and scroll keys.
		releaseTypedAndScroll();

		// Release all pressed keys.
		for (Key key : mappings.values()) {
			key.setPressed(false);
		}

		// Release left and right mouse buttons.
		inputs.leftClick.setPressed(false);
	}

	/**
	 * Called when a input is pressed. Toggles the corresponding action key with
	 * true.
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		toggleKey(e.getKeyCode(), true);

		// Used only for input field caret positioning. Not for ingame movement.
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			inputs.leftArrow.setPressed(true);
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			inputs.rightArrow.setPressed(true);
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			inputs.esc.setPressed(true);
		}
	}

	/**
	 * Called when a input is released. Toggles the corresponding action key
	 * with false.
	 */
	@Override
	public void keyReleased(KeyEvent e) {
		toggleKey(e.getKeyCode(), false);

		// Used only for input field caret positioning. Not for ingame movement.
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			inputs.leftArrow.setPressed(false);
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			inputs.rightArrow.setPressed(false);
		} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			inputs.esc.setPressed(false);
		}
	}

	/**
	 * Called when a key is typed. Add the key char into the input's typed input
	 * map and sets its value as true.
	 */
	@Override
	public void keyTyped(KeyEvent e) {
		inputs.typedInput.put(e.getKeyChar(), true);
	}

	/**
	 * Called when a mouse button is clicked. Calls the corresponding mouse
	 * button's clicked method. Updates the clicked position.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			inputs.leftClick.clicked(e.getX(), e.getY());
		}

		int mouseCode = convertMouseCode(e.getButton());
		if (mappings.containsKey(mouseCode)) {
			mappings.get(mouseCode).setPressed(true);
		}
	}

	public static final int convertMouseCode(int button) {
		switch (button) {
		case MouseEvent.BUTTON1:
			return LEFT_MOUSE_KEY;
		case MouseEvent.BUTTON2:
			return MIDDLE_MOUSE_KEY;
		case MouseEvent.BUTTON3:
			return RIGHT_MOUSE_KEY;
		}
		return 0;
	}

	/**
	 * Called when a mouse button is released. Calls the corresponding mouse
	 * button's release method. Updates the released position.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			inputs.leftClick.released(e.getX(), e.getY());
		}

		int mouseCode = convertMouseCode(e.getButton());
		if (mappings.containsKey(mouseCode)) {
			mappings.get(mouseCode).setPressed(false);
		}
	}

	/**
	 * Called when the mouse is dragged. Updates the input objects mouse x and y
	 * values.
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		inputs.mouseX = e.getX();
		inputs.mouseY = e.getY();
	}

	/**
	 * Called when the mouse is moved. Updates the input objects mouse x and y
	 * values.
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		inputs.mouseX = e.getX();
		inputs.mouseY = e.getY();
	}

	/**
	 * Called when the mouse wheel is moved. Updates the scroll wheel key's
	 * states depending on the mouse wheel event.
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int scrollCode = 0;
		if (e.getWheelRotation() > 0) {
			inputs.scrollDown.setPressed(true);
			scrollCode = SCROLL_DOWN_KEY;
		} else if (e.getWheelRotation() < 0) {
			inputs.scrollUp.setPressed(true);
			scrollCode = SCROLL_UP_KEY;
		}

		if (mappings.containsKey(scrollCode)) {
			mappings.get(scrollCode).setPressed(true);
		}
	}

	/**
	 * Called when the mouse is pressed and released. Do nothing.
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		// Do nothing.
	}

	/**
	 * Called when the mouse enters the canvas area. Do nothing.
	 */
	@Override
	public void mouseEntered(MouseEvent e) {
		// Do nothing.
	}

	/**
	 * Called when the mouse leaves the canvas area. Do nothing.
	 */
	@Override
	public void mouseExited(MouseEvent e) {
		// Do nothing.
	}

	/**
	 * Gets the string of the key based on the keycode
	 * @param keyCode The keycode
	 * @return The string representation of the keycode.
	 */
	public static String getKeyString(int keyCode) {
		switch (keyCode) {
		case SCROLL_DOWN_KEY:
			return "Scroll down";
		case SCROLL_UP_KEY:
			return "Scroll up";
		case LEFT_MOUSE_KEY:
			return "Left mouse";
		case MIDDLE_MOUSE_KEY:
			return "Middle mouse";
		case RIGHT_MOUSE_KEY:
			return "Right mouse";
		default:
			return KeyEvent.getKeyText(keyCode);
		}
	}

	/**
	 * @return The inputs object.
	 */
	public Inputs getInputs() {
		return inputs;
	}

}
