package escort.client.ui.components.panels;

import escort.client.input.Inputs;
import escort.client.ui.components.Component;
import escort.client.ui.components.VerticalScrollBar;

/**
 * A Scrollable list is a list of component entries with a vertical scroll bar.
 * 
 * @author Ahmed Bhallo
 *
 */
public class ScrollableList extends Panel {

	/**
	 * The height of the content.
	 */
	private int contentHeight = 0;

	/**
	 * The scroll bar.
	 */
	private VerticalScrollBar scrollBar;

	/**
	 * Instantiates a new scrollable list.
	 * 
	 * @param inputs
	 *            The inputs object.
	 * @param width
	 *            The width of this scrollable list (this width includes the
	 *            scroll bar width).
	 * @param height
	 *            The height of this scrollable list.
	 */
	public ScrollableList(Inputs inputs, int width, int height) {
		super(inputs, width, height);
		// Add the scroll bar.
		addScrollBar();
	}

	/**
	 * Calls the detectScroll() method.
	 */
	@Override
	public void update() {
		super.update();
		if (isHovered()) {
			detectScroll();
		}
	}

	/**
	 * Detects if the scroll wheel has been pressed.
	 */
	private void detectScroll() {
		if (inputs.scrollUp.isPressed()) {
			scrollBar.moveUp(8);
		}

		if (inputs.scrollDown.isPressed()) {
			scrollBar.moveDown(8);
		}
	}

	public void addEntry(Component entry) {
		addEntry(entry, 0);
	}

	/**
	 * Adds an entry component to this list.
	 * 
	 * @param entry
	 *            To component to be added.
	 * @param xIndent
	 */
	public void addEntry(Component entry, int xIndent) {
		add(entry, xIndent, contentHeight);
		contentHeight += entry.getHeight();
	}

	/**
	 * Gets the height of the actual content of this list.
	 * 
	 * @return The height of the actual content of this list.
	 */
	public int getContentHeight() {
		return contentHeight;
	}

	/**
	 * Returns whether or not the actual content area is being pressed
	 * (excluding the scroll bar).
	 * 
	 * @return True iff the actual content area is being pressed.
	 */
	public boolean contentDown() {
		return isDown() && !(scrollBar.isHovered() || scrollBar.isDown());
	}

	/**
	 * Clears all non fixed entries from the list.
	 */
	public void clear() {
		for (Component c : children.keySet()) {
			// Do not remove fixed children from this list.
			if (!(c.isAlwaysFixed())) {
				children.remove(c);
			}
		}

		// Reset the content height to 0.
		contentHeight = 0;
	}

	/**
	 * Sets the height of this list. Removes the scroll bar and creates a new
	 * one in order to accomodate for the change.
	 */
	@Override
	public void setHeight(int height) {
		super.setHeight(height);
		remove(scrollBar);
		addScrollBar();
	}

	/**
	 * Adds the scroll bar to the list.
	 */
	private void addScrollBar() {
		scrollBar = new VerticalScrollBar(inputs, getHeight(), this);
		add(scrollBar, getWidth() - scrollBar.getWidth(), 0);
	}

	/**
	 * Returns the scroll bar of this list.
	 * 
	 * @return The vertical scroll bar.
	 */
	public VerticalScrollBar getScrollBar() {
		return scrollBar;
	}
}
