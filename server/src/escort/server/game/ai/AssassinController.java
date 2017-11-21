package escort.server.game.ai;

import escort.common.game.GameData;
import escort.common.game.entities.units.Assassin;
import escort.common.game.entities.units.President;
import escort.common.game.entities.units.Unit;
import escort.common.game.routePlanning.RoutePlanner;

/**
 * An AIController implementation for Assassins
 * 
 * @author Ahmed Bhallo
 * @author James Birch
 * @author Edward Dean
 * @author Brendan Hart
 * @author Kwong Hei Tsang
 *
 */
public class AssassinController extends AIController {

	private Assassin assassin;

	/**
	 * Instantiates a new Assassin Controller
	 * 
	 * @param gameData
	 *            The game data
	 * @param assassin
	 *            The unit object to be controller
	 * @param planner
	 *            The planner
	 */
	public AssassinController(GameData gameData, Assassin assassin, RoutePlanner planner) {
		super(gameData, assassin, planner);

		this.assassin = assassin;
		this.assassin.setWeapon(Unit.MACHINE_GUN); // default to MG

	}

	/**
	 * Controls the unit. Finds and attacks the president if there is a line of sight.
	 * Reloads when necessary. Attacks nearby enemies.
	 */
	@Override
	public void control() {
		reloadAndSwitch();
		President pres = gameData.getPresident();

		if (target == null || target.isDead()
				|| !gameData.getMap().lineOfSight(pres.getHitbox(), assassin.getHitbox())) {
			target = findClosestEnemy();
		}

		if (target != null && target.getUnitType() != Unit.PRESIDENT_TYPE
				&& gameData.getMap().lineOfSight(pres.getHitbox(), assassin.getHitbox())) {
			target = pres;
		}

		if (target != null) {
			followAndShootTarget();
			return;
		}

		// if we still don't have a target. go to the president
		if (target == null) {
			followUnit(unit.getGameData().getPresident());
		}

		traverseRoute();
		updateDirection();
	}
}
