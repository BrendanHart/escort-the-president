package escort.common.systime;

/**
 * A timer which can be set in seconds.
 */
public class Timer {

	private boolean isSet;
	private long setTime;
	private int duration;

	/**
	 * Create a new timer, which is not set.
	 */
	public Timer() {
		this.isSet = false;
		this.duration = 0;
		this.setTime = 0;
	}

	/**
	 * Start the timer.
	 */
	public void startTimer() {
		this.setTime = System.nanoTime();
		this.isSet = true;
	}

	/**
	 * Set the timer duration
	 * 
	 * @param duration
	 *            The duration in seconds.
	 */
	public void setTimerDuration(int duration) {
		this.duration = duration;
	}

	/**
	 * Check if the duration has elapsed since the timer is set.
	 * 
	 * @return If the timer is up.
	 */
	public boolean isUp() {
		if (!isSet)
			return true;
		if (duration < (System.nanoTime() - setTime) / 1000000000.0)
			return false;
		isSet = false;
		return true;
	}

	/**
	 * Check if the timer is set.
	 * 
	 * @return If the timer is set.
	 */
	public boolean isSet() {
		return isSet;
	}

	/**
	 * Cancel the timer.
	 */
	public void cancel() {
		this.isSet = false;
	}
}