package escort.common.game.routePlanning;

import java.awt.*;

/**
 * A node used for route planning. Contains information about its position, and
 * the parent node. Contains path costs and total cost.
 * 
 * @author Brendan Hart
 */
public class Node {

	private Node parent;
	private Rectangle p;
	private double pathCost;
	private double totalCost;

	/**
	 * Instantiates a new node
	 * 
	 * @param parent
	 *            The parent node
	 * @param p
	 *            The rectangle
	 * @param pathCost
	 *            The cost of adding to the path.
	 * @param distanceToGoal
	 *            The cost of the distance to the goal
	 */
	public Node(Node parent, Rectangle p, double pathCost, double distanceToGoal) {
		this.parent = parent;
		this.p = p;
		this.pathCost = pathCost;
		this.totalCost = distanceToGoal + pathCost;

	}

	/**
	 * Represents the node as a string.
	 */
	@Override
	public String toString() {
		return p.toString() + " PC:" + pathCost + " TC:" + totalCost;
	}

	// GETTERS AND SETTERS //

	public Rectangle getRect() {
		return p;
	}

	public Node getParent() {
		return parent;
	}

	public double getPathCost() {
		return pathCost;
	}

	public double getEstimatedCost() {
		return totalCost;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Node))
			return false;
		Node n = (Node) obj;
		return (p.equals(n.p));
	}

}
