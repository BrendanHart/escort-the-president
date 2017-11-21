package escort.common.game.weapons;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BlastShieldTest {

	BlastShield blastShield;

	@Before
	public void setUp() {
		blastShield = new BlastShield(BlastShield.MAX_HP);
	}

	@After
	public void tearDown() {
		blastShield = null;
	}

	@Test
	public void correctBrokenWhenTakenDamage() {
		assertTrue(!blastShield.isBroken());

		blastShield.takeDamage(BlastShield.MAX_HP / 2);
		assertTrue(!blastShield.isBroken());

		blastShield.takeDamage((BlastShield.MAX_HP / 2) - 1);
		assertTrue(!blastShield.isBroken());

		blastShield.takeDamage(1);
		assertTrue(blastShield.isBroken());

		blastShield.takeDamage(1000);
		assertTrue(blastShield.isBroken());
	}

}
