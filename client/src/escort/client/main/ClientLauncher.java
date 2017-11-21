package escort.client.main;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * Launching the Client
 * 
 * @author Ahmed Bhallo
 *
 */
public final class ClientLauncher {

	/**
	 * Not an instantiate-able object.
	 */
	private ClientLauncher() {
	}

	/**
	 * Main method to launch the client. Usage: java Launcher
	 * 
	 * @param args
	 *            No arguments.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
		}
		new Thread(() -> {
			Client client = new Client();
			client.start();
		}).start();
	}
}
