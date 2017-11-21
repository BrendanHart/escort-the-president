package escort.client.inputs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Panel;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.client.input.InputHandler;
import escort.client.input.Inputs;

public class InputsTest {

	private InputHandler inputHandler;
	private Inputs inputs;
	private MouseEvent mouseEvent;
	private KeyEvent keyEvent;

	@SuppressWarnings("deprecation")
	@Before
	public void setUp() {
		inputs = new Inputs();
		inputHandler = new InputHandler(null, inputs);
		Component comp = new Panel();
		mouseEvent = new MouseEvent(comp, MouseEvent.BUTTON1, System.currentTimeMillis(), 1, 10, 20, 1, true) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -2163502815060871537L;

			@Override
			public int getButton() {
				return MouseEvent.BUTTON1;
			}
		};
		keyEvent = new KeyEvent(comp, 0, 0, 0, 0) {
			/**
			 * 
			 */
			private static final long serialVersionUID = -5528730393092510524L;

			@Override
			public char getKeyChar() {
				return 'a';
			}
		};
	}

	@After
	public void tearDown() {
		inputHandler = null;
		inputs = null;
	}


	/**
	 * Tests if input handler correctly detects mouse presses.
	 * 
	 * @author Ahmed Bhallo
	 */
	@Test
	public void testMouseClick() {
		assertFalse(inputs.leftClick.isPressed());
		inputHandler.mousePressed(mouseEvent);
		assertTrue(inputs.leftClick.isPressed());
	}


	/**
	 * Tests if input handler correctly detects mouse releases.
	 * 
	 * @author Ahmed Bhallo
	 */
	@Test
	public void testMouseRelease() {
		inputs.leftClick.setPressed(true);
		assertTrue(inputs.leftClick.isPressed());
		inputHandler.mouseReleased(mouseEvent);
		assertFalse(inputs.leftClick.isPressed());
	}

	/**
	 * Tests if input handler correctly detects key presses.
	 * 
	 * @author Ahmed Bhallo
	 */
	@Test
	public void testKeyPress() {
		inputHandler.keyTyped(keyEvent);
		assertTrue(inputs.typedInput.get('a'));
	}
	
	/**
	 * Tests if input handler correctly detects key releases.
	 * 
	 * @author Ahmed Bhallo
	 */
	@Test
	public void testKeyRelease() {
		inputHandler.keyTyped(keyEvent);
		inputHandler.releaseAll();
		assertFalse(inputs.typedInput.get('a'));
	}
	
	/**
	 * Tests if input handler correctly detects mouse movement.
	 * 
	 * @author Ahmed Bhallo
	 */
	@Test
	public void testMouseMovement() {
		int x = inputs.mouseX;
		int y = inputs.mouseY;
		inputHandler.mouseMoved(mouseEvent);
		assertTrue(inputs.mouseX != x);
		assertTrue(inputs.mouseY != y);
	}

}
