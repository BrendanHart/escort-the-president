package escort.client.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Helper static methods to for rendering UI Components and graphics.
 * 
 * @author Ahmed Bhallo
 *
 */
public class RenderUtils {

	/**
	 * Renders a rectangle with specified thickness. Thickness should be
	 * multiplied by Scale.factor
	 * 
	 * @param g
	 *            The graphics object
	 * @param color
	 *            The color of the rectangle
	 * @param x
	 *            Top left x coordinate
	 * @param y
	 *            Top left y coordinate
	 * @param width
	 *            Width of rectangle
	 * @param height
	 *            Height of rectangle
	 * @param thickness
	 *            Thickness of rectangle
	 */
	public static void renderRectBorder(Graphics2D g, Color color, int x, int y, int width, int height, int thickness) {
		g.setColor(color);
		for (int i = 0; i < thickness; i++) {
			g.drawRect(x + i, y + i, width - i * 2, height - i * 2);
		}
	}

	/**
	 * Overlays an image with a color and its alpha value
	 * 
	 * @param src
	 *            The image to be overlayed
	 * @param color
	 *            The color
	 * @param alphaValue
	 *            The alpha value of the color
	 * @return The overlayed image.
	 */
	public static BufferedImage overlayImage(BufferedImage src, Color color, float alphaValue) {
		BufferedImage result = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = result.createGraphics();
		g.drawImage(src, 0, 0, null);
		g.setColor(color);
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alphaValue));
		g.fillRect(0, 0, src.getWidth(), src.getHeight());
		return result;
	}

}
