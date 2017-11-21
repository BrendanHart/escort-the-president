package escort.server.game;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import escort.common.game.GameData;
import escort.common.game.GameState;
import escort.common.game.Outcomes;
import escort.common.game.entities.units.Assassin;
import escort.common.game.entities.units.Escort;
import escort.common.game.entities.units.President;
import escort.common.game.entities.units.Unit;
import escort.common.game.entities.units.UnitInfo;
import escort.common.game.map.GameMap;
import escort.common.game.weapons.BlastShield;
import escort.common.game.weapons.Grenade;
import escort.common.network.Message;
import escort.common.powerups.*;
import escort.server.game.ai.AIUnitFactory;
import escort.server.game.combat.GrenadeTimer;
import escort.server.lobby.LobbyManagement;
import escort.server.lobby.LobbySettings;
import escort.server.network.Player;

/**
 * This represents a game object. Manages the game state of the game. To send
 * message to the client: player.getSender().putMessage(message)
 * 
 * @author Ahmed Bhallo
 * @author James Birch
 * @author Edward Dean
 * @author Brendan Hart
 * @author Kwong Hei Tsang
 *
 */
public class Game implements Runnable {

	public static final int FPS = 60;

	private final Map<Integer, Player> playerMap;
	private final Map<Integer, Long> unitDeathTime;
	private final LobbyManagement mgmt;
	private final int lobbyid;

	private final GameState gameState;
	private final GameData gameData;

	// private final int MAX_PLAYERS = 12;
	private boolean running = true;
	private boolean started = false;
	private final GameMessageQueuer queuer;
	private Thread gameThread;
	// private int actualFPS;
	public int grenadeIDCounter = -1;
	public int bulletIDCounter = -1;
	private int unitIDCount = -1;

	private Map<Grenade, GrenadeTimer> grenadeTimers = new ConcurrentHashMap<>();

	/**
	 * Construct a game object for game logic
	 * 
	 * @param players
	 *            The list of players in the game
	 * @param mgmt
	 *            LobbyManagement Object of the server
	 * @param lobbyid
	 *            The lobby where the players coming from
	 * @param mapid
	 *            The map ID configured by the master of the lobby
	 */
	public Game(List<Player> players, LobbyManagement mgmt, int lobbyid, LobbySettings settings) {
		this.playerMap = new ConcurrentHashMap<Integer, Player>();
		this.mgmt = mgmt;
		this.lobbyid = lobbyid;
		unitDeathTime = new HashMap<Integer, Long>();
		gameState = new GameState(new HashMap<>(), settings.mapID);
		gameData = new GameData(GameMap.loadFromID(settings.mapID), new HashMap<Integer, Unit>());

		queuer = new GameMessageQueuer(this);
		assignUnits(players, settings);
		assignPowerUps();
		// setupGame();
		this.setStarted(false);
		gameThread = new Thread(this);
		gameThread.setUncaughtExceptionHandler((t, e) -> {
			// print the error
			e.printStackTrace();

			// end the game
			for (Player player : this.getPlayers())
				player.getSender().put(new Message(Message.SYSTEM_MESSAGE, null,
						new String[] { "Server game logic error. Game terminated." }));
			this.endGame(Outcomes.OUTCOME_ERROR);

			// Add a log
			Logger.getLogger(Game.class.getName()).log(Level.SEVERE, "Game logic crashed.", e);
		});
		gameThread.start();
	}

	/**
	 * Assigns the power ups in the game. Updates the state of the game and
	 * sends the message to all players.
	 */
	public void assignPowerUps() {
		int id = 0;
		int[] ints = new int[gameData.getMap().getPowerUps().size()];
		double[] doubles = new double[gameData.getMap().getPowerUps().size() * 2];
		for (PowerUp powerUp : gameData.getMap().getPowerUps()) {
			powerUp.setID(id);
			ints[id] = id;
			doubles[id * 2 + 0] = powerUp.getX();
			doubles[id * 2 + 1] = powerUp.getY();
			id++;
		}

		Message msg = new Message(Message.POWERUP_ASSIGNMENT, ints, null, doubles);
		playerMap.values().forEach(p -> p.getSender().put(msg));
	}

	/**
	 * Assign all of the units in the game. Will be represented as an array of
	 * IDs.
	 * 
	 * @param playersInput
	 *            Number of human players in the game.
	 * @return An int array of all the units to be generated.
	 * @author James Birch
	 */
	private void assignUnits(List<Player> playersInput, LobbySettings settings) {
		// use to keep track of ID to assign to a unit
		int numPlayers = playersInput.size();

		// Determine the ratio of players
		int numEscorts = (int) Math.ceil(numPlayers / 3.0);

		int numAssassins = numPlayers - numEscorts;

		int numPresidents = 1;

		LinkedList<Player> players = new LinkedList<>(playersInput);

		// shuffle so the selection of escorts etc is not (feasibly) predictable
		Collections.shuffle(players);

		/*
		 * Create a LinkedList of integers such that they are grouped in twos.
		 * The first of the two is the unit type and the second of the two is
		 * the unit ID (unique identifier for that player/AI). The ID is
		 * regulated by unitIDCount and will be done so that it increments
		 * uniformly so that it matches the unitID in the foreach block below.
		 */

		for (int i = 0; i < numEscorts; i++) {
			Unit unit = new Escort(gameData, null, ++unitIDCount);
			spawn(unit);
			gameData.getUnits().put(unitIDCount, unit);
		}

		for (int i = 0; i < numAssassins; i++) {
			Unit unit = new Assassin(gameData, null, ++unitIDCount);
			spawn(unit);
			gameData.getUnits().put(unitIDCount, unit);
		}

		for (int i = 0; i < numPresidents; i++) {
			President president = (President) makeAIUnit(Unit.PRESIDENT_TYPE, "President");
			gameData.setPresident(president);
		}

		for (int i = 0; i < settings.numCivilianAI; i++) {
			makeAIUnit(Unit.CIVILIAN_TYPE, "Civilian AI");
		}

		for (int i = 0; i < settings.numPoliceAI; i++) {
			makeAIUnit(Unit.POLICE_TYPE, "Police AI");
		}

		for (int i = 0; i < settings.numAssassinsAI; i++) {
			makeAIUnit(Unit.ASSASSIN_TYPE, "Assassin AI");
		}

		for (int i = 0; i < players.size(); i++) {
			Player player = players.get(i);
			playerMap.put(i, player);
			player.setUnitID(i);
			player.setGame(this);
			// int[] assignment = createArray(assignedUnits, unitID);
			Unit unit = gameData.getUnits().get(i);
			unit.setUsername(player.getPlayerName());
			gameState.getUnitsInfo().put(i, new UnitInfo(unit));
		}

		for (int i = 0; i < players.size(); i++) {
			Player player = players.get(i);
			Message msg = new Message(Message.GAME_READY, new int[] { i }, null);
			msg.setGameState(gameState);
			player.getSender().put(msg);
		}
	}

	private Unit makeAIUnit(int unitType, String username) {
		Unit unit = AIUnitFactory.makeUnit(this, unitType, ++unitIDCount, 0, 0);
		unit.setUsername(username);
		spawn(unit);
		gameData.getUnits().put(unitIDCount, unit);
		gameState.getUnitsInfo().put(unitIDCount, new UnitInfo(unit));
		return unit;
	}

	public void randomMapWideSpawn(Unit u) {
		Random r = new Random();

		Rectangle spawn = new Rectangle(0, 0, gameData.getMap().getWidthInPx(), gameData.getMap().getHeightInPx());
		Point p = new Point();
		do {
			p.x = (spawn.x + r.nextInt(spawn.width));
			p.y = (spawn.y + r.nextInt(spawn.height));
		} while (!gameData.getMap().walkable(p.x, p.y, u.getCollisionBounds().width, u.getCollisionBounds().height));

		u.setX(p.x - u.getCollisionBounds().x);
		u.setY(p.y - u.getCollisionBounds().y);

	}

	public void spawn(Unit u) {

		Random r = new Random();

		List<Rectangle> rSpawns;
		if (u.getUnitType() == Unit.POLICE_TYPE || u.getUnitType() == Unit.CIVILIAN_TYPE) {
			randomMapWideSpawn(u);
			return;
		}

		rSpawns = new ArrayList<>(gameData.getMap().getSpawnsFor(u.getUnitType()));

		Rectangle spawn = rSpawns.get(r.nextInt(rSpawns.size()));

		u.setX(spawn.x + r.nextInt(spawn.width - u.getCollisionBounds().width) - u.getCollisionBounds().x);
		u.setY(spawn.y + r.nextInt(spawn.height - u.getCollisionBounds().height) - u.getCollisionBounds().y);

	}

	public void respawn(Unit u) {
		spawn(u);
		u.setXVel(0);
		u.setYVel(0);
		u.setHP(u.getSpawnHealth());
		u.respawn();
		sendPosition(u);

		for (Player player : playerMap.values()) {
			player.getSender().put(new Message(Message.RESPAWN, new int[] { u.getUnitID(), u.getHP() }, null,
					new double[] { u.getX(), u.getY(), u.getDir(), u.getXVel(), u.getYVel() }));
		}
	}

	/**
	 * Convert a LinkedList of integers to an integer array.
	 * 
	 * @param list
	 *            A LinkedList of integers.
	 * @param targetID
	 *            The player ID to add to the end of the array.
	 * @return An integer array which corresponds to the LinkedList.
	 * @author James Birch
	 */
	public int[] createArray(LinkedList<Integer> list, int targetID) {
		int[] array = new int[list.size() + 1];
		for (int i = 0; i < list.size(); i++) {
			array[i] = list.get(i);
		}
		array[list.size()] = targetID;
		return array;
	}

	public Collection<Player> getPlayers() {
		return this.playerMap.values();
	}

	@Override
	public void run() {
		// Target nanoseconds between ticks (updates).
		double nsPerUpdate = 1000000000 / FPS;

		// A measure of update timing. Whether or not we are lagging, leading or
		// spot on.
		// delta < 1 -> Updating too often
		// delta == 1 -> Perfect timing
		// delta > 1 -> Updating too slowly
		double delta = 0;

		// The last time since we last looped
		long lastLoopTime = System.nanoTime();

		// A time that counts the number of frames updates per second.
		long timer = 0;

		// A counter of the number of frames updated and rendered in the past
		// second.
//		int frames = 0;
		while (running) {
			// Store the time now.
			long now = System.nanoTime();

			// Increment delta by the time in nanoseconds from now since the
			// last time we looped divided by the nsPerUpdate.
			delta += (now - lastLoopTime) / nsPerUpdate;

			// The timer is incremented by the time since the last time we
			// looped.
			timer += now - lastLoopTime;

			// Set the last loop time as now.
			lastLoopTime = now;

			// If we are updating on time or lagging, update.
			if (delta >= 1) {
				update();
				// Increment the number of frames.
//				frames++;

				// Decrement delta.
				delta--;
			} else {
				// This is to reduce resources consumption on fast machines
				try {
					long sleeptime = lastLoopTime + (long) (nsPerUpdate) - System.nanoTime();
					if (sleeptime > 0) {
						Thread.sleep(sleeptime / 1000000, (int) (sleeptime % 1000000));
					}
				} catch (InterruptedException e) {
				}
			}

			// Every second, store the number of frames that we rendered for
			// the second into actualFPS.
			if (timer >= 1000000000) {
				// actualFPS = frames;
				// Reset the counters and delta.
				if (delta > 1) {
					delta = 1;
				}
//				frames = 0;
				timer = 0;
			}
		}
	}

	/**
	 * Updates the message queuer and all units in the game. Calls methods to
	 * handle power ups and deaths
	 */
	public void update() {
		// Prevents moving before game actually start
		if (!started) {
			return;
		}

		queuer.update();

		for (Unit unit : gameData.getUnits().values()) {
			if (unit.isDead()) { // notify all players
				handleDeath(unit);
			} else {
				unit.update();

				// Handle collision with power ups
				handlePowerUp(unit);
			}
		}
		grenadeTimers.values().forEach(timer -> timer.update());

		detectEndGame();
	}

	/**
	 * Checks for a unit if they are standing on a power up. If so, send a power
	 * up used message and pick up the power up.
	 * 
	 * @param unit
	 *            The unit to check
	 */
	private void handlePowerUp(Unit unit) {
		for (PowerUp powerUp : gameData.getMap().getPowerUps()) {
			if (unit.collision(powerUp)) {
				if (powerUp.pickup(unit)) {
					playerMap.values().forEach(p -> p.getSender()
							.put(new Message(Message.POWERUP_USED, new int[] { powerUp.getID(), unit.getUnitID() })));
				}
			}
		}
	}

	/**
	 * Detects if the president is standing on the endzone. If so, ends the game
	 * with escort win outcome.
	 */
	private void detectEndGame() {
		President pres = gameData.getPresident();

		Rectangle presBounds = pres.getCollisionBounds();
		presBounds.x += pres.getX();
		presBounds.y += pres.getY();

		for (Rectangle endZone : gameData.getMap().getEndZones()) {
			if (endZone.intersects(presBounds)) {
				endGame(Outcomes.OUTCOME_ESCORT_WIN);
			}
		}
	}

	/**
	 * If the unit is dead, determines if it is time for the unit to respawn. If
	 * the president is dead, the game is ended. If an escort is dead and they
	 * are being following by the president, the following action is removing.
	 * 
	 * @param unit
	 *            The unit to be checked
	 */
	public void handleDeath(Unit unit) {
		if (unitDeathTime.containsKey(unit.getUnitID())) {
			double timeSinceDeath = (System.nanoTime() - unitDeathTime.get(unit.getUnitID())) / 1000000000.0;
			if (timeSinceDeath > unit.getSpawnTime()) {
				unitDeathTime.remove(unit.getUnitID());
				respawn(unit);
				return;
			}
		} else {
			unit.died();
			// First time we have encountered this death
			if (unit.getUnitType() == Unit.ESCORT_TYPE) {
				Escort e = (Escort) unit;
				e.setIsFollower(false);
				for (Unit u : gameData.getUnits().values()) {
					if (u.getUnitType() == Unit.PRESIDENT_TYPE) {
						President pres = (President) u;
						if (pres.isFollowing(e)) {
							pres.unfollow();
							playerMap.values().forEach(p -> p.getSender()
									.put(new Message(Message.PRES_UNFOLLOW, new int[] { pres.getUnitID() }, null)));
						}
					}
				}
			} else if (unit.getUnitType() == Unit.PRESIDENT_TYPE) {
				President pres = (President) unit;
				Escort following = pres.getFollowing();
				if (following != null) {
					following.setIsFollower(false);
				}
				pres.unfollow();
				playerMap.values().forEach(p -> p.getSender()
						.put(new Message(Message.PRES_UNFOLLOW, new int[] { pres.getUnitID() }, null)));
			}

			unitDeathTime.put(unit.getUnitID(), System.nanoTime());
			if (gameData.getPresident().isDead()) {
				endGame(Outcomes.OUTCOME_ASSASSIN_WIN);
			}
		}

	}

	/**
	 * Starts the game. Sets started to true.
	 */
	public void startGame() {
		this.setStarted(true);
	}

	/**
	 * Sends the position of a unit to all human players.
	 * 
	 * @param unit
	 *            The unit whose position is to be sent
	 */
	public void sendPosition(Unit unit) {
		Message msg = new Message(Message.UNIT_MOVED, new int[] { unit.getUnitID(), 0 }, null,
				new double[] { unit.getX(), unit.getY(), unit.getDir(), unit.getXVel(), unit.getYVel() });

		playerMap.values().forEach(p -> p.getSender().put(msg));
	}

	/**
	 * This method is called when player connection is broken
	 * 
	 * @param player
	 */
	public void playerConnectionBroken(Player player) {
		// Get the unit ID
		int unitID = player.getUnitID();

		// Remove the player
		this.playerMap.remove(unitID);

		// no more players left, end the game
		if (this.playerMap.size() == 0) {
			this.running = false;
			return;
		}

		// Get the unit object
		Unit unit = this.getUnitFromID(unitID);
		AIUnitFactory.takeOverUnit(this, unit);

		// Notify other players via lobby message
		Message msg = new Message(Message.SYSTEM_MESSAGE, null,
				new String[] { player.getPlayerName() + " has left and will be replaced by an AI." });
		for (Player eachplayer : this.getPlayers()) {
			eachplayer.getSender().put(msg);
		}
	}

	/**
	 * Creates a new grenade timer to wrap around a grenade and detect explosion
	 * 
	 * @param grenade
	 *            The grenade to be wrapped
	 */
	public void newGrenadeTimer(Grenade grenade) {
		grenadeTimers.put(grenade, new GrenadeTimer(this, grenade));
	}

	/**
	 * Called when a grenade has exploded. Detects damage including blast shield
	 * protected.
	 * 
	 * @param grenade
	 *            The grenade that exploded
	 */
	public void grenadeExploded(Grenade grenade) {
		for (Player player : playerMap.values()) {
			player.getSender().put(new Message(Message.GRENADE_EXPLODED,
					new int[] { grenade.getThrower().getUnitID(), grenade.getGrenadeID() }, null));
		}
		grenadeTimers.remove(grenade);

		Collection<Unit> units = gameData.getUnits().values();

		for (Unit unit : units) {
			// Dead units shouldn't be damaged. Check if the candidate unit is
			// an enemy and not the thrower itself.
			if (unit.isDead() || (!grenade.getThrower().canTarget(unit)
					&& unit.getUnitID() != grenade.getThrower().getUnitID())) {
				continue;
			}
			double proximity = grenade.getCenterPoint().distance(unit.getCenterPoint());
			int damageSoFar = Grenade.GRENADE_DAMAGE;

			if (proximity >= Grenade.GRENADE_EFFECT_AREA) {
				continue;
			}

			// Check obstacles and blast shield
			if (!gameData.getMap().lineOfSight(grenade.getAbsoluteBounds(), unit.getAbsoluteBounds())) {
				continue;
			}

			double effect = 1 - (proximity / Grenade.GRENADE_EFFECT_AREA);
			damageSoFar *= effect;

			if (unit.getWeapon() == Unit.SHIELD && !unit.getBlastShield().isBroken()) {
				BlastShield shield = unit.getBlastShield();
				shield.takeDamage((int) (damageSoFar * 0.8));
				damageSoFar *= 0.2;
			}

			if (damageSoFar < 0) {
				throw new RuntimeException("Why are grenades healing?");
			}

			// Set the player's health
			unit.reduceHP(damageSoFar);

		}
		updateHPValues();
	}

	/**
	 * Updates the HP for all units in the game and their blast shields if they
	 * have one
	 */
	private void updateHPValues() {
		for (Unit unit : gameData.getUnits().values()) {
			updateHPForUnit(unit);
			if (unit.getBlastShield() != null) {
				for (Player player : playerMap.values()) {
					player.getSender().put(new Message(Message.SHIELD_HP_LEFT,
							new int[] { unit.getUnitID(), unit.getBlastShield().getHP() }));
				}
			}
		}
	}

	/**
	 * Sends the new HP for a given unit to all players
	 * 
	 * @param unit
	 *            The unit whose HP is to be sent
	 */
	private void updateHPForUnit(Unit unit) {
		for (Player player : playerMap.values()) {
			player.getSender().put(new Message(Message.HP_LEFT, new int[] { unit.getUnitID(), unit.getHP() }, null));
		}
	}

	/**
	 * Called when a unit is shot. If the unit is holding a blast shield, the
	 * shield takes damage instead.
	 * 
	 * @param unit
	 *            The unit that was shot
	 * @param damage
	 *            The damage of the bullet.
	 */
	public void unitShot(Unit unit, int damage) {
		if (unit.getWeapon() == Unit.SHIELD && !unit.getBlastShield().isBroken()) {
			unit.getBlastShield().takeDamage(damage);
		} else {
			unit.reduceHP(damage);
		}
		updateHPValues();
	}

	/**
	 * This method is call when game ends
	 * 
	 * @param outcome
	 *            The outcome of the game.
	 */
	public void endGame(int outcome) {
		running = false;
		mgmt.endGame(this, outcome);
	}

	/**
	 * Get lobby ID
	 * 
	 * @return lobby ID
	 */
	public int getLobbyID() {
		return this.lobbyid;
	}

	/**
	 * Retreives a unit object from given ID
	 * 
	 * @param unitID
	 *            The unit's ID
	 * @return The unit object
	 */
	public Unit getUnitFromID(int unitID) {
		if (!gameData.getUnits().containsKey(unitID))
			return null;
		return gameData.getUnits().get(unitID);
	}

	/**
	 * Returns whether the game has started
	 * 
	 * @return True iff the game has started
	 */
	public boolean hasStarted() {
		return started;
	}

	/**
	 * Set whether the game has started (countdown finished)
	 * 
	 * @param started
	 *            New game started state
	 */
	public void setStarted(boolean started) {
		this.started = started;
	}

	/**
	 * @return The game message queuer that processess message
	 */
	public GameMessageQueuer getQueuer() {
		return queuer;
	}

	/**
	 * @return The game data object for this game.
	 */
	public GameData getGameData() {
		return gameData;
	}

	/**
	 * @return The map of UnitID to player.
	 */
	public Map<Integer, Player> getPlayerMap() {
		return playerMap;
	}

}
