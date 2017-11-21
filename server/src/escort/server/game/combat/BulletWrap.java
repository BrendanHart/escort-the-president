package escort.server.game.combat;

import escort.common.game.entities.units.Unit;
import escort.common.game.weapons.Bullet;
import escort.common.game.weapons.BulletListener;
import escort.server.game.Game;

/**
 * Implementation of bullet listener that detects when a collision has been made
 * with a unit
 * 
 * @author James Birch
 *
 */
public class BulletWrap implements BulletListener {

	private final Game game;
	private final Bullet bullet;

	/**
	 * Instantiates a new bullet wrap object
	 * 
	 * @param game
	 *            The game object
	 * @param bullet
	 *            The bullet to be observed
	 */
	public BulletWrap(Game game, Bullet bullet) {
		this.game = game;
		this.bullet = bullet;
	}

	/**
	 * A bullet has hit a unit. Notifies the game.
	 */
	@Override
	public void bulletCollision(Unit unit) {
		game.unitShot(unit, bullet.getBulletDamage());
	}

}
