package escort.server.network.test;

import static org.junit.Assert.*;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import escort.common.network.Message;
import escort.server.network.Player;
import escort.server.network.ServerSide;

/**
 * Test for throttling
 * @author Kwong Hei Tsang
 *
 */
public class ThrottleTest {

	@Test
	public void test() throws InterruptedException {
		//create a ServerSide
		ServerSide ss = new ServerSide(null);
		
		// Define first player
		Queue<Message> l1 = new ConcurrentLinkedQueue<Message>();
		for(int i = 0; i < 10000; i++){
			l1.add(new Message(Message.LOBBY_JOIN, null, null));
		}
		FakeMessageControl c1 = new FakeMessageControl(l1);
		Player player1 = new Player(c1,ss);
		player1.start();
		
		Thread.sleep(1000);
		assertTrue(c1.getSentMessageCount() == 2);
	}

}
