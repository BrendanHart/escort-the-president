package escort.common.game.routePlanning;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.Rectangle;
import java.awt.Point;
import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

import escort.common.game.map.GameMap;
import escort.common.game.map.MapLoader;

public class PlanningMapTest {

    private PlanningMap planningMap;
    private GameMap mapToCreate;

    @Before
    public void setUp() {

        try { (new MapLoader()).load(); }catch(Exception e) {}
        mapToCreate = GameMap.loadFromID(GameMap.HOTEL_ID);
        planningMap = PlanningMap.createFromGameMap(mapToCreate);
        
    }

    @After
    public void tearDown() {

        planningMap = null;
        mapToCreate = null;
        
    }

    @Test
    public void testCreationOfPlanningMapFromGameMap() {
        for(int i = 0; i < mapToCreate.getWidthInTiles(); i++) {
            for(int j = 0; j < mapToCreate.getHeightInTiles(); j++) {
                assertTrue(mapToCreate.walkableTile(i, j) == !planningMap.obstacle(new Rectangle(i, j, 0, 0)));
            }
        }
    }

    @Test
    public void testSetObstacle() {
        // Find a point that's not an obstacle and set it as one.
        for(int i = 0; i < mapToCreate.getWidthInTiles(); i++) {
            for(int j = 0; j < mapToCreate.getHeightInTiles(); j++) {
                if(!planningMap.obstacle(new Rectangle(i, j, 0, 0))) {
                    planningMap.setObstacle(i, j);
                    assertTrue(planningMap.obstacle(new Rectangle(i, j, 0, 0)));
                }
            }
        }
    }

    @Test
    public void testHeightIsCorrect() {
        assertTrue(planningMap.getHeight() == mapToCreate.getHeightInTiles());
    }

    @Test
    public void testWidthIsCorrect() {
        assertTrue(planningMap.getWidth() == mapToCreate.getWidthInTiles());
    }

    @Test
    public void testInBounds() {
        assertTrue(planningMap.inBounds(new Rectangle(0,0,planningMap.getWidth()-1, planningMap.getHeight()-1)));
    }

    @Test
    public void testPlanningMapConstructor() {
        planningMap = new PlanningMap(5, 5);
        assertTrue(planningMap.getWidth() == 5);
        assertTrue(planningMap.getHeight() == 5);
        for(int i = 0; i < planningMap.getWidth(); i++) {
            for(int j = 0; j < planningMap.getHeight(); j++) {
                assertTrue(!planningMap.obstacle(new Rectangle(i, j, 0, 0))); 
            }
        }

        List<Point> obstacles = new ArrayList<Point>();
        for(int i = 0; i < 10; i++) {
            obstacles.add(new Point(i, 0));
            obstacles.add(new Point(i, 9));
            obstacles.add(new Point(0, i));
            obstacles.add(new Point(9, i));
        }
        obstacles.add(new Point(1,2)); 
        obstacles.add(new Point(2,2)); 
        obstacles.add(new Point(3,2)); 
        obstacles.add(new Point(4,2)); 
        obstacles.add(new Point(4,3)); 
        obstacles.add(new Point(4,4)); 
        obstacles.add(new Point(4,5));
        obstacles.add(new Point(4,6));
        obstacles.add(new Point(4,7));
        obstacles.add(new Point(4,8));
        planningMap = new PlanningMap(10, 10, obstacles.toArray(new Point[obstacles.size()]));
        for(Point o : obstacles) {
            assertTrue(planningMap.obstacle(new Rectangle(o.x, o.y, 0, 0)));
        }

    }

}
