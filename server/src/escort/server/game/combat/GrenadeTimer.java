package escort.server.game.combat;

import escort.common.game.weapons.Grenade;
import escort.common.systime.SystemTime;
import escort.server.game.Game;

/**
 * Wraps a grenade and waits for the explosion fuse.
 * 
 * @author Edward Dean
 *
 */
public class GrenadeTimer {

	/**
	 * The fuse length of the grenade in milliseconds
	 */
	public final static int FUSE_TIME = 5000;
	private final long msSinceCook;

	private final Game game;
	private final Grenade grenade;

	/**
	 * Create a new grenade timer object
	 * 
	 * @param game
	 *            The game
	 * @param grenade
	 *            The grenade
	 */
	public GrenadeTimer(Game game, Grenade grenade) {
		this.game = game;
		this.grenade = grenade;
		msSinceCook = SystemTime.milliTime();
	}

	/**
	 * Update the grenade timer to check if the grenade has exploded.
	 */
	public void update() {
		if (SystemTime.milliTime() - msSinceCook >= FUSE_TIME) {
			exploded();
		}
	}

	/**
	 * Explode the grenade
	 */
	private void exploded() {
		game.grenadeExploded(grenade);
	}
}