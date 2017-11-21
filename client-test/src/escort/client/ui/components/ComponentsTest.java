package escort.client.ui.components;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.ui.components.panels.Panel;

public class ComponentsTest {

	private boolean clickDetected = false;
	private Inputs inputs;
	private Panel container;

	@Before
	public void setUp() {
		Scale.factor = 1;
		clickDetected = false;
		inputs = new Inputs();
		container = new Panel(inputs, 500, 500);
	}

	@After
	public void tearDown() {
		container = null;
		inputs = null;
		clickDetected = false;
	}

	@Test
	public void testNoActionDetected() {
		// Creating new draggable component. Bounds: 10 >= x and y >= 200 + 10 -
		// 50.
		DraggableComponent comp = new DraggableComponent(inputs, 50, 50, new Rectangle(10, 10, 200, 200));
		ComponentListener listener = new ComponentListener() {
			@Override
			public void componentClicked(Component source) {
				clickDetected = true;
			}
		};
		comp.addListener(listener);
		container.add(comp, 20, 20);

		// Update the container.
		container.update();

		// Ensure the mouse is not over or pressing the component.
		// Ensure listeners have not registered a click.
		assertFalse(comp.isHovered());
		assertFalse(comp.isDown());
		assertFalse(clickDetected);

	}

	@Test
	public void testComponentClick() {
		// Creating new draggable component. Bounds: 10 >= x and y >= 200 + 10 -
		// 50.
		DraggableComponent comp = new DraggableComponent(inputs, 50, 50, new Rectangle(10, 10, 200, 200));
		ComponentListener listener = new ComponentListener() {
			@Override
			public void componentClicked(Component source) {
				clickDetected = true;
			}
		};
		comp.addListener(listener);
		container.add(comp, 20, 20);

		// Update the container.
		container.update();

		// Move the x position of the mouse on the button, but not the y
		// position.
		inputs.mouseX = 51;
		inputs.mouseY = 0;

		// Update the container.
		container.update();

		// Ensure the mouse is not over or pressing the component.
		// Ensure listeners have not registered a click.
		assertFalse(comp.isHovered());
		assertFalse(comp.isDown());
		assertFalse(clickDetected);

		// Now move the y position of the mouse onto the button.
		inputs.mouseX = 51;
		inputs.mouseY = 51;

		// Update the container.
		container.update();

		// Ensure component is hovered but not pressed.
		// Ensure listeners have not registered a click.
		assertTrue(comp.isHovered());
		assertFalse(comp.isDown());
		assertFalse(clickDetected);

		// Now simulate a mouse press outside of the component.
		inputs.mouseX = 0;
		inputs.mouseY = 0;
		inputs.leftClick.clicked(inputs.mouseX, inputs.mouseY);

		// Update the container.
		container.update();

		// Ensure component is not hovered nor pressed.
		// Ensure listeners have not registered a click.
		assertFalse(comp.isHovered());
		assertFalse(comp.isDown());
		assertFalse(clickDetected);

		// Relase the mouse and update.
		inputs.leftClick.released(inputs.mouseX, inputs.mouseY);
		container.update();

		// Now simulate a mouse press inside the component.
		inputs.mouseX = 51;
		inputs.mouseY = 51;
		inputs.leftClick.clicked(inputs.mouseX, inputs.mouseY);

		// Update the container.
		container.update();

		assertTrue(comp.isDown());

		// Release mouse
		inputs.leftClick.released(inputs.mouseX, inputs.mouseY);

		// Update the container.
		container.update();

		// Ensure component is hovered and pressed.
		// Ensure listeners have registered the click.
		assertTrue(comp.isHovered());
		assertTrue(clickDetected);
	}

	@Test
	public void testDragging() {
		// Creating new draggable component. Bounds: 10 >= x and y >= 200 + 10 -
		// 50.
		DraggableComponent comp = new DraggableComponent(inputs, 50, 50, new Rectangle(10, 10, 200, 200));
		ComponentListener listener = new ComponentListener() {
			@Override
			public void componentClicked(Component source) {
				clickDetected = true;
			}
		};
		comp.addListener(listener);
		container.add(comp, 20, 20);

		inputs.mouseX = 20;
		inputs.mouseY = 20;
		inputs.leftClick.clicked(inputs.mouseX, inputs.mouseY);
		
		// Update the container.
		container.update();
		// Ensure the component is at 20, 20 relative to its parent.
		assertTrue(comp.getLocalPoint().x == 20);
		assertTrue(comp.getLocalPoint().y == 20);

		// Now move the mouse by 5 pixels in both directions to simulate
		// dragging.
		inputs.mouseX += 5;
		inputs.mouseY += 5;

		// Update the container.
		container.update();

		// Ensure the component is at 25,25 relative to its parent.
		assertTrue(comp.getLocalPoint().x == 25);
		assertTrue(comp.getLocalPoint().y == 25);

		// Now move the moouse by 300 pixels in both directions. Recall that the
		// component is bounded.
		inputs.mouseX += 300;
		inputs.mouseY += 300;

		// Update the container.
		container.update();

		// Ensure the component is at 160, 160 (its maximum bounds).
		// (200 - 50 + 10).
		assertTrue(comp.getLocalPoint().x == 160);
		assertTrue(comp.getLocalPoint().y == 160);

		// Move the mouse to 0, 0.
		inputs.mouseX = 0;
		inputs.mouseY = 0;

		// Update the container.
		container.update();

		// Ensure the component is at 10, 10 (its minimum bounds).
		assertTrue(comp.getLocalPoint().x == 10);
		assertTrue(comp.getLocalPoint().y == 10);
	}

}
