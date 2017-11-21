package escort.common.game.map;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * A tile class for tiles in the map. Contains information on walkable, and if
 * used on the client, contains images.
 * 
 * @author Ahmed Bhallo
 *
 */
public class Tile {

	public static final int TILE_WIDTH = 32;
	public static final int TILE_HEIGHT = 24;
	public static final int TILE_DEPTH = 18;

	public static final Map<Integer, Tile> ALL_TILES = new HashMap<>();

	/**
	 * Integer ID codes for all tiles.
	 */
	public static final int STONE_WALL = 0;
	public static final int GRASS = 1;
	public static final int WOODEN_FLOOR = 2;
	public static final int BLUE_CARPET = 3;
	public static final int RED_CARPET = 4;
	public static final int PINK_CARPET = 5;
	public static final int CHECKERED_FLOOR = 6;
	public static final int TARMAC = 7;
	public static final int WHITE_FLOOR = 8;
	public static final int MUD = 9;
	public static final int CHAIR = 10;
	public static final int PAVEMENT = 11;
	public static final int BED = 12;
	public static final int BRICK_WALL = 13;
	public static final int DINING_TABLE = 14;
	public static final int BED_PILLOW = 15;
	public static final int ESCORT_SPAWN = 16;
	public static final int ASSASSIN_SPAWN = 17;
	public static final int PRESIDENT_SPAWN = 18;
	public static final int END_ZONE = 19;
	public static final int HP_POWER_UP = 20;
	public static final int AMMO_POWER_UP = 21;
	public static final int GRENADE_POWER_UP = 22;
	public static final int STONE_BRICK = 23;

	/**
	 * Codes for image types
	 */
	public static final int TOP_IMAGE = 0;
	public static final int FULL_SHADOW = 1;
	public static final int TOP_RIGHT_SHADOW = 2;
	public static final int BOTTOM_LEFT_SHADOW = 3;
	public static final int BOTTOM_IMAGE = 4;

	public final int tileID;

	private final Map<Integer, BufferedImage> images = new HashMap<>();

	public final int colorCode;

	private boolean walkable;

	/**
	 * Instantiates a new tile object
	 * 
	 * @param tileID
	 * @param walkable
	 * @param colorCode
	 */
	public Tile(int tileID, boolean walkable, int colorCode) {
		this.tileID = tileID;
		this.walkable = walkable;
		this.colorCode = colorCode;
	}

	/**
	 * Returns whether this tile is walkable by units
	 * @return True iff this tile is walkable
	 */
	public boolean isWalkable() {
		return walkable;
	}

	/**
	 * @return the map of image type code to buffered images map
	 */
	public Map<Integer, BufferedImage> getImages() {
		return images;
	}
}
