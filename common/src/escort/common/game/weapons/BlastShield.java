package escort.common.game.weapons;

/**
 * The blast shield that can reduce grenade damage and bullet damage
 * 
 * @author Dr_Deano
 *
 */
public class BlastShield {

	public static final int MAX_HP = 250;
	private int hp;

	public BlastShield(int hp) {
		this.hp = hp;
	}

	/**
	 * Reduce the hit points of the shield from either bullets or grenade damage
	 * and returns true if the shield has taken the full damage it can take
	 * 
	 * @param d
	 *            The damage that the shield will take
	 * @return Returns true if the shield is broken else false
	 */
	public void takeDamage(int d) {
		if (hp < d) {
			hp = 0;
		} else {
			hp -= d;
		}
	}

	/**
	 * Return whether the shield is broken
	 * 
	 * @return Whether the shield is broken
	 */
	public boolean isBroken() {
		return hp <= 0;
	}

	public int getHP() {
		return hp;
	}

	public void setHP(int hp) {
		this.hp = hp;
	}
}