package escort.common.network;

import java.io.IOException;

/**
 * Implements this interface to facilitate communication between client and server regardless the actual protocol used
 * @author Kwong Hei Tsang
 *
 */
public interface MessageControl {

	/**
	 * Send the message
	 * @param command the message to be sent
	 * @throws IOException
	 */
	public void sendMessage(Message command) throws IOException;
	
	/**
	 * Receive message
	 * @return the next message
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws MalformedMessageException
	 */
	public Message receiveMessage() throws ClassNotFoundException, IOException, MalformedMessageException;
	
	/**
	 * Close the message control
	 * @throws IOException
	 */
	public void close() throws IOException;
	
	/**
	 * Get the type of the protocol
	 * @return The type of the protocol
	 */
	public String protocol();
}
