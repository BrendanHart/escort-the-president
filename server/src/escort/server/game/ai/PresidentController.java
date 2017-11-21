package escort.server.game.ai;

import escort.common.game.GameData;
import escort.common.game.entities.units.Escort;
import escort.common.game.entities.units.President;
import escort.common.game.routePlanning.RoutePlanner;

/**
 * A controller to be controlled by the president AI
 * 
 * @author Ahmed Bhallo
 * @author Brendan Hart
 *
 */
public class PresidentController extends AIController {

	private President president;

	/**
	 * Instantiates a new president controller
	 * 
	 * @param gameData
	 *            The game data
	 * @param president
	 *            The president object
	 * @param planner
	 *            The route planner object
	 */
	public PresidentController(GameData gameData, President president, RoutePlanner planner) {
		super(gameData, president, planner);
		this.president = president;
		this.planner = planner;
	}

	/**
	 * If the president is following an escort, follows them.
	 */
	@Override
	public void control() {
		// If the president is not following any escort, don't move.
		if (!president.isFollowing()) {
			return;
		}

		Escort e = president.getFollowing();

		followUnit(e);

		traverseRoute();
		updateDirection();
	}
}
