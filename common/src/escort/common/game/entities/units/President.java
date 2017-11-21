package escort.common.game.entities.units;

import escort.common.game.GameData;
import escort.common.network.Sender;

/**
 * A President implementation of Unit
 */
public class President extends Unit {

	private Escort lastEscort;
	private boolean stillFollowing;

	/**
	 * Create a president.
	 * 
	 * @param data
	 *            The current game data.
	 * @param sender
	 *            A sender for message handling.
	 * @param unitID
	 *            The unit ID of the President in a game.
	 */
	public President(GameData data, Sender sender, int unitID) {
		super(data, sender, unitID, 0, 0, Unit.PRESIDENT_HP, Unit.PRESIDENT_TYPE);
		stillFollowing = false;
		weaponSlot = NONE;
		respawn();
	}

	/**
	 * Get the escort last followed.
	 * 
	 * @return The unit who was last followed.
	 */
	public Escort getFollowing() {
		return lastEscort;
	}

	/**
	 * Follow an escort.
	 * 
	 * @param escort
	 *            The escort to follow.
	 */
	public void follow(Escort escort) {
		this.lastEscort = escort;
		this.stillFollowing = true;
	}

	/**
	 * Is the president currently following?
	 * 
	 * @return Whether the president is currently following or not.
	 */
	public boolean isFollowing() {
		return stillFollowing;
	}

	/**
	 * Is the president following a particular escort?
	 * 
	 * @param e
	 *            The escort to check.
	 * @return Whether the escort in question is being followed.
	 */
	public boolean isFollowing(Escort e) {
		if (lastEscort == null) {
			return false;
		}
		return (lastEscort.getUnitID() == e.getUnitID()) && stillFollowing;
	}

	/**
	 * Unfollow from an escort.
	 */
	public void unfollow() {
		stillFollowing = false;
	}

	@Override
	/**
	 * President should not be able to target.
	 */
	public boolean canTarget(Unit unit) {
		return false;
	}

	@Override
	/**
	 * Get the starting (initial) health of the President when respawned
	 * 
	 * @return The respawn health of the unit
	 */
	public int getSpawnHealth() {
		return Unit.PRESIDENT_HP;
	}

	@Override
	/**
	 * Get the amount of time the the President has to wait to respawn
	 * (Currently, President should not actually respawn, but could be used in a
	 * future game mode)
	 * 
	 * @return The time for respawning
	 */
	public int getSpawnTime() {
		return -1;
	}

}
