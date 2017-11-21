package escort.client.ui.components.panels;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.concurrent.ConcurrentHashMap;

import escort.client.input.Inputs;
import escort.client.ui.components.Component;

/**
 * A Panel is a container of components. This calls the update and render
 * methods of its children. A panel is also a component, so can perform all of
 * the same operations and be contained inside other panels.
 * 
 * @author Ahmed Bhallo
 *
 */
public class Panel extends Component {

	/**
	 * The map of children to their position relative to this panel.
	 */
	protected final ConcurrentHashMap<Component, Point> children = new ConcurrentHashMap<>();

	/**
	 * The offset in the x direction of this panel. Currently unused (but should
	 * be used if horizontal scrolling is implemented).
	 */
	private int xOffset;

	/**
	 * The offset in the y direction of this panel. Currently only used to
	 * scroll by scrollable list.
	 */
	private int yOffset;

	/**
	 * Instantiates a new panel.
	 * 
	 * @param inputs
	 *            The inputs object.
	 * @param width
	 *            The width of the panel.
	 * @param height
	 *            The height of the panel.
	 */
	public Panel(Inputs inputs, int width, int height) {
		super(inputs, width, height);
	}

	/**
	 * Updates all children. Note that this method updates non fixed children
	 * before fixed children.
	 */
	@Override
	public void update() {
		super.update();
		for (Component child : children.keySet()) {
			// Update only non fixed children first.
			if (child.isAlwaysFixed()) {
				continue;
			}
			child.update();
		}
		for (Component child : children.keySet()) {
			// Now we update fixed children.
			if (!child.isAlwaysFixed()) {
				continue;
			}
			child.update();
		}
	}

	/**
	 * Renders all children in this panel. Automatically clips and translates
	 * the graphics object so that the object is the same dimensions of the
	 * component and starts at the top left position of the component. Note that
	 * this method renders all non-fixed children before fixed children.
	 */
	@Override
	public void render(Graphics2D g) {
		if (!isVisible()) {
			return;
		}
		super.render(g);

		// Iterate through children.
		for (Component child : children.keySet()) {
			Point childPoint = children.get(child);

			// Prevent null pointer exception.
			if (childPoint == null) {
				continue;
			}

			// Store the positions of the child relative to this panel.
			final int childX = childPoint.x;
			final int childY = childPoint.y;

			// Render fixed components after non-fixed ones.
			if (child.isAlwaysFixed()) {
				continue;
			}

			// Translate the graphics object so that a child can start rendering
			// at (0,0).
			g.translate(childX - xOffset, childY - yOffset);

			// Temporarily save the old clip
			Shape oldClip = g.getClip();

			// Clip the graphics object
			Rectangle childRect = new Rectangle(0, 0, child.getWidth(), child.getHeight());
			Rectangle thisRect = new Rectangle(-childX + xOffset, -childY + yOffset, getWidth(), getHeight());
			g.clip(thisRect.intersection(childRect));

			// Render the child
			child.render(g);

			// Restore the old clip
			g.setClip(oldClip);

			// Restore the old translation
			g.translate(-childX + xOffset, -childY + yOffset);
		}

		// Now render fixed children.
		for (Component child : children.keySet()) {
			Point childPoint = children.get(child);

			// Prevent null point exception.
			if (childPoint == null) {
				continue;
			}

			// Store the position.
			final int childX = childPoint.x;
			final int childY = childPoint.y;

			// If a child is always fixed, only translate, ignoring panel
			// offsets and render. Do not clip.
			if (!child.isAlwaysFixed()) {
				continue;
			}
			g.translate(childX, childY);

			// Render the child.
			child.render(g);

			// Restore the tranlsation.
			g.translate(-childX, -childY);
		}
	}

	/**
	 * Add a component to this panel with specified x and y position relative to
	 * this component. This panel will then automatically update and render that
	 * component.
	 * 
	 * @param comp
	 *            The component to add.
	 * @param x
	 *            The x position of the component to add relative to this panel.
	 * @param y
	 *            The y position of the component to add relative to this panel.
	 */
	public void add(Component comp, int x, int y) {
		add(comp, new Point(x, y));
	}

	/**
	 * Add a component to this panel at a specified point relative to this
	 * panel.
	 * 
	 * @param comp
	 *            The component to be added.
	 * @param p
	 *            The point to add the component relative to this panel.
	 */
	public void add(Component comp, Point p) {
		if (comp == null || p == null) {
			throw new IllegalArgumentException();
		}
		comp.setParent(this);
		comp.setVisible(true);
		children.put(comp, p);
	}

	/**
	 * Removes a child component from this panel.
	 * 
	 * @param The
	 *            component to be removed.
	 */
	public void remove(Component c) {
		if (c == null)
			return;
		c.setParent(null);
		c.setVisible(false);
		children.remove(c);
	}

	/**
	 * Gets the x offset of this panel.
	 * 
	 * @return The x offset of this panel.
	 */
	public int getXOffset() {
		return xOffset;
	}

	/**
	 * Sets the x offset of this panel.
	 * 
	 * @param xOffset
	 *            The new x offset of this panel.
	 */
	public void setXOffset(int xOffset) {
		this.xOffset = xOffset;
	}

	/**
	 * Gets the y offset of this panel.
	 * 
	 * @return The y offset of this panel.
	 */
	public int getYOffset() {
		return yOffset;
	}

	/**
	 * Sets the y offset of this panel.
	 * 
	 * @param yOffset
	 *            The y offset of this panel.
	 */
	public void setYOffset(int yOffset) {
		this.yOffset = yOffset;
	}

	/**
	 * Returns a Point object that determines the position of where to add a
	 * given component, if that component is to be centered inside this panel.
	 * To add a cenetered component to a panel, call Panel.add(Component,
	 * center(Component));
	 * 
	 * @param comp
	 *            The component to be centered
	 * @return The point where the component should be added if it is to be
	 *         centered
	 */
	public Point center(Component comp) {
		int centerX = (getWidth() - comp.getWidth()) / 2;
		int centerY = (getHeight() - comp.getHeight()) / 2;
		return new Point(centerX, centerY);
	}

	/**
	 * Packs this container such that the width and height is the minimum size
	 * in which will contain all components without leaving any extra space.
	 */
	public void pack() {
		int maxWidth = 0;
		int maxHeight = 0;

		// Traverse through each child.
		for (Component child : children.keySet()) {
			Point location = children.get(child);

			// Store the current max width and height.
			maxWidth = Math.max(maxWidth, child.getWidth() + location.x);
			maxHeight = Math.max(maxHeight, child.getHeight() + location.y);
		}

		// Update the dimensions to the maximums found.
		setWidth(maxWidth);
		setHeight(maxHeight);
	}

	/**
	 * Gets the point of a component in the parent.
	 * 
	 * @param comp
	 *            The component to get the point of.
	 * @return The point of the component.
	 */
	public Point getPoint(Component comp) {
		return children.get(comp);
	}

	/**
	 * Overides the set visible method and propogrates onto the children of this
	 * panel.
	 */
	@Override
	public void setVisible(boolean isVisible) {
		super.setVisible(isVisible);
		for (Component child : children.keySet()) {
			child.setVisible(isVisible);
		}
	}

}
