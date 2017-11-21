package escort.common.powerups;

import escort.common.game.entities.units.Unit;

/**
 * A PowerUp that replenishes health.
 * @author James Birch
 */
public class ReplenishHealth extends PowerUp {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8472933441518876126L;
	public static final int ADD_HEALTH = 50;
	
	/**
	 * Create an ReplenishHealth PowerUp object.
	 * This completely refills health.
	 * @param x X position of the PowerUp.
	 * @param y Y position of the PowerUp.
	 */
	public ReplenishHealth(double x, double y) {
		super(PowerUp.REPLENISH_HEALTH, x, y);
	}
	
	/**
	 * Detail what to do when the PowerUp is picked up.
	 * Refill health.
     * @param pickedUpBy The unit that triggered the powerup.
     * @return If the action was carried out successfully.
	 */
	public boolean pickup(Unit pickedUpBy) {

        if(pickedUpBy.getUnitType() == Unit.CIVILIAN_TYPE)
            return false;

        if(!super.isActive())
            return false;
        
        if(pickedUpBy.getHP() == pickedUpBy.getMaxHP())
            return false;

        super.setPickedUpTime();
		pickedUpBy.setHP(Math.min(pickedUpBy.getMaxHP(), pickedUpBy.getHP() + ADD_HEALTH));
        return true;
	}

}
