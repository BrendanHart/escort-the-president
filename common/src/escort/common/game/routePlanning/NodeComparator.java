package escort.common.game.routePlanning;

import java.util.Comparator;

/**
 * Compares 2 nodes based on their cost
 * 
 * @author Brendan Hart
 */
public class NodeComparator implements Comparator<Node> {

	/**
	 * Compares 2 nodes based on cost.
	 * 
	 * @return If the first node is greater than the second, returns a positive
	 *         integer. If they are the same, returns 0. If first argument is
	 *         less, returns a negative integer.
	 */
	@Override
	public int compare(Node n1, Node n2) {
		return (int) Math.round(n1.getEstimatedCost() - n2.getEstimatedCost());
	}
}
