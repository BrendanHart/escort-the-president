package escort.client.network.protocols;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import escort.common.network.CriticalCheck;
import escort.common.network.MalformedMessageException;
import escort.common.network.Message;
import escort.common.network.MessageControl;
import escort.common.network.tcp.MessageControlTCP;
import escort.common.network.udp.MessageControlUDP;

/**
 * MessageControlClient object maintains the connection with the server
 * 
 * @author Kwong Hei Tsang
 *
 */
public final class MessageControlClient implements MessageControl {

	private final String server;
	private final int port;
	private final MessageControlTCP tcp;
	private final Queue<Message> queue;
	private MessageControlUDP udp;
	private final Thread tcpthread;
	private Thread udpthread;
	private boolean tcperror;
	private boolean udperror;
	private boolean msgput;

	/**
	 * Construct a messagecontrol client
	 * 
	 * @param tcp
	 *            The TCP control message
	 */
	public MessageControlClient(String server, int port, MessageControlTCP tcp, MessageControlUDP udp) {
		this.port = port;
		this.server = server;
		this.tcp = tcp;
		this.udp = udp;
		this.queue = new ConcurrentLinkedQueue<Message>();

		// initialize TCP receiving thread
		this.tcpthread = new Thread(() -> tcpReceivingThread(), "TCP Receiever");
		this.tcpthread.start();

		// initialize UDP receiving thread
		this.udpthread = new Thread();

		this.tcperror = false;
		this.udperror = false;
		this.msgput = false;
	}

	@Override
	public void sendMessage(Message command) throws IOException {
		// Check if the message is not critical
		if (this.udpthread != null && this.udpthread.isAlive() && !CriticalCheck.isCritical(command)
				&& !this.udperror) {
			try {
				this.udp.sendMessage(command);
				return;
			} catch (IOException e) {
				// problem with UDP
				this.udperror = true;
			}
		}

		// Most messages should still go to TCP
		this.tcp.sendMessage(command);
	}

	@Override
	public synchronized Message receiveMessage() throws ClassNotFoundException, IOException, MalformedMessageException {
		// Poll the message from the queue
		Message message = this.queue.poll();

		// If message is null, wait and try again
		if (message == null) {
			try {
				this.msgput = false;
				while (!this.msgput && !this.tcperror) {
					this.wait();
				}
			} catch (InterruptedException e) {
				throw new IOException("receiving thread interrupted");
			}

			// If TCP is at error
			if (this.tcperror) {
				throw new IOException("TCP Broken");
			}

			// Try to receive message again
			return receiveMessage();
		}

		// If it is a protocol switch response
		if (message.messageType == Message.PROTOCOL_SWITCH_RESPONSE) {
			this.udp.sendMessage(new Message(Message.PROTOCOL_SWITCH_FIND, message.getInts(), message.getStrings()));
		}

		// If it is a protocol switch finish notification
		if (message.messageType == Message.PROTOCOL_SWITCH_DONE) {
			// System.out.println("Successful");
			this.udpthread = new Thread(() -> this.udpReceivingThread(), "UDP Receiver");
			this.udpthread.start();
		}

		// Normally the message is not empty
		return message;
	}

	@Override
	public void close() throws IOException {
		this.tcp.close();
		this.udp.close();

	}

	@Override
	public String protocol() {
		return "client";
	}

	private void tcpReceivingThread() {
		try {
			while (true) {
				try {
					Message msg = this.tcp.receiveMessage();
					this.queue.offer(msg);
					// System.out.println("TCP: " + msg.messageType);
					synchronized (this) {
						this.msgput = true;
						this.notifyAll();
					}
				} catch (ClassNotFoundException | MalformedMessageException e) {
				}
			}
		} catch (IOException e) {
		} finally {
			try {
				this.tcp.close();
			} catch (IOException e) {
			}
		}

		// set tcperror state to true
		synchronized (this) {
			this.tcperror = true;
			this.notifyAll();
		}
	}

	private void udpReceivingThread() {
		try {
			while (true) {
				try {
					Message msg = this.udp.receiveMessage();
					this.queue.offer(msg);
					// System.out.println("UDP: " + msg.messageType);
					synchronized (this) {
						this.msgput = true;
						this.notifyAll();
					}
				} catch (ClassNotFoundException e) {
				}
			}
		} catch (IOException e) {
		} finally {
			try {
				this.udp.close();
			} catch (IOException e) {
			}
		}

		// Set exception state to true
		this.udperror = true;
		handleUDPBrokenConnection();
	}

	private void handleUDPBrokenConnection() {
		// UDP error, attempt to recover
		if (this.udperror) {
			new Thread(() -> recoverUDP(), "UDP Recovery").start();
		}
	}

	private void recoverUDP() {
		try {
			this.udperror = false;
			// UDP fails, fallback to pure TCP
			this.udp = (MessageControlUDP) ClientUDP.getMessageControl(this.server, this.port, this.tcp.getCert());
			// Send protocolswitch request
			this.tcp.sendMessage(new Message(Message.PROTOCOL_SWITCH, null, null));
		} catch (Exception e) {
		}
	}

}
