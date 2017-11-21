package escort.client.properties;

import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import escort.client.input.InputHandler;

/**
 * Default Properties configured here
 * 
 * @author Kwong Hei Tsang
 * @author Ahmed Bhallo
 *
 */
public class DefaultProperties {

	// Put default constants here
	public static final Map<Integer, String> DEFAULT_PROPERTIES = setDefaultProperties();

	/**
	 * Get the default settings
	 * 
	 * @return The default settings, but read only
	 */
	private static Map<Integer, String> setDefaultProperties() {
		// initialize defaults here
		// usage: defaults.put(parameter,value);
		Map<Integer, String> defaults = new ConcurrentHashMap<Integer, String>();
		defaults.put(PropertyManager.BGM_VOLUME, "60");
		defaults.put(PropertyManager.BGM_MUTED, "false");
		defaults.put(PropertyManager.EFFECT_VOLUME, "60");
		defaults.put(PropertyManager.EFFECT_MUTED, "false");
		defaults.put(PropertyManager.SCALE, "" + 2);
		defaults.put(PropertyManager.FULLSCREEN, "false");

		// Default key bindings
		defaults.put(PropertyManager.MOVE_UP_KEY, "" + KeyEvent.VK_W);
		defaults.put(PropertyManager.MOVE_DOWN_KEY, "" + KeyEvent.VK_S);
		defaults.put(PropertyManager.MOVE_LEFT_KEY, "" + KeyEvent.VK_A);
		defaults.put(PropertyManager.MOVE_RIGHT_KEY, "" + KeyEvent.VK_D);
		defaults.put(PropertyManager.CAMERA_LOCK_KEY, "" + KeyEvent.VK_Y);
		defaults.put(PropertyManager.GRENADE_KEY, "" + KeyEvent.VK_G);
		defaults.put(PropertyManager.FOLLOW_KEY, "" + KeyEvent.VK_F);
		defaults.put(PropertyManager.RELOAD_KEY, "" + KeyEvent.VK_R);
		defaults.put(PropertyManager.MG_SWITCH_KEY, "" + KeyEvent.VK_1);
		defaults.put(PropertyManager.PISTOL_SWITCH_KEY, "" + KeyEvent.VK_2);
		defaults.put(PropertyManager.SHIELD_SWITCH_KEY, "" + KeyEvent.VK_3);
		defaults.put(PropertyManager.WEAPON_SCROLL_UP_KEY, "" + InputHandler.SCROLL_UP_KEY);
		defaults.put(PropertyManager.WEAPON_SCROLL_DOWN_KEY, "" + InputHandler.SCROLL_DOWN_KEY);
		defaults.put(PropertyManager.SHOOT_KEY, "" + InputHandler.LEFT_MOUSE_KEY);

		return Collections.unmodifiableMap(defaults);
	}

}
