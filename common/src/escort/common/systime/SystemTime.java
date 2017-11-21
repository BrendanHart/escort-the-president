package escort.common.systime;

/**
 * Convert System.nanoTime() to a milliTime()
 * 
 * @author Kwong Hei Tsang
 *
 */
public final class SystemTime {

	/**
	 * Get the Time converting nanoTime to milliTime
	 * 
	 * @return The nanoTime expressed in milliTime
	 */
	public static long milliTime() {
		return System.nanoTime() / 1000000l;
	}
}