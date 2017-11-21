package escort.common.game.routePlanning;

import java.awt.Rectangle;
import java.util.List;

/**
 * Interface for planning routes on a map. RoutePlanner specifies all methods
 * that a route planner needs to implement to be available for use in our game.
 * 
 * @author Brendan Hart
 */
public interface RoutePlanner {

	/**
	 * Sets the planning map of the planner
	 * 
	 * @param map
	 *            The new planning map
	 */
	public void setMap(PlanningMap map);

	/**
	 * Gets the planning map of the planner
	 * 
	 * @return The planning map
	 */
	public PlanningMap getMap();

	/**
	 * Generates a route from a start to an end
	 * 
	 * @param start
	 *            The start point
	 * @param end
	 *            The end point
	 * @return The list of rectangles which specify the route
	 */
	public List<Rectangle> route(Rectangle start, Rectangle end);

}
