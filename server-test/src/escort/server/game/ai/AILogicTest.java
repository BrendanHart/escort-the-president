package escort.server.game.ai;

import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;
import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import escort.common.game.entities.units.Unit;
import escort.common.game.map.MapLoader;
import escort.common.game.routePlanning.AStarSearch;
import escort.common.game.routePlanning.PlanningMap;
import escort.common.game.routePlanning.RoutePlanner;
import escort.server.game.Game;
import escort.server.lobby.LobbySettings;

/**
 * JUnit tests for AI Controllers
 */
public class AILogicTest {

	private Game game;
	private Unit unit;

	@Before
	/**
	 * @author Kwong-Hei Tsang
	 * @author James Birch
	 * @author Edward Dean
	 * @author Ahmed Bhallo
	 */
	public void setUp() {
		LobbySettings settings = new LobbySettings();
		settings.numAssassinsAI = 0;
		settings.numPoliceAI = 0;
		settings.numCivilianAI = 1;
		try {
			(new MapLoader()).load();
		} catch (Exception e) {
		}
		game = new Game(new ArrayList<>(), null, 1, settings);
		game.setStarted(true);
		unit = game.getUnitFromID(1);
	}

	@Test
	/**
	 * escort.server.game.ai.AISender@4fccd51b
	 * 
	 * @author Edward Dean
	 */
	public void testUpdateDirection() {
		AIController controller = (AIController) unit.getUnitController();

		unit.setXVel(0);
		unit.setYVel(-1);
		controller.updateDirection();
		assertTrue(unit.getDir() == 0.0);

		unit.setXVel(0.5);
		unit.setYVel(0.5);
		controller.updateDirection();
		assertTrue(unit.getDir() == 3 * Math.PI / 4);

		unit.setXVel(0);
		unit.setYVel(0);
		controller.updateDirection();
		assertTrue(unit.getDir() == Math.PI);
	}

	@Test
	/**
	 * @author Edward Dean
	 */
	public void testTraverseRoute() throws InterruptedException {
		RoutePlanner planner = new AStarSearch();
		planner.setMap(PlanningMap.createFromGameMap(game.getGameData().getMap()));
		AIController cont = new AIController(game.getGameData(), unit, planner) {
			@Override
			public void control() {
				traverseRoute();
			}
		};
		unit.setUnitController(cont);
		for (int i = 0; i < 1; i++) {
			Rectangle nextTile = cont.getRandomTile();

			cont.generateRoute(nextTile);

			while (!cont.getRoute().isEmpty()) {
				Thread.sleep(100);
			}

			assertTrue(unit.getAbsoluteBoundsInTiles().equals(nextTile));
		}
	}

}