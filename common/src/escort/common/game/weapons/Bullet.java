package escort.common.game.weapons;

import java.awt.geom.Line2D;
import java.util.HashSet;
import java.util.Set;

import escort.common.game.GameData;
import escort.common.game.entities.Mob;
import escort.common.game.entities.units.Unit;

/**
 * A class describing a Bullet object.
 * 
 * @author James Birch
 */
public class Bullet extends Mob {

	private final Set<BulletListener> listeners = new HashSet<>();

	public static final int BULLET_WIDTH = 5;
	public static final int BULLET_HEIGHT = 5;
	public static final int BULLET_SPEED = 5;
	private int bulletDamage;
	private Unit shooter; // who fired the bullet
	private double dir;
	private final double xStart;
	private final double yStart;

	/**
	 * Appearance of a bullet
	 * 
	 * @param data
	 *            The game data.
	 * @param shooter
	 *            The owner of the gun.
	 * @param bulletDamage
	 *            Damage that the bullet deals.
	 */
	public Bullet(GameData data, Unit shooter, int bulletDamage) {
		super(data, shooter.getX(), shooter.getY(), BULLET_WIDTH, BULLET_HEIGHT);
		this.shooter = shooter;
		this.bulletDamage = bulletDamage;
		setSpeed(BULLET_SPEED);
		setSlideFactor(1); // travel indefinitely
		xStart = getX();
		yStart = getY();
	}

	/**
	 * Custom update method for bullet. Should check whether it should deal damage or not.
	 */
	@Override
	public void update() {
		super.update();
		Line2D.Double line = new Line2D.Double(getxStart(), getyStart(), getX(), getY());
		for (Unit unit : getGameData().getUnits().values()) {
			if (unit.isDead() || unit.getUnitID() == shooter.getUnitID()
					|| (!shooter.canTarget(unit) && unit.getUnitType() != Unit.CIVILIAN_TYPE)) {
				continue;
			}
			if (unit.getHitbox().intersectsLine(line)) {
				listeners.forEach(listener -> listener.bulletCollision(unit));
				bulletEnd();
				return;
			}
		}
	}

	/**
	 * What to do when a bullet has been fired.
	 */
	public void fireBullet() {
		dir = this.shooter.getDir();
		setXVel(Math.sin(dir));
		setYVel(-Math.cos(dir));
	}

	/**
	 * What to do when a bullet reaches the end of its path. E.g. if it hits
	 * something.
	 */
	public void bulletEnd() {
		this.shooter.bulletDeleted(this);
	}

	/**
	 * Add a bullet listener.
	 * @param listener The bullet listener.
	 */
	public void addListener(BulletListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a bullet listener.
	 * @param listener The bullet listener to remove.
	 */
	public void removeListener(BulletListener listener) {
		listeners.remove(listener);
	}

	// Getters and setters

	/**
	 * Get bullet damage.
	 * 
	 * @return Bullet damage (int).
	 */
	public int getBulletDamage() {
		return this.bulletDamage;
	}

	/**
	 * Get the player that fired.
	 *
	 * @return The player that fired.
	 */
	public Unit getFiredBy() {
		return this.shooter;
	}

	/**
	 * Change bullet damage.
	 * 
	 * @param newDamage
	 *            New bullet damage.
	 */
	public void setBulletDamage(int newDamage) {
		this.bulletDamage = newDamage;
	}

	/**
	 * Set the name of the bullet
	 * 
	 * @param name
	 *            The name of the player
	 */
	public void setFiredBy(Unit name) {
		this.shooter = name;
	}

	/**
	 * Stop the bullet when it has hit something.
	 */
	private void hitSomething() {
		setXVel(0);
		setYVel(0);
		bulletEnd();
	}

	/**
	 * What to do on a horizontal collision.
	 */
	@Override
	public void horizontalCollision() {
		hitSomething();
	}

	/**
	 * What to do on a vertical collision.
	 */
	@Override
	public void verticalCollision() {
		hitSomething();
	}

	/**
	 * Get the starting x-coordinate.
	 * @return The y-coordinate.
	 */
	public double getxStart() {
		return xStart;
	}

	/**
	 * Get the starting y-coordinate.
	 * @return The y-coordinate.
	 */
	public double getyStart() {
		return yStart;
	}

}
