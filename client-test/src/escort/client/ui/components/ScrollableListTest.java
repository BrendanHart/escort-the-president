package escort.client.ui.components;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.panels.ScrollableList;

public class ScrollableListTest {

	private Inputs inputs;
	private Panel container;

	@Before
	public void setUp() {
		Scale.factor = 1;
		inputs = new Inputs();
		container = new Panel(inputs, 500, 500);
	}

	@After
	public void tearDown() {
		container = null;
		inputs = null;
	}

	@Test
	public void testScrollableList() {
		// Create a new scrollable list.
		ScrollableList list = new ScrollableList(inputs, 100, 100);

		// Add it to the container.
		container.add(list, 0, 0);

		// Ensure the content height is 0.
		assertTrue(list.getContentHeight() == 0);

		// Ensure the y-offset is 0.
		assertTrue(list.getYOffset() == 0);

		// Add a new entry to the list.
		Component entry1 = new Component(inputs, 30, 30);
		list.addEntry(entry1);

		// Ensure the content height is now the height of the entry.
		assertTrue(list.getContentHeight() == entry1.getHeight());

		// Add a entry that is bigger than the viewable height of the component.
		Component entry2 = new Component(inputs, 200, 200);
		list.addEntry(entry2);

		// Ensure the content height is now the height of both entries.
		assertTrue(list.getContentHeight() == entry1.getHeight() + entry2.getHeight());

		// Ensure the y-offset is still 0.
		assertTrue(list.getYOffset() == 0);

		// Ensure the height of the component has remained unchanged.
		assertTrue(list.getHeight() == 100);

		// Move the mouse onto the scroll thumb and click.
		inputs.mouseX = list.getScrollBar().getGlobalPoint().x;
		inputs.mouseY = list.getScrollBar().getGlobalPoint().y + list.getScrollBar().getWidth();
		inputs.leftClick.clicked(inputs.mouseX, inputs.mouseY);

		// Update the container.
		container.update();

		// Move the mouse down by 10.
		inputs.mouseY += 10;

		// Update the container.
		container.update();

		// Ensure the y-offset has increased.
		assertTrue(list.getYOffset() > 0);

		// Move the scroll thumb to the bottom.
		list.getScrollBar().moveToBottom();

		// Update the container.
		container.update();

		// Ensure the y offset is the content height - viewable height.
		assertTrue(list.getYOffset() == list.getContentHeight() - list.getHeight());

		// Simulate a click on the up arrow button.
		inputs.mouseX = list.getScrollBar().getGlobalPoint().x;
		inputs.mouseY = list.getScrollBar().getGlobalPoint().y;
		inputs.leftClick.clicked(inputs.mouseX, inputs.mouseY);

		// Simulate a hold on the button.
		for (int i = 0; i < 100; i++) {
			container.update();
		}

		// Ensure the y offset is now 0.
		assertTrue(list.getYOffset() == 0);

		// Move the scroll thumb down without dragging.
		list.getScrollBar().moveDown(20);

		// Update the container.
		container.update();

		// Ensure the y-offset has increased.
		assertTrue(list.getYOffset() > 0);

		// Manually set y-offset to 0.
		list.setYOffset(0);

		// Update the container.
		container.update();
	}
}
