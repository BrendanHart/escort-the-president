package escort.common.game.routePlanning;

import static org.junit.Assert.assertTrue;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AStarSearchTest {

    private PlanningMap planningMap;
    private AStarSearch search;
    private List<Point> obstacles;
    private Random random;

    @Before
    public void setUp() {

        obstacles = new ArrayList<Point>();
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

        planningMap = new PlanningMap(10, 10, obstacles.toArray(new Point[obstacles.size()]));
        search = new AStarSearch();
        search.setMap(planningMap);

        random = new Random();
    }

    @After
    public void tearDown() {

        planningMap = null;
        search = null;

    }

    @Test
    public void testSettingOfMap() {
        assertTrue(planningMap == search.getMap());
    }

    @Test
    public void testRoutePlanning() {
        // Test 10,000 times
        for(int n = 0; n < 10000; n++) {
            // Select random point for start
            Rectangle start = new Rectangle(0,0,0,0);
            do {
                start.x = random.nextInt(planningMap.getWidth()); 
                start.y = random.nextInt(planningMap.getHeight()); 
            } while(planningMap.obstacle(start));

            // Select random point for end
            Rectangle end = new Rectangle(0,0,0,0);
            do {
                end.x = random.nextInt(planningMap.getWidth()); 
                end.y = random.nextInt(planningMap.getHeight()); 
            } while(planningMap.obstacle(end));

            // Generate route from start to end
            System.out.println(start + " " + end);
            List<Rectangle> route = search.route(start, end);
            System.out.println(route);

            
            Rectangle previous = start;
            for(int i = 0; i < route.size(); i++) {
                Rectangle current = route.get(i);
                if(i == route.size() - 1) {
                    assertTrue(current.equals(end)); 
                }
                assertTrue(!planningMap.obstacle(current));

                assertTrue(Math.abs(previous.x - current.x) <= 1 && Math.abs(previous.y - current.y) <= 1);

                previous = current;
            }
        }

    }

}
