package escort.client.ui.components;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.panels.ScrollableList;
import escort.client.ui.utils.Colors;

/**
 * A vertical scroll bar that is to be used on a scrollable list.
 * 
 * @author Ahmed Bhallo
 *
 */
public class VerticalScrollBar extends Panel {

	/**
	 * The thickness of the scroll bar.
	 */
	private final int thickness = 8 * Scale.factor;

	/**
	 * The scrollable list that this scroll bar is controlling.
	 */
	private final ScrollableList list;

	/**
	 * The scroll up button.
	 */
	private final Component upButton;

	/**
	 * The scroll down button.
	 */
	private final Component downButton;

	/**
	 * The draggable scroll thumb.
	 */
	private final DraggableComponent scroller;

	/**
	 * The rectangle the defines the boundaries of the movment of the scroll
	 * thumb.
	 */
	private final Rectangle bar;

	/**
	 * Used for rendering the icon of the scroll icon.
	 */
	private final int BUTTON_ICON_MARGIN = 2 * Scale.factor;

	/**
	 * Instantiates a new vertical scroll bar object.
	 * 
	 * @param inputs
	 *            The inputs object.
	 * @param height
	 *            The height of the scroll bar.
	 * @param list
	 *            The list that this component is controlling.
	 */
	public VerticalScrollBar(Inputs inputs, int height, ScrollableList list) {
		super(inputs, 0, height);
		setWidth(thickness);
		setBackground(Colors.VERY_LIGHT_GRAY);

		// Scroll bars should not move when a panel is scrolled. Therefore,
		// always fixed is set to true.
		setAlwaysFixed(true);

		this.list = list;

		// Creates the up and down buttons.
		upButton = new Component(inputs, thickness, thickness) {
			@Override
			public void update() {
				super.update();
				if (isDown()) {
					moveUp(2);
				}
			}

			/**
			 * Renders the triangular icon of the scroll button.
			 */
			@Override
			public void render(Graphics2D g) {
				super.render(g);
				g.setColor(Colors.VERY_DARK_GRAY);
				g.fillPolygon(
						new int[] { BUTTON_ICON_MARGIN, thickness / 2, thickness - BUTTON_ICON_MARGIN }, new int[] {
								thickness - BUTTON_ICON_MARGIN, BUTTON_ICON_MARGIN, thickness - BUTTON_ICON_MARGIN },
						3);
			}
		};

		downButton = new Component(inputs, thickness, thickness) {
			@Override
			public void update() {
				super.update();
				if (isDown()) {
					moveDown(2);
				}
			}

			/**
			 * Renders the triangular icon of the scroll button.
			 */
			@Override
			public void render(Graphics2D g) {
				super.render(g);
				g.setColor(Colors.VERY_DARK_GRAY);
				g.fillPolygon(new int[] { BUTTON_ICON_MARGIN, thickness / 2, thickness - BUTTON_ICON_MARGIN },
						new int[] { BUTTON_ICON_MARGIN, thickness - BUTTON_ICON_MARGIN, BUTTON_ICON_MARGIN }, 3);
			}

		};

		bar = new Rectangle(0, thickness, thickness, height - thickness * 2);
		scroller = new DraggableComponent(inputs, thickness, 10, bar) {
			@Override
			public void update() {
				super.update();
				if (isHovered()) {
					setBackground(Colors.DARK_GRAY);
				} else {
					setBackground(Colors.LIGHT_GRAY);
				}
			}
		};
		upButton.setBackground(Colors.DARK_WHITE);
		add(upButton, 0, 0);

		downButton.setBackground(Colors.DARK_WHITE);
		add(downButton, 0, getHeight() - thickness);

		scroller.setBackground(Colors.LIGHT_GRAY);
		add(scroller, 0, thickness);
	}

	/**
	 * Calls the update scroll ratio method which updates the width of the
	 * scroll and controls the offset value of the scrollable list.
	 */
	public void update() {
		super.update();
		updateScrollRatio();
	}

	/**
	 * Sets the width of the scroll bar and controls the offset value of the
	 * scrollable list.
	 */
	private void updateScrollRatio() {
		// Calculate the ratio of scrolling
		int scrollRatio = (int) (bar.getHeight() * ((double) (list.getHeight()) / list.getContentHeight()));
		scrollRatio = Math.max(scrollRatio, 0);
		scrollRatio = Math.min(scrollRatio, getHeight() - thickness * 2);

		// Calculate the scrolling percentage based on the scroller's position
		// and scroll ratio.
		double scrollerPercentage = (scroller.getLocalPoint().y - thickness) / (bar.getHeight() - scrollRatio);

		// Update the y offset of the list based on the scroll ratio.
		list.setYOffset((int) (scrollerPercentage * (list.getContentHeight() - list.getHeight())));

		// Set the height of the thumb equal to the scroll ratio. If it is too
		// small, use an abitrary small value.
		scroller.setHeight(Math.max(scrollRatio, 1 * Scale.factor));
	}

	/**
	 * Moves the scroll up by a specified amount.
	 * 
	 * @param amount
	 *            The amount to scroll up by.
	 */
	public void moveUp(int amount) {
		Point p = children.get(scroller);
		int newY = Math.max(thickness, p.y - amount);
		add(scroller, p.x, newY);
	}

	/**
	 * Moves the scroll down by a specified amount.
	 * 
	 * @param amount
	 *            The amount to scroll down by.
	 */
	public void moveDown(int amount) {
		Point p = children.get(scroller);
		int newY = Math.min(getHeight() - thickness - scroller.getHeight(), p.y + amount);
		add(scroller, p.x, newY);
	}

	/**
	 * Moves the scroll thumb down to the bottom of the scroll bar.
	 */
	public void moveToBottom() {
		updateScrollRatio();
		Point p = children.get(scroller);
		add(scroller, p.x, getHeight() - thickness - scroller.getHeight());
	}

	/**
	 * Checks whether or not the scroll thumb is currently at the bottom of the
	 * scroll bar.
	 * 
	 * @return True iff the scroll thumb is at the bottom of the scroll bar.
	 */
	public boolean isAtBottom() {
		Point p = children.get(scroller);
		return p.y == getHeight() - thickness - scroller.getHeight();
	}
}
