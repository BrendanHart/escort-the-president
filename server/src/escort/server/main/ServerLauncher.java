package escort.server.main;

import java.io.File;
import java.io.*;

import escort.server.network.Server;

/**
 * Launch the server
 * 
 * @author Kwong Hei Tsang
 *
 */
public final class ServerLauncher {

	/**
	 * The main method for launching the server
	 * 
	 * @param args
	 */
	public final static void main(String[] args) {
		//enable headless mode
		System.setProperty("java.awt.headless", "true"); 
		
		//different keystore, password, key
		if(args.length != 0){
			main2(args);
			return;
		}
		
		//default operations
		File file = new File("key");
		if(!file.exists()){
			generateKey();
		}
		
		//so far so good
		new Server(8888);
	}
	
	/**
	 * Start the server with custom keystore, key and password
	 * @param args The arguments
	 */
	public final static void main2(String[] args){
		//Problem with arguments
		if(args.length != 3){
			System.err.println("This program should take 3 arguments to run with custom key and password.");
			System.err.println("<keystore in JKS format> <alias> <password>");
			return;
		}
		
		//so far so good
		new Server(8888,args[0],args[1],args[2]);
	}
	
	/**
	 * Generate a key
	 */
	private final static void generateKey(){
		// Generate the command for generating code
		System.out.println("Generating Key\n");
		String pathtojre = System.getProperty("java.home");
		String pathseperator = System.getProperty("file.separator");
		String pathtokeytool = pathtojre + pathseperator + "bin" + System.getProperty("file.separator") + "keytool";
		String command = pathtokeytool + " -genkeypair -alias mykey -keyalg RSA -keystore key -storepass 1234567 -keypass 1234567";
		
		try {
			// execute the command
			Process p = Runtime.getRuntime().exec(command);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			
			// Keep entering y
			while(reader.readLine() != null){
				// Write y
				writer.write("y" + System.lineSeparator());
				writer.flush();
			}
			
			//Wait until the process ends
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}