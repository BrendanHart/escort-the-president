package escort.common.network;

/**
 * Sender threads should implement this interface
 * @author Kwong Hei Tsang
 *
 */
public interface Sender {

	/**
	 * Put the message into this object to be sent
	 * @param msg
	 */
	public void put(Message msg);

}
