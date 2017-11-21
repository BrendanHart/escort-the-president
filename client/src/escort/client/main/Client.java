package escort.client.main;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;

import escort.client.game.GameManager;
import escort.client.input.InputHandler;
import escort.client.input.Inputs;
import escort.client.network.NetworkManager;
import escort.client.properties.PropertyManager;
import escort.client.res.ResourceLoader;
import escort.client.sound.SoundManager;
import escort.client.sound.SoundManagerException;
import escort.client.ui.components.panels.DialogPanel;
import escort.client.ui.components.text.TextButton;
import escort.client.ui.menus.MenuManager;

/**
 * A canvas that runs the main game loop, calling update and render methods.
 * Instantiates all client-side subcomponents and the JFrame.
 * 
 * @author Ahmed Bhallo
 *
 */
public class Client extends Canvas implements Runnable {

	/**
	 * The serial ID
	 */
	public static final long serialVersionUID = -1658433071256822397L;

	/**
	 * The target FPS of the game.
	 */
	public static final int FPS = 60;

	/**
	 * The game window object of the game.
	 */
	private GameWindow window;

	/**
	 * The network manager subcomponent.
	 */
	private final NetworkManager networkManager;

	/**
	 * The menu manager subcomponent.
	 */
	private final MenuManager menuManager;

	/**
	 * The game manager subcomponent.
	 */
	private final GameManager gameManager;

	/**
	 * The sound manager subcomponent.
	 */
	private SoundManager soundManager;

	/**
	 * The property manager.
	 */
	private final PropertyManager properties;

	/**
	 * The resource laoder.
	 */
	private final ResourceLoader resourceLoader;

	/**
	 * The input object.
	 */
	private final Inputs inputs = new Inputs();

	/**
	 * The input handler.
	 */
	private final InputHandler inputHandler;

	/**
	 * The message queuer.
	 */
	private final ClientMessageQueuer messageQueuer;

	/**
	 * Whether or not the client should run.
	 */
	private boolean running = false;

	/**
	 * Whether or not the client is currently in a game.
	 */
	private boolean inGame = false;

	/**
	 * The base width of the game.
	 */
	public static final int BASE_WIDTH = 16 * 38;

	/**
	 * The base height of the game.
	 */
	public static final int BASE_HEIGHT = 9 * 38;

	private DialogPanel dialog;

	/**
	 * Instantiates a new client. Calls constructors to instantiate all
	 * client-side subcomponents. Adds the canvas to the JFrame for rendering.
	 */
	public Client() {
		// Do not let the OS repaint the canvas.
		setIgnoreRepaint(true);

		window = new GameWindow();

		// In order to use tab keys, disable focus traversal.
		setFocusTraversalKeysEnabled(false);

		// Load the properties manager and update the scale factor.
		properties = new PropertyManager(this);

		Scale.factor = properties.getInt(PropertyManager.SCALE);

		// Load all resources.
		resourceLoader = new ResourceLoader();
		resourceLoader.load();

		// Create managers for all subcomponents
		networkManager = new NetworkManager(this);
		try {
			soundManager = new SoundManager(this);
			soundManager.loadFromProperties();
		} catch (SoundManagerException e) {
		}
		menuManager = new MenuManager(this, inputs);
		menuManager.displaySetupMenu();
		gameManager = new GameManager(this, inputs);
		inputHandler = new InputHandler(this, inputs);
		inputHandler.reloadBindings();
		messageQueuer = new ClientMessageQueuer(this);

		// Initialise all input handling listeners.
		addKeyListener(inputHandler);
		addMouseListener(inputHandler);
		addMouseMotionListener(inputHandler);
		addMouseWheelListener(inputHandler);

		addToWindow();
	}

	/**
	 * Starts the client main game loop.
	 */
	public void start() {
		running = true;
		new Thread(this).start();
	}

	/**
	 * The main loop of the game. Ensures update() and render() are called a
	 * number of times per second equal to the FPS variable.
	 */
	@Override
	public void run() {
		// Target nanoseconds between ticks (updates).
		double nsPerUpdate = 1000000000 / FPS;

		// A measure of update timing. Whether or not we are lagging, leading or
		// spot on.
		// delta < 1 -> Updating too often
		// delta == 1 -> Perfect timing
		// delta > 1 -> Updating too slowly
		double delta = 0;

		// The last time since we last looped
		long lastLoopTime = System.nanoTime();

		// A time that counts the number of frames updates per second.
		long timer = 0;

		// A counter of the number of frames updated and rendered in the past
		// second. Not needed right now.
		// Uncomment if you want to display/render fps.
		// int frames = 0;

		while (running) {
			// Store the time now.
			long now = System.nanoTime();

			// Increment delta by the time in nanoseconds from now since the
			// last time we looped divided by the nsPerUpdate.
			delta += (now - lastLoopTime) / nsPerUpdate;

			// The timer is incremented by the time since the last time we
			// looped.
			timer += now - lastLoopTime;

			// Set the last loop time as now.
			lastLoopTime = now;

			// If we are updating on time or lagging, update.
			if (delta >= 1) {
				update();
				render();

				// Increment the number of frames.
				// Uncomment if you want to display/render fps.
				// frames++;

				// Decrement delta.
				delta--;
			} else {
				// This is to reduce resources consumption on fast machines
				try {
					long sleeptime = lastLoopTime + (long) (nsPerUpdate) - System.nanoTime();
					if (sleeptime > 0) {
						Thread.sleep(sleeptime / 1000000, (int) (sleeptime % 1000000));
					}
				} catch (InterruptedException e) {
				}
			}

			// Every second, store the number of frames that we rendered for
			// the second into actualFPS.
			if (timer >= 1000000000) {
				// actualFPS = frames;

				// Reset the counters and delta.
				if (delta > 1) {
					delta = 1;
				}
				// Uncomment if you want to display/render fps.
				// frames = 0;
				timer = 0;
			}
		}
	}

	/**
	 * Allows the message queuer to perform updates based on received messages
	 * from the server. Updates the game manager if in game, otherwise update
	 * the menu manager. Releases input keys.
	 */
	public void update() {
		messageQueuer.update();

		if (dialog != null) {
			dialog.update();
		} else if (inGame) {
			gameManager.update();
		} else {
			menuManager.update();
		}
		inputHandler.releaseTypedAndScroll();
		if (!hasFocus()) {
			// Release every key if we have lost focus.
			inputHandler.releaseAll();
		}
	}

	/**
	 * Renders the game manager if in game, otherwise renders the menu manager.
	 */
	public void render() {
		// Get the buffer strategy.
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			// If it is null, call for it to be created and return.
			createBufferStrategy(3);
			return;
		}

		// Get the graphics2d object from the buffer strategy.
		Graphics2D g = (Graphics2D) bs.getDrawGraphics();

		// Clear the rectangle.
		g.clearRect(0, 0, getGameWidth(), getGameHeight());

		if (inGame) {
			// Render the game manager if in game.
			gameManager.render(g);
		} else {
			// Render the menu manager if not in game.
			menuManager.render(g);
		}

		// Render the dialog if there is one.
		if (dialog != null) {
			dialog.render(g);
		}

		// Output the FPS for debugging.
		// g.setColor(Color.cyan);
		// g.drawString("FPS: " + actualFPS, 10, 20 * Scale.factor);

		// Show the buffer strategy and dispose the graphics object.
		bs.show();
		g.dispose();
	}

	/**
	 * Sets whether or not the client is in game.
	 * 
	 * @param inGame
	 *            The new in game boolean
	 */
	public void setInGame(boolean inGame) {
		this.inGame = inGame;
	}

	/**
	 * Returns whether or not the client is in game.
	 * 
	 * @return True iff the client is in game.
	 */
	public boolean isInGame() {
		return inGame;
	}

	/**
	 * Returns the network manager.
	 * 
	 * @return The network manager.
	 */
	public NetworkManager getNetworkManager() {
		return networkManager;
	}

	/**
	 * Returns the menu manager.
	 * 
	 * @return The menu manager.
	 */
	public MenuManager getMenuManager() {
		return menuManager;
	}

	/**
	 * Returns the game manager.
	 * 
	 * @return The game manager.
	 */
	public GameManager getGameManager() {
		return gameManager;
	}

	/**
	 * Returns the width of the canvas area. (The base width multiplied by the
	 * scale factor.)
	 * 
	 * @return The width of the canvas area.
	 */
	public int getGameWidth() {
		return BASE_WIDTH * Scale.factor;
	}

	/**
	 * Returns the height of the canvas area. (The base height multiplied by the
	 * scale factor.)
	 * 
	 * @return The height of the canvas area.
	 */
	public int getGameHeight() {
		return BASE_HEIGHT * Scale.factor;
	}

	/**
	 * Gets the preferred size of the canvas element.
	 * 
	 * @return The dimension of the game width and height.
	 */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(getGameWidth(), getGameHeight());
	}

	/**
	 * Gets the message queuer. Acts upon messages received from the server
	 * within the main game loop.
	 * 
	 * @return The message queuer
	 */
	public ClientMessageQueuer getMessageQueuer() {
		return messageQueuer;
	}

	/**
	 * Gets the sound manager.
	 * 
	 * @return The sound manager.
	 */
	public SoundManager getSoundManager() {
		return soundManager;
	}

	public InputHandler getInputHandler() {
		return inputHandler;
	}

	/**
	 * Gets the property manager.
	 * 
	 * @return The property manager.
	 */
	public PropertyManager getProperties() {
		return properties;
	}

	/**
	 * Sets the current dialog panel and makes it modal. If the argument is
	 * null, removes the current modal panel.
	 * 
	 * @param dialog
	 *            The dialog panel to be modal. Null to remove any pre-existing
	 *            dialog panels.
	 */
	public void setDialog(DialogPanel dialog) {
		this.dialog = dialog;
	}

	/**
	 * Gets the resource loader
	 * 
	 * @return The resource loader
	 */
	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	/**
	 * Readds this canvas element to the game window's JFrame.
	 */
	public void addToWindow() {
		window.setContent(this);
	}

	/**
	 * Displays an error message and takes the client to the setup menu when the
	 * client has disconnected from the server.
	 */
	public void serverDisconnected() {
		inGame = false;
		menuManager.displaySetupMenu();
		TextButton okButton = new TextButton("OK", inputs);
		okButton.addListener(e -> {
			menuManager.displaySetupMenu();
			setDialog(null);
		});
		DialogPanel disconnectDialog = new DialogPanel(inputs, getGameWidth(), getGameHeight(),
				"The server has disconnected", "Taking you back to the server setup menu", okButton);
		setDialog(disconnectDialog);
	}

}