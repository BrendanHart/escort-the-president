package escort.common.game.entities;

import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import escort.common.game.map.GameMap;
import escort.common.game.map.Tile;

public final class EntityUtils {

	/**
	 * Return all tiles that are intersecting (over) a give line
	 * 
	 * @param line
	 *            The line given
	 * @param map
	 *            The game map
	 * @return All the tiles that are over the line
	 */
	public static ArrayList<Point2D.Double> tilesSurroundingLine(Line2D.Double line, GameMap map) {
		ArrayList<Point2D.Double> points = new ArrayList<>();
		ArrayList<Rectangle> toCheck = new ArrayList<>();
		ArrayList<Rectangle> newtoCheck = new ArrayList<>();
		ArrayList<Rectangle> seen = new ArrayList<>();
		boolean checkedGoal = false;

		for (Rectangle r : tilesSurroundingPoint(line.getP1(), map)) {
			toCheck.add(r);
		}

		while (!checkedGoal) {

			for (Rectangle tile : toCheck) {
				seen.addAll(toCheck);
				if (line.intersects(tile)) {
					points.add(getCentre(tile));

					for (Rectangle r : tilesSurroundingPoint(getCentre(tile), map)) {
						if (!seen.contains(r) && !toCheck.contains(r)) {
							newtoCheck.add(r);
						}
					}
				}

				if (tile.contains(line.getP2())) {
					checkedGoal = true;
				}
			}
			toCheck.clear();
			toCheck.addAll(newtoCheck);
			newtoCheck.clear();
		}

		return points;
	}

	/**
	 * Get the 9 tile that surround a given point (including the tile that the
	 * point contains)
	 * 
	 * @param p
	 *            The point
	 * @param map
	 *            The map of the game
	 * @return The tile surround the point
	 */
	private static Set<Rectangle> tilesSurroundingPoint(Point2D p, GameMap map) {
		Set<Rectangle> rec = new HashSet<>();
		int TileCornerX = ((int) p.getX() / Tile.TILE_WIDTH) * Tile.TILE_WIDTH;
		int TileCornerY = ((int) p.getY() / Tile.TILE_HEIGHT) * Tile.TILE_HEIGHT;

		if (TileCornerX - Tile.TILE_WIDTH >= 0 && TileCornerY - Tile.TILE_HEIGHT >= 0) {
			rec.add(new Rectangle(TileCornerX - Tile.TILE_WIDTH, TileCornerY - Tile.TILE_HEIGHT, Tile.TILE_WIDTH,
					Tile.TILE_HEIGHT));
		}

		if (TileCornerY - Tile.TILE_HEIGHT >= 0) {
			rec.add(new Rectangle(TileCornerX, TileCornerY - Tile.TILE_HEIGHT, Tile.TILE_WIDTH, Tile.TILE_HEIGHT));
		}

		if (TileCornerX + Tile.TILE_WIDTH < map.getWidthInPx() && TileCornerY - Tile.TILE_HEIGHT >= 0) {
			rec.add(new Rectangle(TileCornerX + Tile.TILE_WIDTH, TileCornerY - Tile.TILE_HEIGHT, Tile.TILE_WIDTH,
					Tile.TILE_HEIGHT));
		}

		if (TileCornerX - Tile.TILE_WIDTH >= 0) {
			rec.add(new Rectangle(TileCornerX - Tile.TILE_WIDTH, TileCornerY, Tile.TILE_WIDTH, Tile.TILE_HEIGHT));
		}

		rec.add(new Rectangle(TileCornerX, TileCornerY, Tile.TILE_WIDTH, Tile.TILE_HEIGHT));

		if (TileCornerX + Tile.TILE_WIDTH < map.getWidthInPx()) {
			rec.add(new Rectangle(TileCornerX + Tile.TILE_WIDTH, TileCornerY, Tile.TILE_WIDTH, Tile.TILE_HEIGHT));
		}

		if (TileCornerX - Tile.TILE_WIDTH >= 0 && TileCornerY + Tile.TILE_HEIGHT < map.getHeightInPx()) {
			rec.add(new Rectangle(TileCornerX - Tile.TILE_WIDTH, TileCornerY + Tile.TILE_HEIGHT, Tile.TILE_WIDTH,
					Tile.TILE_HEIGHT));
		}

		if (TileCornerY + Tile.TILE_HEIGHT < map.getHeightInPx()) {
			rec.add(new Rectangle(TileCornerX, TileCornerY + Tile.TILE_HEIGHT, Tile.TILE_WIDTH, Tile.TILE_HEIGHT));
		}

		if (TileCornerX + Tile.TILE_WIDTH < map.getWidthInPx()
				&& TileCornerY + Tile.TILE_HEIGHT < map.getHeightInPx()) {
			rec.add(new Rectangle(TileCornerX + Tile.TILE_WIDTH, TileCornerY + Tile.TILE_HEIGHT, Tile.TILE_WIDTH,
					Tile.TILE_HEIGHT));
		}

		return rec;
	}

	/**
	 * Get the centre point of a rectangle
	 * 
	 * @param r
	 *            The rectangle
	 * @return The centre point
	 */
	private static Point2D.Double getCentre(Rectangle r) {
		return new Point2D.Double(r.getCenterX(), r.getCenterY());
	}
}
