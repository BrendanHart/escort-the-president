package escort.common.powerups;

import java.io.Serializable;

import escort.common.game.entities.Entity;
import escort.common.game.entities.units.Unit;
import escort.common.game.map.Tile;

/**
 * A superclass for PowerUp varieties.
 * @author James Birch
 * @author Brendan Hart
 */
public abstract class PowerUp extends Entity implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -8293051805174754601L;
	public static final int EXTRA_MAGS = 0;
    public static final int EXTRA_GRENADES = 1;
    public static final int REPLENISH_HEALTH = 2;

    private int id;
    private long lastPickedUpTime;
    private final int type;
	private int cooldown = 12; // 5 seconds
	
	/**
	 * Create a PowerUp object.
	 * @param x X position of the PowerUp.
	 * @param y Y position of the PowerUp.
	 */
	public PowerUp(int type, double x, double y) {
		super(x, y, Tile.TILE_WIDTH, Tile.TILE_HEIGHT, null);
        this.type = type;
        lastPickedUpTime = -1;
	}

    public void setID(int id) {
        this.id = id;
    }

    public int getID() {
        return id;
    }
	
	/**
	 * What to do when a PowerUp is picked up.
     * @param pickedUpBy The unit that triggered the powerup.
     * @return If the action was carried out successfully.
	 */
	public abstract boolean pickup(Unit pickedUpBy);

    /**
     * Set the last picked up time
     */
    public void setPickedUpTime() {
        this.lastPickedUpTime = System.nanoTime();
    }
	
	/**
	 * Is the PowerUp active?
	 * @return Whether the PowerUp is active or not.
	 */
	public boolean isActive() {
        return ((double)System.nanoTime() - lastPickedUpTime) / 1000000000.0 >= cooldown;
	}

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public String toString() {
        return "PowerUp - ID:"+id+" Type:" + type +" X:"+getX()+" Y:" + getY()+" W:" + getWidth() + " H:"+getHeight();
    }

    public int getCooldown() {
        return cooldown;
    }
    
    public int getType() {
		return type;
	}
	
}
