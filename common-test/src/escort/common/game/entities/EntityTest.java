package escort.common.game.entities;

import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.common.game.GameData;
import escort.common.game.map.GameMap;
import escort.common.game.map.MapLoader;

public class EntityTest {
	
	private Entity entity1;
	private Entity entity2;
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
		entity1 = new Entity(0, 0, 20, 30, gameData);
		entity2 = new Entity(0, 0, 20, 30, gameData);
    }

    @After
    public void tearDown() {
    	entity1 = null;
    	entity2 = null;
    	gameData = null;
    }
    
    @Test
    public void correctSetAndGet() {
    	entity1.setX(40);
    	entity1.setY(50);
    	
    	assertTrue(40 == entity1.getX());
    	assertTrue(50 == entity1.getY());
    }
    
    @Test
    public void correctCenterPoint() {
    	entity1.setX(70);
    	entity1.setY(30);
    	assertTrue(entity1.getCenterPoint().equals(new Point(80, 45)));
    	
    	entity2.setX(50);
    	entity2.setY(100);
    	assertTrue(entity2.getCenterPoint().equals(new Point(60, 115)));
    }
    
    @Test
    public void coreectLineOfSight() {
    	entity1.setX(305);
    	entity1.setY(520);
    	
    	entity2.setX(305);
    	entity2.setY(710);
    	assertTrue(!gameData.getMap().lineOfSight(entity1.getHitbox(),entity2.getHitbox()));
    	
    	entity1.setX(220);
    	entity1.setY(520);
    	
    	entity2.setX(220);
    	entity2.setY(710);
    	assertTrue(gameData.getMap().lineOfSight(entity1.getHitbox(),entity2.getHitbox()));
    	
    	entity1.setX(990);
    	entity1.setY(535);
    	
    	entity2.setX(860);
    	entity2.setY(1105);
    	assertTrue(!gameData.getMap().lineOfSight(entity1.getHitbox(),entity2.getHitbox()));
    }
    
    @Test
    public void correctCollision() {
    	entity1.setX(10);
    	entity1.setY(10);
    	
    	entity2.setX(15);
    	entity2.setY(10);
    	assertTrue(entity1.collision(entity2));
    	
    	entity1.setX(10);
    	entity1.setY(10);
    	
    	entity2.setX(10);
    	entity2.setY(10);
    	assertTrue(entity1.collision(entity2));
    	
    	entity1.setX(10);
    	entity1.setY(10);
    	
    	entity2.setX(10);
    	entity2.setY(41);
    	assertTrue(!entity1.collision(entity2));
    }
}