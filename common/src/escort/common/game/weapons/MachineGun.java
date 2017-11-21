package escort.common.game.weapons;

import escort.common.game.entities.units.Unit;
import escort.common.network.Message;

/**
 * A class describing a Machine Gun object.
 * 
 * @author James Birch
 */
public class MachineGun extends Gun {

	public static final int BULLET_DAMAGE = 20;
	public static final int MAX_BULLETS_IN_BAG = 192;


	/**
	 * Create a Machine Gun object.
	 * 
	 * @param owner
	 *            The unit carrying the gun.
	 */
	public MachineGun(Unit owner) {
		// Rate of fire, magazine bullets, total bullets,
		// reload speed, and then owner of gun.
		super(5, 12, 2500, owner);
	}

	@Override
	/**
	 * Request a bullet ID from the server.
	 */
	public void requestID() {
		owner.sendMessage(new Message(Message.REQUEST_MG_BULLET, new int[] { owner.getUnitID() }, null));
	}

	@Override
	/**
	 * Adding a magazine for this type of gun.
	 * @param amount The number of magazines to add.
	 */
	public void addClip(int amount) {
		int bullets = getBulletsInBag() + amount * getFullMag();
		if(bullets < MAX_BULLETS_IN_BAG) {
			setTotalBullets(bullets);
		} else {
			setTotalBullets(MAX_BULLETS_IN_BAG);
		}
	}
}