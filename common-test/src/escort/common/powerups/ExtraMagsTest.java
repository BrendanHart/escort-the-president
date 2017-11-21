package escort.common.powerups;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.common.game.entities.units.*;
import escort.common.game.GameData;

import static org.junit.Assert.assertTrue;

public class ExtraMagsTest {

    private Unit unit;
    private ExtraMags powerUp;

    @Before
    /**
     * @author Brendan Hart
     */
    public void setUp() {
        unit = new Assassin(new GameData(null, null), null, 1);
        powerUp = new ExtraMags(0, 0);
        powerUp.setCooldown(1);
    }

    @After
    /**
     * @author Brendan Hart
     */
    public void tearDown() {
        unit = null;
        powerUp = null;
    }
    
    @Test
    /**
     * @author Brendan Hart
     */
    public void bulletCountIncreasesByCorrectAmountWhenPickedUp() {
        int bulletCount = unit.getMG().getBulletsInBag();
        powerUp.pickup(unit);
        assertTrue(unit.getMG().getBulletsInBag() == (bulletCount + ExtraMags.NUM_MAGS * unit.getMG().getFullMag()));
    }

    @Test
    /**
     * @author Brendan Hart
     */
    public void testWhenTwoAttemptsNotSeparatedByCooldown() {
        powerUp.pickup(unit);
        int bulletCount = unit.getMG().getBulletsInBag();
        powerUp.pickup(unit);
        assertTrue(unit.getMG().getBulletsInBag() == bulletCount);
    }

    @Test
    /**
     * @author Brendan Hart
     */
    public void testWhenTwoAttemptsSeparatedByCooldown() {
        powerUp.pickup(unit);
        try {
            Thread.sleep(powerUp.getCooldown() * 1000);
        } catch (InterruptedException e) {
            System.err.println("InterruptedException in ExtraMagsTest");
        }
        int bulletCount = unit.getMG().getBulletsInBag();
        System.out.println(unit.getMG().getBulletsInBag());
        powerUp.pickup(unit);
        System.out.println(unit.getMG().getBulletsInBag());
        assertTrue(unit.getMG().getBulletsInBag() == (bulletCount + ExtraMags.NUM_MAGS * unit.getMG().getFullMag()));
    }

    @Test
    /**
     * @author Brendan Hart
     */
    public void testActiveWhenCooldownPeriodHasPassed() {
        
        powerUp.pickup(unit);

        try {
            Thread.sleep(powerUp.getCooldown() * 1000);
        } catch (InterruptedException e) {
            System.err.println("InterruptedException in ExtraMagsTest");
        }

        assertTrue(powerUp.isActive());

    }

    @Test
    /**
     * @author Brendan Hart
     */
    public void testInactiveWhenCooldownHasNotPassed() {

        powerUp.pickup(unit);

        assertTrue(!powerUp.isActive());

    }

}
