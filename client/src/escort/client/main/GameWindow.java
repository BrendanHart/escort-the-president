package escort.client.main;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.Image;

import javax.swing.JFrame;
import javax.swing.JPanel;

import escort.client.properties.PropertyManager;

/**
 * Contains the JFrame of the game.
 * 
 * @author Ahmed Bhallo
 *
 */
public class GameWindow {

	/**
	 * The JFrame of the game.
	 */
	private final JFrame frame;

	public static Image THUMBNAIL;

	/**
	 * Instantiates a new Game Window object.
	 */
	public GameWindow() {
		// Creates the JFrame
		frame = new JFrame("Escort The President");

		frame.setResizable(false);

		// This is so we can use the tab key.
		frame.setFocusTraversalKeysEnabled(false);

		frame.setAutoRequestFocus(true);

		// Close on exit.
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	/**
	 * Sets the client object as the content of the window and the JFrame.
	 * 
	 * @param client
	 *            The client object.
	 */
	public void setContent(Client client) {
		frame.dispose();

		boolean fullscreen = Boolean.parseBoolean(client.getProperties().getProperty(PropertyManager.FULLSCREEN));
		if (fullscreen) {
			frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			frame.setUndecorated(true);
		} else {
			frame.setExtendedState(JFrame.NORMAL);
			frame.setUndecorated(false);
		}
		// Create a JPanel container for the canvas.
		JPanel container = new JPanel(new GridBagLayout());
		container.setBackground(Color.BLACK);

		// Add the client to the container.
		container.add(client);

		// Sets the content pane of the JFrame as the container.
		frame.setContentPane(container);

		// Packs the frame.
		frame.pack();

		// Repaint.
		frame.revalidate();
		frame.repaint();

		// Set its position to the center of the screen.
		frame.setLocationRelativeTo(null);

		frame.setIconImage(THUMBNAIL);

		// Set it to be visible.
		frame.setVisible(true);
	}

	/**
	 * Gets the JFrame window.
	 * 
	 * @return The JFrame window.
	 */
	public JFrame getFrame() {
		return frame;
	}
}