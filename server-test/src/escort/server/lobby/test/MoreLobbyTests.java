package escort.server.lobby.test;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import escort.common.game.Outcomes;
import escort.common.game.lobby.LobbyMessageConstants;
import escort.common.network.Message;
import escort.server.network.Player;
import escort.server.network.ServerSide;
import escort.server.network.test.FakeMessageControl;

/**
 * Tests for lobby sweeper, time for player cannot join again after being kicked
 * @author Kwong Hei Tsang
 *
 */
public class MoreLobbyTests {
	
	@Test
	public void test() throws Exception {		
		//Initialize server side
		ServerSide ss = new ServerSide(null);
		
		//Initialize first player
		int p1 = 0;
		FakeMessageControl c1 = new FakeMessageControl(new ConcurrentLinkedQueue<Message>());
		Player player1 = new Player(c1,ss);
		player1.start();
		c1.putMessage(new Message(Message.PLAYER_REQUESTID,null,new String[]{"Player1"})); p1++;
		c1.putMessage(new Message(Message.LOBBY_NEW,null,new String[]{"Lobby1","password1"})); p1++;
		{
			Message msg = c1.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_ACCEPT && msg.getInts()[0] == 1);
			msg = c1.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_CREATED);
		}
		assertTrue(player1.getLobbyID() == 1);
		
		//Initialize second player
		int p2 = 0;
		FakeMessageControl c2 = new FakeMessageControl(new ConcurrentLinkedQueue<Message>());
		Player player2 = new Player(c2,ss);
		player2.start();
		c2.putMessage(new Message(Message.PLAYER_REQUESTID,null,new String[]{"Player2"})); p2++;
		c2.putMessage(new Message(Message.LOBBY_JOIN,new int[]{2},new String[]{"12345678"})); p2++;
		c2.putMessage(new Message(Message.LOBBY_JOIN,new int[]{1},new String[]{"12345678"})); p2++;
		c2.putMessage(new Message(Message.LOBBY_JOIN,new int[]{1},new String[]{"password1"})); p2+=3; p1++;
		{
			Message msg = c2.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_ACCEPT);
			msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_IDINVALID);
			msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_AUTHFAIL && msg.getInts()[0] == LobbyMessageConstants.WRONG_PASSWORD);
			msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_JOINED);
			assertTrue(Arrays.equals(new int[]{1, 1}, msg.getInts()));
			assertTrue(Arrays.equals(new String[]{"Lobby1"}, msg.getStrings()));
			msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_SETTINGS);
			msg = c2.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_LIST_UPDATE);
			msg = c1.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_LIST_UPDATE);
		}
		assertTrue(player2.getLobbyID() == 1);
		
		//Kick player2
		c1.putMessage(new Message(Message.LOBBY_KICK_PLAYER,new int[]{3},null));
		c1.putMessage(new Message(Message.LOBBY_KICK_PLAYER,new int[]{2},null)); p2++;p1++;
		{
			Message msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_LEFT && msg.getInts()[0] == LobbyMessageConstants.KICKED);
			msg = c1.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_LIST_UPDATE);
		}
		assertTrue(player2.getLobbyID() == -1);
		c2.putMessage(new Message(Message.LOBBY_JOIN,new int[]{1},new String[]{"password1"}));p2++;
		{
			Message msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_AUTHFAIL && msg.getInts()[0] == LobbyMessageConstants.KICKED_BEFORE);
		}
		assertTrue(player2.getLobbyID() == -1);
		// wait to join again
		Thread.sleep(3000);
		c2.putMessage(new Message(Message.LOBBY_JOIN,new int[]{1},new String[]{"password1"}));p2+=3;p1++;
		{
			Message msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_JOINED);
			assertTrue(Arrays.equals(new int[]{1, 1}, msg.getInts()));
			assertTrue(Arrays.equals(new String[]{"Lobby1"}, msg.getStrings()));
			msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_SETTINGS);
			msg = c2.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_LIST_UPDATE && msg.getInts().length == 2);
			msg = c1.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_LIST_UPDATE && msg.getInts().length == 2);
		}
		assertTrue(player2.getLobbyID() == 1);
		assertTrue(player2.getLobbyID() == 1);
		
		// initialize player 3
		int p3 = 0;
		FakeMessageControl c3 = new FakeMessageControl(new ConcurrentLinkedQueue<Message>());
		Player player3 = new Player(c3,ss);
		player3.start();
		c3.putMessage(new Message(Message.PLAYER_REQUESTID,null,new String[]{"Player3"})); p3++;
		c3.putMessage(new Message(Message.LOBBY_JOIN,new int[]{1},new String[]{"password1"})); p3++;
		{
			Message msg = c3.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_ACCEPT);
			msg = c3.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_FULL);
			assertTrue(player3.getLobbyID() == -1);
		}
		
		// Lobby inactive
		Thread.sleep(8000);
		{
			Message msg = c2.getMessage(); p2++;
			assertTrue(msg.messageType == Message.LOBBY_LEFT && msg.getInts()[0] == LobbyMessageConstants.MASTER_LEFT);
			assertTrue(player2.getLobbyID() == -1);
			msg = c1.getMessage(); p1++;
			assertTrue(msg.messageType == Message.PLAYER_LIST_UPDATE && msg.getInts().length == 1);
			msg = c1.getMessage(); p1++;
			assertTrue(msg.messageType == Message.LOBBY_LEFT && msg.getInts()[0] == LobbyMessageConstants.INACTIVITY);
		}
		
		// Create a lobby again and start game and wait and see
		c1.putMessage(new Message(Message.LOBBY_NEW, null, new String[]{"Lobby1", ""})); p1++;
		c1.putMessage(new Message(Message.GAME_START, null, null)); p1++;
		{
			Message msg = c1.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_CREATED);
			assertTrue(Arrays.equals(msg.getInts(), new int[]{2,1}));
			assertTrue(msg.getStrings()[0].equals("Lobby1"));
			assertTrue(player1.getLobbyID() == 2);
			msg = c1.getMessage();
			assertTrue(msg.messageType == Message.GAME_READY);
			assertTrue(c1.getMessage().messageType == Message.COUNTDOWN_TICK); p1++;
			assertTrue(c1.getMessage().messageType == Message.COUNTDOWN_TICK); p1++;
			assertTrue(c1.getMessage().messageType == Message.GAME_START); p1++;
		}
		
		// should not have timeout
		Thread.sleep(12000);
		assertTrue(player1.getLobbyID() == 2);
		// end game
		player1.getGame().endGame(Outcomes.OUTCOME_DRAW); p1++;
		{
			Message msg = c1.getMessage();
			assertTrue(msg.messageType == Message.GAME_END && Arrays.equals(msg.getInts(), new int[]{Outcomes.OUTCOME_DRAW}));
			assertTrue(player1.getGame() == null);
		}
		
		// should have timeout after game end
		Thread.sleep(8000);
		assertTrue(player1.getLobbyID() == 2);
		Thread.sleep(3000);
		{
			Message msg = c1.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_LEFT); p1++;
			assertTrue(player1.getLobbyID() == -1);
		}
		
		//Check message count
		assertTrue(p1 == c1.getSentMessageCount());
		assertTrue(p2 == c2.getSentMessageCount());
		assertTrue(p3 == c3.getSentMessageCount());
		
		//terminate players and server
		c1.terminate();
		c2.terminate();
		c3.terminate();
		ss.shutdownServer();
	}
	
}
