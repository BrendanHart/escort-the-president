package escort.common.game.entities.units;

import java.io.Serializable;

/**
 * Stores all information on a certain unit.
 * 
 * @author Brendan Hart
 *
 */
public class UnitInfo implements Serializable {

	private static final long serialVersionUID = 8176445211131574117L;

	public final int unitType;
	public final int unitID;
	public final String username;
	public final double x;
	public final double y;
	public final double dir;
	public final double xVel;
	public final double yVel;
	public final int hpLeft;
	public final int weaponSlot;
	public final int grenadesLeft;
	public final int mgBulletsInMag;
	public final int mgBulletsInBag;

	/**
	 * Create information relating to a unit.
	 * 
	 * @param unit
	 *            The unit to create information about.
	 */
	public UnitInfo(Unit unit) {
		unitType = unit.getUnitType();
		unitID = unit.getUnitID();
		username = unit.getUsername();
		x = unit.getX();
		y = unit.getY();
		dir = unit.getDir();
		xVel = unit.getXVel();
		yVel = unit.getYVel();
		hpLeft = unit.getHP();
		weaponSlot = unit.getWeapon();
		grenadesLeft = unit.getGrenadesLeft();
		if (unit.getMG() != null) {
			mgBulletsInMag = unit.getMG().getBulletsInMag();
			mgBulletsInBag = unit.getMG().getBulletsInBag();
		} else {
			mgBulletsInMag = 0;
			mgBulletsInBag = 0;
		}
	}
}
