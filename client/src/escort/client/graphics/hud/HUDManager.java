package escort.client.graphics.hud;

import java.awt.Graphics2D;

import escort.client.game.GameManager;
import escort.client.input.Inputs;
import escort.client.main.Client;
import escort.client.main.Scale;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.text.TextLabel;
import escort.client.ui.menus.settings.SettingsPanel;
import escort.client.ui.utils.Colors;
import escort.client.ui.utils.Fonts;

/**
 * Manages the in-game HUD. Displays the chat, radar, weapons panel, and
 * settings panel.
 * 
 * @author Ahmed Bhallo
 *
 */
public class HUDManager {

	private final GameManager gameManager;
	private final SpectatorSystem specSystem;
	private final ChatBox chat;
	private final TextLabel countDown;
	private final WeaponsPanel weaponsPanel;
	private final Radar radar;
	private final Client client;
	private final Panel hudContainer;

	private final SettingsPanel settingsPanel;
	private final Panel settingsContainer;
	private boolean displaySettings = false;

	private final Inputs inputs;

	/**
	 * Instantiates a new HUD manager
	 * 
	 * @param gameManager
	 *            The game manager
	 * @param inputs
	 *            The inputs object
	 */
	public HUDManager(GameManager gameManager, Inputs inputs) {
		this.gameManager = gameManager;
		this.client = gameManager.getClient();
		this.inputs = inputs;
		chat = client.getMenuManager().getChat();
		chat.transformToGameChat();
		countDown = new TextLabel("", inputs, Colors.PRESIDENTIAL_RED);
		countDown.setCentered(true);
		countDown.setFont(Fonts.HEADER);
		
		// Create components of UI
		
		settingsContainer = new Panel(inputs, client.getGameWidth(), client.getGameHeight());
		settingsPanel = new SettingsPanel(inputs, client, 400 * Scale.factor, 300 * Scale.factor);
		settingsContainer.add(settingsPanel, settingsContainer.center(settingsPanel));
		hudContainer = new Panel(inputs, client.getGameWidth(), client.getGameHeight());
		client.getMenuManager().getLobbyMenu().remove(chat);
		
		// Add them to the container
		
		hudContainer.add(chat, 0, hudContainer.getHeight() - chat.getHeight());
		specSystem = new SpectatorSystem(gameManager, inputs, hudContainer);
		radar = new Radar(this, inputs);
		hudContainer.add(radar, hudContainer.getWidth() - radar.getWidth(),
				hudContainer.getHeight() - radar.getHeight());
		weaponsPanel = new WeaponsPanel(this, inputs);
		hudContainer.add(weaponsPanel, hudContainer.center(weaponsPanel).x, 0);
	}

	/**
	 * Updates the spectator system, the hud controller, the display settings
	 * (if active), and detects if the settings has been opened.
	 */
	public void update() {
		specSystem.update();
		hudContainer.update();
		if (displaySettings) {
			settingsContainer.update();
		}
		if (inputs.esc.isPressed()) {
			displaySettings = !displaySettings;
			inputs.esc.setPressed(false);
		}
	}

	/**
	 * Renders the hud controller and the display settings (if active),
	 * 
	 * @param g
	 *            The graphics object
	 */
	public void render(Graphics2D g) {
		hudContainer.render(g);
		if (displaySettings) {
			settingsContainer.render(g);
		}
	}

	/**
	 * Updates the countdown count.
	 * 
	 * @param i
	 *            The current count.
	 */
	public void updateCount(int i) {
		countDown.setText("Start in: " + i, true);
		hudContainer.add(countDown, hudContainer.center(countDown).x, 50 * Scale.factor);
	}

	/**
	 * Called when the game starts. Displays the "go" message and removes it
	 * after 2 seconds.
	 */
	public void gameStarted() {
		countDown.setText("GO!", true);
		hudContainer.add(countDown, hudContainer.center(countDown).x, 50 * Scale.factor);
		new Thread(() -> {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
			hudContainer.remove(countDown);
		}).start();
	}

	/**
	 * @return The game manager.
	 */
	public GameManager getGameManager() {
		return gameManager;
	}

	/**
	 * @return The chat box.
	 */
	public ChatBox getChat() {
		return chat;
	}

	/**
	 * Removes the chat box from the hud.
	 */
	public void removeChat() {
		hudContainer.remove(chat);
	}

	/**
	 * @return True if the settings are being displayed.
	 */
	public boolean isDisplaySettings() {
		return displaySettings;
	}
}
