package escort.client.ui.components.text;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.ui.components.Component;
import escort.client.ui.utils.Colors;
import escort.client.ui.utils.Fonts;

/**
 * A text label that displays text on a component.
 * 
 * @author Ahmed Bhallo
 *
 */
public class TextLabel extends Component {

	/**
	 * The text on the label.
	 */
	private String text;

	/**
	 * The font of the label.
	 */
	private Font font = Fonts.BODY;

	/**
	 * 
	 */
	private int vPadding = 4 * Scale.factor;
	private int hPadding = 4 * Scale.factor;

	private boolean centered = true;

	private boolean renderShadow = true;

	public TextLabel(String text, Inputs inputs) {
		this(text, inputs, Colors.LIGHT_GRAY);
	}

	/**
	 * Instantiates a new text label
	 * 
	 * @param text
	 *            The text
	 * @param inputs
	 *            The inputs object
	 * @param color
	 *            The color to render
	 */
	public TextLabel(String text, Inputs inputs, Color color) {
		super(inputs, 0, 0);
		this.text = text;
		setFont(getFont());
		setForeground(color);
	}

	/**
	 * Renders the text and shadow if it has any.
	 */
	@Override
	public void render(Graphics2D g) {
		super.render(g);
		g.setFont(getFont());
		FontMetrics metrics = g.getFontMetrics();
		String text = getText();
		int x = centered ? ((getWidth() - metrics.stringWidth(text)) / 2) : 1;
		int y = ((getHeight() - metrics.getHeight()) / 2) + metrics.getAscent() + 1;
		if (renderShadow) {
			g.setColor(Colors.setAlpha(Colors.LIGHT_BLACK, 150));
			g.drawString(text, x + 2, y + 2);
			g.setColor(Colors.setAlpha(Colors.BLACK, 200));
			g.drawString(text, x + 1, y + 1);
		}
		g.setColor(getForeground());
		g.drawString(text, x, y);
	}

	/**
	 * Updates the dimensions on this component.
	 */
	private void updateDimension() {
		Dimension d = TextUtils.getTextDimension(text, font);
		setWidth(d.width + hPadding * 2);
		setHeight(d.height + vPadding * 2);
	}

	/**
	 * Sets the text on this component
	 * 
	 * @param text
	 *            The new text
	 * @param updateWidth
	 *            Whether or not the dimensions should be updated
	 */
	public void setText(String text, boolean updateWidth) {
		this.text = text;
		if (updateWidth) {
			updateDimension();
		}
	}

	/**
	 * @return The text on this component
	 */
	public String getText() {
		return text;
	}

	/**
	 * Sets the font on this component
	 * 
	 * @param font
	 *            The new font
	 */
	public void setFont(Font font) {
		this.font = font;
		updateDimension();
	}

	/**
	 * @return The font
	 */
	public Font getFont() {
		return font;
	}

	/**
	 * Sets the vertical and horizontal padding on this component. Updates the
	 * dimensions
	 * 
	 * @param hPadding
	 *            The new horizontal padding
	 * @param vPadding
	 *            The new vertical padding
	 */
	public void setPadding(int hPadding, int vPadding) {
		this.hPadding = hPadding;
		this.vPadding = vPadding;
		updateDimension();
	}

	/**
	 * Sets whether or not this text label should have centered text.
	 * 
	 * @param centered
	 */
	public void setCentered(boolean centered) {
		this.centered = centered;
	}

	/**
	 * @return Whether or not this label has centered text.
	 */
	public boolean isCentered() {
		return centered;
	}

	/**
	 * Sets whether or not the shadow of the text should be rendered.
	 * 
	 * @param renderShadow
	 *            Whether or not the shadow should be rendereed.
	 */
	public void setShadow(boolean renderShadow) {
		this.renderShadow = renderShadow;
	}
}
