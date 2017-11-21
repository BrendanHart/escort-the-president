package escort.common.game.weapons;

import java.awt.Point;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import escort.common.game.entities.units.Unit;
import escort.common.systime.SystemTime;

/**
 * A general class describing for describing weapons.
 * 
 * @author James Birch
 */
public abstract class Gun {

	private int rateFire; // default fire rate
	private int fullMag; // reference for size of full magazine
	private int magBullets; // number of bullets in magazine
	private int bulletsInBag; // total number of bullets
	private int reloadSpeed; // reload speed in milliseconds
	protected Unit owner; // unit carrying the gun
	private long timeLastShot; // milliseconds since last shot was fired
	private Queue<Bullet> firedBullets = new ConcurrentLinkedQueue<>(); // track
																		// bullets
																		// fired

	/**
	 * Create a Gun object.
	 * 
	 * @param rateFire
	 *            Rate of fire, number of bullets per second. Negative int means
	 *            single shot fire.
	 * @param bulletsInMag
	 *            Number of bullets in a magazine.
	 * @param reloadSpeed
	 *            Reload speed.
	 * @param owner
	 *            The unit holding the gun.
	 */
	public Gun(int rateFire, int bulletsInMag, int reloadSpeed, Unit owner) {
		this.rateFire = rateFire;
		this.fullMag = bulletsInMag;
		magBullets = 0;
		bulletsInBag = 0;
		this.reloadSpeed = reloadSpeed;
		this.owner = owner;
		this.timeLastShot = SystemTime.milliTime();
	}

	/**
	 * A gun being shot. Checks if it can shoot first then requests an ID.
	 */
	public void shoot() {
		if (canShoot()) {
			timeLastShot = SystemTime.milliTime();
			requestID();
		}
	}

	/**
	 * Can the gun be shot? This is to stop the gun being fired too often.
	 * @return Whether the gun can be shot or not.
	 */
	public boolean canShoot() {
		long diff = SystemTime.milliTime() - timeLastShot;
		double rate = (1.0 / rateFire) * 1000;

		if (diff < rate || getBulletsInMag() == 0) {
			return false;
		}
		return true;
	}

	/**
	 * Request a bullet ID. Should be implemented by any subclasses.
	 */
	public abstract void requestID();

	/**
	 * Firing a gun.
	 * 
	 * @param bullet The bullet to fire.
	 */
	public void fire(Bullet bullet) {
		Point ownerCenter = this.owner.getCenterPoint();
		int baseOffset = 20;
		ownerCenter.x += baseOffset * Math.sin(owner.getDir());
		bullet.setX(ownerCenter.x);
		bullet.setY(ownerCenter.y);
		bullet.fireBullet();
		timeLastShot = SystemTime.milliTime();
		firedBullets.add(bullet);
		magBullets--;
	}

	/**
	 * Reloading a gun.
	 */
	public void reload() {
		if (bulletsInBag == 0) {
			return;
		}

		bulletsInBag += magBullets;

		if (bulletsInBag >= fullMag) {
			magBullets = fullMag;
			bulletsInBag -= fullMag;
		} else {
			// totalBullets < fullMag -> put all remaining bullets in last
			// magazine
			magBullets = bulletsInBag;
			bulletsInBag = 0;
		}
	}

	// Getters and setters

	/**
	 * Get rate of fire.
	 * 
	 * @return Rate of fire (int).
	 */
	public int getRateFire() {
		return this.rateFire;
	}

	/**
	 * Get current magazine size.
	 * 
	 * @return Magazine size (int).
	 */
	public int getBulletsInMag() {
		return this.magBullets;
	}

	/**
	 * Get number of bullets available.
	 * 
	 * @return Number of bullets (int).
	 */
	public int getBulletsInBag() {
		return this.bulletsInBag;
	}

	/**
	 * Returns the amount of bullets that are in a full magazine.
	 * 
	 * @return The full amount of bullets.
	 */
	public int getFullMag() {
		return this.fullMag;
	}

	/**
	 * Get reload speed.
	 * 
	 * @return Reload speed (int).
	 */
	public int getReloadSpeed() {
		return this.reloadSpeed;
	}

	/**
	 * Return the time when the gun was last shot.
	 * 
	 * @return The time when the gun was last shot in nanoseconds.
	 */
	public long getTimeLastShot() {
		return this.timeLastShot;
	}

	/**
	 * Change the rate of fire.
	 * 
	 * @param newRF
	 *            New rate of fire (int).
	 */
	public void setRateFire(int newRF) {
		this.rateFire = newRF;
	}

	/**
	 * Change the number of bullets in the magazine.
	 * 
	 * @param mb
	 *            The new number of bullets in the magazine (int).
	 */
	public void setMagBullets(int mb) {
		this.magBullets = mb;
	}

	public void addClip(int amount) {
		setTotalBullets(bulletsInBag + amount * fullMag);
	}

	/**
	 * Change the number of bullets that a full magazine has.
	 * 
	 * @param fm
	 *            The new number of bullets that a full magazine has (int).
	 */
	public void setFullMag(int fm) {
		this.fullMag = fm;
	}

	/**
	 * Change the reload speed of the gun.
	 * 
	 * @param newRS
	 *            New reload speed (int).
	 */
	public void setReloadSpeed(int newRS) {
		this.reloadSpeed = newRS;
	}

	/**
	 * Get a queue of the fired bullets by the gun.
	 * @return A queue of fired bullets.
	 */
	public Queue<Bullet> getFiredBullets() {
		return firedBullets;
	}

	/**
	 * Set the total bullets a gun can hold.
	 * @param totalBullets The new total bullets.
	 */
	public void setTotalBullets(int totalBullets) {
		this.bulletsInBag = totalBullets;
	}
}