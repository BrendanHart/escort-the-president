package escort.client.properties;

import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Uer properties
 * 
 * @author Kwong Hei Tsang
 *
 */
public class Properties implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7547911219688696355L;
	private final Map<Integer, String> settings;

	/**
	 * Create a new settings object with default settings
	 * 
	 * @throws FileNotFoundException
	 */
	public Properties() {
		this.settings = new HashMap<Integer, String>();
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
		String value = settings.get(parameter);
		if (value == null) {
			return DefaultProperties.DEFAULT_PROPERTIES.get(parameter);
		} else {
			return value;
		}
	}

	/**
	 * Put a setting
	 * 
	 * @param parameter
	 * @param value
	 */
	public synchronized void putProperty(int parameter, String value) {
		this.settings.put(parameter, value);

	}
}
