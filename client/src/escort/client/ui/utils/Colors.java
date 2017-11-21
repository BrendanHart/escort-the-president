package escort.client.ui.utils;

import java.awt.Color;

/**
 * A class containing color constants to be used as a color palette.
 * 
 * @author Ahmed Bhallo
 *
 */
public final class Colors {

	/**
	 * Not instantiate-able
	 */
	private Colors() {
	}

	public static final Color WHITE = Color.decode("#FFFFFF");
	public static final Color DARK_WHITE = Color.decode("#f0f0f0");
	public static final Color VERY_LIGHT_GRAY = Color.decode("#eeeeee");
	public static final Color LIGHT_GRAY = Color.decode("#cccccc");
	public static final Color GRAY = Color.decode("#aaaaaa");
	public static final Color DARK_GRAY = Color.decode("#888888");
	public static final Color VERY_DARK_GRAY = Color.decode("#2a2a2a");
	public static final Color LIGHT_BLACK = Color.decode("#1a1a1a");
	public static final Color BLACK = Color.decode("#000000");

	public static final Color PRESIDENTIAL_RED = Color.decode("#e92f35");
	public static final Color ORANGE = Color.decode("#db9915");
	public static final Color GREEN = Color.decode("#157f38");

	public static final Color WEAPON_PANEL = Color.decode("#213059");
	public static final Color BIG_STONE = Color.decode("#121d3f");
	public static final Color LIGHT_BLUE = Color.decode("#338fd0");
	public static final Color BLUE = Color.decode("#0c4d9b");
	public static final Color PALE_BLUE = Color.decode("#225a83");
	public static final Color DARK_BLUE = Color.decode("#14364e");
	public static final Color MENU_BG_A = Color.decode("#374962");
	public static final Color MENU_BG_B = Color.decode("#253141");
	public static final Color HUD_BG = BIG_STONE;
	public static final Color UI_BG = BIG_STONE;
	public static final Color SHIELD_OUTLINE = DARK_WHITE;
	public static final Color BAR_OUTLINE = LIGHT_GRAY;
	public static final Color TRANSPARENT_CHAT = setAlpha(VERY_DARK_GRAY, 100);
	public static final Color MG_BULLET_COLOR = Colors.PRESIDENTIAL_RED;
	public static final Color PISTOL_BULLET_COLOR = Colors.BLUE;

	/**
	 * Sets the alpha value of a color
	 * 
	 * @param color
	 *            The color to be changed
	 * @param alpha
	 *            The alpha value
	 * @return The resulting color object
	 */
	public static Color setAlpha(Color color, int alpha) {
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		return new Color(red, green, blue, alpha);
	}

}