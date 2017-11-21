package escort.server.game;

import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.common.game.Outcomes;
import escort.common.game.entities.units.Police;
import escort.common.game.entities.units.Unit;
import escort.server.game.ai.PoliceController;
import escort.server.lobby.LobbySettings;
import escort.server.network.Player;
import escort.server.network.ServerSide;

/**
 * JUnit tests for AI Controllers
 */
public class TraverseRoute {

	private Game game;
	private ServerSide ss;

	@Before
	/**
	 * @author Kwong-Hei Tsang
	 * @author James Birch
	 * @author Edward Dean
	 */
	public void setUp() {
		// No human players in this AI test class
		ss = new ServerSide(null);
		ArrayList<Player> players = new ArrayList<Player>();

		// construct the game logic, and configure the lobby settings here
		LobbySettings settings = new LobbySettings();
		settings.numAssassinsAI = 0;
		settings.numPoliceAI = 1;
		settings.numCivilianAI = 0;
		game = new Game(players, ss.getLobbyManagement(), 1, settings);

		// start game
		game.startGame();
		assertTrue(game.hasStarted()); // Game should have started by this point
	}

	@After
	/**
	 * @author James Birch
	 */
	public void tearDown() {
		game.endGame(Outcomes.OUTCOME_DRAW);
		ss.shutdownServer();
		game = null;
		ss = null;
	}

	@Test
	/**
	 * @author Edward Dean
	 */
	public void testTraverseRoute() {
		Collection<Unit> units = game.getGameData().getUnits().values();
		Police police = null;

		for (Unit u : units) {
			if (u.getUnitType() == Unit.POLICE_TYPE) {
				police = (Police) u;
			}
		}

		PoliceController policeAI = (PoliceController) police.getUnitController();
		police.setUnitController(null);

		for (int i = 0; i < 5; i++) {
			Rectangle nextTile = policeAI.getRandomTile();

            System.out.println("TILE: " + nextTile);

            policeAI.generateRoute(nextTile);

            System.out.println(policeAI.getRoute());

			assertTrue(!policeAI.getRoute().isEmpty());

			while (!policeAI.getRoute().isEmpty()) {
                System.out.println(police.getAbsoluteBoundsInTiles());
				policeAI.traverseRoute();
				police.update();
			}

			assertTrue(police.getAbsoluteBoundsInTiles().equals(nextTile));
		}
	}

}
