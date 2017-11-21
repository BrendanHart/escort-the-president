package escort.common.powerups;

import escort.common.game.entities.units.Unit;

/**
 * A PowerUp that gives extra grenades.
 * @author James Birch
 * @author Brendan Hart
 */
public class ExtraGrenades extends PowerUp {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8441506318685119698L;
	
	public static final int EXTRA_GRENADES = 2;
	
	/**
	 * Create an ExtraGrenades PowerUp object.
	 * This gives extra grenades.
	 * @param x X position of the PowerUp.
	 * @param y Y position of the PowerUp.
	 */
	public ExtraGrenades(double x, double y) {
		super(PowerUp.EXTRA_GRENADES, x, y);
	}
	
	/**
	 * Detail what to do when the PowerUp is picked up.
	 * Give extra grenades.
     * @param pickedUpBy The unit that triggered the powerup.
     * @return If the action was carried out successfully.
	 */
	public boolean pickup(Unit pickedUpBy) {
        if(!super.isActive())
            return false;

        if(pickedUpBy.getUnitType() == Unit.CIVILIAN_TYPE || pickedUpBy.getUnitType() == Unit.PRESIDENT_TYPE
				|| pickedUpBy.getGrenadesLeft() >= Unit.MAX_NUM_GRENADES)
            return false;
        
        super.setPickedUpTime();
		pickedUpBy.setNumberOfGrenades(pickedUpBy.getGrenadesLeft() + EXTRA_GRENADES);
        return true;
	}
}
