package escort.common.game.entities.units;

/**
 * An interface to control a unit. This is so that the controller of a unit can
 * be changed at any time (i.e. when a player leaves a game, and is to be
 * replaced by an AI).
 * 
 * @author Ahmed Bhallo
 *
 */
public interface UnitController {

	/**
	 * Control the unit
	 */
	void control();
	
	/**
	 * What to do then the unit has died
	 */
    void unitDied();

}