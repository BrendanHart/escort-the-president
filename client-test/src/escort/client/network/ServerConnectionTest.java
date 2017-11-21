package escort.client.network;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.client.network.protocols.ClientTCP;
import escort.common.network.Message;
import escort.common.network.MessageControl;

/**
 * A server must be running on 127.0.0.1 port 8888 for this test to work.
 * @author Ahmed Bhallo
 *
 */
public class ServerConnectionTest {

	private MessageControl control;

	@Before
	public void setUp() {
		try {
			control = ClientTCP.getMessageControlManual(null, "localhost", 8888);
		} catch (Exception e) {
			System.err.println("Could not connect to server to carry out tests");
		}
	}

	@After
	public void tearDown() {
		try {
			control.close();
		} catch (IOException e) {
		}
	}

	/**
	 * This test will fail if the username is already on the server. In this
	 * case, simply restart the server.
	 * 
	 * @throws Exception
	 */
	@Test
	public void connectToServer() throws Exception {
		control.sendMessage(new Message(Message.PLAYER_REQUESTID, null, new String[] { "username" }));
		assertTrue(control.receiveMessage().messageType == Message.PLAYER_ACCEPT);
	}

}
