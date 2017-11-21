package escort.client.ui.menus;

import java.awt.Color;
import java.io.IOException;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.ui.components.RadioButtonGroup;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.text.InputField;
import escort.client.ui.components.text.TextButton;
import escort.client.ui.components.text.TextLabel;
import escort.client.ui.utils.Colors;
import escort.client.ui.utils.Fonts;
import escort.common.game.lobby.NameValidation;

/**
 * The setup menu for connecting to a server. Allows user to enter name, server
 * address and port and specify connection method.
 * 
 * @author Ahmed Bhallo
 *
 */
public class SetupMenu extends AbstractMenu {

	private static final String PORT_NAN_STATUS = "Invalid port number";
	private static final String ATTEMPTING_CONN_STATUS = "Connecting...";
	private static final String EMPTY_FIELD_STATUS = "You must fill in all fields";
	private static final String NO_CONN_STATUS = "Could not establish a connection to that server";
	private static final String INVALID_NAME_STATUS = "Names must be alphanumeric and at most 12 characters";
	private static final String REJECT_NAME_STATUS = "That name is currently already in use";

	private InputField nameInput, serverInput, portInput;
	private RadioButtonGroup connTypeGroup;
	private TextLabel status, connectButton;

	/**
	 * Instantiates a new set up menu
	 * 
	 * @param inputs
	 *            The inputs object
	 * @param menuManager
	 *            The menu manager
	 */
	public SetupMenu(Inputs inputs, MenuManager menuManager) {
		super(inputs, menuManager, "CONNECT TO SERVER");
		initForm();
		addListeners();
		displayBackButton = false;
	}

	/**
	 * Initialises the forms
	 */
	private void initForm() {
		int separation = 3 * Scale.factor;
		int separationMultiplier = 6;
		int yPos = 24 * Scale.factor;

		TextLabel usernameLabel = new TextLabel("What's your name?", inputs, Color.WHITE);
		add(usernameLabel, center(usernameLabel).x, yPos);
		yPos += usernameLabel.getHeight();
		yPos += separation;

		nameInput = new InputField(inputs, 20);
		add(nameInput, center(nameInput).x, yPos);

		yPos += nameInput.getHeight();
		yPos += separation * separationMultiplier;

		TextLabel serverLabel = new TextLabel("Enter server address and port", inputs, Color.WHITE);
		add(serverLabel, center(serverLabel).x, yPos);

		yPos += serverLabel.getHeight();
		yPos += separation;

		serverInput = new InputField(inputs, 20);
		serverInput.setText("localhost");
		TextLabel colon = new TextLabel(":", inputs, Color.WHITE);
		colon.setPadding(1, 0);
		colon.setFont(Fonts.HEADER);
		portInput = new InputField(inputs, 5);
		portInput.setText("8888");
		Panel serverAndPort = new Panel(inputs, 0, 0);
		serverAndPort.add(serverInput, 0, 0);
		serverAndPort.pack();
		serverAndPort.add(colon, serverAndPort.getWidth() + Scale.factor, serverAndPort.center(colon).y);
		serverAndPort.pack();
		serverAndPort.add(portInput, serverAndPort.getWidth(), 0);
		serverAndPort.pack();
		add(serverAndPort, center(serverAndPort).x, yPos);

		yPos += portInput.getHeight();
		yPos += separation * separationMultiplier;

		TextLabel connTypeLabel = new TextLabel("Choose connection method", inputs, Color.WHITE);
		add(connTypeLabel, center(connTypeLabel).x, yPos);

		yPos += connTypeLabel.getHeight();
		yPos += separation;

		connTypeGroup = new RadioButtonGroup(inputs);
		connTypeGroup.addRadioButtons("Normal (recommended)", "Pure TCP");
		add(connTypeGroup, center(connTypeGroup).x, yPos);

		yPos += connTypeGroup.getHeight();
		yPos += separation * separationMultiplier;

		connectButton = new TextButton("Connect", inputs);
		connectButton.addListener(e -> connect());
		add(connectButton, center(connectButton).x, yPos);

		status = new TextLabel("", inputs, Colors.PRESIDENTIAL_RED) {
			public void setText(String text, boolean updateWidth) {
				super.setText(text, updateWidth);
				add(status, center(status).x, SetupMenu.this.getHeight() - this.getHeight());
			};
		};
		add(status, center(status).x, getHeight() - status.getHeight() - separation);
	}

	/**
	 * Called when the connect button is pressed. Does client side validation
	 * and tried to connect via network manager.
	 */
	private void connect() {
		if (!connectButton.isEnabled()) {
			return;
		}
		connectButton.setEnabled(false);

		if (nameInput.getText().isEmpty() || serverInput.getText().isEmpty() || portInput.getText().isEmpty()) {
			status.setText(EMPTY_FIELD_STATUS, true);
			connectButton.setEnabled(true);
			return;
		}

		if (!NameValidation.validatePlayer(nameInput.getText())) {
			status.setText(INVALID_NAME_STATUS, true);
			connectButton.setEnabled(true);
			return;
		}

		try {
			final int port = Integer.parseInt(portInput.getText());
			new Thread(() -> {
				try {
					menuManager.getClient().getNetworkManager().establishConnection(nameInput.getText(),
							serverInput.getText(), port, connTypeGroup.getSelectedIndex());
				} catch (IOException e) {
					status.setText(NO_CONN_STATUS, true);
					connectButton.setEnabled(true);
				}
			}).start();

			status.setText(ATTEMPTING_CONN_STATUS, true);

		} catch (NumberFormatException e) {
			status.setText(PORT_NAN_STATUS, true);
			connectButton.setEnabled(true);
		}
	}

	/**
	 * Adds the listeners and focus traversal components to the input fields.
	 */
	private void addListeners() {
		nameInput.setNextTraversable(serverInput);
		serverInput.setNextTraversable(portInput);

		nameInput.addListener(e -> nameInput.traverseFocus());
		serverInput.addListener(e -> serverInput.traverseFocus());
		portInput.addListener(e -> {
			portInput.setFocussed(false);
			connect();
		});
	}

	/**
	 * Resets the form
	 */
	public void resetForm() {
		status.setText("", true);
		connectButton.setEnabled(true);
	}

	/**
	 * Called when username is already taken.
	 */
	public void rejectUsername() {
		status.setText(REJECT_NAME_STATUS, true);
		connectButton.setEnabled(true);
	}

	@Override
	public void goBack() {
		// Do nothing.
	}

}
