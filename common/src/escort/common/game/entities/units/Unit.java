package escort.common.game.entities.units;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import escort.common.game.GameData;
import escort.common.game.entities.Mob;
import escort.common.game.weapons.BlastShield;
import escort.common.game.weapons.Bullet;
import escort.common.game.weapons.Grenade;
import escort.common.game.weapons.MachineGun;
import escort.common.game.weapons.Pistol;
import escort.common.network.Message;
import escort.common.network.Sender;
import escort.common.systime.SystemTime;

/**
 * A unit
 * 
 * @author Ahmed Bhallo (main)
 * @author James Birch (additions)
 * @author Dr_Deano (additions)
 *
 */
public abstract class Unit extends Mob {

	// Unit types
	public static final int ESCORT_TYPE = 1;
	public static final int ASSASSIN_TYPE = 2;
	public static final int PRESIDENT_TYPE = 3;
	public static final int CIVILIAN_TYPE = 4;
	public static final int POLICE_TYPE = 5;

	// Spawn times (in seconds)
	public static final int ESCORT_SPAWN_TIME = 3;
	public static final int ASSASSIN_SPAWN_TIME = 5;
	public static final int CIVILIAN_SPAWN_TIME = 5;
	public static final int POLICE_SPAWN_TIME = 3;

	// HP
	public static final int ESCORT_HP = 100;
	public static final int ASSASSIN_HP = 100;
	public static final int CIVILIAN_HP = 75;
	public static final int POLICE_HP = 75;
	public static final int PRESIDENT_HP = 800;

	public static final int UNIT_WIDTH = 48;
	public static final int UNIT_HEIGHT = 66;

	public static final int UNIT_HEAD_WIDTH = 48;
	public static final int UNIT_HEAD_HEIGHT = 36;

	public static final int UNIT_TORSO_WIDTH = 24;
	public static final int UNIT_TORSO_HEIGHT = 21;

	public static final int UNIT_LEGS_WIDTH = 24;
	public static final int UNIT_LEGS_HEIGHT = 9;

	public static final int NONE = 0;
	public static final int MACHINE_GUN = 1;
	public static final int PISTOL = 2;
	public static final int SHIELD = 3;

	private final int unitID;

	protected int weaponSlot = NONE; // toggle for which weapon

	protected Pistol pistol;
	protected MachineGun mg;
	protected BlastShield blastShield = null;

	private UnitController controller;

	private double dir;

	public static final int MAX_NUM_GRENADES = 6;
	private int grenadesLeft = 0;
	private Grenade heldGrenade = null;

	private boolean holdingGrenade = false;

	private final Queue<Grenade> airborneGrenades = new ConcurrentLinkedQueue<Grenade>();

	private int hp;
	private int maxHP;
	private final int unitType;
	protected Sender sender;

	private String username;

	private long msSinceBusy = SystemTime.milliTime(); // time since last
	// busy
	private boolean isReloadPistol = false; // prevent shooting and throwing
											// grenades
	// while doing an action (like
	// reloading)
	private boolean isReloadMG = false;

	private long timeSinceSent = 0;
	
	private long lastMoveFrame = 0;

	/**
	 * Create a unit object
	 * 
	 * @param data
	 *            The game data
	 * @param sender
	 *            The players sender to send messages to the server
	 * @param unitID
	 *            The unit id (unique)
	 * @param x
	 *            The x position of the unit
	 * @param y
	 *            The y position of the unit
	 * @param hp
	 *            The starting hit point of the unit
	 * @param unitType
	 *            The type of unit (police, assassin, escort, ...)
	 */
	public Unit(GameData data, Sender sender, int unitID, int x, int y, int hp, int unitType) {
		super(data, x, y, UNIT_WIDTH, UNIT_HEIGHT);
		this.unitID = unitID;
		this.sender = sender;
		this.hp = hp;
		this.maxHP = hp;
		this.unitType = unitType;
	}

	@Override
	/**
	 * Detail how a unit should be updated.
	 */
	public void update() {
		super.update();

		if (controller != null) {
			controller.control();
		}

		if (heldGrenade != null) {
			double d = dir - Math.PI / 2;
			int offsetBase = 24;
			int xOffset = (int) (offsetBase * Math.cos(d));
			int yOffset = (int) (offsetBase * Math.sin(d));
			heldGrenade.setX(getCenterPoint().x - heldGrenade.getWidth() / 2 + xOffset);
			heldGrenade.setY(getCenterPoint().y - heldGrenade.getHeight() / 2 + yOffset);
			heldGrenade.update();
		}

		for (Grenade airborne : airborneGrenades) {
			airborne.update();
		}

		if (pistol != null) {
			for (Bullet bullet : pistol.getFiredBullets()) {
				bullet.update();
			}
		}

		if (mg != null) {
			for (Bullet bullet : mg.getFiredBullets()) {
				bullet.update();
			}
		}

		delayReload(Unit.PISTOL);
		delayReload(Unit.MACHINE_GUN);

		if (SystemTime.milliTime() - timeSinceSent >= 50) {
			sendMessage(new Message(Message.UNIT_MOVED, new int[] { getUnitID(),0 }, null,
					new double[] { getX(), getY(), getDir(), getXVel(), getYVel()}));
			timeSinceSent = SystemTime.milliTime();
		}
	}

	/**
	 * Delay the reloading of a weapon, so takes time to reload
	 * 
	 * @param weapon
	 *            The weapon to reload
	 */
	private void delayReload(int weapon) {
		switch (weapon) {
		case Unit.PISTOL:
			if (isReloadPistol) {
				if (SystemTime.milliTime() - msSinceBusy >= pistol.getReloadSpeed()) {
					isReloadPistol = false;
					pistol.reload();
				}
			}
		case Unit.MACHINE_GUN:
			if (isReloadMG) {
				if (SystemTime.milliTime() - msSinceBusy >= mg.getReloadSpeed()) {
					isReloadMG = false;
					mg.reload();
				}
			}
		}
	}

	/**
	 * Get the units blast shield (can be null)
	 * 
	 * @return The blast shield
	 */
	public BlastShield getBlastShield() {
		return blastShield;
	}

	/**
	 * Remove a bullet from the list of fired bullets.
	 * 
	 * @param bullet
	 *            The bullet to remove.
	 */
	public void bulletDeleted(Bullet bullet) {
		// remove from both lists because the bullet is guaranteed to
		// exist in only one of them
		pistol.getFiredBullets().remove(bullet);
		mg.getFiredBullets().remove(bullet);
	}

	/**
	 * Get the current held grenade of the unit (can be null)
	 * 
	 * @return The current held grenade
	 */
	public Grenade getHeldGrenade() {
		return heldGrenade;
	}

	/**
	 * Get all the grenades that have been thrown
	 * 
	 * @return Thrown grenades
	 */
	public Collection<Grenade> getAirborneGrenades() {
		return airborneGrenades;
	}

	/**
	 * Whether the unit is currently holding a grenade
	 * 
	 * @return If the unit is holding a grenade the return true else false
	 */
	public boolean hasHeldGrenade() {
		return holdingGrenade;
	}

	/**
	 * Reduce the hit point of the unit
	 * 
	 * @param damage
	 *            The amount to reduce the hit point by
	 */
	public void reduceHP(double damage) {
		if (this.hp < damage) {
			this.hp = 0;
		} else {
			this.hp -= damage;
		}
	}

	/**
	 * Get the current hit points
	 * 
	 * @return The current hit points
	 */
	public int getHP() {
		return hp;
	}

	/**
	 * Get the maximum hit points that t
	 * 	his unit can have
	 * 
	 * @return The maximum health the the unit can have
	 */
	public int getMaxHP() {
		return maxHP;
	}

	/**
	 * Get whether the unit is dead (hit points equal or less than 0)
	 * 
	 * @return True if the unit is dead (equal or less than 0 hit points)
	 */
	public boolean isDead() {
		return hp <= 0;
	}

	/**
	 * Shoot a gun. Reloads if the gun has no bullets in its magazine.
	 */
	public void shoot() {
		if (!canShoot()) {
			return;
		}

		switch (weaponSlot) {
		case Unit.MACHINE_GUN:
			mg.requestID();
			break;
		case Unit.PISTOL:
			pistol.requestID();
			break;
		}
	}

	/**
	 * Get whether a unit can shoot currently
	 * 
	 * @return Whether the unit can shoot
	 */
	public boolean canShoot() {
		if (isBusy()) {
			return false;
		}

		switch (weaponSlot) {
		case Unit.MACHINE_GUN:
			return mg.canShoot();
		case Unit.PISTOL:
			return pistol.canShoot();
		default:
			return false;
		}
	}

	/**
	 * Send a message to the server to request to reload the currently held
	 * weapon
	 */
	public void requestReloadToServer() {
		sendMessage(new Message(Message.RELOAD, new int[] { getUnitID() }));
	}

	/**
	 * Reload a weapon.
	 */
	public void reload() {
		if (isBusy()) {
			return;
		}
		msSinceBusy = SystemTime.milliTime();
		switch (weaponSlot) {
		case Unit.MACHINE_GUN:
			isReloadMG = true;
			break;
		case Unit.PISTOL:
			isReloadPistol = true;
			break;
		default:
			return;
		}
	}

	@Override
	/**
	 * What to do when a unit makes a horizontal collision (set X velocity to zero).
	 */
	public void horizontalCollision() {
		setXVel(0);
	}

	@Override
	/**
	 * What do when a unit makes a vertical collision (set Y velocity to zero).
	 */
	public void verticalCollision() {
		setYVel(0);
	}

	/**
	 * Gets the boundaries of this unit, relative to this unit's top left
	 * position.
	 */
	@Override
	public Rectangle getCollisionBounds() {
		return new Rectangle((UNIT_WIDTH - UNIT_LEGS_WIDTH) / 2, UNIT_HEAD_HEIGHT + UNIT_TORSO_HEIGHT, UNIT_LEGS_WIDTH,
				UNIT_LEGS_HEIGHT);
	}

	/**
	 * Get the type of the unit
	 * 
	 * @return The type of the unit
	 */
	public int getUnitType() {
		return unitType;
	}

	/**
	 * Get the unit's controller (can be null).
	 * 
	 * @return The unit's controller.
	 */
	public UnitController getUnitController() {
		return controller;
	}

	/**
	 * Set the units controller
	 * 
	 * @param controller
	 *            The units controller
	 */
	public void setUnitController(UnitController controller) {
		this.controller = controller;
	}

	/**
	 * Get the units id
	 * 
	 * @return The units id
	 */
	public int getUnitID() {
		return unitID;
	}

	/**
	 * Get the direction that the unit is facing in radians
	 * 
	 * @return The direction that the unit is faceting
	 */
	public double getDir() {
		return dir;
	}

	/**
	 * Set the direction that the unit is facing in radians
	 * 
	 * @param dir
	 *            The new direction
	 */
	public void setDir(double dir) {
		this.dir = dir;
	}

	/**
	 * Return the currently held weapon.
	 * 
	 * @return The currently held weapon.
	 */
	public int getWeapon() {
		return this.weaponSlot;
	}

	/**
	 * Change the weapon by passing in the int code of the weapon wanted.
	 * 
	 * @param weapon
	 *            The int code of the weapon.
	 */
	public void switchWeaponServer(int weapon) {
		sendMessage(new Message(Message.UNIT_WEAPON_SWITCH, new int[] { getUnitID(), weapon }));
	}

	/**
	 * Set the weapon slot of the unit
	 * 
	 * @param weaponSlot
	 *            Weapon slot
	 */
	public void setWeapon(int weaponSlot) {
		this.weaponSlot = weaponSlot;
	}

	/**
	 * Is the unit reloading their pistol?
	 * @return Whether the unit is reloading their pistol.
	 */
	public boolean isReloadingPistol() {
		return isReloadPistol;
	}

	/**
	 * Is the unit reloading their MG?
	 * @return Whether the unit is reloading their MG.
	 */
	public boolean isReloadingMG() {
		return isReloadMG;
	}

	/**
	 * Check if the unit is busy: if reloading or holding a grenade
	 * 
	 * @return Whether the unit is busy
	 */
	public boolean isBusy() {
		return isReloadPistol || isReloadMG || holdingGrenade;
	}

	/**
	 * Set the units sender to send messages to the server
	 * 
	 * @param sender
	 *            The units sender
	 */
	public void setSender(Sender sender) {
		this.sender = sender;
	}

	/**
	 * Create a new grenade with a grenade id and is holding the grenade. Reduce
	 * the number of grenades left
	 * 
	 * @param grenadeID
	 *            The grenade id
	 */
	public void createGrenade(int grenadeID) {
		grenadesLeft--;
		heldGrenade = new Grenade(getGameData(), grenadeID, getX(), getY(), this);
	}

	/**
	 * What to do after a grenade is deemed to have been thrown successfully.
	 */
	public void grenadeThrownSuccessful() {
		if (heldGrenade == null) {
			return;
		}
		heldGrenade.release();
		airborneGrenades.add(heldGrenade);
		heldGrenade = null;
		holdingGrenade = false;
	}

	/**
	 * What to do after a grenade is deemed to have exploded correctly. (Remove the grenadeID from airborne grenades).
	 * @param grenadeID The grenade ID to remove.
	 */
	public void explodeSuccessful(int grenadeID) {
		if (!airborneGrenades.remove(getGrenadeFromID(grenadeID))) {
			if (heldGrenade != null) {
				heldGrenade = null;
				holdingGrenade = false;
			}
		}
	}

	/**
	 * Get the grenade from airborne grenades.
	 * @param grenadeID The grenade ID to fetch.
	 * @return The grenade object associated with the ID.
	 */
	public Grenade getGrenadeFromID(int grenadeID) {
		for (Grenade g : airborneGrenades) {
			if (g.getGrenadeID() == grenadeID) {
				return g;
			}
		}
		return null;
	}

	/**
	 * Check whether the unit has grenades left
	 * 
	 * @return Whether the unit has grenades left
	 */
	public boolean hasGrenadeStored() {
		return grenadesLeft > 0;
	}

	/**
	 * Set the number of grenades that a unit has. Maximum of MAX_NUM_GRENADES
	 * 
	 * @param num
	 *            The number of grenades to set the unit
	 */
	public void setNumberOfGrenades(int num) {
		if (num < MAX_NUM_GRENADES) {
			grenadesLeft = num;
		} else {
			grenadesLeft = MAX_NUM_GRENADES;
		}
	}

	/**
	 * Start to hold a grenade, there must not be one already held
	 */
	public boolean holdGrenade() {
		if (!hasGrenadeStored() || isBusy()) {
			return false;
		}

		if (holdingGrenade) {
			throw new RuntimeException("Unit is already holding one. what has happened?");
		}

		holdingGrenade = true;
		sendMessage(new Message(Message.REQUEST_GRENADE_ID, new int[] { unitID }, null));
		return true;
	}

	/**
	 * Release/throw the held grenade (There must be a held grenade)
	 */
	public void releaseGrenade() {
		if (!holdingGrenade) {
			return;
		}

		sendMessage(new Message(Message.THROW_GRENADE, new int[] { unitID }, null));
		holdingGrenade = false;
	}

	/**
	 * Send a message to the server using the units sender. If the sender is
	 * null, then do nothing, otherwise send the given message
	 * 
	 * @param msg
	 *            The message to send
	 */
	public void sendMessage(Message msg) {
		if (sender != null) {
			sender.put(msg);
		}
	}

	/**
	 * Set the units hit points
	 * 
	 * @param hp
	 *            The hit points
	 */
	public void setHP(int hp) {
		this.hp = hp;
	}

	/**
	 * Get the units pistol
	 * 
	 * @return The units pistol
	 */
	public Pistol getPistol() {
		return pistol;
	}

	/**
	 * Get the units machine gun
	 * 
	 * @return The units machine gun
	 */
	public MachineGun getMG() {
		return mg;
	}

	/**
	 * Get the number of grenades left the unit has
	 * 
	 * @return The number of grenades left
	 */
	public int getGrenadesLeft() {
		return grenadesLeft;
	}

	/**
	 * Get the amount of time the the unit (player) has to wait to respawn
	 * 
	 * @return The time for respawning
	 */
	public abstract int getSpawnTime();

	/**
	 * Get the starting (initial) health of the unit when respawned
	 * 
	 * @return The respawn health of the unit
	 */
	public abstract int getSpawnHealth();

	/**
	 * Get the distance between this unit and another
	 * 
	 * @param u
	 *            The other unit to get the distance to
	 * @return The distance between the two units
	 */
	public double distance(Unit u) {
		return Math.sqrt(Math.pow(u.getX() - this.getX(), 2) + Math.pow(u.getY() - this.getY(), 2));
	}

	/**
	 * Fire the pistol bullet
	 * 
	 * @param bullet
	 *            The bullet to fire
	 */
	public void createPistolBullet(Bullet bullet) {
		pistol.fire(bullet);
	}

	/**
	 * Fir the machine gun bullet
	 * 
	 * @param bullet
	 *            The bullet to fire
	 */
	public void createMGBullet(Bullet bullet) {
		mg.fire(bullet);
	}

	/**
	 * Given a unit, test when this unit can target it and return whether it
	 * can. If a friendly, return false, if and enemy return true.
	 * 
	 * @param unit
	 *            The unit to test
	 * @return Whether this unit can target the given unit
	 */
	public abstract boolean canTarget(Unit unit);

	/**
	 * Reset the unit to have default items, (grenade and ammo)
	 */
	public void respawn() {
		setNumberOfGrenades(2);
		if (pistol != null) {
			pistol.addClip(1);
			pistol.reload();
		}
		if (mg != null) {
			mg.addClip(4);
			mg.reload();
		}
		if (blastShield != null) {
			blastShield.setHP(BlastShield.MAX_HP);
		}
	}

	/**
	 * The unit has died, oh dear, clear everything
	 */
	public void died() {
		if (hp != 0) {
			throw new RuntimeException("How can I be dead if my hp is: " + hp);
		}
		setXVel(0);
		setYVel(0);
		setNumberOfGrenades(0);
		heldGrenade = null;
		airborneGrenades.clear();
		if (pistol != null) {
			pistol.getFiredBullets().clear();
			pistol.setTotalBullets(0);
		}
		if (mg != null) {
			mg.getFiredBullets().clear();
			mg.setTotalBullets(0);
		}
		if (controller != null) {
			controller.unitDied();
		}
		isReloadMG = false;
		isReloadPistol = false;
		holdingGrenade = false;
	}

	/**
	 * Get the angle between the unit and a point
	 * 
	 * @param p
	 *            The point
	 * @return The angle between the unit and the point
	 */
	public double angleFromPoint(Point p) {
		Point center1 = getCenterPoint();
		double dx = p.x - center1.x;
		double dy = center1.y - p.y;

		double newDir = Math.atan2(dx, dy);
		if (newDir < 0) {
			newDir += 2 * Math.PI;
		}
		return newDir;
	}

	/**
	 * Get the user name of the unit
	 * 
	 * @return The user name
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set the user name of the unit (user name of the player that is playing
	 * the game)
	 * 
	 * @param username
	 *            The user name of the player
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	public long getLastMoveFrame() {
		return lastMoveFrame;
	}

	public void setLastMoveFrame(long lastMoveFrame) {
		this.lastMoveFrame = lastMoveFrame;
	}
}
