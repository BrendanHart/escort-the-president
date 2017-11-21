package escort.client.main;

/**
 * Calculate the scale according to system configurations
 * 
 * @author Kwong Hei Tsang
 *
 */
public class Scale {

	/**
	 * The global scale factor variable. Initially -1, however the constructor
	 * of client will override this as soon as the properties manager is loaded
	 * (which is the first manager that is loaded).
	 * 
	 */
	public static int factor = -1;
}
