package escort.client.network.protocols;

import java.io.IOException;

import escort.client.main.Client;
import escort.common.network.MalformedMessageException;
import escort.common.network.Message;
import escort.common.network.MessageControl;
import escort.common.network.tcp.MessageControlTCP;
import escort.common.network.udp.MessageControlUDP;

/**
 * Get message control for the client
 * 
 * @author Kwong Hei Tsang
 *
 */
public final class ClientGeneric {
	
	/**
	 * Get Message Control for Client
	 * 
	 * @param server
	 *            The server hostname
	 * @param port
	 *            The server port
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws MalformedMessageException
	 */
	public static final MessageControl getMessageControl(Client client, String server, int port) throws IOException{
		// construct message control client
		MessageControlTCP tcp = ClientTCP.getMessageControl(client, server, port);
		MessageControlUDP udp = null;

		try {
			// UDP fails, fallback to pure TCP
			udp = ClientUDP.getMessageControl(server, port, tcp.getCert());
		} catch (Exception e) {
			return tcp;
		}

		// request protocol switch
		tcp.sendMessage(new Message(Message.PROTOCOL_SWITCH, null, null));

		// read key
		Message msg = null;
		try {
			msg = tcp.receiveMessage();
			if (msg.messageType != Message.PROTOCOL_SWITCH_RESPONSE) {
				tcp.close();
				throw new IOException("Protocol switch handshake failed");
			}
		} catch (ClassNotFoundException | MalformedMessageException e) {
			tcp.close();
			throw new IOException("Protocol Error");
		}

		// Construct a message control client
		MessageControlClient msgControlclient = new MessageControlClient(server, port, tcp, udp);

		// send response to UDP
		udp.sendMessage(new Message(Message.PROTOCOL_SWITCH_FIND, msg.getInts(), msg.getStrings()));

		return msgControlclient;
	}
}
