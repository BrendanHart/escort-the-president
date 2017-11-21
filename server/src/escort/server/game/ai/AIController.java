package escort.server.game.ai;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import escort.common.game.GameData;
import escort.common.game.entities.Entity;
import escort.common.game.entities.units.Unit;
import escort.common.game.entities.units.UnitController;
import escort.common.game.map.Tile;
import escort.common.game.routePlanning.RoutePlanner;
import escort.common.systime.SystemTime;

/**
 * A class that controls an AI unit.
 */
public abstract class AIController implements UnitController {

	protected Unit unit;
	protected List<Rectangle> currentRoute;
	protected RoutePlanner planner;
	protected Random r = new Random();
	protected Rectangle targetTile;
	private Rectangle previousFollowPos;
	protected final GameData gameData;
	public Unit target;
	protected final long delayTimeMillis = 750;
	protected long timeLastShot = SystemTime.milliTime();
	public static final double MAX_OFFSET = 0.6; // in radians

	public static final int VISION_RANGE = 300;
	public static final int SHOOTING_RANGE = 200;

	/**
	 * Create an AIController
	 * 
	 * @param gameData
	 *            The current game data.
	 * @param unit
	 *            The unit to take control of.
	 * @param planner
	 *            A route planner for the AI.
	 */
	public AIController(GameData gameData, Unit unit, RoutePlanner planner) {
		this.gameData = gameData;
		this.unit = unit;
		this.planner = planner;
		currentRoute = new ArrayList<>();
	}

	/**
	 * Update the direction of the AI unit.
	 */
	public void updateDirection() {
		double newDir = Math.atan2(unit.getXVel(), -unit.getYVel());
		if (newDir < 0) {
			newDir += 2 * Math.PI;
		}
		unit.setDir(newDir);
	}

	/**
	 * Find the closest enemy to the AI unit.
	 * 
	 * @return The closest enemy as a Unit object.
	 */
	public Unit findClosestEnemy() {
		Collection<Unit> units = gameData.getUnits().values();

		double distanceInSight = VISION_RANGE;
		Unit targetInSight = null;
		double distanceOutSight = VISION_RANGE;
		Unit targetOutSight = null;

		for (Unit u : units) {
			if (u.isDead()) {
				continue;
			}
			double dist = unit.distance(u);
			if (dist < distanceInSight && unit.canTarget(u)
					&& gameData.getMap().lineOfSight(unit.getHitbox(), u.getHitbox())) {
				targetInSight = u;
				distanceInSight = dist;
			} else if (dist < distanceOutSight && unit.canTarget(u)) {
				targetOutSight = u;
				distanceOutSight = dist;
			}
		}

		return (targetInSight != null) ? targetInSight : targetOutSight;
	}

	/**
	 * Generate a new route to a particular Entity object.
	 * 
	 * @param e
	 *            The Entity object to move towards.
	 */
	public void newRoute(Entity e) {
		Rectangle currentPos = unit.getAbsoluteBoundsInTiles();
		Rectangle moveToTile = e.getAbsoluteBoundsInTiles();
		currentRoute = planner.route(currentPos, moveToTile);
	}

	/**
	 * Generate a new route to a particular Rectangle object.
	 * 
	 * @param rect
	 *            The rectangle object.
	 */
	public void generateRoute(Rectangle rect) {
		currentRoute = planner.route(unit.getAbsoluteBoundsInTiles(), rect);
	}

	/**
	 * Get a random walkable tile.
	 * 
	 * @return A Rectangle which is walkable.
	 */
	public Rectangle getRandomTile() {
		int boundWidthInTile = unit.getCollisionBounds().width / Tile.TILE_WIDTH;
		int boundHeightInTile = unit.getCollisionBounds().height / Tile.TILE_HEIGHT;
		Rectangle rect = null;
		do {
			rect = new Rectangle(r.nextInt(planner.getMap().getWidth()), r.nextInt(planner.getMap().getHeight()),
					boundWidthInTile, boundHeightInTile);

		} while (planner.getMap().obstacle(rect));
		return rect;
	}

	/**
	 * Walk a route.
	 */
	public void traverseRoute() {
		if (currentRoute.isEmpty()) {
			return;
		}
		Rectangle currentPos = unit.getAbsoluteBoundsInTiles();
		Rectangle moveTo = currentRoute.get(0);
		if (moveTo.equals(currentPos)) {
			currentRoute.remove(0);
		} else {
			Rectangle b = unit.getCollisionBounds();
			int rightMostTile = ((int) Math.floor((unit.getX() + b.x + b.width) / Tile.TILE_WIDTH));
			int bottomMostTile = ((int) Math.floor((unit.getY() + b.y + b.height) / Tile.TILE_HEIGHT));

			if (moveTo.getX() > currentPos.getX()) {
				unit.setXVel(1);
			} else if (rightMostTile > moveTo.getX() + moveTo.getWidth()) {
				unit.setXVel(-1);
			}

			if (moveTo.getY() > currentPos.getY()) {
				unit.setYVel(1);
			} else if (bottomMostTile > moveTo.getY() + moveTo.getHeight()) {
				unit.setYVel(-1);
			}
		}

		// System.err.println(currentRoute);
		// System.err.println("moveTo X: " + moveTo.getX());
		// System.err.println("moveTo Y: " + moveTo.getY());
	}

	/**
	 * Make this unit follow a particular unit.
	 * 
	 * @param u
	 *            The unit to follow.
	 */
	public void followUnit(Unit u) {
		Rectangle targetPos = u.getAbsoluteBoundsInTiles();
		Rectangle unitPos = unit.getAbsoluteBoundsInTiles();

		targetPos.width = unitPos.width;
		targetPos.height = unitPos.height;

		// If the escort position is not the same as last time, we need to
		// calculate the path.
		if (!targetPos.equals(previousFollowPos))
			currentRoute = planner.route(unitPos, targetPos);

		// Remember the last escort position, so next time we know if it's worth
		// recalculating the path.
		previousFollowPos = targetPos;
	}

	/**
	 * Follow and shoot a target.
	 */
	public void followAndShootTarget() {
		if (unit.distance(target) <= SHOOTING_RANGE
				&& gameData.getMap().lineOfSight(unit.getHitbox(), target.getHitbox())) {
			if (timeLastShot + delayTimeMillis < SystemTime.milliTime() || unit.getWeapon() == Unit.MACHINE_GUN) {
				// random double between -maxOffset and maxOffset for the offset
				double offset = (-MAX_OFFSET) + (2 * MAX_OFFSET) * r.nextDouble();
				unit.setDir(unit.angleFromPoint(target.getCenterPoint()) + offset);
				unit.shoot();
				timeLastShot = SystemTime.milliTime();
			}
		} else if (unit.distance(target) <= VISION_RANGE) {
			followUnit(target);
			traverseRoute();
			updateDirection();
		}
	}

	/**
	 * Get a random Rectangle near to a Unit.
	 * 
	 * @param unit
	 *            The unit to get a rectangle for.
	 * @param radius
	 *            The radius to search in.
	 * @return A random rectangle.
	 */
	public Rectangle randomRectNearUnit(Unit unit, int radius) {
		int randomX = r.nextInt(radius * 2) - radius;
		int randomY = r.nextInt(radius * 2) - radius;
		Rectangle bounds = unit.getAbsoluteBoundsInTiles();
		int x = Math.min(gameData.getMap().getWidthInTiles() - 1, Math.max(0, bounds.x + randomX));
		int y = Math.min(gameData.getMap().getHeightInTiles() - 1, Math.max(0, bounds.y + randomY));
		return new Rectangle(x, y, 0, 0);
	}

	/**
	 * Reload and switch weapon.
	 */
	public void reloadAndSwitch() {
		if (unit.isBusy()) {
			return;
		}
		if ((unit.getWeapon() == Unit.PISTOL && unit.getPistol().getBulletsInMag() == 0)
				|| (unit.getWeapon() == Unit.MACHINE_GUN && unit.getMG().getBulletsInMag() == 0)) {
			// Doing this should just reload the currently used weapon that went
			// down to zero
			unit.requestReloadToServer();
		}

		if (unit.getMG().getBulletsInBag() == 0 && unit.getWeapon() == Unit.MACHINE_GUN) {
			// swap to pistol if no bullets in MG
			unit.switchWeaponServer(Unit.PISTOL);
		} else if (unit.getMG().getBulletsInBag() != 0 && unit.getWeapon() == Unit.PISTOL) {
			// swap back to MG if ammo available
			unit.switchWeaponServer(Unit.MACHINE_GUN);
		}
	}

	/**
	 * Get the currently active route.
	 * 
	 * @return The currently active route.
	 */
	public List<Rectangle> getRoute() {
		return currentRoute;
	}

	/**
	 * Carry out clean up for unit death.
	 */
	public void unitDied() {
		this.currentRoute.clear();
	}

}
