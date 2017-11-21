package escort.common.game;

import java.util.Map;

import escort.common.game.entities.units.UnitInfo;

import java.io.Serializable;

/**
 * Contains the state of the game used to transmit information over the network.
 * 
 * @author Brendan Hart
 *
 */
public class GameState implements Serializable {

	private static final long serialVersionUID = 7176445211131574117L;
	private final int mapID;
	private final Map<Integer, UnitInfo> units;

	/**
	 * Create a new game state object
	 * 
	 * @param units
	 *            The units and info
	 * @param mapID
	 *            The map id
	 */
	public GameState(Map<Integer, UnitInfo> units, int mapID) {
		this.units = units;
		this.mapID = mapID;
	}

	/**
	 * Get all the units and their info
	 * 
	 * @return The units and their info
	 */
	public Map<Integer, UnitInfo> getUnitsInfo() {
		return units;
	}

	/**
	 * Get the map id of the game
	 * 
	 * @return The map id
	 */
	public int getMapID() {
		return mapID;
	}
}