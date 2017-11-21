package escort.server.game.ai;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

import escort.common.game.entities.units.Assassin;
import escort.common.game.entities.units.Police;
import escort.common.game.map.MapLoader;
import escort.server.game.Game;
import escort.server.lobby.LobbySettings;
import escort.server.network.Player;

public class PoliceControllerTest {
	private Game game;
	private Police police;
	private PoliceController policeController;

	@Before
	public void setUp() {
		// No human players in this AI test class
		ArrayList<Player> players = new ArrayList<Player>();

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
		police = (Police) game.getGameData().getUnits().get(1);
		policeController = new PoliceController(game.getGameData(), police, null);
		police.setUnitController(policeController);
		police.setSender(new AISender(game.getQueuer()));
		game.setStarted(true);
	}

	@Test
	public void shootAssassinIfNear() throws InterruptedException {
		Assassin assassin =(Assassin) game.getGameData().getUnits().get(2);
		assassin.setX(50);
		assassin.setY(50);
		
		police.setX(50);
		police.setY(50);
		assertTrue(assassin.getHP() == assassin.getSpawnHealth());

		Thread.sleep(500);
		assertTrue(assassin.getHP() < assassin.getSpawnHealth());
		
	}
}
