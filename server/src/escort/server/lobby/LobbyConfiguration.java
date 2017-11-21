package escort.server.lobby;

import java.io.BufferedInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Represents the configuration of the lobby
 * @author Kwong Hei Tsang
 *
 */
public class LobbyConfiguration {

	private static final Element config = getConfigs();
	
	public final static long KICK_PREVENT_JOIN = getKickPreventJoin();
	public final static long MAX_INACTIVE_PERIOD = getMaxInactivePeriod();
	public final static long SWEEPER_PERIOD = getSweeperPeriod();
	public final static int MAX_PLAYERS = getMaxPlayers();
	public final static int START_COUNT_DOWN = getStartCountDown();
	public final static int MAX_CHAT_IN_S = getMaxChatInS();
	
	public final static int MAX_NUM_POLICE_AI = getMaxNumPoliceAI();
	public final static int MAX_NUM_CIVILIAN_AI = getMaxNumCivilianAI();
	public final static int MAX_NUM_ASSASSINS_AI = getMaxNumAssassinsAI();
	
	/**
	 * Get the configurations from the configuration file
	 * @return The configuration file
	 */
	private static Element getConfigs(){
		try {
			// Get document builder
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			BufferedInputStream stream = new BufferedInputStream(LobbyConfiguration.class.getResourceAsStream("config.xml"));
			Document doc = builder.parse(stream);
			stream.close();
			Element elem = doc.getDocumentElement();
			// elem should not be null
			if(elem == null){
				System.err.println("Failed to find lobby configurations.");
				System.exit(1);
			}
			
			return elem;
		} catch (SAXException | IOException | ParserConfigurationException e) {
			System.err.println("Failed to load lobby configuration.");
			System.exit(1);
			return null;
		}
	}

	/**
	 * Get the time before game start after master pressing start
	 * @return The time the players have to wait before they can move
	 */
	private static int getStartCountDown(){
		int result = 0;
		try{
			result = Integer.parseInt(config.getAttribute("START_COUNTDOWN"));
		}catch(NumberFormatException e){
			System.err.println("Failed to load START_COUNTDOWN");
			System.exit(1);
		}
		return result;
	}
	
	/**
	 * Get the time interval before a player can rejoin the same lobby after being kicked
	 * @return The time interval
	 */
	private static long getKickPreventJoin(){
		long result = 0;
		try{
			result = Long.parseLong(config.getAttribute("KICK_PREVENT_JOIN"));
		}catch(NumberFormatException e){
			System.err.println("Failed to load KICK_PREVENT_JOIN");
			System.exit(1);
		}
		return result;
	}
	
	/**
	 * Get the maximum time allowed for waiting
	 * @return the maximum time allowed for waiting
	 */
	private static long getMaxInactivePeriod(){
		long result = 0;
		try{
			result = Long.parseLong(config.getAttribute("MAX_INACTIVE_PERIOD"));
		}catch(NumberFormatException e){
			System.err.println("Failed to load MAX_INACTIVE_PERIOD");
			System.exit(1);
		}
		return result;
	}
	
	/**
	 * How often does the LobbyManagement remove inactive lobbies exceeding time limit
	 * @return The corresponding time
	 */
	private static long getSweeperPeriod(){
		long result = 0;
		try{
			result = Long.parseLong(config.getAttribute("SWEEPER_PERIOD"));
		}catch(NumberFormatException e){
			System.err.println("Failed to load SWEEPER_PERIOD");
			System.exit(1);
		}
		return result;
	}
	
	/**
	 * Get the maximum number of players of each lobby
	 * @return The maximum number of players of each lobby
	 */
	private static int getMaxPlayers(){
		int result = 0;
		try{
			result = Integer.parseInt(config.getAttribute("MAX_PLAYERS"));
		}catch(NumberFormatException e){
			System.err.println("Failed to load MAX_PLAYERS");
			System.exit(1);
		}
		return result;
	}
	
	/**
	 * Get the maximum number of chat messages per second
	 * @return The maximum number of chat messages per second
	 */
	private static int getMaxChatInS(){
		int result = 0;
		try{
			result = Integer.parseInt(config.getAttribute("MAX_CHAT_IN_S"));
		}catch(NumberFormatException e){
			System.err.println("Failed to load MAX_CHAT_IN_S");
			System.exit(1);
		}
		return result;
	}
	
	/**
	 * Get the maximum number of police AI
	 * @return maximum number of police AI
	 */
	private static int getMaxNumPoliceAI(){
		int result = 0;
		try{
			result = Integer.parseInt(config.getAttribute("MAX_NUM_POLICE_AI"));
		}catch(NumberFormatException e){
			System.err.println("Failed to load MAX_NUM_POLICE_AI");
			System.exit(1);
		}
		return result;
	}
	
	/**
	 * Get the maximum number of Civilian AI
	 * @return The maximum number of Civilian AI
	 */
	private static int getMaxNumCivilianAI(){
		int result = 0;
		try{
			result = Integer.parseInt(config.getAttribute("MAX_NUM_CIVILIAN_AI"));
		}catch(NumberFormatException e){
			System.err.println("Failed to load MAX_NUM_CIVILIAN_AI");
			System.exit(1);
		}
		return result;
	}
	
	/**
	 * Get the maximum number of Assassins AI
	 * @return The maximum number of Assassins AI
	 */
	private static int getMaxNumAssassinsAI(){
		int result = 0;
		try{
			result = Integer.parseInt(config.getAttribute("MAX_NUM_ASSASSINS_AI"));
		}catch(NumberFormatException e){
			System.err.println("Failed to load MAX_NUM_ASSASSINS_AI");
			System.exit(1);
		}
		return result;
	}
}
