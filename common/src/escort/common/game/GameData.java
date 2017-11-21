package escort.common.game;

import java.util.Map;

import escort.common.game.entities.units.President;
import escort.common.game.entities.units.Unit;
import escort.common.game.map.GameMap;

public class GameData {

	private final GameMap map;
	private final Map<Integer, Unit> units;
	private President president;

	/**
	 * Create the game data object to store all the units and the map of the
	 * game
	 * 
	 * @param map
	 *            The game map
	 * @param units
	 *            All the units in the game
	 */
	public GameData(GameMap map, Map<Integer, Unit> units) {
		this.map = map;
		this.units = units;
	}

	/**
	 * Return the current map that is being played
	 * 
	 * @return The map
	 */
	public GameMap getMap() {
		return map;
	}

	/**
	 * Return all the units in the game
	 * 
	 * @return The units in the game
	 */
	public Map<Integer, Unit> getUnits() {
		return units;
	}

	/**
	 * Get the president object in the game
	 * 
	 * @return The president object
	 */
	public President getPresident() {
		return president;
	}

	/**
	 * Set the president object in the game
	 * 
	 * @param president
	 *            president object
	 */
	public void setPresident(President president) {
		this.president = president;
	}
}