package escort.server.game.ai;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import escort.common.game.entities.units.Escort;
import escort.common.game.entities.units.President;
import escort.common.game.map.MapLoader;
import escort.common.game.routePlanning.AStarSearch;
import escort.common.game.routePlanning.PlanningMap;
import escort.common.game.routePlanning.RoutePlanner;
import escort.server.game.Game;
import escort.server.lobby.LobbySettings;
import escort.server.network.Player;

public class EscortControllerTest {

	private Game game;
	private Escort escort;
	private EscortController escortController;

	@Before
	public void setUp() {
		// No human players in this AI test class
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(new Player(null, null));

		LobbySettings settings = new LobbySettings();
		settings.numAssassinsAI = 0;
		settings.numPoliceAI = 1;
		settings.numCivilianAI = 0;
		try {
			(new MapLoader()).load();
		} catch (Exception e) {
		}
		game = new Game(players, null, 1, settings);
		RoutePlanner planner = new AStarSearch();
		planner.setMap(PlanningMap.createFromGameMap(game.getGameData().getMap()));
		System.out.println(game.getGameData().getUnits());
		escort = (Escort) game.getGameData().getUnits().get(0);
		escortController = new EscortController(game.getGameData(), escort, planner);
		escort.setUnitController(escortController);
		escort.setSender(new AISender(game.getQueuer()));
		game.setStarted(true);
	}

	@Test
	public void escortWalksToPresident() throws InterruptedException {
		President pres = game.getGameData().getPresident();
		pres.setX(60);
		pres.setY(60);

		escort.setX(600);
		escort.setY(600);

		// Sleep for a bit
		Thread.sleep(1000);

		// Ensure escort has moved closer towards president
		assertTrue(escort.getX() < 600);
		assertTrue(escort.getY() < 600);
	}

	@Test
	public void escortRequestsFollowPresident() throws InterruptedException {
		President pres = game.getGameData().getPresident();
		pres.setX(60);
		pres.setY(60);

		escort.setX(60);
		escort.setY(60);

		// Ensure president isn't following and escort isn't a follower.
		assertFalse(pres.isFollowing());
		assertFalse(escort.isFollower());

		// Sleep for a bit
		Thread.sleep(1000);

		assertTrue(pres.isFollowing());
		assertTrue(escort.isFollower());
	}

}
