package escort.common.powerups;

import escort.common.game.entities.units.Unit;
import escort.common.game.weapons.MachineGun;

/**
 * A PowerUp that gives extra MG bullets.
 * @author James Birch
 */
public class ExtraMags extends PowerUp {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8726585942012198966L;
	public static final int NUM_MAGS = 3;
	
	/**
	 * Create an ExtraMags PowerUp object.
	 * This gives extra MG bullets.
	 * @param x X position of the PowerUp.
	 * @param y Y position of the PowerUp.
	 */
	public ExtraMags(double x, double y) {
		super(PowerUp.EXTRA_MAGS, x, y);
	}
	
	/**
	 * Detail what to do when the PowerUp is picked up.
	 * Give extra MG bullets.
     * @param pickedUpBy The unit that triggered the powerup.
     * @return If the action was carried out successfully.
	 */
	public boolean pickup(Unit pickedUpBy) {
        if(pickedUpBy.getUnitType() == Unit.CIVILIAN_TYPE || pickedUpBy.getUnitType() == Unit.PRESIDENT_TYPE
				|| pickedUpBy.getMG().getBulletsInBag() >= MachineGun.MAX_BULLETS_IN_BAG)
            return false;

        if(!super.isActive())
            return false;

        super.setPickedUpTime();
		MachineGun mg = pickedUpBy.getMG();
		mg.addClip(NUM_MAGS);
		return true;
	}
}
