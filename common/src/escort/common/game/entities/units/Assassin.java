package escort.common.game.entities.units;

import escort.common.game.GameData;
import escort.common.game.weapons.BlastShield;
import escort.common.game.weapons.MachineGun;
import escort.common.game.weapons.Pistol;
import escort.common.network.Sender;

/**
 * An Assassin implementation of Unit
 *
 */
public class Assassin extends Unit {

	/**
	 * Create a new assassin object
	 * 
	 * @param data
	 *            The game data
	 * @param sender
	 *            The unit sender to send message to the server
	 * @param unitID
	 *            The unique unit id
	 */
	public Assassin(GameData data, Sender sender, int unitID) {
		super(data, sender, unitID, 0, 0, Unit.ASSASSIN_HP, Unit.ASSASSIN_TYPE);
		this.pistol = new Pistol(this);
		this.mg = new MachineGun(this);
		this.weaponSlot = Unit.MACHINE_GUN; // default to pistol
		this.blastShield = new BlastShield(BlastShield.MAX_HP);
		respawn();
	}

	@Override
	/**
	 * Assassins should not be able to target assassins nor civilians.
	 */
	public boolean canTarget(Unit unit) {
		return unit.getUnitType() != Unit.ASSASSIN_TYPE && unit.getUnitType() != Unit.CIVILIAN_TYPE;
	}

	@Override
	/**
	 * Get the starting (initial) health of the Assassin when respawned
	 * 
	 * @return The respawn health of the unit
	 */
	public int getSpawnHealth() {
		return Unit.ASSASSIN_HP;
	}

	@Override
	/**
	 * Get the amount of time the the Assassin has to wait to respawn
	 * 
	 * @return The time for respawning
	 */
	public int getSpawnTime() {
		return Unit.ASSASSIN_SPAWN_TIME;
	}

}
