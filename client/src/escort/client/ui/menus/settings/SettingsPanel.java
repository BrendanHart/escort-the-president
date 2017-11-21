package escort.client.ui.menus.settings;

import java.awt.Toolkit;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import escort.client.input.Inputs;
import escort.client.main.Client;
import escort.client.main.Scale;
import escort.client.properties.PropertyManager;
import escort.client.ui.components.Component;
import escort.client.ui.components.Stepper;
import escort.client.ui.components.panels.DialogPanel;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.panels.ScrollableList;
import escort.client.ui.components.text.TextButton;
import escort.client.ui.components.text.TextLabel;
import escort.client.ui.utils.Colors;
import escort.common.systime.SystemTime;

/**
 * A panel that contains the settings panel. For each input setting, contains a
 * binding detector. Performs functionality to save, restore and revert changes.
 * 
 * @author Ahmed Bhallo
 *
 */
public class SettingsPanel extends Panel {

	private final int BUTTONS_HEIGHT = 32 * Scale.factor;
	private final int SEPARATION = 15 * Scale.factor;

	public final int TOP_PADDING = 20 * Scale.factor;
	public final int BUTTONS_WIDTH = 50 * Scale.factor;
	public final int BUTTON_SEPARATION = 40 * Scale.factor;

	public final int COMP_WIDTH = 160 * Scale.factor;
	private final int COMP_HEIGHT = 24 * Scale.factor;
	private final int LEFT_INSET = 20 * Scale.factor;
	private final int RIGHT_INSET = 50 * Scale.factor;

	private DialogPanel changesDialog;
	private TextButton acceptChangesButton;
	private long timeWhenDisplayingChanges = -1;

	private static final int[] BINDING_PROPERTY_CODES = new int[] { PropertyManager.MOVE_UP_KEY,
			PropertyManager.MOVE_DOWN_KEY, PropertyManager.MOVE_LEFT_KEY, PropertyManager.MOVE_RIGHT_KEY,
			PropertyManager.SHOOT_KEY, PropertyManager.RELOAD_KEY, PropertyManager.GRENADE_KEY,
			PropertyManager.FOLLOW_KEY, PropertyManager.PISTOL_SWITCH_KEY, PropertyManager.MG_SWITCH_KEY,
			PropertyManager.SHIELD_SWITCH_KEY, PropertyManager.WEAPON_SCROLL_UP_KEY,
			PropertyManager.WEAPON_SCROLL_DOWN_KEY, PropertyManager.CAMERA_LOCK_KEY };

	private final VolumePanel bgmVolume;
	private final VolumePanel effectsVolume;
	private final Stepper resolutionStepper;
	private final Stepper displayStepper;

	private final Map<Integer, BindingDetector> detectionMap = new ConcurrentHashMap<>();

	private final Client client;

	private final ScrollableList list;

	private DialogPanel duplicateBindingDialog;
	// private final TextLabel label;

	/**
	 * Instantiates a new settings panel object
	 * 
	 * @param inputs
	 *            The inputs object
	 * @param client
	 *            The client
	 * @param width
	 *            The width of the panel
	 * @param height
	 *            The height of the panel
	 */
	public SettingsPanel(Inputs inputs, Client client, int width, int height) {
		super(inputs, width, height);
		list = new ScrollableList(inputs, width, height - BUTTONS_HEIGHT);
		add(list, 0, 0);
		// set the attributes and the background colour
		this.client = client;

		setBackground(Colors.UI_BG);

		initButtonsPanel();

		addHeading("Video");

		TextButton detectButton = new TextButton("Detect", inputs);
		detectButton.addListener(e -> autoDetectScale());
		resolutionStepper = new Stepper(inputs, COMP_WIDTH - detectButton.getWidth() - 5 * Scale.factor, 0,
				Client.BASE_WIDTH + " x " + Client.BASE_HEIGHT, Client.BASE_WIDTH * 2 + " x " + Client.BASE_HEIGHT * 2,
				Client.BASE_WIDTH * 3 + " x " + Client.BASE_HEIGHT * 3,
				Client.BASE_WIDTH * 4 + " x " + Client.BASE_HEIGHT * 4,
				Client.BASE_WIDTH * 5 + " x " + Client.BASE_HEIGHT * 5);
		Panel resContainer = new Panel(inputs, COMP_WIDTH,
				Math.max(detectButton.getHeight(), resolutionStepper.getHeight()));
		resContainer.add(resolutionStepper, 0, resContainer.center(resolutionStepper).y);
		resContainer.add(detectButton, resContainer.getWidth() - detectButton.getWidth(),
				resContainer.center(detectButton).y);
		addEntryPair("Resolution", resContainer);
		displayStepper = new Stepper(inputs, COMP_WIDTH, 0, "Windowed", "Fullscreen");
		addEntryPair("Diplay mode", displayStepper);
		addHeading("Audio");
		bgmVolume = new VolumePanel(inputs, COMP_WIDTH, COMP_HEIGHT);
		addEntryPair("Music volume", bgmVolume);
		effectsVolume = new VolumePanel(inputs, COMP_WIDTH, COMP_HEIGHT);
		addEntryPair("Sound effects volume", effectsVolume);
		addHeading("Inputs");
		addBindingEntry("Move Up", PropertyManager.MOVE_UP_KEY);
		addBindingEntry("Move Down", PropertyManager.MOVE_DOWN_KEY);
		addBindingEntry("Move Left", PropertyManager.MOVE_LEFT_KEY);
		addBindingEntry("Move Right", PropertyManager.MOVE_RIGHT_KEY);
		addBindingEntry("Fire Weapon", PropertyManager.SHOOT_KEY);
		addBindingEntry("Reload Weapon", PropertyManager.RELOAD_KEY);
		addBindingEntry("Cook Grenade", PropertyManager.GRENADE_KEY);
		addBindingEntry("Request President Follow", PropertyManager.FOLLOW_KEY);
		addBindingEntry("Switch to Machine Gun", PropertyManager.MG_SWITCH_KEY);
		addBindingEntry("Switch to Pistol", PropertyManager.PISTOL_SWITCH_KEY);
		addBindingEntry("Switch to Blast Shield", PropertyManager.SHIELD_SWITCH_KEY);
		addBindingEntry("Weapon Slot Scroll Up", PropertyManager.WEAPON_SCROLL_UP_KEY);
		addBindingEntry("Weapon Slot Scroll Down", PropertyManager.WEAPON_SCROLL_DOWN_KEY);
		addBindingEntry("Toggle camera lock", PropertyManager.CAMERA_LOCK_KEY);

		loadFromProperties();
		initDialogPanels();
	}

	/**
	 * Automatically detects the scale and updates the resolution stepper
	 */
	private void autoDetectScale() {
		int detectedScale = Math.min(4, Math.max(0, computeScale() - 1));
		resolutionStepper.setIndex(detectedScale);
	}

	/**
	 * Computes the scale based on the DPI of the client's monitor.
	 * 
	 * @return the scale
	 */
	private static final int computeScale() {
		Toolkit tool = Toolkit.getDefaultToolkit();
		int res = tool.getScreenResolution();
		int scale = (int) Math.round(2 * res / 100.0);
		return scale;
	}

	/**
	 * Initialises the buttons panel
	 */
	private void initButtonsPanel() {
		Panel buttons = new Panel(inputs, getWidth(), BUTTONS_HEIGHT);

		int x = 0;

		TextButton save = new TextButton("Apply changes", inputs);
		save.addListener(e -> saveToProperties());
		buttons.add(save, x, buttons.center(save).y);
		x += save.getWidth() + SEPARATION;

		TextButton restore = new TextButton("Restore defaults", inputs);
		restore.addListener(e -> restoreDefault());
		buttons.add(restore, x, buttons.center(restore).y);
		x += restore.getWidth() + SEPARATION;

		TextButton discardChanges = new TextButton("Discard changes", inputs);
		discardChanges.addListener(e -> loadFromProperties());
		buttons.add(discardChanges, x, buttons.center(discardChanges).y);
		x += discardChanges.getWidth() + SEPARATION;
		buttons.pack();
		add(buttons, center(buttons).x, getHeight() - BUTTONS_HEIGHT);
	}

	/**
	 * Initialises the dialog panels for binding multiple actions error and
	 * display change confirmation.
	 */
	private void initDialogPanels() {
		TextButton okButton = new TextButton("OK", inputs);
		okButton.addListener(e -> {
			client.setDialog(null);
		});
		duplicateBindingDialog = new DialogPanel(inputs, client.getGameWidth(), client.getGameHeight(),
				"Invalid settings", "You may not bind multiple actions to the same key.", okButton);
	}

	private int oldScale;
	private boolean oldFullscreen;

	private void displayChangeWarning() {
		TextButton cancelButton = new TextButton("Revert", inputs);
		cancelButton.addListener(e -> revertChanges());
		acceptChangesButton = new TextButton("Keep (15)", inputs);
		acceptChangesButton.addListener(e ->client.setDialog(null));
		Panel buttons = new Panel(inputs, 140 * Scale.factor, cancelButton.getHeight());
		buttons.add(cancelButton, 0, 0);
		buttons.add(acceptChangesButton, buttons.getWidth() - acceptChangesButton.getWidth(), 0);
		changesDialog = new DialogPanel(inputs, client.getGameWidth(), client.getGameHeight(), "Keep these settings?",
				"Your old display settings will revert in 15 seconds.", buttons) {
			@Override
			public void update() {
				super.update();
				int secElapsed = (int) (SystemTime.milliTime() - timeWhenDisplayingChanges) / 1000;
				acceptChangesButton.setText("Keep (" + (15 - secElapsed) + ")", true);
				if (secElapsed >= 15) {
					revertChanges();
				}
			}
		};

		timeWhenDisplayingChanges = SystemTime.milliTime();
		client.setDialog(changesDialog);
	}

	/**
	 * Reverts the changes made. Rebuilds the client.
	 */
	private void revertChanges() {
		client.setDialog(null);
		PropertyManager prop = client.getProperties();
		Scale.factor = oldScale;
		prop.putProperty(PropertyManager.SCALE, "" + oldScale);
		prop.putProperty(PropertyManager.FULLSCREEN, Boolean.toString(oldFullscreen));
		rebuildClient();
	}

	/**
	 * Restores all defaults. Rebuilds the client.
	 */
	private void restoreDefault() {
		PropertyManager prop = client.getProperties();
		prop.resetToDefaults();
		loadFromProperties();
		Scale.factor = prop.getInt(PropertyManager.SCALE);
		client.getSoundManager().loadFromProperties();
		client.getInputHandler().reloadBindings();
		rebuildClient();
	}

	/**
	 * Saves current changes to properties. Displays display change dialog for
	 * confirmation when display settings have been modified.
	 */
	public void saveToProperties() {
		if (duplicateBindingUsed()) {
			client.setDialog(duplicateBindingDialog);
			return;
		}
		PropertyManager prop = client.getProperties();

		for (int propertyCode : BINDING_PROPERTY_CODES) {
			prop.putProperty(propertyCode, "" + detectionMap.get(propertyCode).getKeyEventCode());
		}

		prop.putProperty(PropertyManager.BGM_VOLUME, "" + bgmVolume.getVolume());
		prop.putProperty(PropertyManager.EFFECT_VOLUME, "" + effectsVolume.getVolume());
		prop.putProperty(PropertyManager.BGM_MUTED, Boolean.toString(bgmVolume.getMute().isSelected()));
		prop.putProperty(PropertyManager.EFFECT_MUTED, Boolean.toString(effectsVolume.getMute().isSelected()));

		boolean displayChangeMade = false;

		int newScale = resolutionStepper.getIndex() + 1;
		oldScale = prop.getInt(PropertyManager.SCALE);
		if (oldScale != newScale) {
			displayChangeMade = true;
		}
		prop.putProperty(PropertyManager.SCALE, "" + newScale);
		Scale.factor = newScale;

		boolean fullscreen = displayStepper.getIndex() == 1;
		oldFullscreen = prop.getBoolean(PropertyManager.FULLSCREEN);
		if (oldFullscreen != fullscreen) {
			displayChangeMade = true;
		}
		prop.putProperty(PropertyManager.FULLSCREEN, Boolean.toString(fullscreen));

		client.getSoundManager().loadFromProperties();
		client.getInputHandler().reloadBindings();

		if (displayChangeMade) {
			rebuildClient();
			displayChangeWarning();
		}
		
	}

	private void rebuildClient() {
		try {
			client.getResourceLoader().loadAllFonts();
		} catch (IOException e) {
			e.printStackTrace();
		}
		client.getMenuManager().init();
		client.getMenuManager().displaySettingsMenu();
		if (client.isInGame()) {
			client.getGameManager().initGraphicsComponents();
		}
		client.addToWindow();
	}

	/**
	 * Loads all settings from the properties object
	 */
	public void loadFromProperties() {
		PropertyManager properties = client.getProperties();
		for (int propertyCode : BINDING_PROPERTY_CODES) {
			detectionMap.get(propertyCode).updateBinding(properties.getInt(propertyCode));
		}
		bgmVolume.setVolume(properties.getInt(PropertyManager.BGM_VOLUME));
		effectsVolume.setVolume(properties.getInt(PropertyManager.EFFECT_VOLUME));
		bgmVolume.getMute().setSelected(properties.getBoolean(PropertyManager.BGM_MUTED));
		effectsVolume.getMute().setSelected(properties.getBoolean(PropertyManager.EFFECT_MUTED));
		resolutionStepper.setIndex(properties.getInt(PropertyManager.SCALE) - 1);
		displayStepper.setIndex(properties.getBoolean(PropertyManager.FULLSCREEN) ? 1 : 0);
	}

	/**
	 * Adds a heading to the UI
	 * 
	 * @param text
	 *            The text of the heading
	 */
	private void addHeading(String text) {
		TextLabel h = new TextLabel(text.toUpperCase(), inputs, Colors.DARK_WHITE);
		list.addEntry(h);
	}

	/**
	 * Adds a binding entry
	 * 
	 * @param label
	 *            The name of the binding
	 * @param propertyCode
	 *            The property code of the binding
	 */
	private void addBindingEntry(String label, int propertyCode) {
		BindingDetector detector = new BindingDetector(inputs, this, COMP_WIDTH);
		addEntryPair(label, detector);
		detectionMap.put(propertyCode, detector);
	}

	/**
	 * Adds an entry pair to the settings
	 * 
	 * @param labelText
	 *            The name of the entry
	 * @param comp
	 *            The component object
	 */
	private void addEntryPair(String labelText, Component comp) {
		TextLabel label = new TextLabel(labelText, inputs);
		Panel entry = new Panel(inputs, getWidth() - RIGHT_INSET, COMP_HEIGHT);
		entry.add(label, LEFT_INSET, entry.center(label).y);
		entry.add(comp, entry.getWidth() - comp.getWidth(), entry.center(comp).y);
		list.addEntry(entry);
	}

	/**
	 * Checks if duplicate bindings have been made.
	 * 
	 * @return True iff the client has used the same binding for multiple keys.
	 */
	private boolean duplicateBindingUsed() {
		Set<Integer> usedEventCodes = new HashSet<>();
		for (BindingDetector detector : detectionMap.values()) {
			if (usedEventCodes.contains(detector.getKeyEventCode())) {
				return true;
			}
			usedEventCodes.add(detector.getKeyEventCode());
		}
		return false;
	}

	/**
	 * @return The client object
	 */
	public Client getClient() {
		return client;
	}
}
