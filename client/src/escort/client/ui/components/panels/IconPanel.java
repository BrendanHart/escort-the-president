package escort.client.ui.components.panels;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import escort.client.input.Inputs;

/**
 * A component that contains an icon image
 * 
 * @author Ahmed Bhallo
 *
 */
public class IconPanel extends Panel {

	/**
	 * The image
	 */
	private final BufferedImage image;

	/**
	 * Instantiate a new icon panel
	 * 
	 * @param inputs
	 *            The inputs object
	 * @param image
	 *            The image of the icon
	 * @param scale
	 *            The scale of the image
	 */
	public IconPanel(Inputs inputs, BufferedImage image, int scale) {
		super(inputs, image.getWidth() * scale, image.getHeight() * scale);
		this.image = image;
	}

	/**
	 * Renders the image.
	 */
	@Override
	public void render(Graphics2D g) {
		super.render(g);
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
	}
}
