package escort.client.res;

import java.awt.Color;
import java.awt.image.BufferedImage;

import escort.client.ui.utils.Colors;

/**
 * Contains sprites of all units. A resource loader must be created and loaded
 * before using any of the sprites in this class.
 * 
 * @author Ahmed Bhallo
 *
 */
public class UnitSprites {

	/**
	 * The width in px of a sprite.
	 */
	public static final int SPRITE_WIDTH = 16;

	/**
	 * The height in px of a sprite's height.
	 */
	public static final int HEAD_HEIGHT = 12;

	public static final int TORSO_WIDTH = 8;

	/**
	 * The height in px of a sprite's torso.
	 */
	public static final int TORSO_HEIGHT = 7;

	/**
	 * The height in px of a sprite's legs.
	 */
	public static final int LEG_WIDTH = 8;

	public static final int LEG_HEIGHT = 3;

	public static final Color PRESIDENT_LEGS_COLOR = Colors.PRESIDENTIAL_RED;
	public static final Color ESCORT_LEGS_COLOR = Colors.DARK_GRAY;
	public static final Color ASSASSIN_LEGS_COLOR = Colors.ORANGE;
	public static final Color MENACE_LEGS_COLOR = Color.yellow;
	public static final Color CIVILIAN_LEGS_COLOR = Colors.LIGHT_BLUE;
	public static final Color POLICE_LEGS_COLOR = Color.GREEN;

	public static final BufferedImage[] PRESIDENT_HEAD = new BufferedImage[8];
	public static final BufferedImage[] PRESIDENT_TORSO = new BufferedImage[8];
	public static final BufferedImage[] PRESIDENT_LEGS = new BufferedImage[3];
	
	public static final BufferedImage[] ESCORT_HEAD = new BufferedImage[8];
	public static final BufferedImage[] ESCORT_TORSO = new BufferedImage[8];
	public static final BufferedImage[] ESCORT_LEGS = new BufferedImage[3];
	
	public static final BufferedImage[] ASSASSIN_HEAD = new BufferedImage[8];
	public static final BufferedImage[] ASSASSIN_TORSO = new BufferedImage[8];
	public static final BufferedImage[] ASSASSIN_LEGS = new BufferedImage[3];
	
	public static final BufferedImage[] MENACE_HEAD = new BufferedImage[8];
	public static final BufferedImage[] MENACE_TORSO = new BufferedImage[8];
	public static final BufferedImage[] MENACE_LEGS = new BufferedImage[3];

	public static final BufferedImage[] CIVILIAN_HEAD = new BufferedImage[8];
	public static final BufferedImage[] CIVILIAN_TORSO = new BufferedImage[8];
	public static final BufferedImage[] CIVILIAN_LEGS = new BufferedImage[3];

	public static final BufferedImage[] POLICE_HEAD = new BufferedImage[8];
	public static final BufferedImage[] POLICE_TORSO = new BufferedImage[8];
	public static final BufferedImage[] POLICE_LEGS = new BufferedImage[3];

}
