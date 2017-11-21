package escort.server.game.ai;

import java.awt.Rectangle;
import java.util.ArrayList;

import escort.common.game.GameData;
import escort.common.game.entities.units.Civilian;
import escort.common.game.routePlanning.RoutePlanner;

/**
 * An AIController implementation for Civilians.
 * 
 * @author Ahmed Bhallo
 * @author James Birch
 * @author Edward Dean
 * @author Brendan Hart
 * @author Kwong Hei Tsang
 *
 */
public class CivilianController extends AIController {

	private Civilian civ;

	private boolean timerSet;
	private long timerSetTime;
	private long timerTime;

	/**
	 * Instantiates a new Civilian Controller
	 * 
	 * @param gameData
	 *            The game data
	 * @param civ
	 *            The civilian to be controller
	 * @param planner
	 *            The route planner object
	 */
	public CivilianController(GameData gameData, Civilian civ, RoutePlanner planner) {
		super(gameData, civ, planner);
		this.civ = civ;

		this.timerSet = false;
		this.timerSetTime = -1;
		this.timerTime = -1;
	}

	/**
	 * Sets a simple route from the current position to specified coordinates.
	 * 
	 * @param currentPos
	 *            The current position.
	 * @param i
	 *            The x coordinate in tiles of the goal.
	 * @param j
	 *            The y coordinate in tiles of the goal.
	 * @param steps
	 *            The number of steps to traverse.
	 */
	private void setSimpleRoute(Rectangle currentPos, int i, int j, int steps) {
		currentRoute = new ArrayList<>();
		for (int s = 1; s <= steps; s++) {
			Rectangle nextPos = new Rectangle(currentPos.x + (s * i), currentPos.y + (s * j), currentPos.width,
					currentPos.height);
			if (gameData.getMap().walkableRect(nextPos)) {
				currentRoute.add(nextPos);
			} else {
				break;
			}
		}
	}

	/**
	 * @return True iff. the timer for finding a new route is up.
	 */
	public boolean timerUp() {
		return timerTime <= System.nanoTime() - timerSetTime;
	}

	/**
	 * Sets a new timer given the time in seconds for traversing a new route.
	 * 
	 * @param timeInSeconds
	 *            The new time (in seconds)
	 */
	public void setTimer(int timeInSeconds) {
		timerSet = true;
		timerSetTime = System.nanoTime();
		timerTime = 0;// timeInSeconds * 1000000000;
	}

	/**
	 * Controls the civilian. Generates new routes when needed and traverses the
	 * route.
	 */
	@Override
	public void control() {
		if (currentRoute.isEmpty()) {
			if (timerSet) {
				if (!timerUp())
					return;
				else
					timerSet = false;
			} else {
				setTimer(r.nextInt(5));
				timerSet = true;
				return;
			}
			generateRandomRoute();
		}

		traverseRoute();
		updateDirection();
	}

	/**
	 * Generates a random route from the civilian's current position. Calls the
	 * method for setting a simple route.
	 */
	private void generateRandomRoute() {
		Rectangle currentPos = civ.getAbsoluteBoundsInTiles();

		// Generate the amount of steps (1-20)
		int steps = 1 + r.nextInt(20);
		// Generate random direction:
		switch (r.nextInt(9)) {
		case 0:
			// UP
			setSimpleRoute(currentPos, 0, -1, steps);
			break;
		case 1:
			// UP RIGHT
			setSimpleRoute(currentPos, 1, -1, steps);
			break;
		case 2:
			// RIGHT
			setSimpleRoute(currentPos, 1, 0, steps);
			break;
		case 3:
			// RIGHT DOWN
			setSimpleRoute(currentPos, 1, 1, steps);
			break;
		case 5:
			// DOWN
			setSimpleRoute(currentPos, 0, 1, steps);
			break;
		case 6:
			// DOWN LEFT
			setSimpleRoute(currentPos, -1, 1, steps);
			break;
		case 7:
			// LEFT
			setSimpleRoute(currentPos, -1, 0, steps);
			break;
		case 8:
			// UP LEFT
			setSimpleRoute(currentPos, -1, -1, steps);
			break;
		}
	}
}
