package escort.client.graphics;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Map;

import escort.client.main.Scale;
import escort.common.game.map.GameMap;
import escort.common.game.map.Tile;

/**
 * Renders an individual tile on the map.
 * 
 * @author Ahmed Bhallo
 *
 */
public class MapRenderer {

	/**
	 * The displayable width of a tile.
	 */
	public final int TILE_DISPLAY_W = Tile.TILE_WIDTH * Scale.factor;

	/**
	 * The displayable height of a tile.
	 */
	public final int TILE_DISPLAY_H = Tile.TILE_HEIGHT * Scale.factor;

	/**
	 * The displayable depth of a tile.
	 */
	public final int TILE_DISPLAY_D = Tile.TILE_DEPTH * Scale.factor;

	/**
	 * The camera object.
	 */
	private final Camera camera;

	/**
	 * The map data of the current map.
	 */
	private final int[][] mapData;

	public MapRenderer(Camera camera, GameMap map) {
		this.camera = camera;
		this.mapData = map.getMapData();
	}

	/**
	 * Rendered a tile at a specified position on the map.
	 * 
	 * @param g
	 *            The graphics object.
	 * @param i
	 *            The x coordinate of the tile on the map (in tiles).
	 * @param j
	 *            The y coordinate of the tile on the map (in tiles).
	 */
	public void renderTile(Graphics2D g, int i, int j) {
		// Get the tile type
		Tile t = getTile(i, j);

		// Calculate x position for rendering.
		int displayX = i * TILE_DISPLAY_W - camera.getxOffset();

		// Calculate y position for rendering.
		int displayY = j * TILE_DISPLAY_H - camera.getyOffset();

		// Get the images of the tile.
		Map<Integer, BufferedImage> images = t.getImages();

		if (t.isWalkable()) {
			// If the tile is walkable, check if the surrounding tiles are
			// walkable.
			Tile left = getTile(i - 1, j);
			Tile up = getTile(i, j - 1);
			Tile topLeft = getTile(i - 1, j - 1);

			// For shading, given a tile, if surrounding tiles are unwalkable,
			// shade the center tile accordingly.

			// Following the following cases:
			// Top left tile is unwalkable -> Full shade
			// Only top tile is unwalkable -> Top right shade
			// Only left tile unwalkable -> Bottom left shade

			BufferedImage img;
			if (topLeft != null && !topLeft.isWalkable()) {
				img = images.get(Tile.FULL_SHADOW);
			} else if (up != null && !up.isWalkable()) {
				img = images.get(Tile.TOP_RIGHT_SHADOW);
			} else if (left != null && !left.isWalkable()) {
				img = images.get(Tile.BOTTOM_LEFT_SHADOW);
			} else {
				img = images.get(Tile.TOP_IMAGE);
			}
			// Draw the image.
			g.drawImage(img, displayX, displayY, TILE_DISPLAY_W, TILE_DISPLAY_H, null);
		} else {
			// If the tile is not walkable, render the top and bottom of the
			// image.
			g.drawImage(images.get(Tile.TOP_IMAGE), displayX, displayY - TILE_DISPLAY_D, TILE_DISPLAY_W, TILE_DISPLAY_H,
					null);
			g.drawImage(images.get(Tile.BOTTOM_IMAGE), displayX, displayY - TILE_DISPLAY_D + TILE_DISPLAY_H,
					TILE_DISPLAY_W, TILE_DISPLAY_D, null);
		}
		// Uncomment this to print i and j position on the map.
		// g.drawString(i + "," + j, displayX, displayY);
	}

	/**
	 * Returns a tile given i and j coordinates on the map.
	 * 
	 * @param i
	 *            The x coordinate on the map (in tiles).
	 * @param j
	 *            The y coordinate on the map (in tiles).
	 * @return The tile object.
	 */
	private Tile getTile(int i, int j) {
		try {
			return Tile.ALL_TILES.get(mapData[j][i]);
		} catch (ArrayIndexOutOfBoundsException e) {
			return null;
		}
	}
}
