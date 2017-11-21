package escort.client.graphics.hud;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Map.Entry;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.network.ClientSender;
import escort.client.network.NetworkManager;
import escort.client.ui.components.Component;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.panels.ScrollableList;
import escort.client.ui.components.text.InputField;
import escort.client.ui.components.text.TextUtils;
import escort.client.ui.utils.Colors;
import escort.common.network.Message;

/**
 * A panel for the chat box to be used in lobby menu and in game.
 * 
 * @author Ahmed Bhallo
 *
 */
public class ChatBox extends Panel {

	private final NetworkManager networkManager;
	private final ClientSender sender;
	private final String username;
	private final ScrollableList messagesList;
	private final InputField inputField;
	private final int col;
	private boolean inGame = false;

	/**
	 * Instantiates a new chat box objects.
	 * 
	 * @param inputs
	 *            The inputs object.
	 * @param networkManager
	 *            The network manager.
	 * @param col
	 *            The number of columns for the input field.
	 */
	public ChatBox(Inputs inputs, NetworkManager networkManager, int col) {
		super(inputs, 0, 130 * Scale.factor);
		this.col = col;
		inputField = new InputField(inputs, col);
		setWidth(inputField.getWidth());
		this.networkManager = networkManager;
		this.username = networkManager.getUsername();
		this.sender = networkManager.getSender();
		messagesList = new ScrollableList(inputs, getWidth(), getHeight() - inputField.getHeight());
		messagesList.setBackground(Colors.VERY_DARK_GRAY);
		inputField.setPromptText("Press ENTER");
		inputField.addListener(e -> {
			String message = inputField.getText();
			if (!message.isEmpty()) {
				sendMessage(inputField.getText());
			}
			inputField.setText("");
			if (inGame) {
				inputField.setFocussed(false);
				messagesList.getScrollBar().setVisible(false);
			}
		});
		add(messagesList, 0, 0);
		add(inputField, 0, messagesList.getHeight());
		transformToLobbyChat();
		networkManager.getLobbyMessageCache().forEach(message -> addMessage(message, Colors.LIGHT_GRAY));
	}

	/**
	 * Updates the panel. Sets the visibility of the scroll bar if the chat is
	 * in game. Detects enter presses and sets the inputfield to be focussed.
	 */
	@Override
	public void update() {
		if (inGame) {
			if (networkManager.getClient().getGameManager().getHUDManager().isDisplaySettings()) {
				return;
			}
		}

		if (!inputField.isFocussed()) {
			for (Entry<Character, Boolean> entry : inputs.typedInput.entrySet()) {
				if (!entry.getValue()) {
					continue;
				}
				Character c = entry.getKey();
				if (c == KeyEvent.VK_ENTER) {
					inputField.setFocussed(true);
					inputs.typedInput.replace(c, false);
					if (inGame) {
						messagesList.getScrollBar().setVisible(true);
					}
				}
			}
		}

		if (inGame) {
			messagesList.getScrollBar().setVisible(inputField.isFocussed() || messagesList.getScrollBar().isHovered());
		}

		// Key presses will be consumed by the input field if we update before
		// detecting presses.
		super.update();
	}

	/**
	 * Sends a message to the server.
	 * 
	 * @param message
	 *            The message to be sent
	 */
	private void sendMessage(String message) {
		sender.put(new Message(Message.LOBBY_MESSAGE, null, new String[] { username, message }));
	}

	/**
	 * Called with a message from the server when a system message has been
	 * received.
	 * 
	 * @param message
	 *            The system message.
	 */
	public void systemMessageReceived(String message) {
		addMessage(message, Colors.PRESIDENTIAL_RED);
	}

	/**
	 * Called with a message from the server when a message from a player has
	 * been received.
	 * 
	 * @param username
	 *            The username of the sender.
	 * @param message
	 *            The message.
	 */
	public void messageReceived(String username, String message) {
		addMessage(username + ": " + message, Colors.LIGHT_GRAY);
	}

	/**
	 * Adds a message to the chat box. If the chat box is at the bottom, set the
	 * scroll to be at the bottom again.
	 * 
	 * @param message The message to be added.
	 * @param color The color of the message.
	 */
	private void addMessage(String message, Color color) {
		Panel messageContainer = TextUtils.wrappedTextLabel(inputs, message, col - 1, color);
		boolean atBottom = messagesList.getScrollBar().isAtBottom();
		messagesList.addEntry(messageContainer, 5 * Scale.factor);
		Component padding = new Component(inputs, Scale.factor, 4 * Scale.factor);
		messagesList.addEntry(padding);
		if (atBottom) {
			messagesList.getScrollBar().moveToBottom();
		}
	}

	/**
	 * Transform to the in-game chat look-and-feel.
	 */
	public void transformToGameChat() {
		inGame = true;
		messagesList.getScrollBar().setVisible(false);
		messagesList.setBackground(Colors.TRANSPARENT_CHAT);
		messagesList.getScrollBar().moveToBottom();
	}

	/**
	 * Transform to the in-lobby menu chat look-and-feel.
	 */
	public void transformToLobbyChat() {
		inGame = false;
		messagesList.getScrollBar().setVisible(true);
		messagesList.setBackground(Colors.UI_BG);
		messagesList.getScrollBar().moveToBottom();
	}

	/**
	 * @return True if the input field is focussed.
	 */
	public boolean isFocussed() {
		return inputField.isFocussed();
	}

}