package escort.client.ui.components;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.res.ResourceLoader;

public class StepperTest {

	private Inputs inputs;
	private Stepper stepper;

	@Before
	public void setUp() {
		try {
			new ResourceLoader().loadAllFonts();
		} catch (IOException e) {
		}
		Scale.factor = 1;
		inputs = new Inputs();
		stepper = new Stepper(inputs, 200, 1, "one", "two", "three");
	}

	@After
	public void tearDown() {
		inputs = null;
		stepper = null;
	}

	@Test
	public void testStepperIncrement() {
		stepper.increment();
		assertTrue(stepper.getIndex() == 2);

		// Decrementing again should have no effect.
		stepper.increment();
		assertTrue(stepper.getIndex() == 2);
	}

	@Test
	public void testStepperDecrement() {
		stepper.decrement();
		assertTrue(stepper.getIndex() == 0);

		// Decrementing again should have no effect.
		stepper.decrement();
		assertTrue(stepper.getIndex() == 0);
	}

}
