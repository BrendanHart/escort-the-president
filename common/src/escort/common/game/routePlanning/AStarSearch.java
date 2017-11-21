package escort.common.game.routePlanning;

import escort.common.game.map.Tile;

import java.awt.Rectangle;
import java.util.*;

/**
 * Performs A* search between two positions represented as rectangles (x, y,
 * width, height).
 * 
 * @author Brendan Hart
 */
public class AStarSearch implements RoutePlanner {

	private PlanningMap map;

	/**
	 * Set the planning map.
	 * 
	 * @param map
	 *            The planning map.
	 */
	public void setMap(PlanningMap map) {
		this.map = map;
	}

	/**
	 * Generate a route from a given position to the goal position.
	 * 
	 * @param start
	 *            The starting position.
	 * @param goal
	 *            The goal position.
	 * @return A list of positions of the route in order.
	 */
	public List<Rectangle> route(Rectangle start, Rectangle end) {

		Node startNode = new Node(null, start, 0, heuristic(start, end));
		Node endNode = new Node(null, end, -1, 0);
		if (map == null)
			return null; // Possibly route not found exception?

		Queue<Node> frontier = new PriorityQueue<>(new NodeComparator());
		frontier.add(startNode);
		List<Node> seen = new ArrayList<>();

		Node current;
		do {
			// frontier.remove() will throw an exception if
			// frontier is empty.
			current = frontier.remove();
			if (seen.contains(current)) {
				continue;
			}
			for (Node n : getSuccessors(current, endNode)) {
				frontier.add(n);
			}
			seen.add(current);
		} while (!current.equals(endNode));

		return getPathFromNode(current);

	}

	/**
	 * Generates a path to the target node
	 * 
	 * @param n
	 *            The goal node
	 * @return The path specified by a list of rectangles
	 */
	private List<Rectangle> getPathFromNode(Node n) {
		List<Rectangle> l = new LinkedList<>();
		while (n != null) {
			l.add(0, n.getRect());
			n = n.getParent();
		}
		return l;
	}

	/**
	 * Calculate the distance from current to end in actual pixels (the
	 * heuristic)
	 * 
	 * @param current
	 *            The current position
	 * @param goal
	 *            The goal position
	 * @return The distance.
	 */
	private double heuristic(Rectangle current, Rectangle goal) {
		return Math.sqrt(Math.pow(current.getX() * Tile.TILE_WIDTH - goal.getX() * Tile.TILE_WIDTH, 2)
				+ Math.pow(current.getY() * Tile.TILE_HEIGHT - goal.getY() * Tile.TILE_HEIGHT, 2));
	}

	/**
	 * Generate the successors of a given node.
	 * 
	 * @param n
	 *            The node to generate successors from.
	 * @param goal
	 *            The goal node, used for calculating the heuristic.
	 * @return A list of successor nodes.
	 */
	private List<Node> getSuccessors(Node n, Node goal) {

		Rectangle pos = n.getRect();
		List<Node> successors = new ArrayList<Node>();

		for (int i = (int) pos.getX() - 1; i <= (int) pos.getX() + 1; i++) {
			for (int j = (int) pos.getY() - 1; j <= (int) pos.getY() + 1; j++) {
				Rectangle p = new Rectangle(i, j, (int) pos.getWidth(), (int) pos.getHeight());
				if (p.equals(pos)) {
					continue;
				}
				if (!map.inBounds(p)) {
					continue;
				}
				if (map.obstacle(p)) {
					continue;
				}
				if (i != pos.getX() && j != pos.getY()) {
					Rectangle horizontalChange = new Rectangle(i, (int) pos.getY(), (int) pos.getWidth(),
							(int) pos.getHeight());
					Rectangle verticalChange = new Rectangle((int) pos.getX(), j, (int) pos.getWidth(),
							(int) pos.getHeight());
					if (map.inBounds(horizontalChange))
						if (map.obstacle(horizontalChange))
							continue;
					if (map.inBounds(verticalChange))
						if (map.obstacle(verticalChange))
							continue;
				}
				successors.add(new Node(n, p, n.getPathCost() + getCost(pos, p), heuristic(p, goal.getRect())));
			}
		}

		return successors;

	}

	/**
	 * Get the path cost of moving from one position to another.
	 * 
	 * @param from
	 *            The position moving from.
	 * @param to
	 *            The position moving to.
	 * @return The cost of the movement.
	 */
	private double getCost(Rectangle from, Rectangle to) {
		boolean difX = from.getX() != to.getX();
		boolean difY = from.getY() != to.getY();
		if (difX) {
			if (difY) {
				return Math.sqrt(Math.pow(Tile.TILE_HEIGHT, 2) + Math.pow(Tile.TILE_WIDTH, 2));
			} else {
				return Tile.TILE_WIDTH;
			}
		} else if (difY) {
			return Tile.TILE_HEIGHT;
		}
		return 0;
	}

	@Override
	/**
	 * Get the planning map.
	 * 
	 * @return The map.
	 */
	public PlanningMap getMap() {
		return map;
	}

}
