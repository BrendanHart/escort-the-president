package escort.client.ui.menus.settings;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import escort.client.input.InputHandler;
import escort.client.input.Inputs;
import escort.client.main.Client;
import escort.client.main.Scale;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.text.TextLabel;
import escort.client.ui.utils.Colors;

/**
 * A panel that listens to input presses and sets the binding accordingly.
 * 
 * @author Ahmed Bhallo
 *
 */
public class BindingDetector extends Panel implements KeyListener, MouseListener, MouseWheelListener {

	private final Client client;
	private final TextLabel label;
	private int keyCode = KeyEvent.VK_UNDEFINED;

	private boolean detecting = false;

	/**
	 * Instantiates a new binding detector object
	 * 
	 * @param inputs
	 *            The inputs object
	 * @param settings
	 *            The settings panel
	 * @param width
	 *            The width of the detector
	 */
	public BindingDetector(Inputs inputs, SettingsPanel settings, int width) {
		super(inputs, width, 0);
		label = new TextLabel("", inputs, Colors.LIGHT_BLACK);
		label.setPadding(0, 0);
		setHeight(label.getHeight() + 10 * Scale.factor);
		label.setShadow(false);
		this.client = settings.getClient();
		setBorder(Colors.LIGHT_GRAY);
		setBackground(Colors.DARK_WHITE);
		addListener(e -> detectBindingUpdate());
	}

	@Override
	public void update() {
		super.update();
		if (!detecting) {
			return;
		}

		// Consume scrolls so the scrollable list doesn't scroll.
		inputs.scrollUp.setPressed(false);
		inputs.scrollDown.setPressed(false);

		if (inputs.leftClick.isPressed() && !isHovered()) {
			save();
		}
	}

	/**
	 * Detets binding when it is pressed
	 */
	private void detectBindingUpdate() {
		detecting = true;
		client.addKeyListener(this);
		client.addMouseListener(this);
		client.addMouseWheelListener(this);
		setBorder(Colors.LIGHT_BLUE);
		setBackground(Colors.WHITE);
	}

	/**
	 * Saves the current binding
	 */
	private void save() {
		detecting = false;
		client.removeKeyListener(this);
		client.removeMouseListener(this);
		client.removeMouseWheelListener(this);
		setBorder(Colors.LIGHT_GRAY);
		setBackground(Colors.DARK_WHITE);
	}

	/**
	 * Updates the text on the label with given keyCode.
	 * 
	 * @param keyCode
	 *            The new keycode
	 */
	public void updateBinding(int keyCode) {
		this.keyCode = keyCode;
		label.setText(InputHandler.getKeyString(keyCode), true);
		add(label, center(label));
	}

	/**
	 * @return the keycode of the binding detector
	 */
	public int getKeyEventCode() {
		return keyCode;
	}

	/**
	 * A key is pressed. Updates the binding with the keycode
	 */
	@Override
	public void keyPressed(KeyEvent e) {
		if (!detecting) {
			return;
		}
		updateBinding(e.getKeyCode());
	}

	/**
	 * The mouse is pressed, updates the binding with the mouse event
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (!detecting) {
			return;
		}
		int keyCode;
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			if (!isHovered()) {
				return;
			}
			keyCode = InputHandler.LEFT_MOUSE_KEY;
			break;
		case MouseEvent.BUTTON2:
			keyCode = InputHandler.MIDDLE_MOUSE_KEY;
			break;
		case MouseEvent.BUTTON3:
			keyCode = InputHandler.RIGHT_MOUSE_KEY;
			break;
		default:
			return;
		}
		updateBinding(keyCode);
	}

	/**
	 * Scroll wheel movement detected. Updates the binding.
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (!detecting) {
			return;
		}
		int keyCode;
		if (e.getWheelRotation() > 0) {
			keyCode = InputHandler.SCROLL_DOWN_KEY;
		} else if (e.getWheelRotation() < 0) {
			keyCode = InputHandler.SCROLL_UP_KEY;
		} else {
			return;
		}
		updateBinding(keyCode);
	}

	// UNUSED //

	@Override
	public void keyReleased(KeyEvent arg0) {
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}

}
