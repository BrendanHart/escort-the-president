package escort.client.ui.components.text;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.ui.components.panels.Panel;

/**
 * Utility methods to work on strings and text labels.
 * 
 * @author Ahmed Bhallo
 *
 */
public class TextUtils {

	/**
	 * Not instantiate-able.
	 */
	private TextUtils() {
	}

	/**
	 * Inputtable punctuation on input fields.
	 */
	private static final char[] PUNCTUATION = { ' ', '.', ',', ';', ':', '?', '!', '"', '/', '\\', '\'', ')', '(', '[',
			']', '{', '}', '<', '>', '^', '~', '&', '#', '_', '=', '*', '+', '-', '$', '%', '@', '|' };

	/**
	 * Calculated the dimension of a string with given font.
	 * 
	 * @param text
	 *            The string
	 * @param font
	 *            The font
	 * @return The dimensions of the string with the font.
	 */
	public static Dimension getTextDimension(String text, Font font) {
		FontRenderContext frc = new FontRenderContext(new AffineTransform(), false, false);
		Rectangle2D strBounds = font.getStringBounds(text, frc);
		return new Dimension((int) Math.ceil(strBounds.getWidth()),
				(int) Math.ceil(strBounds.getHeight()) + 2 * Scale.factor);
	}

	/**
	 * Returns true iff the character is input0able on the inputfield.
	 * 
	 * @param c
	 *            The character to be tested
	 * @return true iff the character is input0able on the inputfield.
	 */
	public static boolean isInputtableChar(char c) {
		if (Character.isLetterOrDigit(c)) {
			return true;
		} else {
			for (char c1 : PUNCTUATION) {
				if (c == c1) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Wraps the text
	 * 
	 * @param inputs
	 *            The inputs object
	 * @param text
	 *            The text to be wrapped
	 * @param colPerLine
	 *            The columns per line
	 * @param color
	 *            The color
	 * @return A panel containing the wrapped text.
	 */
	public static Panel wrappedTextLabel(Inputs inputs, String text, int colPerLine, Color color) {
		return wrappedTextLabel(inputs, text, colPerLine, color, true);
	}

	/**
	 * Wraps the text
	 * 
	 * @param inputs
	 *            The inputs object
	 * @param text
	 *            The text to be wrapped
	 * @param colPerLine
	 *            The columns per line
	 * @param color
	 *            The color
	 * @param renderShadow
	 *            whether or not the shadow should be rendered.
	 * @return A panel containing the wrapped text.
	 */
	public static Panel wrappedTextLabel(Inputs inputs, String text, int colPerLine, Color color,
			boolean renderShaddow) {
		Panel result = new Panel(inputs, 0, 2 * Scale.factor);
		while (!text.isEmpty()) {
			int col = Math.min(colPerLine, text.length());
			int lastSpace = text.substring(0, col).lastIndexOf(' ') + 1;
			if (lastSpace > 0 && text.length() > col) {
				col = lastSpace;
			}
			String line = text.substring(0, col);
			text = text.substring(col);
			TextLabel label = new TextLabel(line, inputs, color);
			label.setPadding(2 * Scale.factor, 0);
			label.setShadow(renderShaddow);
			result.add(label, 0, result.getHeight());
			result.pack();
		}
		return result;
	}
}
