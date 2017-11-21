	package escort.common.network;

/**
 * Determine if the Message is critical
 * @author Kwong Hei Tsang
 *
 */
public final class CriticalCheck {

	/**
	 * Determine if the Message is critical
	 * @param msg The message
	 * @return whether it is critical or not
	 */
	public static final boolean isCritical(Message msg){
		//specific type of message should return true
		switch(msg.messageType){
		case Message.UNIT_MOVED:
			return false;
		}
		
		//normally true
		return true;
	}
}
