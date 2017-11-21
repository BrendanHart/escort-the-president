package escort.common.game.map;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import escort.common.game.entities.units.Unit;
import escort.common.powerups.ExtraGrenades;
import escort.common.powerups.ExtraMags;
import escort.common.powerups.PowerUp;
import escort.common.powerups.ReplenishHealth;

/**
 * Loads a map and stores tiles in array
 * 
 * @author Ahmed Bhallo
 * @author Brendan Hart (Additions)
 *
 */
public class GameMap {

	public static final int HOTEL_ID = 0;
	public static final int UNIVERSITY_ID = 1;

	public static BufferedImage HOTEL_CODE;
	public static BufferedImage UNIVERSITY_CODE;

	// private String mapName;

	private final Set<PowerUp> powerUps = new HashSet<>();
	private final Set<Rectangle> escortSpawns = new HashSet<>();
	private final Set<Rectangle> assassinSpawns = new HashSet<>();
	private final Set<Rectangle> presidentSpawns = new HashSet<>();
	private final Set<Rectangle> endZones = new HashSet<>();

	private final int[][] mapData;

	private final int widthInTiles;
	private final int heightInTiles;

	private int mapID;

	/**
	 * Instantiates a new GameMap
	 * 
	 * @param mapData
	 *            The map data
	 * @param mapName
	 *            The map name
	 * @param mapID
	 *            The map ID
	 */
	public GameMap(int[][] mapData, String mapName, int mapID) {
		this.mapData = mapData;
		this.mapID = mapID;
		this.widthInTiles = mapData[0].length;
		this.heightInTiles = mapData.length;
	}

	/**
	 * Loads the game map from the map code by traversing the pixels and finding
	 * corresponding tiles.
	 * 
	 * @param mapGridCode
	 *            The map code
	 * @param mapName
	 *            The map name
	 * @param mapID
	 *            The map id
	 * @return The game map object
	 */
	private static GameMap loadFromMapCode(BufferedImage mapGridCode, String mapName, int mapID) {
		int widthInTiles = mapGridCode.getWidth();
		int heightInTiles = mapGridCode.getHeight();
		int[] mapArrayRGB = mapGridCode.getRGB(0, 0, widthInTiles, heightInTiles, null, 0, widthInTiles);

		GameMap gameMap = new GameMap(new int[heightInTiles][widthInTiles], mapName, mapID);

		for (int j = 0; j < heightInTiles; j++) {
			for (int i = 0; i < widthInTiles; i++) {
				for (Tile tile : Tile.ALL_TILES.values()) {
					if (tile.colorCode == mapArrayRGB[i + j * widthInTiles]) {
						gameMap.getMapData()[j][i] = tile.tileID;
						Rectangle tileRect = new Rectangle(i * Tile.TILE_WIDTH, j * Tile.TILE_HEIGHT, Tile.TILE_WIDTH,
								Tile.TILE_HEIGHT);
						switch (tile.tileID) {
						case Tile.HP_POWER_UP:
							gameMap.getPowerUps().add(new ReplenishHealth(tileRect.x, tileRect.y));
							break;
						case Tile.AMMO_POWER_UP:
							gameMap.getPowerUps().add(new ExtraMags(tileRect.x, tileRect.y));
							break;
						case Tile.GRENADE_POWER_UP:
							gameMap.getPowerUps().add(new ExtraGrenades(tileRect.x, tileRect.y));
							break;
						case Tile.ESCORT_SPAWN:
							gameMap.getEscortSpawns().add(tileRect);
							break;
						case Tile.ASSASSIN_SPAWN:
							gameMap.getAssassinSpawns().add(tileRect);
							break;
						case Tile.PRESIDENT_SPAWN:
							gameMap.getPresidentSpawns().add(tileRect);
							break;
						case Tile.END_ZONE:
							gameMap.getEndZones().add(tileRect);
						}
						break;
					}
				}
			}
		}
		return gameMap;
	}

	/**
	 * Returns whether given coordinates in tiles is walkable
	 * 
	 * @param x
	 *            x coordinate in tiles
	 * @param y
	 *            y coordinate in tiles
	 * @return True iff the point is walkable
	 */
	public boolean walkableInTiles(int x, int y) {
		try {
			return (Tile.ALL_TILES.get(mapData[y][x]).isWalkable());
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}

	/**
	 * Check whether an area is walkable or not
	 * 
	 * @param x
	 *            The x position of the area
	 * @param y
	 *            The y position of the area
	 * @param width
	 *            The width of the area
	 * @param height
	 *            The height of the area
	 * @return Whether the tile is walkable
	 */
	public boolean walkable(int x, int y, int width, int height) {
		int left = (int) Math.floor(x / Tile.TILE_WIDTH);
		int right = (int) Math.floor((x + width) / Tile.TILE_WIDTH);
		int top = (int) Math.floor((y) / Tile.TILE_HEIGHT);
		int bottom = (int) Math.floor((y + height) / Tile.TILE_HEIGHT);
		return walkableTile(left, top) && walkableTile(right, top) && walkableTile(left, bottom)
				&& walkableTile(right, bottom);
	}

	/**
	 * Returns whether or not a rectangle is walkable
	 * 
	 * @param r
	 *            The rectangle
	 * @return True iff the rectangle is walkable
	 */
	public boolean walkableRect(Rectangle r) {
		return walkableTile(r.x, r.y) && walkableTile(r.x + r.width, r.y) && walkableTile(r.x, r.y + r.height)
				&& walkableTile(r.x + r.width, r.y + r.height);
	}

	/**
	 * Check whether a tile is walkable or not
	 * 
	 * @param x
	 *            The x position of the tile
	 * @param y
	 *            The y position of the tile
	 * @return Whether the tile is walkable
	 */
	public boolean walkableTile(int i, int j) {
		try {
			return (Tile.ALL_TILES.get(mapData[j][i]).isWalkable());
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}

	/**
	 * Gets the spawn points for a given unit type
	 * 
	 * @param type
	 *            The unit type
	 * @return The set of rectangles where they can spawn
	 */
	public Set<Rectangle> getSpawnsFor(int type) {
		switch (type) {
		case Unit.ESCORT_TYPE:
			return escortSpawns;
		case Unit.PRESIDENT_TYPE:
			return presidentSpawns;
		case Unit.ASSASSIN_TYPE:
			return assassinSpawns;
		default:
			throw new IllegalArgumentException("No spawns for type: " + type);
		}
	}

	/**
	 * Loads the map from the MAP ID
	 * 
	 * @param mapID
	 *            The map id
	 * @return The game map
	 */
	public static GameMap loadFromID(int mapID) {
		switch (mapID) {
		case HOTEL_ID:
			return loadFromMapCode(GameMap.HOTEL_CODE, "Hotel", mapID);
		case UNIVERSITY_ID:
			return loadFromMapCode(GameMap.UNIVERSITY_CODE, "University", mapID);
		default:
			// invalid map ID
			return loadFromMapCode(GameMap.HOTEL_CODE, "hotel", GameMap.HOTEL_ID);
		}
	}

	/**
	 * Test whether an entity is in line of sight of you by testing if there is
	 * a wall in the way. If there is a wall in the way, return false otherwise
	 * return true
	 * 
	 * @param entity
	 *            An entity to test if you are in line of sight of
	 * @return Whether you are in line of sight of the entity
	 */
	public boolean lineOfSight(Rectangle thisBounds, Rectangle otherBounds) {
		Line2D.Double lineOfSight = new Line2D.Double(thisBounds.x + thisBounds.width / 2,
				thisBounds.y + thisBounds.height / 2, otherBounds.x + otherBounds.width / 2,
				otherBounds.y + otherBounds.height / 2);

		int upperLeftX = Math.min(thisBounds.x, otherBounds.x);
		int upperLeftY = Math.min(thisBounds.y, otherBounds.y);
		int lowerRightX = Math.max(thisBounds.x, otherBounds.x);
		int lowerRightY = Math.max(thisBounds.y, otherBounds.y);
		for (int i = (upperLeftX / Tile.TILE_WIDTH) - 1; i <= (lowerRightX / Tile.TILE_WIDTH) + 1; i++) {
			for (int j = (upperLeftY / Tile.TILE_HEIGHT) - 1; j <= (lowerRightY / Tile.TILE_HEIGHT) + 1; j++) {
				if (!walkableInTiles(i, j)) {
					if (new Rectangle(i * Tile.TILE_WIDTH, j * Tile.TILE_HEIGHT, Tile.TILE_WIDTH, Tile.TILE_HEIGHT)
							.intersectsLine(lineOfSight)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	// GETTERS AND SETTERS //

	/**
	 * @return The id of the map.
	 */
	public int getMapID() {
		return mapID;
	}

	public Set<PowerUp> getPowerUps() {
		return powerUps;
	}

	public Set<Rectangle> getPresidentSpawns() {
		return presidentSpawns;
	}

	public Set<Rectangle> getAssassinSpawns() {
		return assassinSpawns;
	}

	public Set<Rectangle> getEscortSpawns() {
		return escortSpawns;
	}

	public Set<Rectangle> getEndZones() {
		return endZones;
	}

	public int[][] getMapData() {
		return mapData;
	}

	public int getWidthInTiles() {
		return widthInTiles;
	}

	public int getHeightInTiles() {
		return heightInTiles;
	}

	public int getWidthInPx() {
		return widthInTiles * Tile.TILE_WIDTH;
	}

	public int getHeightInPx() {
		return heightInTiles * Tile.TILE_HEIGHT;
	}

}
