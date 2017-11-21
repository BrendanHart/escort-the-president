package escort.client.ui.components;

import java.awt.Point;
import java.awt.Rectangle;

import escort.client.input.Inputs;
import escort.client.ui.components.panels.Panel;

/**
 * A component that can be dragged by the clients mouse. Contains a rectangle
 * that defines the boundaries in which this component can be dragged up to.
 * 
 * @author Ahmed Bhallo
 *
 */
public class DraggableComponent extends Component {

	/**
	 * The mouse click starting point of the current drag.
	 */
	private Point dragStart;

	/**
	 * Whether or not this component is being dragged.
	 */
	private boolean dragging;

	/**
	 * The rectangle that defines the boundaries in which this component can be
	 * dragged up to.
	 */
	private Rectangle bounds;

	/**
	 * Instantiates a new draggable component.
	 * 
	 * @param inputs
	 *            The input keys.
	 * @param width
	 *            The width of this component.
	 * @param height
	 *            The height of this component.
	 * @param bounds
	 *            The rectangle that defines the boundaries of this component's
	 *            position.
	 */
	public DraggableComponent(Inputs inputs, int width, int height, Rectangle bounds) {
		super(inputs, width, height);
		this.bounds = bounds;
	}

	/**
	 * Calls the drag detection method.
	 */
	@Override
	public void update() {
		super.update();
		detectDrag();
	}

	/**
	 * Detects whether or not this componnet is currently being dragged. Updates
	 * the position of this component based on the drag of the mouse.
	 */
	private void detectDrag() {
		// Store the current mouse positions.
		int mouseX = inputs.mouseX;
		int mouseY = inputs.mouseY;

		// If we are not down, and the mouse is not down, we are not being
		// dragged.
		if (!isDown() && !inputs.leftClick.isPressed()) {
			dragging = false;
		}

		// If we are down, but not yet dragging, start the drag.
		if (isDown() && !dragging) {
			// Store the drag start point and set dragging to true.
			dragStart = new Point(mouseX, mouseY);
			dragging = true;
		}

		// If we are dragging, get parent of this component and calculate the
		// new position of this component.
		if (dragging) {
			Panel parent = getParent();
			Point oldPoint = parent.getPoint(this);

			// This is our new position.
			int newX = oldPoint.x - (dragStart.x - mouseX);
			int newY = oldPoint.y - (dragStart.y - mouseY);

			// Perform bounds checking.
			if (newX < bounds.x) {
				newX = bounds.x;
			}
			if (newX + getWidth() > bounds.x + bounds.width) {
				newX = bounds.x + bounds.width - getWidth();
			}
			if (newY < bounds.y) {
				newY = bounds.y;
			}
			if (newY + getHeight() > bounds.y + bounds.height) {
				newY = bounds.y + bounds.height - getHeight();
			}

			// Replace this component's position in the parent children map.
			// Note that parent uses a hash map, so there is no need to remove
			// this component from the panel first.
			parent.add(this, newX, newY);

			// Update the start drag point.
			dragStart = new Point(mouseX, mouseY);
		}
	}
}
