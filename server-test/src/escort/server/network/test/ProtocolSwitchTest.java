package escort.server.network.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import escort.common.network.Message;
import escort.server.network.Player;
import escort.server.network.ServerSide;

/**
 * Test for protocol switch
 * @author Kwong Hei Tsang
 *
 */
public class ProtocolSwitchTest {

	@Test
	public void test() throws InterruptedException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		//create a ServerSide
		ServerSide ss = new ServerSide(null);
		
		//Create a player to request for protocol switch
		Queue<Message> l1 = new ConcurrentLinkedQueue<Message>();
		l1.add(new Message(Message.PROTOCOL_SWITCH, null, null));
		FakeMessageControl c1 = new FakeMessageControl(l1);
		Player player1 = new Player(c1,ss);
		player1.start();
		Field anotherplayer = player1.getClass().getDeclaredField("anotherclient");
		anotherplayer.setAccessible(true);
		Thread.sleep(100);
		
		//Obtain the switch credential
		Message credential = c1.getSwitchCredential();
		int requestid = credential.getInts()[0];
		String key = credential.getStrings()[0];
		assertFalse(credential == null);
		
		//Intialized test, should not be able to authenticate
		Queue<Message> l5 = new ConcurrentLinkedQueue<Message>();
		l5.add(new Message(Message.PLAYER_REQUESTID,null,new String[]{"Player"}));
		l5.add(new Message(Message.PROTOCOL_SWITCH_FIND,new int[]{requestid},new String[]{key}));
		FakeMessageControl c5 = new FakeMessageControl(l5);
		Player player5 = new Player(c5,ss);
		player5.start();
		Thread.sleep(100);
		assertFalse(anotherplayer.get(player1) == player5);
		
		//Construct the second player, and replay the message
		Queue<Message> l2 = new ConcurrentLinkedQueue<Message>();
		l2.add(credential);
		FakeMessageControl c2 = new FakeMessageControl(l2);
		Player player2 = new Player(c2,ss);
		player2.start();
		Thread.sleep(100);
		assertFalse(anotherplayer.get(player1) == player2);
		
		//Construct the third player, and authenticate really
		Queue<Message> l3 = new ConcurrentLinkedQueue<Message>();
		l3.add(new Message(Message.PROTOCOL_SWITCH_FIND,new int[]{requestid},new String[]{key}));
		FakeMessageControl c3 = new FakeMessageControl(l3);
		Player player3 = new Player(c3,ss);
		player3.start();
		Thread.sleep(100);
		assertTrue(anotherplayer.get(player1) == player3);
		
		//Reusing credential test
		Queue<Message> l4 = new ConcurrentLinkedQueue<Message>();
		l4.add(new Message(Message.PROTOCOL_SWITCH_FIND,new int[]{requestid},new String[]{key}));
		FakeMessageControl c4 = new FakeMessageControl(l4);
		Player player4 = new Player(c4,ss);
		player4.start();
		Thread.sleep(100);
		assertFalse(anotherplayer.get(player1) == player4);
		assertTrue(anotherplayer.get(player1) == player3);
		
		//Lobby message security test
		c1.putMessage(new Message(Message.PLAYER_REQUESTID,null,new String[]{"Player1"}));
		c1.putMessage(new Message(Message.LOBBY_NEW,null,new String[]{"Lobbyname","123456"}));
		c1.putMessage(new Message(Message.LOBBY_MESSAGE,null,new String[]{"Player1","1234567"}));
		Thread.sleep(100);
		assertTrue(c1.getSentMessageCount() == 5);
		c3.putMessage(new Message(Message.LOBBY_MESSAGE,null,new String[]{"Player1","123456"}));
		Thread.sleep(100);
		assertTrue(c3.getSentMessageCount() == 0);
		assertTrue(c1.getSentMessageCount() == 5);
		
		//Terminate players
		c1.terminate();
		c2.terminate();
		c3.terminate();
		c4.terminate();
		c5.terminate();
		
		//Shutdown server
		ss.shutdownServer();
	}

}
