package escort.common.powerups;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.common.game.GameData;
import escort.common.game.entities.units.Assassin;
import escort.common.game.entities.units.Unit;

public class ReplenishHealthTest {

    private Unit unit;
    private ReplenishHealth powerUp;

    @Before
    /**
     * @author Brendan Hart
     */
    public void setUp() {
        unit = new Assassin(new GameData(null, null), null, 1);
        unit.setHP(0);
        powerUp = new ReplenishHealth(0, 0);
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
    public void healthIsReplenishedWhenPickedUp() {
        unit.setHP(1);
        int health = unit.getHP();
        powerUp.pickup(unit);
        int newHealth = health + ReplenishHealth.ADD_HEALTH;
        if(newHealth > unit.getMaxHP()) {
            newHealth = unit.getMaxHP();
        }
        assertTrue(unit.getHP() == newHealth); 
    }

    @Test
    /**
     * @author Brendan Hart
     */
    public void healthDoesNotExceedFullWhenPickedUp() {
        unit.setHP(unit.getMaxHP());
        powerUp.pickup(unit);
        assertTrue(unit.getHP() == unit.getMaxHP()); 
    }

    @Test
    /**
     * @author Brendan Hart
     */
    public void testWhenTwoAttemptsNotSeparatedByCooldown() {
        powerUp.pickup(unit);
        unit.setHP(0);
        powerUp.pickup(unit);
        assertTrue(unit.getHP() == 0);
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
            System.err.println("InterruptedException in ReplenishHealthTest");
        }
        unit.setHP(0);
        powerUp.pickup(unit);
        int newHealth = ReplenishHealth.ADD_HEALTH;
        if(newHealth > unit.getMaxHP())
            newHealth = unit.getMaxHP();
        assertTrue(unit.getHP() == newHealth);
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
            System.err.println("InterruptedException in ReplenishHealthTest");
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
