package escort.common.game.weapons;

import escort.common.game.entities.units.Unit;
import escort.common.network.Message;

/**
 * A class describing a Pistol object.
 * 
 * @author James Birch
 */
public class Pistol extends Gun {

	public static final int BULLET_DAMAGE = 40;

	/**
	 * Create a Pistol object. Note that -1 for rate of fire and number of mags
	 * means that it is single shot and has infinite ammo.
	 * 
	 * @param owner
	 *            The unit carrying the gun.
	 */
	public Pistol(Unit owner) {
		// Rate of fire, magazine bullets, total bullets,
		// reload speed, and then owner of gun.
		super(3, 8, 1500, owner);
	}

	@Override
	/**
	 * Request a bullet ID from the server.
	 */
	public void reload() {
		setMagBullets(getFullMag());
	}

	@Override
	/**
	 * Adding a magazine for this type of gun.
	 * @param amount The number of magazines to add.
	 */
	public void requestID() {
		owner.sendMessage(new Message(Message.REQUEST_PISTOL_BULLET, new int[] { owner.getUnitID() }, null));
	}
}