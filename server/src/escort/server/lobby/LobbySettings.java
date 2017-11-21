package escort.server.lobby;

import escort.common.game.map.GameMap;

/**
 * Represents the settings of a lobby
 * 
 * @author Kwong Hei Tsang
 *
 */
public class LobbySettings {

	public int numAssassinsAI;
	public int numPoliceAI;
	public int numCivilianAI;
	public int mapID;

	/**
	 * Construct a lobby  settings object with default settings
	 */
	public LobbySettings() {
		this.mapID = GameMap.HOTEL_ID;
		this.numPoliceAI = 3;
		this.numCivilianAI = 3;
		this.numAssassinsAI = 3;
	}

	/**
	 * Get a copy of the lobby settings
	 * 
	 * @return A copy of the LobbySettings
	 */
	public LobbySettings copy() {
		LobbySettings settings = new LobbySettings();
		settings.numAssassinsAI = this.numAssassinsAI;
		settings.numPoliceAI = this.numPoliceAI;
		settings.numCivilianAI = this.numCivilianAI;
		settings.mapID = this.mapID;
		return settings;
	}

	@Override
	public String toString() {
		return "Map: " + mapID + "\nPoliceAI: " + numPoliceAI + "\nAssassinAI: " + numAssassinsAI + "\nCivilianAI: "
				+ numCivilianAI;
	}
}
