package escort.common.game.weapons;

import escort.common.game.GameData;
import escort.common.game.entities.Mob;
import escort.common.game.entities.units.Unit;

/**
 * A class describing the behaviour of a grenade.
 * 
 * @author Edward Dean
 *
 */
public class Grenade extends Mob {

	/**
	 * The area of effect the grenade damage has (in pixels)
	 */
	public final static double GRENADE_EFFECT_AREA = 200;

	/**
	 * The maximum damage that the grenade can do
	 */
	public final static int GRENADE_DAMAGE = 50;

	/**
	 * The air drag of the grenade when thrown
	 */
	public final static double AIR_DRAG_MULTIPLIER = 0.97;

	private final static double COLLISION_EXCHANGE = 0.5;

	/**
	 * The width of the grenade
	 */
	public final static int WIDTH = 18;

	/**
	 * The height of the grenade
	 */
	public final static int HEIGHT = 18;

	/**
	 * The speed of the grenade when thrown
	 */
	public final static int VELOCITY = 10;

	private final Unit thrower;
	private final int grenadeID;

	/**
	 * Create a grenade object
	 * 
	 * @param data
	 *            The game data that contains the map and units of the game
	 * @param x
	 *            The initial x position of the grenade
	 * @param y
	 *            The initial y position of the grenade
	 * @param thrower
	 *            The unit that owns the grenade
	 */
	public Grenade(GameData data, int grenadeID, double x, double y, Unit thrower) {
		super(data, x, y, WIDTH, HEIGHT);
		this.grenadeID = grenadeID;
		this.thrower = thrower;
		setSpeed(VELOCITY);
		setSlideFactor(AIR_DRAG_MULTIPLIER);
	}

	/**
	 * Release the grenade after being held so now is being thrown
	 */
	public void release() {
		double dir = thrower.getDir();
		setXVel(Math.sin(dir));
		setYVel(-Math.cos(dir));
	}

	/**
	 * Get the thrower of the grenade
	 * 
	 * @return The thrower of the grenade
	 */
	public Unit getThrower() {
		return thrower;
	}

	/**
	 * Get the grenade ID
	 * 
	 * @return Grenade ID
	 */
	public int getGrenadeID() {
		return grenadeID;
	}

	@Override
	/**
	 * What to do when a grenade collides horizontally.
	 */
	public void horizontalCollision() {
		setXVel(getXVel() * -(1 - COLLISION_EXCHANGE));
	}

	@Override
	/**
	 * What to do when a grenade collides vertically.
	 */
	public void verticalCollision() {
		setYVel(getYVel() * -(1 - COLLISION_EXCHANGE));
	}
}