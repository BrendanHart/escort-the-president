package escort.common.game.map;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Loads the tiles and the map codes for loading gamemaps.
 * 
 * @author Ahmed Bhallo
 *
 */
public class MapLoader {

	/**
	 * Loads the tiles and map code
	 * 
	 * @throws IOException
	 */
	public void load() throws IOException {
		loadTiles();
		loadMapCodes();
	}

	/**
	 * Loads each tile with its color code and whether it is walkable or not.
	 */
	private void loadTiles() {
		loadTile(Tile.BED, false, 0xFFd69d9d);
		loadTile(Tile.BLUE_CARPET, true, 0xFF434a83);
		loadTile(Tile.BRICK_WALL, false, 0xFF873e3e);
		loadTile(Tile.CHECKERED_FLOOR, true, 0xFFdedede);
		loadTile(Tile.CHAIR, false, 0xFF4f3711);
		loadTile(Tile.DINING_TABLE, false, 0xFFba8427);
		loadTile(Tile.GRASS, true, 0xFF479431);
		loadTile(Tile.MUD, true, 0xFF472d2d);
		loadTile(Tile.PAVEMENT, true, 0xFFa3a3a3);
		loadTile(Tile.PINK_CARPET, true, 0xFFe96161);
		loadTile(Tile.RED_CARPET, true, 0xFFff8080);
		loadTile(Tile.STONE_WALL, false, 0xFF878787);
		loadTile(Tile.STONE_BRICK, false, 0XFF5e5e5e);
		loadTile(Tile.TARMAC, true, 0xFF252525);
		loadTile(Tile.WHITE_FLOOR, true, 0xFFe9e9e9);
		loadTile(Tile.WOODEN_FLOOR, true, 0xFFe1a74d);
		loadTile(Tile.BED_PILLOW, false, 0xFFdec2c2);
		loadTile(Tile.ESCORT_SPAWN, true, 0xFF436bd0);
		loadTile(Tile.ASSASSIN_SPAWN, true, 0xFFffd800);
		loadTile(Tile.PRESIDENT_SPAWN, true, 0xFF2d0077);
		loadTile(Tile.END_ZONE, true, 0xFFff8400);
		loadTile(Tile.HP_POWER_UP, true, 0xFFff3e3e);
		loadTile(Tile.AMMO_POWER_UP, true, 0xFF9fdd80);
		loadTile(Tile.GRENADE_POWER_UP, true, 0xFF8f1bac);
	}

	/**
	 * Puts a the tile and its information in the ALL_TILES map.
	 * 
	 * @param tileID
	 *            The tile id
	 * @param walkable
	 *            Whether the tile is walkable
	 * @param colorCode
	 *            The color code of the tile
	 */
	private void loadTile(int tileID, boolean walkable, int colorCode) {
		Tile.ALL_TILES.put(tileID, new Tile(tileID, walkable, colorCode));
	}

	/**
	 * Loads the map codes for gamemaps
	 * 
	 * @throws IOException
	 */
	private void loadMapCodes() throws IOException {
		GameMap.HOTEL_CODE = loadBufferedImage("mapcodes/hotel.png");
		GameMap.UNIVERSITY_CODE = loadBufferedImage("mapcodes/university.png");
	}

	/**
	 * Loads a Buffered Image
	 * 
	 * @param path
	 *            the path of the image
	 * @return The image loaded
	 * @throws IOException
	 */
	private BufferedImage loadBufferedImage(String path) throws IOException {
		return ImageIO.read(getClass().getResourceAsStream(path));
	}

}
