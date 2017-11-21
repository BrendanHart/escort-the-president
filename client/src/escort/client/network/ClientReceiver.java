package escort.client.network;

import java.io.IOException;

import escort.client.main.Client;
import escort.common.network.MalformedMessageException;
import escort.common.network.Message;
import escort.common.network.MessageControl;

/**
 * Receiver thread for client communication from the server.
 * 
 * @author Kwong Hei Tsang
 *
 */
public class ClientReceiver implements Runnable {

	/**
	 * The message control.
	 */
	private final MessageControl control;

	/**
	 * The network manager.
	 */
	private final NetworkManager netManager;

	/**
	 * The client object.
	 */
	private final Client client;

	/**
	 * Instantiate a new client receiver
	 * 
	 * @param netManager
	 *            The network manager
	 * @param control
	 *            The message control object
	 */
	public ClientReceiver(NetworkManager netManager, MessageControl control) {
		this.netManager = netManager;
		this.control = control;
		client = netManager.getClient();
	}

	/**
	 * Continuously retrieves a message from the message control and performs
	 * appropriate actions based on the message.
	 */
	@Override
	public void run() {
		try {
			while (true) {
				Message msg = control.receiveMessage();
				client.getMessageQueuer().add(msg);
			}
		} catch (IOException e) {
			netManager.getClient().serverDisconnected();
		} catch (MalformedMessageException | ClassNotFoundException e) {
		}
	}
}
