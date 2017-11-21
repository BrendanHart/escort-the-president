package escort.client.ui.components;

import java.awt.Graphics2D;
import java.awt.Rectangle;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.utils.Colors;

/**
 * A slider component where a user can drag the thumb across a bar.
 * 
 * @author Ahmed Bhallo
 *
 */
public class Slider extends Panel {

	/**
	 * The scroll thumb component.
	 */
	private DraggableComponent thumb;

	/**
	 * The rectangle of the bar.
	 */
	private Rectangle bar;

	/**
	 * Instantiates a new slider.
	 * 
	 * @param inputs
	 *            The inputs object,
	 * @param width
	 *            The width of the slider.
	 */
	public Slider(Inputs inputs, int width) {
		super(inputs, width, 0);
		final int barHeight = 5 * Scale.factor;
		final int THUMB_DIAMETER = 10 * Scale.factor;
		setHeight(THUMB_DIAMETER);

		bar = new Rectangle(0, (THUMB_DIAMETER - barHeight) / 2, width, barHeight);
		thumb = new DraggableComponent(inputs, THUMB_DIAMETER, THUMB_DIAMETER,
				new Rectangle(0, 0, getWidth(), getHeight()));
		thumb.setBackground(Colors.PALE_BLUE);
		add(thumb, 0, 0);
	}

	/**
	 * Renders the rectangle.
	 */
	@Override
	public void render(Graphics2D g) {
		g.setColor(Colors.DARK_GRAY);
		g.fill(bar);
		super.render(g);
	}

	/**
	 * Gets the value of the slider position. The result returned is in range
	 * from 0.0000 to 1.0000 (inclusive).
	 * 
	 * @return The value of the slider amount.
	 */
	public int getValue() {
		int thumbX = children.get(thumb).x;
		int trackWidth = bar.width - thumb.getWidth();
		return (int) Math.floor(thumbX / (double) (trackWidth) * 100);
	}

	/**
	 * Sets the value of the slider
	 * @param value The new value
	 */
	public void setValue(int value) {
		int trackWidth = bar.width - thumb.getWidth();
		int thumbX = (int) (trackWidth * (double) (value) / 100);
		add(thumb, (int) Math.floor(thumbX), 0);
	}
}
