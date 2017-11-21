package escort.server.game.ai;

import java.awt.Rectangle;

import escort.common.game.GameData;
import escort.common.game.entities.units.Police;
import escort.common.game.routePlanning.RoutePlanner;
import escort.common.systime.SystemTime;

/**
 * An AIController implementation for Police
 * 
 * @author Ahmed Bhallo
 * @author James Birch
 * @author Edward Dean
 * @author Brendan Hart
 * @author Kwong Hei Tsang
 *
 */
public class PoliceController extends AIController {

	// private Police police;
	private long timeSinceReset;
	private static final long RESET_DURATION = 5000;

	/**
	 * Instantiates a new police controller
	 * 
	 * @param gameData
	 *            The game data
	 * @param police
	 *            The police object to be controller
	 * @param planner
	 *            The route planner object
	 */
	public PoliceController(GameData gameData, Police police, RoutePlanner planner) {
		super(gameData, police, planner);
		// this.police = police;
	}

	/**
	 * Stays within a certain distance from the president. Attacks and shoots
	 * any nearby assassins.
	 */
	@Override
	public void control() {
		reloadAndSwitch();
		if (target == null || target.isDead()) {
			target = findClosestEnemy();
		}

		if (target != null) {
			followAndShootTarget();
			return;
		}

		if (currentRoute.isEmpty() || SystemTime.milliTime() - timeSinceReset >= RESET_DURATION) {
			Rectangle rect;
			do {
				rect = randomRectNearUnit(gameData.getPresident(), 15);
			} while (!gameData.getMap().walkableTile(rect.x, rect.y));
			generateRoute(rect);
			timeSinceReset = SystemTime.milliTime();
		}

		traverseRoute();
		updateDirection();
	}
}