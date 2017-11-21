package escort.server.lobby.test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import escort.common.game.Outcomes;
import escort.common.game.lobby.LobbyMessageConstants;
import escort.common.game.map.GameMap;
import escort.common.network.Message;
import escort.server.network.Player;
import escort.server.network.ServerSide;
import escort.server.network.test.FakeMessageControl;

/**
 * This is a JUnit test for the Server lobby operations.
 * Be careful that using this as a sample for how to communicate with the server.
 * Some commands are INVALID and should be ignored by the server.
 * 
 * I have also done some counting for the number of messages to be sent
 * from the server for each player.
 * All VALID messages have a statement for changing the counter p1++, p2++ etc.
 * 
 * @author Kwong Hei Tsang
 *
 */
public class TestLobby {

	@Test
	public void test() throws InterruptedException, IOException{
		int p1 = 0;
		int p2 = 0;
		int p3 = 0;
		//LobbyConfiguration.enableEditing();
		//LobbyConfiguration.setKickPreventJoin(300);
		//LobbyConfiguration.setMaxInactivePeriod(3000);
		//LobbyConfiguration.setSweeperPeriod(1000);
		
		//messages of player 1
		Queue<Message> l1 = new ConcurrentLinkedQueue<Message>();
		l1.add(new Message(Message.PLAYER_REQUESTID,null,new String[]{"Player1"})); p1++;
		l1.add(new Message(Message.LOBBY_NEW,null,new String[]{"sdirhfnasresrhesrheashkhkjjh","123456"}));p1++;
		l1.add(new Message(Message.LOBBY_NEW,null,new String[]{"Lobbyname","123456"})); p1++;
		
		//create a ServerSide
		ServerSide ss = new ServerSide(null);
		
		//create player 1
		FakeMessageControl c1 = new FakeMessageControl(l1);
		Player player1 = new Player(c1,ss);
		assertTrue(player1.getPlayerID() == -1);
		assertTrue(player1.getLobbyID() == -1);
		player1.start();
		// Get the registration message and lobby created message
		{
			Message msg = c1.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_ACCEPT && msg.getInts()[0] == 1);
			msg = c1.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_NAME_INVALID);
			msg = c1.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_CREATED);
			assertTrue(msg.getInts()[0] == 1 && msg.getInts()[1] == 1 && msg.getStrings()[0].equals("Lobbyname"));
		}
		// Check server values
		assertFalse(player1.getPlayerID() == -1);
		assertTrue(ss.getLobbyManagement().getLobbyList().size() == 1);
		assertTrue(ss.getLobbyManagement().getLobbyList().size() == 1);
		assertTrue(ss.getPlayers().size() == 1);
		assertTrue(ss.getLobbyManagement().getLobbyList().get(0).getPlayers().size() == 1);
		
		
		//messages of player 2
		Queue<Message> l2 = new ConcurrentLinkedQueue<Message>();
		l2.add(new Message(Message.LOBBY_NEW,null,new String[]{"name2","password2"}));
		l2.add(new Message(Message.LOBBY_JOIN,new int[]{1},new String[]{"123456"}));
		l2.add(new Message(Message.LOBBY_LEAVE,null,null));
		l2.add(new Message(Message.LOBBY_LIST,null,null)); p2++;
		l2.add(new Message(Message.PLAYER_REQUESTID,null,new String[]{"Player1"})); p2++;
		l2.add(new Message(Message.PLAYER_REQUESTID,null,new String[]{"Player2"})); p2++;
		l2.add(new Message(Message.LOBBY_JOIN,new int[]{3},new String[]{"123456"})); p2++;
		l2.add(new Message(Message.LOBBY_JOIN,new int[]{1},new String[]{"1234567"}));p2++;
		l2.add(new Message(Message.LOBBY_LEAVE,null,null));
		l2.add(new Message(Message.LOBBY_JOIN,new int[]{1},new String[]{"123456"}));p2+=3;p1++;
		l2.add(new Message(Message.LOBBY_JOIN,new int[]{10},new String[]{"123456"})); // should only join lobby once
		l2.add(new Message(Message.LOBBY_NEW,null,new String[]{"Lobbyname","123456"})); // should not be able to create lobby after joining
		
		//create player 2
		FakeMessageControl c2 = new FakeMessageControl(l2);
		Player player2 = new Player(c2, ss);
		assertTrue(player2.getPlayerID() == -1);
		assertTrue(player2.getLobbyID() == -1);
		player2.start();
		
		// Check returned messages
		{
			Message msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_LIST_RESULT);
			assertTrue(Arrays.equals(msg.getInts(), new int[]{1,1,0,1}));
			msg = c2.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_REJECT && msg.getInts()[0] == 1);
			msg = c2.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_ACCEPT);
			assertTrue(msg.getInts()[0] == 2);
			msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_IDINVALID);
			msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_AUTHFAIL);
			assertTrue(msg.getInts()[0] == LobbyMessageConstants.WRONG_PASSWORD);
			msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_JOINED);
			msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_SETTINGS);
			assertTrue(Arrays.equals(msg.getInts(), new int[]{GameMap.HOTEL_ID,3,3,3}));
			msg = c2.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_LIST_UPDATE);
			assertTrue(msg.getInts()[0] != msg.getInts()[1] && !msg.getStrings()[0].equals(msg.getStrings()[1]));
			
			// Check player 1 notification
			Message msg2 = c1.getMessage();
			assertTrue(msg2.messageType == Message.PLAYER_LIST_UPDATE);
			assertTrue(msg2.getInts()[0] != msg2.getInts()[1] && !msg2.getStrings()[0].equals(msg2.getStrings()[1]));
			
			// Check the content of the player list
			assertTrue(msg == msg2);
			switch(msg.getInts()[0]){
			case 1:
				assertTrue(msg.getStrings()[0].equals(player1.getPlayerName()));
				break;
			case 2:
				assertTrue(msg.getStrings()[0].equals(player2.getPlayerName()));
				break;
			}
			switch(msg.getInts()[1]){
			case 1:
				assertTrue(msg.getStrings()[1].equals(player1.getPlayerName()));
				break;
			case 2:
				assertTrue(msg.getStrings()[1].equals(player2.getPlayerName()));
				break;
			}
		}
		// Check server values
		assertTrue(ss.getLobbyManagement().getLobbyList().size() == 1);
		assertFalse(player2.getPlayerID() == -1);
		assertTrue(player2.getLobbyID() == player1.getLobbyID());
		assertFalse(player2.getPlayerID() == player1.getPlayerID());
		assertTrue(ss.getPlayers().size() == 2);
		assertTrue(ss.getLobbyManagement().getLobbyList().get(0).getPlayers().size() == 2);
		
		//Player 2 leave lobby
		c2.putMessage(new Message(Message.LOBBY_SET_PASSWORD,null,new String[]{"newpassword"}));
		c2.putMessage(new Message(Message.LOBBY_LEAVE,null,null)); p2++;p1++;
		{
			Message msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_LEFT && msg.getInts()[0] == LobbyMessageConstants.ACTIVE);
			msg = c1.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_LIST_UPDATE);
			assertTrue(msg.getInts()[0] == 1 && msg.getStrings()[0].equals("Player1"));
			assertTrue(msg.getInts().length == 1);
		}
		//check variables
		assertTrue(ss.getLobbyManagement().getLobbyList().size() == 1);
		assertFalse(player2.getLobbyID() == player1.getLobbyID());
		assertTrue(player2.getLobbyID() == -1);
		assertTrue(ss.getPlayers().size() == 2);
		assertTrue(ss.getLobbyManagement().getLobbyList().get(0).getPlayers().size() == 1);
		
		//Player 1 change password
		c1.putMessage(new Message(Message.LOBBY_SET_PASSWORD,null,new String[]{"newpassword2"}));p1++;
		{
			Message msg = c1.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_PASSWORD_SET);
		}
		
		//Player 2 join again
		c2.putMessage(new Message(Message.LOBBY_JOIN,new int[]{1},new String[]{"newpassword2"}));p2+=3;p1++;
		// Check returned messages
		{
			Message msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_JOINED);
			msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_SETTINGS);
			assertTrue(Arrays.equals(msg.getInts(), new int[]{GameMap.HOTEL_ID,3,3,3}));
			msg = c2.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_LIST_UPDATE);
			assertTrue(msg.getInts()[0] != msg.getInts()[1] && !msg.getStrings()[0].equals(msg.getStrings()[1]));
			
			// Check player 1 notification
			Message msg2 = c1.getMessage();
			assertTrue(msg2.messageType == Message.PLAYER_LIST_UPDATE);
			assertTrue(msg2.getInts()[0] != msg2.getInts()[1] && !msg2.getStrings()[0].equals(msg2.getStrings()[1]));
		}
		assertTrue(player2.getLobbyID() == player1.getLobbyID());
		
		//kick player test
		c2.putMessage(new Message(Message.LOBBY_KICK_PLAYER,new int[]{player1.getPlayerID()},null));
		c2.putMessage(new Message(Message.LOBBY_KICK_PLAYER,new int[]{player2.getPlayerID()},null));
		Thread.sleep(100);
		assertTrue(player2.getLobbyID() == player1.getLobbyID());
		assertTrue(ss.getLobbyManagement().getLobbyList().get(0).getPlayers().size() == 2);
		c1.putMessage(new Message(Message.LOBBY_KICK_PLAYER,new int[]{player2.getPlayerID()},null));p1++;p2++;
		{
			Message msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_LEFT && msg.getInts()[0] == LobbyMessageConstants.KICKED);
			msg = c1.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_LIST_UPDATE);
			assertTrue(Arrays.equals(msg.getInts(), new int[]{1}));
			assertTrue(Arrays.equals(msg.getStrings(), new String[]{"Player1"}));
		}
		assertTrue(ss.getLobbyManagement().getLobbyList().get(0).getPlayers().size() == 1);
		assertTrue(player2.getLobbyID() == -1);
		// attempt to join again, but will fail
		c2.putMessage(new Message(Message.LOBBY_JOIN,new int[]{1}, new String[]{"newpassword2"}));p2++;
		{
			Message msg = c2.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_AUTHFAIL);
			assertTrue(msg.getInts()[0] == LobbyMessageConstants.KICKED_BEFORE);
		}
		
		//Player 3 join the game
		FakeMessageControl c3 = new FakeMessageControl(new ConcurrentLinkedQueue<Message>());
		Player player3 = new Player(c3,ss);
		player3.start();
		c3.putMessage(new Message(Message.PLAYER_REQUESTID,null,new String[]{"Player3"})); p3++;
		c3.putMessage(new Message(Message.LOBBY_JOIN,new int[]{1}, new String[]{"newpassword2"})); p3+=3;p1+=1;
		c3.getMessage();c3.getMessage();c3.getMessage();c3.getMessage();c1.getMessage();
		
		//Game test
		assertTrue(player1.getGame() == null);
		assertTrue(player3.getGame() == null);
		assertFalse(ss.getLobbyManagement().getLobbyList().get(0).isStarted());
		c3.putMessage(new Message(Message.GAME_START,null,null));
		c1.putMessage(new Message(Message.GAME_START,null,null)); p1+=4;p3+=4;
		{
			Message msg = c3.getMessage();
			assertTrue(msg.messageType == Message.GAME_READY);
			msg = c1.getMessage();
			assertTrue(msg.messageType == Message.GAME_READY);
		}
		assertTrue(player1.getGame() != null);
		assertTrue(player3.getGame() != null);
		assertTrue(player1.getGame() == player3.getGame());
		assertTrue(ss.getLobbyManagement().getLobbyList().get(0).isStarted());
		{			
			Message msg = c3.getMessage();
			Message msg2 = c1.getMessage();
			assertTrue(msg == msg2);
			assertTrue(msg.messageType == Message.COUNTDOWN_TICK);
			assertTrue(msg.getInts()[0] == 2);
			
			msg = c3.getMessage();
			msg2 = c1.getMessage();
			assertTrue(msg == msg2);
			assertTrue(msg.messageType == Message.COUNTDOWN_TICK);
			assertTrue(msg.getInts()[0] == 1);
			
			msg = c3.getMessage();
			msg2 = c1.getMessage();
			assertTrue(msg.messageType == Message.GAME_START);
			assertTrue(msg2.messageType == Message.GAME_START);
		}
		// player 2 should get game already started when attempt to join the lobby
		assertTrue(player2.getLobbyID() == -1);
		c2.putMessage(new Message(Message.LOBBY_JOIN,new int[]{1},new String[]{"newpassword2"}));p2+=1;
		Thread.sleep(100);
		// request lobby list as player 2
		c2.putMessage(new Message(Message.LOBBY_LIST,null, null)); p2+=1;
		{
			Message msg = c2.getMessage();
			assertTrue(player2.getLobbyID() == -1);
			assertTrue(msg.messageType == Message.LOBBY_GAME_ALREADY_STARTED);
			
			msg = c2.getMessage();
			assertTrue(player2.getLobbyID() == -1);
			assertTrue(msg.messageType == Message.LOBBY_LIST_RESULT);
			assertTrue(Arrays.equals(msg.getInts(), new int[]{1, 1, 1, 2}));
		}
		player1.getGame().endGame(Outcomes.OUTCOME_DRAW);p1++;p3++;
		{
			Message msg = c3.getMessage();
			Message msg2 = c1.getMessage();
			assertTrue(msg.messageType == Message.GAME_END);
			assertTrue(msg2.messageType == Message.GAME_END);
			assertTrue(Arrays.equals(msg.getInts(), msg2.getInts()));
			assertTrue(msg.getInts().length == 1 && msg.getInts()[0] == Outcomes.OUTCOME_DRAW);
		}
		assertTrue(player1.getGame() == null);
		assertTrue(player3.getGame() == null);
		assertFalse(ss.getLobbyManagement().getLobbyList().get(0).isStarted());
		
		//Lobby message test
		for(int i = 0; i < 100; i++){
			c3.putMessage(new Message(Message.LOBBY_MESSAGE,null,new String[]{"Garbage","Message"}));
			c1.putMessage(new Message(Message.LOBBY_MESSAGE,null,new String[]{"Garbage","Message"}));
		}
		// only 10 message per player are actually processed
		p3 += 20;
		p1 += 20;
		{
			Message msg;
			Message msg2;
			for(int i = 0; i < 20; i++){
				msg = c1.getMessage();
				msg2 = c3.getMessage();
				assertTrue(msg.messageType == Message.LOBBY_MESSAGE);
				assertTrue(msg2.messageType == Message.LOBBY_MESSAGE);
				assertFalse(msg.getStrings()[0].equals("Garbage"));
				assertFalse(msg2.getStrings()[0].equals("Garbage"));
				assertTrue(msg.getStrings()[1].equals("Message"));
				assertTrue(msg2.getStrings()[1].equals("Message"));
			}
		}
		
		//Change settings test
		assertTrue(ss.getLobbyManagement().getLobbyList().get(0).getMap() == 0);
		c1.putMessage(new Message(Message.LOBBY_SETTINGS_CHANGE,new int[]{9, 1, 2, 3},null));p1+=1;p3+=1;
		c1.putMessage(new Message(Message.LOBBY_SETTINGS_CHANGE,new int[]{10, 5, 6, 7},null));p1+=1;p3+=1;
		{
			Message msg1 = c1.getMessage();
			Message msg2 = c3.getMessage();
			assertTrue(msg1 == msg2);
			assertTrue(msg1.messageType == Message.LOBBY_SETTINGS);
			assertTrue(Arrays.equals(msg1.getInts(), new int[]{9, 1, 2, 3}));
			msg1 = c1.getMessage();
			msg2 = c3.getMessage();
			assertTrue(msg1 == msg2);
			assertTrue(msg1.messageType == Message.LOBBY_SETTINGS);
			assertTrue(Arrays.equals(msg1.getInts(), new int[]{10, 3, 4, 5}));
		}
		assertTrue(ss.getLobbyManagement().getLobbyList().get(0).getMap() == 10);
		assertTrue(ss.getLobbyManagement().getLobbyList().get(0).getSettings().mapID == 10);
		assertTrue(ss.getLobbyManagement().getLobbyList().get(0).getSettings().numAssassinsAI == 3);
		assertTrue(ss.getLobbyManagement().getLobbyList().get(0).getSettings().numPoliceAI == 4);
		assertTrue(ss.getLobbyManagement().getLobbyList().get(0).getSettings().numCivilianAI == 5);
		
		c3.putMessage(new Message(Message.LOBBY_SETTINGS_CHANGE,null,null));
		c3.putMessage(new Message(Message.LOBBY_SETTINGS_CHANGE,new int[]{1},null));
		assertTrue(ss.getLobbyManagement().getLobbyList().get(0).getMap() == 10);
		c1.putMessage(new Message(Message.LOBBY_SETTINGS_CHANGE,null,null));
		assertTrue(ss.getLobbyManagement().getLobbyList().get(0).getMap() == 10);
		c1.putMessage(new Message(Message.LOBBY_SETTINGS_CHANGE,new int[]{5},null));
		assertTrue(ss.getLobbyManagement().getLobbyList().get(0).getMap() == 10);
		c3.putMessage(new Message(Message.LOBBY_SETTINGS_CHANGE,new int[]{1, 2, 3, 4},null));
		Thread.sleep(100);
		assertTrue(ss.getLobbyManagement().getLobbyList().get(0).getMap() == 10);
		
		// Player 1 remove password
		c1.putMessage(new Message(Message.LOBBY_SET_PASSWORD, null, new String[]{""})); p1++;
		Thread.sleep(100);
		c1.putMessage(new Message(Message.LOBBY_LIST, null, null)); p1++;
		{
			Message msg = c1.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_PASSWORD_SET);
			assertTrue(ss.getLobbyManagement().getLobbyList().get(0).getPassword().equals(""));
			
			msg = c1.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_LIST_RESULT);
			assertTrue(Arrays.equals(msg.getInts(), new int[]{1, 0, 0, 2}));
		}
		
		// Player 3 leave and join again
		c3.putMessage(new Message(Message.LOBBY_LEAVE, null, null)); p3++;p1++;
		c3.putMessage(new Message(Message.LOBBY_JOIN,new int[]{1}, new String[]{""})); p3+=3;p1+=1;
		{
			Message msg = c3.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_LEFT && msg.getInts()[0] == LobbyMessageConstants.ACTIVE);
			msg = c1.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_LIST_UPDATE);
			assertTrue(msg.getInts()[0] == 1 && msg.getStrings()[0].equals("Player1"));
			assertTrue(msg.getInts().length == 1);
			assertTrue(c3.getMessage().messageType == Message.LOBBY_JOINED);
			msg = c3.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_SETTINGS && Arrays.equals(msg.getInts(), new int[]{10,3,4,5}));
			Message msg2 = c1.getMessage();
			msg = c3.getMessage();
			assertTrue(msg == msg2);
			assertTrue(msg.messageType == Message.PLAYER_LIST_UPDATE);
		}
		
		
		//interrupt the player 1
		c1.terminate(); p1+=2; p3++;
		{
			assertTrue(c1.getMessage().messageType == Message.PLAYER_LIST_UPDATE);
			assertTrue(c1.getMessage().messageType == Message.LOBBY_LEFT);
			Message msg3 = c3.getMessage();
			assertTrue(msg3.messageType == Message.LOBBY_LEFT && msg3.getInts()[0] == LobbyMessageConstants.MASTER_LEFT);
		}
		
		//wait
		Thread.sleep(100);
		assertTrue(player3.getLobbyID() == -1);
		assertTrue(ss.getPlayers().size() == 2);
		assertTrue(ss.getLobbyManagement().getLobbyList().size() == 0);
		c2.terminate();
		
		//wait
		Thread.sleep(100);
		
		//Check server variables
		assertTrue(ss.getPlayers().size() == 1);
		assertTrue(ss.getLobbyManagement().getLobbyList().size() == 0);
		
		//Terminate player 3
		c3.terminate();
		Thread.sleep(100);
		assertTrue(ss.getPlayers().size() == 0);
		assertTrue(ss.getLobbyManagement().getLobbyList().size() == 0);
		
		//Check if player name is freed properly
		int p4 = 0;
		Queue<Message> l4 = new ConcurrentLinkedQueue<Message>();
		l4.add(new Message(Message.LOBBY_NEW,null,new String[]{"name2","password2"}));
		l4.add(new Message(Message.LOBBY_JOIN,new int[]{1},new String[]{"123456"}));
		l4.add(new Message(Message.LOBBY_LEAVE,null,null));
		l4.add(new Message(Message.LOBBY_LIST,null,null)); p4++;
		l4.add(new Message(Message.PLAYER_REQUESTID,null,new String[]{"Player 1"})); p4++;
		l4.add(new Message(Message.PLAYER_REQUESTID,null,new String[]{"Player1"})); p4++;
		l4.add(new Message(Message.PLAYER_REQUESTID,null,new String[]{"Player2"}));
		FakeMessageControl c4 = new FakeMessageControl(l4);
		Player player4 = new Player(c4,ss);
		player4.start();
		{
			Message msg = c4.getMessage();
			assertTrue(msg.messageType == Message.LOBBY_LIST_RESULT);
			
			msg = c4.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_REJECT);
			assertTrue(msg.getInts()[0] == 0);
			
			msg = c4.getMessage();
			assertTrue(msg.messageType == Message.PLAYER_ACCEPT);
			assertTrue(msg.getInts()[0] == 4);
		}
		assertFalse(player4.getPlayerID() == -1);
		assertTrue(player4.getPlayerID() == 4);
		
		//check sent messages count
		assertTrue(p1 == c1.getSentMessageCount());
		assertTrue(p2 == c2.getSentMessageCount());
		assertTrue(p3 == c3.getSentMessageCount());
		assertTrue(p4 == c4.getSentMessageCount());
		
		ss.shutdownServer();
		
	}

}
