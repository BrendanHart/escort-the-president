package escort.common.game.entities.units;

import escort.common.game.GameData;
import escort.common.game.entities.units.President;
import escort.common.network.Sender;
import escort.common.network.Message;
import escort.common.game.map.Tile;
import escort.common.game.weapons.BlastShield;
import escort.common.game.weapons.MachineGun;
import escort.common.game.weapons.Pistol;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * An Escort implementation of Unit
 */
public class Escort extends Unit {

	private boolean isFollower = false;

	/**
	 * Create a new escort object
	 * 
	 * @param data
	 *            The game data
	 * @param sender
	 *            The unit sender to send message to the server
	 * @param unitID
	 *            The unique unit id
	 */
	public Escort(GameData data, Sender sender, int unitID) {
		super(data, sender, unitID, 0, 0, Unit.ESCORT_HP, Unit.ESCORT_TYPE);
		this.pistol = new Pistol(this);
		this.mg = new MachineGun(this);
		this.weaponSlot = Unit.MACHINE_GUN; // default to pistol
		this.blastShield = new BlastShield(BlastShield.MAX_HP);
		respawn();
	}

	/**
	 * Gets President if they are nearby.
	 * 
	 * @return The president (if they are nearby).
	 */
	public President getPresidentCollision() {
		SortedMap<Double, President> distanceMap = new TreeMap<Double, President>();
		for (Unit u : getGameData().getUnits().values()) {
			if (u.getUnitType() == Unit.PRESIDENT_TYPE) {
				double d = distance(u);
				if (d < (5 * Tile.TILE_WIDTH))
					distanceMap.put(d, (President) u);
			}
		}

		if (distanceMap.isEmpty())
			return null;

		return distanceMap.get(distanceMap.firstKey());
	}

	/**
	 * Escort requests president to follow
	 */
	public void follow() {
		sendMessage(new Message(Message.UNIT_FOLLOW, new int[] { getUnitID() }, null));
	}

	/**
	 * Escort requests president to unfollow.
	 */
	public void unfollow() {
		sendMessage(new Message(Message.UNIT_UNFOLLOW, new int[] { getUnitID() }, null));
	}

	/**
	 * Change the isFollower (whether the unit has a follower) flag.
	 * 
	 * @param isFollower
	 *            The new value of isFollower.
	 */
	public void setIsFollower(boolean isFollower) {
		this.isFollower = isFollower;
	}

	/**
	 * Gets whether the unit has a follower.
	 * 
	 * @return Whether the unit has a follower or not.
	 */
	public boolean isFollower() {
		return isFollower;
	}

	@Override
	/**
	 * Escorts should only be able to target assassins.
	 * 
	 * @param unit
	 *            The unit to check.
	 * @return Whether the unit can be targeted.
	 */
	public boolean canTarget(Unit unit) {
		return unit.getUnitType() == Unit.ASSASSIN_TYPE;
	}

	@Override
	/**
	 * Get the starting (initial) health of the Escort when respawned
	 * 
	 * @return The respawn health of the unit
	 */
	public int getSpawnHealth() {
		return Unit.ESCORT_HP;
	}

	@Override
	/**
	 * Get the amount of time the the Escort has to wait to respawn
	 * 
	 * @return The time for respawning
	 */
	public int getSpawnTime() {
		return Unit.ESCORT_SPAWN_TIME;
	}

}
