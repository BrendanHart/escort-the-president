package escort.client.network;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import escort.common.network.Message;
import escort.common.network.MessageControl;
import escort.common.network.Sender;

/**
 * Client sender thread for messages
 * 
 * @author Kwong Hei Tsang
 *
 */
public class ClientSender implements Runnable, Sender {

	private final MessageControl control;
	private final Queue<Message> messages;
	private NetworkManager netManager;

	/**
	 * Construct a client sender object
	 * 
	 * @param netManager
	 *            The network manager
	 * @param control
	 *            The message control object
	 */
	public ClientSender(NetworkManager netManager, MessageControl control) {
		this.messages = new ConcurrentLinkedQueue<Message>();
		this.netManager = netManager;
		this.control = control;
	}

	@Override
	public void run() {
		try {
			while (true) {
				Message msg = this.messages.poll();
				if (msg != null) {
					this.control.sendMessage(msg);
				} else {
					synchronized (this) {
						// wait for message
						this.wait();
					}
				}
			}
		} catch (InterruptedException e) {
		} catch (IOException e) {
			netManager.getClient().serverDisconnected();
		}
	}

	/**
	 * Put a message into the queue in order to be sent to the server.
	 */
	@Override
	public void put(Message msg) {
			this.messages.add(msg);
			synchronized (this) {
				this.notifyAll();
			}
	}
}
