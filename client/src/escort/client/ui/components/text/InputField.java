package escort.client.ui.components.text;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.util.Map.Entry;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.ui.components.Component;
import escort.client.ui.utils.Colors;
import escort.client.ui.utils.Fonts;
import escort.common.systime.SystemTime;

/**
 * A component used to detect key presses from the client.
 * 
 * @author Ahmed Bhallo
 *
 */
public class InputField extends Component {

	/**
	 * The padding above and below the text.
	 */
	private final int vPadding = 5 * Scale.factor;

	/**
	 * The padding to the left and right of the text.
	 */
	private final int hPadding = 6 * Scale.factor;

	/**
	 * The font used to render the text.
	 */
	private Font font = Fonts.BODY;

	/**
	 * The text inputed.
	 */
	private StringBuilder text = new StringBuilder("");

	/**
	 * The number of columns. This is used to initialise the default width.
	 */
	private final int col;

	/**
	 * The position of the caret. A caret is the thin vertical bar which lets
	 * the user know where they are currently typing.
	 */
	private int caret = 0;

	/**
	 * The offset of the text relative to the starting point. An offset > 0
	 * means that text will be rendered to the left.
	 */
	private int offset = 0;

	/**
	 * The maximum number of characters that can be input by the user.
	 */
	private int limit = -1;

	/**
	 * Whether or not the caret is currently blinking.
	 */
	private boolean caretBlink = true;

	/**
	 * The last time the caret blinking has been stoped.
	 */
	private long lastFlashReset;

	/**
	 * Whether or not this component is currently focussed by the user. If it is
	 * focussed,
	 */
	private boolean focussed;

	/**
	 * If the user presses tab (traverses focus), this is the next field to be
	 * focussed.
	 */
	private InputField nextField;

	/**
	 * The time in milliseconds of the blinking of a caret.
	 */
	private static final int CARET_BLINK = 800;

	/**
	 * The text to display when this field is empty and unfocussed.
	 */
	private String promptText = "";

	/**
	 * Instantiates a new input field.
	 * 
	 * @param inputs
	 *            The inputs object.
	 * @param col
	 *            The default number of columns.
	 */
	public InputField(Inputs inputs, int col) {
		super(inputs, 0, 0);
		this.col = col;
		setForeground(Colors.LIGHT_BLACK);
		setBackground(Colors.DARK_WHITE);
		updateDimensions();
	}

	/**
	 * Updates the dimensions of this field. Call this method when any changes
	 * to the font or number of displayable columns have been made.
	 */
	private void updateDimensions() {
		String str = "";
		for (int i = 0; i < col; i++) {
			str += "w";
		}
		Dimension d = TextUtils.getTextDimension(str, font);
		setWidth(d.width + hPadding * 2);
		setHeight(d.height + vPadding * 2);
	}

	/**
	 * Detects whether or not this component is focussed or onfocussed. If it is
	 * focussed, calls detectKeyPresses() method. Detects new position of the
	 * caret and offset.
	 */
	@Override
	public void update() {
		super.update();

		if (isDown()) {
			// Focus this component if this component is down.
			focussed = true;
		} else if (inputs.leftClick.isPressed()) {
			// If the left mouse button is pressed, but we are not down,
			// unfocus.
			focussed = false;
		}

		if (focussed) {
			// Detect key presses and update colours based on focus state.
			detectKeyPresses();
			setBorder(Colors.LIGHT_BLUE);
			setBackground(Colors.WHITE);
		} else {
			setBorder(Color.LIGHT_GRAY);
			setBackground(Colors.DARK_WHITE);
		}

		if (inputs.leftClick.isPressed() && isHovered()) {
			// If the mouse is dagging over this component, detect new caret
			// positioning.
			stopCaretBlink();
			detectMousePress();
		} else {
			resetCaretFlash();
		}
	}

	/**
	 * Detects key presses by the user, such as letters typed, deleteted, or
	 * caret position changes from the left and right keys.
	 */
	private void detectKeyPresses() {
		if (inputs.leftArrow.isPressed()) {
			inputs.leftArrow.setPressed(false);

			// If the left arrow key is pressed, consume it and move the caret
			// accordingly.
			caret--;

			if (offset > 0) {
				// If the offset was greater than 0, decrement it by 1.
				offset--;
			}

			// The caret may not be less than 0.
			caret = Math.max(caret, 0);

			// Stop the caret from blinking because input was detected.
			stopCaretBlink();
		}

		if (inputs.rightArrow.isPressed()) {
			inputs.rightArrow.setPressed(false);

			// If the right arrow key is pressed, consume it and move the caret
			// accordingly.
			caret++;

			// The caret position may not be greater than the length of the
			// text.
			caret = Math.min(caret, text.length());

			// Stop the caret from blinking because input was detected.
			stopCaretBlink();
		}

		// Traverse through all typed keys.
		for (Entry<Character, Boolean> entry : inputs.typedInput.entrySet()) {
			if (!entry.getValue()) {
				// If the key was not pressed, skip it.
				continue;
			}
			Character c = entry.getKey();
			if (c == KeyEvent.VK_DELETE) {
				// The delete key has been pressed.
				if (caret < text.length()) {
					// If the caret is not at the end of the text, delete the
					// next character and decrement the offset.
					text.deleteCharAt(caret);
					offset--;
				}
			} else if (c.equals('\b')) {
				// The backspace key has been pressed.
				if (caret > 0) {
					// If the caret is not at the beginning of the text,
					// deletect the previous character and decrement the caret
					// position and offset amounts by 1.
					text.deleteCharAt(caret - 1);
					caret--;
					offset--;
				}
			} else if (c == KeyEvent.VK_ENTER) {
				// The enter key was pressed. Notify all listeners of this
				// component.
				notifyListeners();
			} else if (c == KeyEvent.VK_ESCAPE) {
				// The escape key was pressed, no longer focus this component.
				setFocussed(false);
			} else if (c.equals('\t')) {
				// The tab key was pressed, traverse the focus of this
				// component.
				traverseFocus();
			} else if (c == 22) {
				paste();
			} else if (TextUtils.isInputtableChar(c)) {
				// An inputtable character was pressed.
				if (limit != -1 && text.length() > col) {
					// If we have a limit, ensure that a character doesn't
					// exceed the limit.
					continue;
				}

				// Insert the character at the index of the caret.
				text.insert(caret, c);

				// Increment the caret position by 1.
				caret++;
			}

			// Stop the caret from blinking because input was detected.
			stopCaretBlink();

			// Consume the key press.
			inputs.typedInput.replace(c, false);
		}

		// If the caret is above the offset and column of this component,
		// increment the offset by 1. This will allow new text to be visible.
		if (caret > col + offset) {
			offset++;
		}

		// Reset the offset to 0 if the number of characters is smaller than the
		// number of displayable characters, or if the offset is less than 0.
		if (text.length() <= col || offset < 0) {
			offset = 0;
		}
	}

	private void paste() {
		Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
		if (transferable == null) {
			return;
		}
		String pasted = "";
		try {
			pasted = (String) transferable.getTransferData(DataFlavor.stringFlavor);
		} catch (Exception e) {
			return;
		}
		text.insert(caret, pasted);
		caret += pasted.length();
	}

	/**
	 * Detects new position of the caret based on inputs of the mouse.
	 */
	private void detectMousePress() {
		int clickX = inputs.mouseX - getGlobalPoint().x;
		caret = Math.round((float) clickX / TextUtils.getTextDimension(" ", font).width) + offset - 1;
		caret = Math.max(caret, 0);
		caret = Math.min(caret, text.length());
	}

	/**
	 * Renders the text on this component.
	 */
	@Override
	public void render(Graphics2D g) {
		super.render(g);

		// Set the font of the graphics object and obtain the font metrics.
		g.setFont(font);
		FontMetrics metrics = g.getFontMetrics();
		String text = getText();

		// Calculate the positioning of the rendered text.
		int x = hPadding - offset * TextUtils.getTextDimension(" ", font).width;
		int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent() + 1;

		// Clip the graphics object to ensure text is not rendered outside the
		// non displayable border. We must first store the current clip so we
		// can revert it after we are done.
		Shape oldClip = g.getClip();
		g.clip(new Rectangle(hPadding, 1, getWidth() - hPadding * 2, getHeight() - 2));

		if (!text.isEmpty()) {
			// If there is text to render, render it.
			g.setColor(getForeground());
			g.drawString(text, x, y);
		} else if (!promptText.isEmpty() && !focussed) {
			// If there is no text to render, and we are not focussed, render
			// the prompt text.
			g.setColor(Colors.VERY_DARK_GRAY);
			g.drawString(promptText, x, y);
		}

		// Revert the old clip.
		g.setClip(oldClip);

		// | If focussed and caretBlink = false, caret will be rendered always.
		// | If focussed and caretBlink = true, render the caret only if it is a
		// valid time to.
		// | Otherwise, caret it not rendered.
		if (focussed && (!caretBlink || (SystemTime.milliTime() / (CARET_BLINK / 2)) % 2 == 0)) {
			// If we are focussed, render the caret if it is renderable.
			renderCaret(g);
		}
	}

	/**
	 * Renders the caret at its position. This method does not check if the
	 * caret is blinking.
	 * 
	 * @param g
	 *            The graphics object.
	 */
	private void renderCaret(Graphics2D g) {
		// Calculate the x position of the caret.
		int xPos = (TextUtils.getTextDimension(text.toString().substring(0, caret), font).width / (6 * Scale.factor)
				* (6 * Scale.factor));
		xPos += hPadding - offset * TextUtils.getTextDimension(" ", font).width - 2;

		// The starting y position of the caret is the padding.
		int yPos = vPadding;

		// Set the colour of the graphics object and fill a rectangle.
		g.setColor(Colors.BLACK);
		g.fillRect(xPos, yPos, 2, getHeight() - yPos * 2);
	}

	/**
	 * Called to stop the caret from blinking. Updates the flash reset to the
	 * current time.
	 */
	private void stopCaretBlink() {
		caretBlink = false;
		lastFlashReset = SystemTime.milliTime();
	}

	/**
	 * Checks if the caret blinking should be reset to true if it is not true.
	 */
	private void resetCaretFlash() {
		if (caretBlink) {
			return;
		}
		long timeSinceReset = SystemTime.milliTime() - lastFlashReset;
		if (timeSinceReset >= CARET_BLINK) {
			caretBlink = true;
		}
	}

	/**
	 * Gets the text inputed by the user on this component.
	 * 
	 * @return The inputed text of this component.
	 */
	public String getText() {
		return text.toString();
	}

	/**
	 * Sets the text of this component. Updates the caret positioning
	 * accordingly.
	 * 
	 * @param string
	 *            The new text to be added.
	 */
	public void setText(String string) {
		this.text = new StringBuilder(string);
		if (caret > text.length()) {
			caret = text.length();
		}
	}

	/**
	 * Sets whether or not this input field is focussed by the user. A focussed
	 * input field means key presses are detected.
	 * 
	 * @param focussed
	 *            Should this input field be focussed?
	 */
	public void setFocussed(boolean focussed) {
		this.focussed = focussed;
	}

	/**
	 * Gets whether or not this input field is focussed by the user. A focussed
	 * input field means key presses are detected.
	 * 
	 * @return True iff this component is focussed.
	 */
	public boolean isFocussed() {
		return focussed;
	}

	/**
	 * Traverses focus to the next input field. Typically, this method is called
	 * when the tab key is pressed.
	 */
	public void traverseFocus() {
		if (!focussed || nextField == null) {
			// Return if we are not focussed, or if no next field is specified.
			return;
		}
		// Sets this component to be unfocussed.
		setFocussed(false);

		// Set the next component to be focussed.
		nextField.setFocussed(true);

		// Update the caret position of the next component to the end of the
		// component.
		nextField.setCaretPosition(nextField.getText().length());
	}

	/**
	 * Sets the next input field to be traversed when this one is travered
	 * 
	 * @param nextField
	 *            The next input field to be traversed.
	 */
	public void setNextTraversable(InputField nextField) {
		this.nextField = nextField;
	}

	/**
	 * 
	 * @param pos
	 */
	public void setCaretPosition(int pos) {
		this.caret = pos;
	}

	@Override
	public void onClick() {
		// Do nothing. Do not notify listeners when this component has been
		// clicked.
	}

	/**
	 * Sets the prompt text of this input field. The prompt text is displayed
	 * when the input field has no text on it and is not focussed.
	 * 
	 * @param promptText
	 *            The new prompt text of this input field.
	 */
	public void setPromptText(String promptText) {
		this.promptText = promptText;
	}

	/**
	 * Gets the prompt text of this input field. The prompt text is displayed
	 * when the input field has no text on it and is not focussed.
	 * 
	 * @return The prompt text of this input field.
	 */
	public String getPromptText() {
		return promptText;
	}
}
