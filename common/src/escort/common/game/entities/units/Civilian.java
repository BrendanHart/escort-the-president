package escort.common.game.entities.units;

import escort.common.game.GameData;
import escort.common.network.Sender;

/**
 * A Civilian implementation of Unit
 */
public class Civilian extends Unit {

	/**
	 * Create a new civilian object
	 * 
	 * @param data
	 *            The game data
	 * @param sender
	 *            The unit sender to send message to the server
	 * @param unitID
	 *            The unique unit id
	 */
	public Civilian(GameData data, Sender sender, int unitID) {
		super(data, sender, unitID, 0, 0, Unit.CIVILIAN_HP, Unit.CIVILIAN_TYPE);
		weaponSlot = NONE;
		respawn();
	}

	@Override
	/**
	 * Civilians cannot target.
	 */
	public boolean canTarget(Unit unit) {
		return false;
	}

	@Override
	/**
	 * Get the starting (initial) health of the Civilian when respawned
	 * 
	 * @return The respawn health of the unit
	 */
	public int getSpawnHealth() {
		return Unit.CIVILIAN_HP;
	}

	@Override
	/**
	 * Get the amount of time the the Civilian has to wait to respawn
	 * 
	 * @return The time for respawning
	 */
	public int getSpawnTime() {
		return Unit.CIVILIAN_SPAWN_TIME;
	}

}
