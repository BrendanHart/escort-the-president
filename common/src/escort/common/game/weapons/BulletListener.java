package escort.common.game.weapons;

import escort.common.game.entities.units.Unit;

/**
 * A listener for changes occurring on a bullet.
 */
public interface BulletListener {

	/**
	 * When a bullet collides with a unit, the listener should detail what happens
	 * @param unit The unit the bullet collides with.
	 */
	void bulletCollision(Unit unit);

}
