package escort.common.game.entities.units;

import escort.common.game.GameData;
import escort.common.game.weapons.MachineGun;
import escort.common.game.weapons.Pistol;
import escort.common.network.Sender;

/**
 * A Police implementation of Unit
 */
public class Police extends Unit {

	/**
	 * Create a new police object
	 * 
	 * @param data
	 *            The game data
	 * @param sender
	 *            The unit sender to send message to the server
	 * @param unitID
	 *            The unique unit id
	 */
	public Police(GameData data, Sender sender, int unitID) {
		super(data, sender, unitID, 0, 0, Unit.POLICE_HP, Unit.POLICE_TYPE);
		this.pistol = new Pistol(this);
		this.mg = new MachineGun(this);
		this.weaponSlot = Unit.MACHINE_GUN;
		respawn();
	}

	@Override
	/**
	 * Police can only target assassins.
	 * 
	 * @param unit
	 *            The unit to check
	 * @return Whether the unit can be targeted.
	 */
	public boolean canTarget(Unit unit) {
		return unit.getUnitType() == Unit.ASSASSIN_TYPE;
	}

	@Override
	/**
	 * Get the starting (initial) health of the Police when respawned
	 * 
	 * @return The respawn health of the unit
	 */
	public int getSpawnHealth() {
		return Unit.POLICE_HP;
	}

	@Override
	/**
	 * Get the amount of time the the Police has to wait to respawn
	 * 
	 * @return The time for respawning
	 */
	public int getSpawnTime() {
		return Unit.POLICE_SPAWN_TIME;
	}

}
