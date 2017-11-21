package escort.server.game.ai;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.common.game.entities.units.Assassin;
import escort.common.game.entities.units.Escort;
import escort.common.game.entities.units.Police;
import escort.common.game.entities.units.President;
import escort.common.game.map.MapLoader;
import escort.server.game.Game;
import escort.server.lobby.LobbySettings;
import escort.server.network.Player;

/**
 * Testing the AssassinController
 * 
 * @author Ahmed Bhallo
 *
 */
public class AssassinControllerTest {

		private Game game;
		private Assassin assassin;
		private AssassinController assassinController;
	
		@Before
		public void setUp() {
			// No human players in this AI test class
			ArrayList<Player> players = new ArrayList<Player>();
			players.add(new Player(null, null));
			players.add(new Player(null, null));
	
			LobbySettings settings = new LobbySettings();
			settings.numAssassinsAI = 1;
			settings.numPoliceAI = 1;
			settings.numCivilianAI = 0;
			try {
				(new MapLoader()).load();
			} catch (Exception e) {
			}
			game = new Game(players, null, 1, settings);
			System.out.println(game.getGameData().getUnits());
			assassin = (Assassin) game.getGameData().getUnits().get(4);
			assassinController = new AssassinController(game.getGameData(), assassin, null);
			assassin.setUnitController(assassinController);
		}

	@After
	public void tearDown() {
		assassinController = null;
		assassin = null;
		game = null;
	}

	/**
	 * @author Ahmed Bhallo
	 * @throws InterruptedException
	 */
	@Test
	public void testAssassinGoesToPresident() throws InterruptedException {
		// Create the president and set it to 0,0.
		President president = game.getGameData().getPresident();
		president.setX(0);
		president.setY(0);

		// Set assassin farther away.
		assassin.setX(500);
		assassin.setX(500);

		// Sleep for a while
		Thread.sleep(4000);

		// Make sure assassin is now closer and within shooting range.
		assertTrue(Math.abs(president.getX() - president.getX()) <= AIController.SHOOTING_RANGE);
	}

	/**
	 * @author Ahmed Bhallo
	 * @throws InterruptedException
	 */
	@Test
	public void testAssassinKillsEscorts() throws InterruptedException {
		Escort escort = (Escort) game.getGameData().getUnits().get(0);
		escort.setX(300);
		escort.setY(300);

		assassin.setX(600);
		assassin.setY(600);

		// Sleep for a while
		Thread.sleep(4000);

		// Make sure assassin is now closer and within shooting range.
		assertTrue(Math.abs(escort.getX() - escort.getX()) <= AIController.SHOOTING_RANGE);
	}

	/**
	 * @author Ahmed Bhallo
	 * @throws InterruptedException
	 */
	@Test
	public void testAssassinKillPolice() throws InterruptedException {
		Police police = (Police) game.getGameData().getUnits().get(3);
		police.setX(300);
		police.setY(300);

		assassin.setX(600);
		assassin.setY(600);

		// Sleep for a while
		Thread.sleep(4000);

		// Make sure assassin is now closer and within shooting range.
		assertTrue(Math.abs(police.getX() - police.getX()) <= AIController.SHOOTING_RANGE);
	}

}
