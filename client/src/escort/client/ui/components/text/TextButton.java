package escort.client.ui.components.text;

import java.awt.Color;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.ui.utils.Colors;

/**
 * A component for a button with text on it.
 * 
 * @author Ahmed Bhallo
 *
 */
public class TextButton extends TextLabel {

	/**
	 * The background colours at each state of this button.
	 */
	private Color bgDefault = null, bgHover = Colors.PALE_BLUE, bgDown = Colors.LIGHT_BLUE,
			bgDisabled = Colors.DARK_GRAY;

	/**
	 * The border colours at each state of this button.
	 */
	private Color borderDefault = Colors.BLUE, borderHover = Colors.LIGHT_BLUE, borderDown = Colors.LIGHT_BLUE,
			borderDisabled = Colors.DARK_GRAY;

	/**
	 * Instantiates a new text button.
	 * 
	 * @param text
	 *            The text on the button.
	 * @param inputs
	 *            The inputs object.
	 */
	public TextButton(String text, Inputs inputs) {
		super(text, inputs, Colors.LIGHT_GRAY);
		setPadding(10 * Scale.factor, 6 * Scale.factor);
	}

	/**
	 * Updates the colours of the button.
	 */
	@Override
	public void update() {
		super.update();
		if (!isEnabled()) {
			setBorder(borderDisabled);
			setBackground(bgDisabled);
		} else if (isDown()) {
			setBorder(borderDown);
			setBackground(bgDown);
		} else if (isHovered()) {
			setBorder(borderHover);
			setBackground(bgHover);
		} else {
			setBorder(borderDefault);
			setBackground(bgDefault);
		}
	}

	/**
	 * Detects input iff this text button is enabled.
	 */
	@Override
	protected void detectInput() {
		if (isEnabled()) {
			super.detectInput();
		} else {
			// Set hover and down to false if this button has been disabled.
			hover = false;
			down = false;
		}
	}

	// GETTERS AND SETTERS //

	public Color getBgDefault() {
		return bgDefault;
	}

	public void setBgDefault(Color bgDefault) {
		this.bgDefault = bgDefault;
	}

	public Color getBgHover() {
		return bgHover;
	}

	public void setBgHover(Color bgHover) {
		this.bgHover = bgHover;
	}

	public Color getBgDown() {
		return bgDown;
	}

	public void setBgDown(Color bgDown) {
		this.bgDown = bgDown;
	}

	public Color getBgDisabled() {
		return bgDisabled;
	}

	public void setBgDisabled(Color bgDisabled) {
		this.bgDisabled = bgDisabled;
	}

	public Color getBorderDefault() {
		return borderDefault;
	}

	public void setBorderDefault(Color borderDefault) {
		this.borderDefault = borderDefault;
	}

	public Color getBorderHover() {
		return borderHover;
	}

	public void setBorderHover(Color borderHover) {
		this.borderHover = borderHover;
	}

	public Color getBorderDown() {
		return borderDown;
	}

	public void setBorderDown(Color borderDown) {
		this.borderDown = borderDown;
	}

	public Color getBorderDisabled() {
		return borderDisabled;
	}

	public void setBorderDisabled(Color borderDisabled) {
		this.borderDisabled = borderDisabled;
	}
}
