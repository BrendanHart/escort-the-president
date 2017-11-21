package escort.server.console;

import java.security.cert.Certificate;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Scanner;

import escort.common.game.Outcomes;
import escort.server.game.Game;
import escort.server.lobby.Lobby;
import escort.server.network.Player;
import escort.server.network.ServerSide;

/**
 * This class is for monitoring the activity of the server
 * @author Kwong Hei Tsang
 *
 */
public class ServerConsole implements Runnable {
	
	private final ServerSide ss;
	private boolean tcpready;
	private boolean udpready;
	private Object wait;
	private Certificate cert;

	/**
	 * Construct a server console for monitoring
	 * @param ss
	 */
	public ServerConsole(ServerSide ss){
		this.ss = ss;
		this.tcpready = false;
		this.udpready = false;
		this.wait = new Object();
		this.cert = null;
	}
	
	public void tcpReady(){
		synchronized(this.wait){
			this.tcpready = true;
			this.wait.notifyAll();
		}
	}
	
	public void udpReady(Certificate cert){
		synchronized(this.wait){
			this.udpready = true;
			this.cert = cert;
			this.wait.notifyAll();
		}
	}
	
	/**
	 * The method running in the server console
	 */
	@Override
	public void run() {
		synchronized(this.wait){
			while(!this.tcpready || !this.udpready){
				try {
					this.wait.wait();
				} catch (InterruptedException e) {
				}
			}
		}
		
		if(this.cert != null){
			//Print the public key on screen
			Encoder b64encode = Base64.getEncoder();
			String encodedkey = new String(b64encode.encode(this.cert.getPublicKey().getEncoded()));
			String printkey = "";
			for(int i = 0; i < encodedkey.length(); i++){
				printkey += encodedkey.charAt(i);
				if(i > 0 && i%100 == 0){
					printkey += "\n";
				}
			}
			System.out.println("The public key of the server is: ");
			System.out.println(printkey);
			System.out.println("Please provide this public key to your friends to verify your server.");
			System.out.println("");
		}
		
		// Start the server console
		Scanner in = new Scanner(System.in);
		while(true){
			System.out.println("Please enter command:\n");
			System.out.println("players : list players");
			System.out.println("player : show information of specific player");
			System.out.println("lobbies : list lobbies");
			System.out.println("end : Force end game");
			System.out.println("exit : shutdown server");
			System.out.print("#");
			System.out.flush();
			
			//read command
			String command = in.nextLine();
			
			if(command.equals("players")){
				System.out.println("List of players:");
				for(Player player : this.ss.getPlayers()){
					int id = player.getPlayerID();
					String name = player.getPlayerName();
					if(name != null){
						System.out.println("Player ID: " + id + ", Name: " + name + ", In game: " + (player.getGame() != null));
					}
				}
			}else if(command.equals("player")){
				System.out.println("Please provide a player name");
				String playerstr = in.nextLine();
				int id = -1;
				Integer ido = this.ss.getNameToID().get(playerstr);
				if(ido != null){
					id = ido;
				}
				Player player = this.ss.getPlayer(id);
				if(player == null){
					System.out.println("Player invalid");
				}else{
					System.out.println("Player ID: " + id + ", Name: " + player.getPlayerName() + ", In game: " + (player.getGame() != null) + ", Lobby ID: " + player.getLobbyID());
				}
			}else if(command.equals("lobbies")){
				System.out.println("List of lobbies:");
				for(Lobby lobby : this.ss.getLobbyManagement().getLobbyList()){
					String players = "";
					for(Player player : lobby.getPlayers()){
						players += " " + player.getPlayerID() + " ";
					}
					System.out.println("Lobby ID: " + lobby.getID() + ", Lobby name: " + lobby.getName() + ", Master: " + lobby.getMaster().getPlayerID() + ", List of players: " + players);
				}
			}else if(command.equals("exit")){
				this.ss.shutdownServer();
				System.exit(0);
				break;
			}else if(command.equals("end")){
				System.out.println("Please provide one of the player IDs");
				String idstr = in.nextLine();
				try{
					int id = Integer.parseInt(idstr);
					Player player = this.ss.getPlayers().get(id);
					if(player == null){
						System.out.println("Player ID invalid");
					}else{
						Game game = player.getGame();
						if(game == null){
							System.out.println("Player not on any game");
						}else{
							game.endGame(Outcomes.OUTCOME_DRAW);
							System.out.println("Game ended.");
						}
					}
				}catch(NumberFormatException e){
					System.out.println("Player ID format error.");
				}
			}else{
				System.out.println("Unknown command");
			}
			
			System.out.println("");
		}	
		in.close();
	}
}
