package escort.common.game.weapons;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.common.game.GameData;
import escort.common.game.entities.units.Escort;
import escort.common.game.entities.units.Unit;
import escort.common.game.map.GameMap;
import escort.common.game.map.MapLoader;

public class GrenadeTest {
	
	private Grenade grenade;
	private Unit escort;
	private GameData gameData;

	@Before
    public void setUp() {
		try {
			new MapLoader().load();
		} catch (IOException e) {
			System.out.println("Oh");
			e.printStackTrace();
		}
		gameData = new GameData(GameMap.loadFromID(0), null);
		escort = new Escort(gameData, null, 0);
		grenade = new Grenade(gameData, 0, 0, 0, escort);
    }

    @After
    public void tearDown() {
    	escort = null;
    	grenade = null;
    	gameData = null;
    }
    
    @Test
    public void correctReleaseDirection() {
    	double dir = Math.PI;
    	escort.setDir(dir);
    	
    	grenade.release();
    	
    	assertTrue(grenade.getXVel() == Math.sin(dir));
    	assertTrue(grenade.getYVel() == -Math.cos(dir));
    	
    	dir = 3 * Math.PI;
    	escort.setDir(dir);
    	
    	grenade.release();
    	
    	assertTrue(grenade.getXVel() == Math.sin(dir));
    	assertTrue(grenade.getYVel() == -Math.cos(dir));
    }

}
