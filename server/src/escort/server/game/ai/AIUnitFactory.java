package escort.server.game.ai;

import escort.common.game.GameData;
import escort.common.game.entities.units.Assassin;
import escort.common.game.entities.units.Civilian;
import escort.common.game.entities.units.Escort;
import escort.common.game.entities.units.Police;
import escort.common.game.entities.units.President;
import escort.common.game.entities.units.Unit;
import escort.common.game.entities.units.UnitController;
import escort.common.game.routePlanning.AStarSearch;
import escort.common.game.routePlanning.PlanningMap;
import escort.common.game.routePlanning.RoutePlanner;
import escort.server.game.Game;

/**
 * The AIUnitFactory generate AI Units or take over existing units
 * @author Brendan Hart
 * @author Kwong Hei Tsang
 *
 */
public class AIUnitFactory {

	/**
	 * Create a AI Uniy
	 * @param game The game logic
	 * @param type The ynit type
	 * @param id The unit ID
	 * @param x 
	 * @param y
	 * @return A new Unit
	 */
	public static Unit makeUnit(Game game, int type, int id, int x, int y) {
		Unit unit;
		GameData gameData = game.getGameData();
		switch (type) {
		case Unit.ASSASSIN_TYPE:
			unit = new Assassin(gameData, null, id);
			break;
		case Unit.PRESIDENT_TYPE:
			unit = new President(gameData, null, id);
			break;
		case Unit.CIVILIAN_TYPE:
			unit = new Civilian(gameData, null, id);
			break;
		case Unit.POLICE_TYPE:
			unit = new Police(gameData, null, id);
			break;
		default:
			return null;
		}
		takeOverUnit(game, unit);
		return unit;
	}
	
	/**
	 * Take over a unit when player's connection is broken
	 * @param game The game logic
	 * @param unit The unit
	 */
	public static void takeOverUnit(Game game, Unit unit){
		RoutePlanner planner = new AStarSearch();
		planner.setMap(PlanningMap.createFromGameMap(game.getGameData().getMap()));
		UnitController controller;
		AISender sender = new AISender(game.getQueuer());
		unit.setSender(sender);
		GameData gameData = game.getGameData();
		switch (unit.getUnitType()) {
		case Unit.ASSASSIN_TYPE:
			controller = new AssassinController(gameData, (Assassin) unit, planner);
			break;
		case Unit.PRESIDENT_TYPE:
			controller = new PresidentController(gameData, (President) unit, planner);
			break;
		case Unit.CIVILIAN_TYPE:
			controller = new CivilianController(gameData, (Civilian) unit, planner);
			break;
		case Unit.POLICE_TYPE:
			controller = new PoliceController(gameData, (Police) unit, planner);
			break;
		case Unit.ESCORT_TYPE:
			controller = new EscortController(gameData, (Escort) unit, planner);
			break;
		default:
			return;
		}
		unit.setUnitController(controller);
	}
}
