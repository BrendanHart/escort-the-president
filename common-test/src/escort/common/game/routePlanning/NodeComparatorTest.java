package escort.common.game.routePlanning;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class NodeComparatorTest {

    private NodeComparator comparator;

    @Before
    public void setUp() {
    
        comparator = new NodeComparator();
        
    }

    @After
    public void tearDown() {

        comparator = null;
        
    }

    @Test
    public void testCompare() {
        Node n1, n2;
        n1 = new Node(null, null, 10, 10);
        n2 = new Node(null, null, 5, 5);
        assertTrue(comparator.compare(n1, n2) > 0);
        n2 = new Node(null, null, 0, 25);
        assertTrue(comparator.compare(n1, n2) < 0);
        n2 = new Node(null, null, 10, 10);
        assertTrue(comparator.compare(n1, n2) == 0);
    }

}
