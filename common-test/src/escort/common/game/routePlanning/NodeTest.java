package escort.common.game.routePlanning;

import java.awt.Rectangle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class NodeTest {

    private Node node;
    private Node parent;

    @Before
    public void setUp() {
    
        parent = new Node(null, new Rectangle(0,0,0,0), 0, 10);
        node = new Node(parent, new Rectangle(0,1,0,0), 5, 5);
        
    }

    @After
    public void tearDown() {

        parent = null;
        node = null;
        
    }

    @Test
    public void testGetParent() {
        assertTrue(node.getParent() == parent);
    }

    @Test
    public void testGetterMethods() {
        assertTrue(node.getPathCost() == 5);
        assertTrue(parent.getPathCost() == 0);
        assertTrue(node.getRect().equals(new Rectangle(0,1,0,0)));
        assertTrue(parent.getRect().equals(new Rectangle(0,0,0,0)));
        assertTrue(parent.getEstimatedCost() == 10);
        assertTrue(node.getEstimatedCost() == 10);
    }

    @Test
    public void testEquals() {
        assertTrue(!node.equals(parent));
        Node shouldBeEqualNode = new Node(null, new Rectangle(0,0,0,0), 4, 2);
        assertTrue(parent.equals(shouldBeEqualNode));
    }

}
