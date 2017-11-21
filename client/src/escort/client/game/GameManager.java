package escort.client.game;

import java.awt.Graphics2D;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import escort.client.graphics.Camera;
import escort.client.graphics.GameRenderer;
import escort.client.graphics.hud.HUDManager;
import escort.client.input.Inputs;
import escort.client.main.Client;
import escort.client.main.Scale;
import escort.client.sound.SoundManager;
import escort.common.game.GameData;
import escort.common.game.GameState;
import escort.common.game.entities.units.Assassin;
import escort.common.game.entities.units.Civilian;
import escort.common.game.entities.units.Escort;
import escort.common.game.entities.units.Police;
import escort.common.game.entities.units.President;
import escort.common.game.entities.units.Unit;
import escort.common.game.entities.units.UnitInfo;
import escort.common.game.map.GameMap;
import escort.common.game.weapons.BlastShield;
import escort.common.game.weapons.Bullet;
import escort.common.game.weapons.Grenade;
import escort.common.game.weapons.MachineGun;
import escort.common.game.weapons.Pistol;
import escort.common.powerups.PowerUp;
import escort.common.systime.SystemTime;

/**
 * Manages the game state and performs client-side game logic. Handles all
 * entities client-side and calls game rendering methods.
 * 
 * @author Ahmed Bhallo
 *
 */
public class GameManager {

	/**
	 * The client
	 */
	private final Client client;

	/**
	 * A map of <UnitID, Unit> for all the units currently in the game.
	 */
	private final Map<Integer, Unit> units = new ConcurrentHashMap<>();

	/**
	 * The client's player's unit id.
	 */
	private int unitID = -1;

	/**
	 * The client's player's unit.
	 */
	private Unit clientUnit = null;

	/**
	 * The game rendering camera.
	 */
	private Camera camera;

	/**
	 * The sound manager.
	 */
	private final SoundManager soundManager;

	/**
	 * The game renderer.
	 */
	private GameRenderer renderer;

	/**
	 * The map object for the current game.
	 */
	private GameMap gameMap;

	/**
	 * The client's input keys
	 */
	private Inputs inputs;

	/**
	 * The game data object for the current game.
	 */
	private GameData data;

	/**
	 * Whether or not the game has started yet.
	 */
	private boolean gameStarted = false;

	/**
	 * The heads-up display object.
	 */
	private HUDManager hud;

	/**
	 * Instantiates a new game manager object.
	 * 
	 * @param client
	 *            The client.
	 * @param inputs
	 *            The input keys.
	 */
	public GameManager(Client client, Inputs inputs) {
		this.client = client;
		this.inputs = inputs;
		soundManager = client.getSoundManager();
	}

	/**
	 * Updates the game manager. Updates all units and rendering objects.
	 */
	public void update() {
		// Update the camera and the render manager
		hud.update();
		renderer.update();
		camera.update();

		// Don't update the units if the game hasn't started.
		if (!gameStarted) {
			return;
		}

		// Update all non-dead units.
		for (Unit unit : units.values()) {
			if (!unit.isDead()) {
				unit.update();
			}
		}
	}

	/**
	 * Renders the game. Calls the render method in the render manager.
	 * 
	 * @param g
	 */
	public void render(Graphics2D g) {
		renderer.render(g);
		hud.render(g);
	}

	/**
	 * Called to notify the game manager to create a new game. Takes in as an
	 * input the assignment info in order to create units.
	 * 
	 * @param gameState
	 *            Contains all information needed to start a game.
	 * @param unitID
	 * 			  The ID of the last in the units array.
	 */
	public void createNewGame(GameState gameState, int unitID) {
		client.getSoundManager().playBGM("bgmusic.wav");
		gameStarted = false;

		// Create a new game map.
		gameMap = GameMap.loadFromID(gameState.getMapID());

		// Clear the units map
		units.clear();

		data = new GameData(gameMap, units);

		// Parse assignment info and create corresponding unit objects.
		parseAssignmentInfo(gameState);

		// The unit id for this client is in the last index of this array.
		this.unitID = unitID;

		// Sets the unit object for this client
		clientUnit = units.get(unitID);
		clientUnit.setSender(client.getNetworkManager().getSender());

		// Create new graphics components.
		initGraphicsComponents();

		// Creates a new player controller to control the corresponding unit.
		PlayerController controller = new PlayerController(client, clientUnit, inputs);

		// Sets the client's unit's controller as the player controller just
		// created.
		clientUnit.setUnitController(controller);

		// Tells the client that we are now in a game.
		client.setInGame(true);
	}

	/**
	 * Parses the unit assignment information. Creates corresponding unit
	 * objects and adds them to the unit map.
	 * 
	 * @param gameState
	 *            The assignment info received from the server. The array is in
	 *            the form of [unitType1, unitID1, unitType2, unitID2, ...,
	 *            clientUnitID]
	 */
	private void parseAssignmentInfo(GameState gameState) {
		for (UnitInfo info : gameState.getUnitsInfo().values()) {
			Unit unit;
			switch (info.unitType) {
			case Unit.ESCORT_TYPE:
				unit = new Escort(data, null, info.unitID);
				break;
			case Unit.CIVILIAN_TYPE:
				unit = new Civilian(data, null, info.unitID);
				break;
			case Unit.PRESIDENT_TYPE:
				unit = new President(data, null, info.unitID);
				break;
			case Unit.ASSASSIN_TYPE:
				unit = new Assassin(data, null, info.unitID);
				break;
			case Unit.POLICE_TYPE:
				unit = new Police(data, null, info.unitID);
				break;
			default:
				throw new RuntimeException();
			}
			updateBasedOnInfo(info, unit);
			units.put(info.unitID, unit);
		}
	}

	/**
	 * Updates a unit's fields based on the information in the unit info object.
	 * 
	 * @param info
	 *            The unit info object.
	 * @param unit
	 *            The unit object.
	 */
	private static void updateBasedOnInfo(UnitInfo info, Unit unit) {
		unit.setX(info.x);
		unit.setY(info.y);
		unit.setDir(info.dir);
		unit.setXVel(info.xVel);
		unit.setYVel(info.yVel);
		unit.setHP(info.hpLeft);
		unit.setNumberOfGrenades(info.grenadesLeft);
		unit.switchWeaponServer(info.weaponSlot);
		unit.setUsername(info.username);
		// unit. pistol.setTotalBullets(info.pistolBulletsLeft);
		// unit. mg.setTotalBullets(info.mgBulletsLeft);
	}

	/**
	 * Initialises the graphics components for the game.
	 */
	public void initGraphicsComponents() {
		// Instantiates a new camera object based on the game map.
		camera = new Camera(inputs, gameMap.getWidthInPx(), gameMap.getHeightInPx(), this);

		// Initially sets the focussed unit of the camera as this client's unit
		// and locks it.
		camera.setFocussedUnit(clientUnit);
		camera.setLocked(true);

		// Creates a new game render and passed the map object.
		renderer = new GameRenderer(this, gameMap);
		hud = new HUDManager(this, inputs);
	}

	/**
	 * Starts the game.
	 */
	public void startGame() {
		gameStarted = true;
		hud.gameStarted();
	}

	/**
	 * Called when a grenade has been created.
	 * 
	 * @param unitID
	 *            The unit ID of the unit who created it.
	 * @param grenadeID
	 *            The grenade ID.
	 */
	public void grenadeCreated(int unitID, int grenadeID) {
		Unit creator = units.get(unitID);
		playSound(SoundManager.GRENADE_COOK, creator.getX(), creator.getY(), SoundManager.MISC_PLAYER);
		creator.createGrenade(grenadeID);
	}

	/**
	 * Called when a grenade has been thrown.
	 * 
	 * @param unitID
	 *            The Unit ID of the thrower.
	 */
	public void grenadeThrown(int unitID) {
		Unit thrower = units.get(unitID);
		playSound(SoundManager.GRENADE_THROW, thrower.getX(), thrower.getY(), SoundManager.MISC_PLAYER);
		thrower.grenadeThrownSuccessful();
	}

	/**
	 * Called when a grenade has been exploded.
	 * 
	 * @param unitID
	 *            The Unit ID of the thrower of the grenade.
	 * @param grenadeID
	 *            The ID of the grenade.
	 */
	public void grenadeExplode(int unitID, int grenadeID) {
		Unit owner = units.get(unitID);
		// Look inside their airborne grenades.
		Grenade grenade = owner.getGrenadeFromID(grenadeID);
		if (grenade == null) {
			// If it's not in their airborne grenades, it must be their held
			// grenade.
			grenade = owner.getHeldGrenade();
			if (grenade == null) {
				return;
			}
		}
		renderer.addExplosion(grenade.getCenterPoint(), SystemTime.milliTime());
		playSound(SoundManager.GRENADE_EXPLOSION, grenade.getX(), grenade.getY(), SoundManager.GRENADE_SOUND_PLAYER);
		owner.explodeSuccessful(grenadeID);
	}

	/**
	 * Called when the president is following an escort.
	 * 
	 * @param presidentID
	 *            The president's ID.
	 * @param escortID
	 *            The escort's ID.
	 */
	public void presidentFollow(int presidentID, int escortID) {
		Unit president = units.get(presidentID);
		Unit escort = units.get(escortID);
		if (president instanceof President && escort instanceof Escort) {
			((President) president).follow((Escort) escort);
			((Escort) escort).setIsFollower(true);
		}
	}

	/**
	 * Called when the president is no longer following anyone.
	 * 
	 * @param presidentID
	 *            The president ID.
	 */
	public void presidentUnfollow(int presidentID) {
		Unit president = units.get(presidentID);
		if (president instanceof President) {
			((President) president).unfollow();
			if (((President) president).getFollowing() != null)
				((President) president).getFollowing().setIsFollower(false);
		}
	}

	/**
	 * Called when a unit's hp has been updated.
	 * 
	 * @param unitID
	 *            The unit's ID.
	 * @param newHP
	 *            The unit's new HP.
	 */
	public void updateHP(int unitID, int newHP) {
		Unit unit = units.get(unitID);
		unit.setHP(newHP);
		if (unit.isDead()) {
			unit.died();
		}
	}

	/**
	 * Called when a pistol bullet has been fired.
	 * 
	 * @param unitID
	 *            The shooter's unit id.
	 * @param bulletID
	 *            The bullet's id.
	 */
	public void pistolBulletCreated(int unitID, int bulletID) {
		Unit shooter = units.get(unitID);
		shooter.createPistolBullet(new Bullet(data, units.get(unitID), Pistol.BULLET_DAMAGE));
		playSound(SoundManager.PISTOL_GUN_SHOT, shooter.getX(), shooter.getY(), SoundManager.SHOOTING_SOUND_PLAYER);
	}

	/**
	 * Called when a machine gun bullet has been fired.
	 * 
	 * @param unitID
	 *            The shooter's unit id.
	 * @param bulletID
	 *            THe bullet's id.
	 */
	public void mgBulletCreated(int unitID, int bulletID) {
		Unit shooter = units.get(unitID);
		shooter.createMGBullet(new Bullet(data, units.get(unitID), MachineGun.BULLET_DAMAGE));
		playSound(SoundManager.MG_GUN_SHOT, shooter.getX(), shooter.getY(), SoundManager.SHOOTING_SOUND_PLAYER);
	}

	/**
	 * Updates the position and velocity of a unit with a given unitID.
	 * 
	 * @param unitID
	 *            The ID of the unit to be updates.
	 * @param x
	 *            The new x position of the unit.
	 * @param y
	 *            The new y position of the unit.
	 * @param dir
	 *            The new direction of the unit.
	 * @param xVel
	 *            The new velocity in the x component of the unit.
	 * @param yVel
	 *            The new velocity in the y component of the unit.
	 */
	public void updateRemoteUnit(int unitID, double x, double y, double dir, double xVel, double yVel, boolean force) {
		// Do not update the unit if the unit to be updated is this client. In
		// order to maintain synchronisation, only update this unit when a game
		// state update is received.
		if (unitID == getUnitID() && !force) {
			return;
		}

		// Get the unit to be updated.
		Unit unit = getUnitMap().get(unitID);

		// Update the unit's new values.
		unit.setX(x);
		unit.setY(y);
		unit.setDir(dir);
		unit.setXVel(xVel);
		unit.setYVel(yVel);
	}

	/**
	 * Called when a unit has respawned
	 * 
	 * @param unitID
	 *            The unit id of the respawned unit.
	 * @param hp
	 *            The new HP of that unit.
	 * @param unitData
	 *            The new position and direction of that unit.
	 */
	public void respawn(int unitID, int hp, double[] unitData) {
		updateHP(unitID, hp);
		Unit unit = units.get(unitID);
		unit.setX(unitData[0]);
		unit.setY(unitData[1]);
		unit.setDir(unitData[2]);
		unit.respawn();
	}

	/**
	 * @return The game data object.
	 */
	public GameData getGameData() {
		return data;
	}

	/**
	 * Called when a unit has switched their weapon slot.
	 * 
	 * @param unitID
	 *            The unit id of the unit who switched.
	 * @param weaponSlot
	 *            The unit's new weapon slot.
	 */
	public void weaponSwitched(int unitID, int weaponSlot) {
		units.get(unitID).setWeapon(weaponSlot);
	}

	/**
	 * Called when a unit is reloading
	 * 
	 * @param unitID
	 *            The unit id of the reloading unit
	 */
	public void weaponReloaded(int unitID) {
		Unit reloader = units.get(unitID);
		reloader.reload();
		playSound(SoundManager.UNIT_RELOADING, reloader.getX(), reloader.getY(), SoundManager.MISC_PLAYER);
	}

	/**
	 * Ends the client main game loop.
	 * 
	 * @param outcome
	 *            The outcome of the game.
	 */
	public void endGame(int outcome) {
		client.getMenuManager().displayEndGame(outcome);
		client.getMenuManager().getChat().transformToLobbyChat();
		hud.removeChat();
		client.getMenuManager().getLobbyMenu().addChat();
		client.setInGame(false);
		soundManager.stopBGM();
	}

	/**
	 * Players a sound based on where the camera is facing and the position of
	 * the source of the sound.
	 * 
	 * @param sound
	 *            The string of the file of the sound.
	 * @param x
	 *            The x coordinate of the source of the sound.
	 * @param y
	 *            The y coordinate of the source of the sound.
	 * @param soundType
	 *            What type of sound is it. Used to determine which sound player
	 *            to use.
	 */
	public void playSound(String sound, double x, double y, int soundType) {
		if (soundManager == null) {
			return;
		}

		int xPos = (int) x * Scale.factor - camera.getxOffset();
		int yPos = (int) y * Scale.factor - camera.getyOffset();
		int centerCamX = client.getGameWidth() / 2;
		int centerCamY = client.getGameHeight() / 2;
		double distance = Math.sqrt(Math.pow(xPos - centerCamX, 2) + Math.pow(yPos - centerCamY, 2));
		distance /= 8;
		int volume = (int) (100 - distance);
		float pan = (float) (xPos * 2) / ((float) client.getGameWidth() * 2) - 1;
		if (volume <= 2) {
			return;
		}
		pan = Math.min(1, Math.max(pan, -1));
		soundManager.playEffect(unitID, sound, pan, soundType, volume);
	}

	/**
	 * Assigns the power-ups given the power-up id and positions.
	 * 
	 * @param ids
	 *            The array power-up ids.
	 * @param positions
	 *            The position of the corresponding power-up.
	 */
	public void assignPowerUps(int[] ids, double[] positions) {
		for (int i = 0; i < ids.length; i++) {
			for (PowerUp powerUp : gameMap.getPowerUps()) {
				if (powerUp.getX() == positions[i * 2] && powerUp.getY() == positions[i * 2 + 1])
					powerUp.setID(ids[i]);
			}
		}
	}

	/**
	 * Called when a power up has been used by a unit.
	 * 
	 * @param powerUpID
	 *            The power up used.
	 * @param unitID
	 *            The unit who used it.
	 */
	public void powerUpUsed(int powerUpID, int unitID) {
		Unit user = units.get(unitID);
		for (PowerUp powerUp : gameMap.getPowerUps()) {
			if (powerUp.getID() == powerUpID)
				powerUp.pickup(user);
		}
		playSound(SoundManager.POWER_UP, user.getX(), user.getY(), SoundManager.MISC_PLAYER);
	}

	/**
	 * Called to update the health of a shield.
	 * 
	 * @param unitID
	 *            The owner of the shield.
	 * @param shieldHP
	 *            The shield's new hp.
	 */
	public void updateShieldHP(int unitID, int shieldHP) {
		Unit unit = units.get(unitID);
		BlastShield shield = unit.getBlastShield();
		if (shield != null) {
			shield.setHP(shieldHP);
		}
	}

	/**
	 * Get the collection of units currently in the game.
	 * 
	 * @return A collection of units currently in the game.
	 */
	public Collection<Unit> getUnits() {
		return units.values();
	}

	/**
	 * Get the camera object of the game.
	 * 
	 * @return The camera object of the current game.
	 */
	public Camera getCamera() {
		return camera;
	}

	/**
	 * Get the client object.
	 * 
	 * @return The client object.
	 */
	public Client getClient() {
		return client;
	}

	/**
	 * @return The unit id of the client's unit.
	 */
	public int getUnitID() {
		return unitID;
	}

	/**
	 * @return The HUD manager.
	 */
	public HUDManager getHUDManager() {
		return hud;
	}

	/**
	 * @return The map of UnitID to Unit object.
	 */
	public Map<Integer, Unit> getUnitMap() {
		return units;
	}

	/**
	 * @return The unit controlled by this client.
	 */
	public Unit getClientUnit() {
		return clientUnit;
	}

}
