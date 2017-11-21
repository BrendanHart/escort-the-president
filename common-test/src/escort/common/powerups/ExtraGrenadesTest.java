package escort.common.powerups;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.common.game.entities.units.*;
import escort.common.game.GameData;

import static org.junit.Assert.assertTrue;

public class ExtraGrenadesTest {

    private Unit unit;
    private ExtraGrenades powerUp;

    @Before
    /**
     * @author Brendan Hart
     */
    public void setUp() {
        unit = new Assassin(new GameData(null, null), null, 1);
        powerUp = new ExtraGrenades(0, 0);
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
    public void grenadeCountIncreasesByTwoWhenPickedUp() {
        int grenadeCount = unit.getGrenadesLeft();
        powerUp.pickup(unit);
        assertTrue(unit.getGrenadesLeft() == (grenadeCount + ExtraGrenades.EXTRA_GRENADES));
    }

    @Test
    /**
     * @author Brendan Hart
     */
    public void testWhenTwoAttemptsNotSeparatedByCooldown() {
        powerUp.pickup(unit);
        int grenadeCount = unit.getGrenadesLeft();
        powerUp.pickup(unit);
        assertTrue(unit.getGrenadesLeft() == grenadeCount);
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
            System.err.println("InterruptedException in ExtraGrenadesTest");
        }
        int grenadeCount = unit.getGrenadesLeft();
        powerUp.pickup(unit);
        assertTrue(unit.getGrenadesLeft() == (grenadeCount + ExtraGrenades.EXTRA_GRENADES));
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
            System.err.println("InterruptedException in ExtraGrenadesTest");
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
