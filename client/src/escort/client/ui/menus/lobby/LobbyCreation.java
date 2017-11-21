package escort.client.ui.menus.lobby;

import java.awt.Color;
import java.io.IOException;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.ui.components.text.InputField;
import escort.client.ui.components.text.TextButton;
import escort.client.ui.components.text.TextLabel;
import escort.client.ui.menus.AbstractMenu;
import escort.client.ui.menus.MenuManager;
import escort.client.ui.utils.Colors;
import escort.common.game.lobby.NameValidation;

/**
 * The menu displayed to the client when they want to create a lobby. Contains
 * input field for the desired name and input field for optional password.
 * 
 * @author Ahmed Bhallo
 *
 */
public class LobbyCreation extends AbstractMenu {

	private static final String NO_CONNECTION = "Connection lost. Try again later";
	private static final String CONNECTING_STATUS = "Creating a lobby...";
	private static final String EMPTY_FIELD_STATUS = "You must enter a lobby name";
	private static final String INVALID_NAME_STATUS = "Lobby names must be alphanumeric and at most 20 characters";

	private InputField lobbyNameInput, passwordInput;
	private TextLabel status, createButton;

	/**
	 * Instantiates a new lobby creation menu
	 * 
	 * @param inputs
	 *            The inputs object
	 * @param menuManager
	 *            The menu manager
	 */
	public LobbyCreation(Inputs inputs, MenuManager menuManager) {
		super(inputs, menuManager, "CREATE A LOBBY");

		int separation = 3 * Scale.factor;
		int separationMultiplier = 6;
		int yPos = 42 * Scale.factor;

		TextLabel lobbyNameLabel = new TextLabel("Enter lobby name", inputs, Color.WHITE);
		add(lobbyNameLabel, center(lobbyNameLabel).x, yPos);

		yPos += lobbyNameLabel.getHeight();
		yPos += separation;

		lobbyNameInput = new InputField(inputs, 20);
		add(lobbyNameInput, center(lobbyNameInput).x, yPos);

		yPos += lobbyNameInput.getHeight();
		yPos += separation * separationMultiplier;

		TextLabel passwordLabel = new TextLabel("Create a password (optional)", inputs, Color.WHITE);
		add(passwordLabel, center(passwordLabel).x, yPos);

		yPos += passwordLabel.getHeight();
		yPos += separation;

		passwordInput = new InputField(inputs, 20);
		add(passwordInput, center(passwordInput).x, yPos);

		yPos += passwordInput.getHeight();
		yPos += separation * separationMultiplier;

		createButton = new TextButton("Create", inputs);
		createButton.addListener(e -> createLobby());
		add(createButton, center(createButton).x, yPos);

		status = new TextLabel("", inputs, Colors.PRESIDENTIAL_RED) {
			public void setText(String text, boolean updateWidth) {
				super.setText(text, updateWidth);
				add(status, center(status).x, LobbyCreation.this.getHeight() - this.getHeight());
			};
		};
		add(status, center(status).x, getHeight() - status.getHeight() - separation);

		lobbyNameInput.setNextTraversable(passwordInput);
		lobbyNameInput.addListener(e -> lobbyNameInput.traverseFocus());

		passwordInput.addListener(e -> createLobby());
	}

	/**
	 * Called when the create lobby message is called. Does client-side
	 * validation and sends message to the server to request to create a lobby.
	 * Awaits confirmation.
	 */
	private void createLobby() {
		if (!createButton.isEnabled()) {
			return;
		}
		createButton.setEnabled(false);

		if (lobbyNameInput.getText().isEmpty()) {
			status.setText(EMPTY_FIELD_STATUS, true);
			createButton.setEnabled(true);
			return;
		}

		if (!NameValidation.validateLobby(lobbyNameInput.getText())) {
			status.setText(INVALID_NAME_STATUS, true);
			createButton.setEnabled(true);
			return;
		}

		new Thread(() -> {
			try {
				menuManager.getClient().getNetworkManager().createLobby(lobbyNameInput.getText(),
						passwordInput.getText());
			} catch (IOException e) {
				createButton.setEnabled(true);
				status.setText(NO_CONNECTION, true);
			}
		}).start();
		status.setText(CONNECTING_STATUS, true);
	}

	/**
	 * Resets the form fields to empty.
	 */
	public void restoreToDefault() {
		lobbyNameInput.setText("");
		passwordInput.setText("");
		status.setText("", true);
		createButton.setEnabled(true);
	}

	/**
	 * Called when the back buttin is pressed.
	 */
	@Override
	public void goBack() {
		menuManager.displayLobbyList();
	}

}
