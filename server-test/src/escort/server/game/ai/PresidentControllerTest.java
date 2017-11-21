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

public class PresidentControllerTest {

	private Game game;
	private Escort escort;
	private President president;

	@Before
	public void setUp() {
		// No human players in this AI test class
		ArrayList<Player> players = new ArrayList<Player>();
		players.add(new Player(null, null));
		LobbySettings settings = new LobbySettings();
		settings.numAssassinsAI = 0;
		settings.numPoliceAI = 0;
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
		president = game.getGameData().getPresident();
		PresidentController presidentController = new PresidentController(game.getGameData(), president, planner);
		president.setUnitController(presidentController);
		AISender sender = new AISender(game.getQueuer());
		escort.setSender(sender);
		game.setStarted(true);
		
	}

	@Test
	public void presidentFollowsEscort() throws InterruptedException{
		escort.setX(60);
		escort.setY(60);
		president.setX(60);
		president.setY(60);
		assertFalse(president.isFollowing());
		assertFalse(escort.isFollower());
		escort.follow();
		Thread.sleep(1000);
		assertTrue(escort.isFollower());
		assertTrue(president.isFollowing());
	}

}
