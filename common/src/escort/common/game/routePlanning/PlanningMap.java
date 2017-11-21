package escort.common.game.routePlanning;

import escort.common.game.map.GameMap;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * A planning map used for path finding.
 * 
 * @author Brendan Hart
 */
public class PlanningMap {

	private int width;
	private int height;
	private boolean grid[][];

	/**
	 * Instantiates a new planning map object
	 * 
	 * @param width
	 *            The width of the map
	 * @param height
	 *            The height of the map
	 */
	public PlanningMap(int width, int height) {
		this.width = width;
		this.height = height;
		grid = new boolean[width][height];
	}

	/**
	 * Instantiates a new planning map object
	 * 
	 * @param width
	 *            The width of the map
	 * @param height
	 *            The height of the map
	 * @param points
	 *            THe points for obstacles
	 */
	public PlanningMap(int width, int height, Point[] points) {
		this.width = width;
		this.height = height;
		grid = new boolean[width][height];
		setObstacles(points);
	}

	/**
	 * Creates an obstacle at specified point
	 * 
	 * @param x
	 *            x coordinate
	 * @param y
	 *            y coodinate
	 */
	public void setObstacle(int x, int y) {
		if (inBounds(new Rectangle(x, y, 0, 0)))
			this.grid[x][y] = true;
	}

	/**
	 * Sets all obstacles from an array of points
	 * 
	 * @param points
	 *            The array of points
	 */
	public void setObstacles(Point[] points) {
		for (Point p : points)
			if (inBounds(new Rectangle((int) p.getX(), (int) p.getY(), 0, 0)))
				this.grid[(int) p.getX()][(int) p.getY()] = true;
	}

	/**
	 * Creates a planning map object from a game map
	 * 
	 * @param gm
	 *            The game map object
	 * @return The planning map object
	 */
	public static PlanningMap createFromGameMap(GameMap gm) {
		PlanningMap map = new PlanningMap(gm.getWidthInTiles(), gm.getHeightInTiles());
		for (int i = 0; i < gm.getWidthInTiles(); i++)
			for (int j = 0; j < gm.getHeightInTiles(); j++)
				if (!gm.walkableInTiles(i, j))
					map.setObstacle(i, j);
		return map;
	}

	/**
	 * Determines if a rectangle is in bounds of the map
	 * 
	 * @param r
	 *            The rectangle to test
	 * @return True if the rectangle is in bounds
	 */
	public boolean inBounds(Rectangle r) {
		return (r.x >= 0 && r.y >= 0 && (r.x + r.width) < width && (r.y + r.height) < height);
	}

	/**
	 * Returns true if a specified rectangle is on an obstacle
	 * 
	 * @param r
	 *            The rectangle to test
	 * @return True iff the rectangle is on an obstacle
	 * @throws ArrayIndexOutOfBoundsException
	 */
	public boolean obstacle(Rectangle r) throws ArrayIndexOutOfBoundsException {
		if (!inBounds(r))
			throw new ArrayIndexOutOfBoundsException("Out of map bounds!");
		for (int i = r.x; i <= (r.x + r.width); i++) {
			for (int j = r.y; j <= (r.y + r.height); j++) {
				if (grid[i][j])
					return true;
			}
		}
		return false;
	}

	/**
	 * @return The width of the map
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @return The height of the map
	 */
	public int getHeight() {
		return height;
	}


}
