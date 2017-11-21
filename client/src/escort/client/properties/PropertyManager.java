package escort.client.properties;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import escort.client.main.Client;

/**
 * Manage the properties
 * 
 * @author Kwong Hei Tsang
 * @author Ahmed Bhallo
 *
 */
public class PropertyManager {

	private Properties config;
	private static final String directory = "settings";

	// Volume properties.
	public static final int BGM_VOLUME = 0;
	public static final int EFFECT_VOLUME = 1;
	public static final int BGM_MUTED = 2;
	public static final int EFFECT_MUTED = 3;

	// UI Scale property.
	public static final int SCALE = 4;

	// In game input keys.
	public static final int MOVE_LEFT_KEY = 5;
	public static final int MOVE_DOWN_KEY = 6;
	public static final int MOVE_UP_KEY = 7;
	public static final int MOVE_RIGHT_KEY = 8;
	public static final int CAMERA_LOCK_KEY = 9;
	public static final int GRENADE_KEY = 10;
	public static final int FOLLOW_KEY = 11;
	public static final int RELOAD_KEY = 12;
	public static final int PISTOL_SWITCH_KEY = 13;
	public static final int MG_SWITCH_KEY = 14;
	public static final int SHIELD_SWITCH_KEY = 15;
	public static final int SHOOT_KEY = 16;
	public static final int WEAPON_SCROLL_UP_KEY = 17;
	public static final int WEAPON_SCROLL_DOWN_KEY = 18;
	public static final int FULLSCREEN = 19;

	//private final Client client;

	/**
	 * Construct a property manager object
	 */
	public PropertyManager(Client client) {
		//this.client = client;
		// read settings from file, if fails, create a new one
		FileInputStream readfile = null;
		ObjectInputStream readobject = null;
		try {
			readfile = new FileInputStream(directory);
			readobject = new ObjectInputStream(readfile);
			Object config = readobject.readObject();
			if (!(config instanceof Properties)) {
				this.config = new Properties();
				return;
			}
			this.config = (Properties) config;
		} catch (IOException | ClassNotFoundException e) {
			// create a new one
			this.config = new Properties();
		} finally {
			// close resources
			if (readobject != null) {
				try {
					readobject.close();
				} catch (IOException e) {
				}
			}
			if (readfile != null) {
				try {
					readfile.close();
				} catch (IOException e) {
				}
			}
		}

	}

	/**
	 * Get an integer configuration
	 * 
	 * @param parameter
	 *            The parameter
	 * @return The value of the configuration
	 */
	public synchronized int getInt(int parameter) {
		String result = this.config.getProperty(parameter);

		if (result == null) {
			// If the setting does not exist
			return Integer.parseInt(DefaultProperties.DEFAULT_PROPERTIES.get(parameter));
		}

		try {
			// Try to read configuration
			return Integer.parseInt(result);
		} catch (NumberFormatException e) {
		}

		// If something went wrong
		return Integer.parseInt(DefaultProperties.DEFAULT_PROPERTIES.get(parameter));
	}

	public synchronized boolean getBoolean(int parameter) {
		String result = this.config.getProperty(parameter);

		if (result == null) {
			// If the setting does not exist
			return Boolean.parseBoolean(DefaultProperties.DEFAULT_PROPERTIES.get(parameter));
		}

		// Try to read configuration
		if (result.toLowerCase().equals("false")) {
			return false;
		} else if (result.toLowerCase().equals("true")) {
			return true;
		}

		// If something went wrong
		return Boolean.parseBoolean(DefaultProperties.DEFAULT_PROPERTIES.get(parameter));
	}

	/**
	 * Get a key configuration
	 * 
	 * @param parameter
	 *            The parameter
	 * @return The parameter
	 */
	public synchronized char getKey(int parameter) {
		String value = this.getProperty(parameter);
		if (value == null || value.length() < 1) {
			// malformed configuration, get from defaults
			return DefaultProperties.DEFAULT_PROPERTIES.get(parameter).charAt(0);
		}

		return value.charAt(0);
	}

	/**
	 * Get a setting
	 * 
	 * @param parameter
	 *            The parameter
	 * @return The value if exists, the default if does not exist, null for
	 *         invalid parameters or values
	 */
	public synchronized String getProperty(int parameter) {
		return this.config.getProperty(parameter);
	}

	/**
	 * Put a setting
	 * 
	 * @param parameter
	 * @param value
	 */
	public synchronized void putProperty(int parameter, String value) {
		// put the property
		this.config.putProperty(parameter, value);

		// write config to file
		this.writeConfig();
	}

	/**
	 * Reset to default settings and write to settings file
	 */
	public synchronized void resetToDefaults() {
		// Create a new property object
		this.config = new Properties();

		// write to file
		this.writeConfig();
	}

	/**
	 * Write configuration to file
	 */
	private void writeConfig() {
		// write to file
		FileOutputStream writefile = null;
		ObjectOutputStream writeobject = null;
		try {
			writefile = new FileOutputStream(directory);
			writeobject = new ObjectOutputStream(writefile);
			writeobject.writeObject(this.config);
		} catch (IOException e) {
			// cannot write file
		} finally {
			// close resources
			if (writeobject != null) {
				try {
					writeobject.close();
				} catch (IOException e) {
				}
			}
			if (writefile != null) {
				try {
					writefile.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
