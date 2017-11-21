package escort.server.game;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import escort.common.game.entities.units.Escort;
import escort.common.game.entities.units.President;
import escort.common.game.entities.units.Unit;
import escort.common.game.weapons.Bullet;
import escort.common.game.weapons.MachineGun;
import escort.common.game.weapons.Pistol;
import escort.common.network.Message;
import escort.server.game.combat.BulletWrap;
import escort.server.network.Player;

/**
 * Contains a queue for Message objects that are parsed and appropriate actions
 * are taken.
 * 
 * @author Ahmed Bhallo
 * @author Brendan Hart (Game logic/Message handling)
 *
 */
public class GameMessageQueuer {
	/**
	 * The client object
	 */
	private Game game;
	/**
	 * A concurrent queue for messages.
	 */
	private final ConcurrentLinkedQueue<Message> queue = new ConcurrentLinkedQueue<>();

	private long currentFrame = 0;

	/**
	 * Instantiates a new Lobby Message Queuer object
	 * 
	 * @param game
	 */
	public GameMessageQueuer(Game game) {
		this.game = game;
	}

	/**
	 * Loops through all messages in the queue and performs appropriate actions.
	 */
	public void update() {
		// initial environment
		this.currentFrame++;
		Message msg = null;
		Collection<Player> players = game.getPlayerMap().values();

		// obtain initial unit locations
		Collection<Unit> units = this.game.getGameData().getUnits().values();
		int numUnits = units.size();
		double[] locations = new double[2 * numUnits];
		boolean[] updateframes = new boolean[numUnits];
		for (Unit unit : units) {
			locations[2 * unit.getUnitID()] = unit.getX();
			locations[2 * unit.getUnitID() + 1] = unit.getY();
			updateframes[unit.getUnitID()] = false;
		}

		while ((msg = queue.poll()) != null) {
			int unitID = msg.getInts()[0];
			Unit unit = game.getUnitFromID(unitID);
			if (unit.isDead()) {
				continue;
			}

			boolean valid = false;
			Escort escort = null;

			switch (msg.messageType) {
			case Message.UNIT_MOVED:
				// check if the message and the move is valid
				valid = true;
				valid &= msg.getInts() != null && msg.getInts().length == 2;
				valid &= msg.getDoubles() != null && msg.getDoubles().length == 5;
				// long frameDifference = Math.min(this.currentFrame -
				// unit.getLastMoveFrame(), 120);
				// valid &= Math.pow(locations[2*unit.getUnitID()] -
				// msg.getDoubles()[0], 2) +
				// Math.pow(locations[2*unit.getUnitID()+1] -
				// msg.getDoubles()[1], 2) <=
				// 2*Math.pow(unit.getSpeed()*frameDifference, 2);
				// updateframes[unitID] = true;
				if (valid) {
					// also update gamelogic
					unit.setX(msg.getDoubles()[0]);
					unit.setY(msg.getDoubles()[1]);
					unit.setDir(msg.getDoubles()[2]);
					unit.setXVel(msg.getDoubles()[3]);
					unit.setYVel(msg.getDoubles()[4]);
					for (Player player : players) {
						player.getSender().put(msg);
					}
				} else {
					// System.err.println("Speed: " +
					// Math.pow(locations[2*unit.getUnitID()] -
					// msg.getDoubles()[0], 2) +
					// Math.pow(locations[2*unit.getUnitID()+1] -
					// msg.getDoubles()[1], 2));
					// System.err.println("Maximum: " +
					// Math.pow(unit.getSpeed()*frameDifference, 2));
					// go back to original location
					Player player = this.game.getPlayerMap().get(unitID);
					if (player == null) {
						break;
					}
					player.getSender().put(new Message(Message.UNIT_MOVED, new int[] { unitID, 1 }, null,
							new double[] { unit.getX(), unit.getY(), unit.getDir(), unit.getXVel(), unit.getYVel() }));
				}
				break;
			case Message.UNIT_FOLLOW:
				// Check if the unit is an escort
				if (unit.getUnitType() != Unit.ESCORT_TYPE) {
					break;
				}
				escort = (Escort) unit;
				President presCollision = escort.getPresidentCollision();
				if (presCollision != null && !presCollision.isFollowing()) {
					// Follow the escort
					presCollision.follow(escort);
					escort.setIsFollower(true);
					// Update the players who the president is following
					int[] vals = { presCollision.getUnitID(), escort.getUnitID() };
					String[] strings = new String[0];
					for (Player player : players) {
						player.getSender().put(new Message(Message.PRES_FOLLOW, vals, strings));
					}
				}
				break;
			case Message.UNIT_UNFOLLOW:
				// Check if the unit is an escort
				if (unit.getUnitType() != Unit.ESCORT_TYPE) {
					break;
				}
				escort = (Escort) unit;
				// Loop through all units and check if they're a
				// president
				for (Unit unit1 : game.getGameData().getUnits().values()) {
					if (unit1.getUnitType() != Unit.PRESIDENT_TYPE) {
						continue;
					}
					President president = (President) unit1;
					// Check if this president is following the escort
					if (!president.isFollowing(escort)) {
						continue;
					}
					// Unfollow the escort
					president.unfollow();
					escort.setIsFollower(false);
					// Update all players that the president is no
					// longer following
					for (Player player : players) {
						player.getSender()
								.put(new Message(Message.PRES_UNFOLLOW, new int[] { president.getUnitID() }, null));
					}
					break;
				}
				break;
			case Message.REQUEST_GRENADE_ID:
				// check constraints
				if (unit.hasHeldGrenade() || !unit.holdGrenade()) {
					break;
				}

				int grenadeID = ++game.grenadeIDCounter;
				unit.createGrenade(grenadeID);
				for (Player player : players) {
					player.getSender()
							.put(new Message(Message.GRENADE_ID, new int[] { msg.getInts()[0], grenadeID }, null));
				}
				game.newGrenadeTimer(game.getUnitFromID(unitID).getHeldGrenade());
				break;
			case Message.THROW_GRENADE:
				if (!unit.hasHeldGrenade()) {
					break;
				}
				for (Player player : players) {
					player.getSender().put(new Message(Message.THROW_GRENADE, new int[] { msg.getInts()[0] }, null));
					// add grenade to server unit
					Unit unit1 = game.getUnitFromID(player.getUnitID());
					if (player.getUnitID() == msg.getInts()[0]) {
						unit1.grenadeThrownSuccessful();
					}
				}
				// }
				break;
			case Message.REQUEST_PISTOL_BULLET:
				// CHECK IF THEY HAVE A PISTOL BULLET IN THEIR MAGSIZE THING
				// check if player is using correct weapon and have enough
				// bullets
				if (!(unit.getWeapon() == Unit.PISTOL) || !unit.canShoot()) {
					break;
				}

				int bulletID = ++game.bulletIDCounter;
				Bullet bullet1 = new Bullet(game.getGameData(), unit, Pistol.BULLET_DAMAGE);
				BulletWrap listener1 = new BulletWrap(game, bullet1);
				bullet1.addListener(listener1);
				game.getUnitFromID(unitID).createPistolBullet(bullet1);
				for (Player player : players) {
					player.getSender()
							.put(new Message(Message.PISTOL_BULLET, new int[] { msg.getInts()[0], bulletID }));
				}
				break;
			case Message.REQUEST_MG_BULLET:
				// CHECK IF THEY HAVE A PISTOL BULLET IN THEIR MAGSIZE THING
				// check if player is using correct weapon and have enough
				// bullets
				if (!(unit.getWeapon() == Unit.MACHINE_GUN) || !unit.canShoot()) {
					break;
				}

				int bulletIDMG = ++game.bulletIDCounter;
				Bullet bullet = new Bullet(game.getGameData(), unit, MachineGun.BULLET_DAMAGE);
				BulletWrap listener = new BulletWrap(game, bullet);
				bullet.addListener(listener);
				game.getUnitFromID(unitID).createMGBullet(bullet);
				for (Player player : players) {
					player.getSender().put(new Message(Message.MG_BULLET, new int[] { msg.getInts()[0], bulletIDMG }));
				}
				break;
			case Message.RELOAD:
				// reload weapon
				if (unit.isBusy()) {
					break;
				}
				unit.reload();

				// send to other players
				for (Player player : players) {
					player.getSender().put(new Message(Message.WEAPON_RELOAD_ACK, new int[] { unitID }, null));
				}
				break;
			case Message.UNIT_WEAPON_SWITCH:
				// switch weapon

				if (unit.isBusy() || msg.getInts() == null || msg.getInts().length != 2) {
					break;
				}
				unit.setWeapon(msg.getInts()[1]);

				// send to other players
				for (Player player : players) {
					player.getSender()
							.put(new Message(Message.WEAPON_SWITCH_ACK, new int[] { unitID, msg.getInts()[1] }, null));
				}
				break;
			default:
				break;
			}
		}

		// Update unit moved frames
		for (int i = 0; i < updateframes.length; i++) {
			if (updateframes[i]) {
				this.game.getGameData().getUnits().get(i).setLastMoveFrame(this.currentFrame);
			}
		}
	}

	/**
	 * Adds a message to the queue.
	 * 
	 * @param msg
	 *            The message to be added.
	 */
	public void add(Message msg) {
		// Check if the game is started
		if (!this.game.hasStarted()) {
			return;
		}

		queue.offer(msg);
	}
}