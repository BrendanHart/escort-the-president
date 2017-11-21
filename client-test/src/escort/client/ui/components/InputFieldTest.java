package escort.client.ui.components;

import static org.junit.Assert.*;

import java.awt.event.KeyEvent;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.res.ResourceLoader;
import escort.client.ui.components.text.InputField;

public class InputFieldTest {
	private Inputs inputs;
	private InputField inputField;

	@Before
	public void setUp() {
		try {
			new ResourceLoader().loadAllFonts();
		} catch (IOException e) {
		}
		Scale.factor = 1;
		inputs = new Inputs();
		inputField = new InputField(inputs, 20);
	}

	@After
	public void tearDown() {
		inputs = null;
		inputField = null;
	}

	@Test
	public void detectFocus() {
		inputField.update();
		inputField.setFocussed(false);
		assertTrue(inputField.getText().isEmpty());

		inputs.typedInput.put('a', true);
		inputs.typedInput.put('c', false);
		inputs.typedInput.put('b', true);
		inputField.update();

		assertTrue(inputField.getText().isEmpty());
	}

	@Test
	public void detectFocusTraversal() {
		InputField toFocus = new InputField(inputs, 30);
		inputField.update();

		// Give it the next traversable
		inputField.setNextTraversable(toFocus);
		inputField.setFocussed(true);
		toFocus.setFocussed(false);

		// Put the tab key in the inputs map
		inputs.typedInput.put((char) KeyEvent.VK_TAB, true);
		inputField.update();

		// Make sure only the new input field is now focussed
		assertFalse(inputField.isFocussed());
		assertTrue(toFocus.isFocussed());
	}

	@Test
	public void testCharacterInput() {
		inputField.update();
		inputField.setFocussed(true);
		assertTrue(inputField.getText().isEmpty());

		inputs.typedInput.put('a', true);
		inputs.typedInput.put('c', false);
		inputs.typedInput.put('b', true);
		inputField.update();

		assertTrue(inputField.getText().equals("ab") || inputField.getText().equals("ba"));
	}

}
