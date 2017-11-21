package escort.server.game.ai;

import java.awt.Rectangle;
import java.util.Random;

import escort.common.game.GameData;
import escort.common.game.entities.units.Escort;
import escort.common.game.entities.units.President;
import escort.common.game.map.Tile;
import escort.common.game.routePlanning.RoutePlanner;
import escort.common.systime.SystemTime;

/**
 * Escort AI controller
 * 
 * @author Kwong Hei Tsang
 *
 */
public class EscortController extends AIController {

	private final Escort escort;
	private long timeSinceReset;
	private static final long RESET_DURATION = 5000;
	private final Random rand;

	/**
	 * Construct an escort controller
	 * 
	 * @param gameData
	 *            The game data
	 * @param escort
	 *            The escort unit
	 * @param planner
	 *            The route planner
	 */
	public EscortController(GameData gameData, Escort escort, RoutePlanner planner) {
		super(gameData, escort, planner);
		this.escort = escort;
		this.timeSinceReset = SystemTime.milliTime();
		this.rand = new Random();
	}

	/**
	 * Controls the escort. Reloads when needed, finds the president and attemps
	 * to request follow to take to the safe zone.
	 */
	@Override
	public void control() {
		// reload and switch weapon
		reloadAndSwitch();

		// Look at enemy
		if (target == null || target.isDead() || unit.distance(target) > SHOOTING_RANGE) {
			target = findClosestEnemy();
		}

		// Shoot enemy
		if (target != null && unit.distance(target) <= SHOOTING_RANGE) {
			escort.setDir(unit.angleFromPoint(target.getCenterPoint()));
			escort.shoot();
			escort.unfollow();
			return;
		}

		// Check president collision
		President presCollision;
		if (!escort.isFollower() && (presCollision = escort.getPresidentCollision()) != null
				&& !presCollision.isFollowing()) {
			// When president is following anyone, follow this unit if this is
			// not a follower
			escort.follow();
		}

		if (currentRoute.isEmpty() || SystemTime.milliTime() - timeSinceReset >= RESET_DURATION) {
			Rectangle rect;
			if (!escort.isFollower()) {
				// should find president when not a follower
				do {
					rect = randomRectNearUnit(gameData.getPresident(), 15);
				} while (!gameData.getMap().walkableTile(rect.x, rect.y));
			} else {
				// otherwise should go to endzone
				Rectangle[] endzones = gameData.getMap().getEndZones().toArray(new Rectangle[0]);
				Rectangle endzone = endzones[this.rand.nextInt(endzones.length)];
				// convert to tiles
				rect = new Rectangle(endzone.x / Tile.TILE_WIDTH, endzone.y / Tile.TILE_HEIGHT, 0, 0);
			}
			generateRoute(rect);
			timeSinceReset = SystemTime.milliTime();
		}

		traverseRoute();
		updateDirection();

	}

}
