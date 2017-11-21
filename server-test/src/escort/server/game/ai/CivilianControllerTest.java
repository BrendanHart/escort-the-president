package escort.server.game.ai;

import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;
import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.common.game.entities.units.Civilian;
import escort.common.game.map.MapLoader;
import escort.server.game.Game;
import escort.server.lobby.LobbySettings;
import escort.server.network.Player;
import escort.server.network.ServerSide;

public class CivilianControllerTest {

	private Game game;
	private Civilian civ;
	private CivilianController civController;

	@Before
	public void setUp() {
		// No human players in this AI test class
		ServerSide ss = new ServerSide(null);
		ArrayList<Player> players = new ArrayList<Player>();
		LobbySettings settings = new LobbySettings();
		settings.numAssassinsAI = 0;
		settings.numPoliceAI = 0;
		settings.numCivilianAI = 1;

		try {
			(new MapLoader()).load();
		} catch (Exception e) {
		}
		game = new Game(players, ss.getLobbyManagement(), 1, settings);
		System.out.println(game.getGameData().getUnits());
		civ = (Civilian) game.getGameData().getUnits().get(1);
		civController = new CivilianController(game.getGameData(), civ, null);
		civ.setUnitController(civController);
	}

	@After
	public void tearDown() {
		civController = null;
		civ = null;
		game = null;
	}

	@Test
	public void testThatAValidRouteIsGenerated() {
		// Test n times that a route is okay
		for (int i = 0; i < 100000; i++) {

			// Set the civ to a random position in the map
			game.spawn(civ);

			// Generate a new route and test it
			civController.control();
			for (Rectangle rect : civController.getRoute()) {
				assertTrue(game.getGameData().getMap().walkableRect(rect));
			}
			civ.setHP(0);
			civ.died();
			civ.setHP(civ.getMaxHP());
		}
	}

	@Test
	public void routeIsClearedOnceCivilianDies() {
		civController.control();
		civ.setHP(0);
		civ.died();
		assertTrue(civController.getRoute().isEmpty());
	}

}
