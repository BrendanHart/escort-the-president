package escort.client.ui.components;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashSet;
import java.util.Set;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.ui.RenderUtils;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.utils.Colors;

/**
 * A UI Component. The base super class of the component class hierarchy.
 * 
 * @author Ahmed Bhallo
 *
 */
public class Component {

	/**
	 * The parent panel of this component.
	 */
	private Panel parent = null;

	/**
	 * The width of this component.
	 */
	private int width;

	/**
	 * The height of this component.
	 */
	private int height;

	/**
	 * The background color.
	 */
	private Color backgroundColor = null;

	/**
	 * The foreground color.
	 */
	private Color foregroundColor = Colors.LIGHT_BLACK;

	/**
	 * The color of the border.
	 */
	private Color borderColor = null;

	/**
	 * A component that is always fixed will remain stationary relative to a
	 * scroll able panel's contents if that panel is scrolled.
	 */
	private boolean alwaysFixed;

	/**
	 * The inputs objects of the client.
	 */
	protected final Inputs inputs;

	/**
	 * Listeners of this component.
	 */
	private final Set<ComponentListener> listeners = new HashSet<>();

	/**
	 * Whether or not this component is being hovered by the mouse.
	 */
	protected boolean hover;

	/**
	 * Whether or not this component is being held down by the left mouse
	 * button.
	 */
	protected boolean down;

	/**
	 * Whether or not this component has been enabled.
	 */
	private boolean isEnabled = true;

	/**
	 * Whether or not this component should be rendered.
	 */
	private boolean isVisible = true;

	/**
	 * Instantiates a new component object.
	 * 
	 * @param inputs
	 *            The inputs object.
	 * @param width
	 *            This width of this component.
	 * @param height
	 *            The height of this component.
	 */
	public Component(Inputs inputs, int width, int height) {
		this.inputs = inputs;
		this.width = width;
		this.height = height;
	}

	/**
	 * Updating the component will detect input.
	 */
	public void update() {
		detectInput();
	}

	/**
	 * Checks if this component has been hovered or clicked by the user.
	 */
	protected void detectInput() {
		Rectangle globalRect = getGlobalRect();
		if (getParent() != null) {
			globalRect = globalRect.intersection(getParent().getGlobalRect());
		}
		hover = globalRect.contains(inputs.mouseX, inputs.mouseY);
		boolean compClicked = globalRect.contains(inputs.leftClick.getClickX(), inputs.leftClick.getClickY());
		boolean downNow = inputs.leftClick.isPressed() && compClicked;
		if (down && !downNow) {
			onClick();
		}
		down = downNow;
	}

	/**
	 * Called when the component has been clicked. Calling this method will
	 * simulate a button click.
	 */
	public void onClick() {
		if (isVisible) {
			notifyListeners();
		}
	}

	/**
	 * Notifies all listeners of this component that this component has been
	 * clicked.
	 */
	public void notifyListeners() {
		listeners.forEach(listener -> listener.componentClicked(this));
	}

	/**
	 * Add a listener to listen to clicks on this component.
	 * 
	 * @param listener
	 *            The listener to add.
	 */
	public void addListener(ComponentListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a listener. Returns whether or not the removal was successful.
	 * 
	 * @param listener
	 *            The listener of this component to be removed.
	 * @return True iff the listener was listening to this component.
	 */
	public boolean removeListener(ComponentListener listener) {
		return listeners.remove(listener);
	}

	/**
	 * Renders the component. The Graphics object must first be clipped and
	 * translated such that the (0,0) point of the graphics object is at the top
	 * left position (0,0) of this component.
	 * 
	 * @param g
	 *            The graphics2D object.
	 */
	public void render(Graphics2D g) {
		if (!isVisible) {
			return;
		}
		// Render the background
		if (backgroundColor != null) {
			g.setColor(backgroundColor);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		// Render the border and its "glow" effect.
		if (borderColor != null) {
			RenderUtils.renderRectBorder(g, borderColor, 0, 0, getWidth() - 1, getHeight() - 1, Scale.factor);
			g.setColor(Colors.setAlpha(borderColor, 100));
			g.drawRect(Scale.factor, Scale.factor, getWidth() - 1 - Scale.factor * 2,
					getHeight() - 1 - Scale.factor * 2);
		}
	}

	/**
	 * Gets the position of this component relative to its parent. If this
	 * component has no parent, returns (0,0).
	 * 
	 * @return The position of this compnent relative to its parent.
	 */
	public Point getLocalPoint() {
		if (parent == null) {
			return new Point(0, 0);
		} else {
			return parent.getPoint(this);
		}
	}

	/**
	 * Gets the position of this component relative to the top left position of
	 * the canvas element.
	 * 
	 * @return The global position of this component relative to (0,0) of the
	 *         canvas element.
	 */
	public Point getGlobalPoint() {
		if (parent == null) {
			// If parent is null, assume this component is at (0,0).
			return new Point(0, 0);
		} else {
			// Get the position relative to the parent.
			Point localPoint = parent.getPoint(this);

			// Recursively, get the gloval position of the parent.
			Point parentGlobal = parent.getGlobalPoint();

			// The position is the sum of these 2 points.
			int x = localPoint.x + parentGlobal.x;
			int y = localPoint.y + parentGlobal.y;

			// If this component is always fixed, subtract the panel offsets.
			if (!alwaysFixed) {
				x -= parent.getXOffset();
				y -= parent.getYOffset();
			}

			// Return the point.
			return new Point(x, y);
		}
	}

	/**
	 * Gets the global rectangle. Calls the global point method and get
	 * width/height methods.
	 * 
	 * @return The rectangle of this component relative to the top left position
	 *         of the root component.
	 */
	public Rectangle getGlobalRect() {
		Point globalPoint = getGlobalPoint();
		return new Rectangle(globalPoint.x, globalPoint.y, getWidth(), getHeight());
	}

	/**
	 * Sets the parent of this component.
	 * 
	 * @param parent
	 *            The new parent of this component.
	 */
	public void setParent(Panel parent) {
		this.parent = parent;
	}

	/**
	 * Gets the parent of this component.
	 * 
	 * @return The parent of this component.
	 */
	public Panel getParent() {
		return parent;
	}

	/**
	 * Gets the width of this component.
	 * 
	 * @return The width of this component.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Sets the width of this component.
	 * 
	 * @param width
	 *            The new width of the component.
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Gets the height of this component.
	 * 
	 * @return The height of this component.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Sets the height of this component.
	 * 
	 * @param height
	 *            The new height of this component.
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Sets the background colour of this component.
	 * 
	 * @param backgroundColor
	 *            The new background colour of this component.
	 */
	public void setBackground(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}

	/**
	 * Gets the background colour of this component.
	 * 
	 * @return The background colour of this component,
	 */
	public Color getBackground() {
		return backgroundColor;
	}

	/**
	 * Gets the foreground colour of this component. Not used directly by this
	 * class, but is used by classes inheriting this class for elements such as
	 * text rendering.
	 * 
	 * @return The foreground colour of this component.
	 */
	public Color getForeground() {
		return foregroundColor;
	}

	/**
	 * Sets he foreground colour of this component. Not used directly by this
	 * class, but is used by classes inheriting this class for elements such as
	 * text rendering.
	 * 
	 * @param foregroundColor
	 *            The new foreground colour of this component.
	 */
	public void setForeground(Color foregroundColor) {
		this.foregroundColor = foregroundColor;
	}

	/**
	 * Gets the border colour of this component.
	 * 
	 * @return The border colour of this component.
	 */
	public Color getBorder() {
		return borderColor;
	}

	/**
	 * Sets the border colour of this component.
	 * 
	 * @param borderColor
	 *            The new border colour of this component.
	 */
	public void setBorder(Color borderColor) {
		this.borderColor = borderColor;
	}

	/**
	 * Sets whether not this component is always fixed. An always fixed
	 * component will never move when a panel is scrolled.
	 * 
	 * @param alwaysFixed
	 *            Whether or not this component should always be fixed.
	 */
	public void setAlwaysFixed(boolean alwaysFixed) {
		this.alwaysFixed = alwaysFixed;
	}

	/**
	 * Returns whether or not this component is always fixed. An always fixed
	 * component will never move when a panel is scrolled.
	 * 
	 * @return Whether or not this component is always fixed.
	 * 
	 */
	public boolean isAlwaysFixed() {
		return alwaysFixed;
	}

	/**
	 * Returns whether or not this component is currently being hovered by the
	 * client's mouse.
	 * 
	 * @return True iff the client's mouse is above this component.
	 */
	public boolean isHovered() {
		return hover;
	}

	/**
	 * Returns whether or not this component is currently being pressed by the
	 * client's mouse.
	 * 
	 * @return True iff the client's mouse is pressed and above this component.
	 */
	public boolean isDown() {
		return down;
	}

	/**
	 * Sets whether or not this component should be enabled. Not directly used
	 * by this class, but is used by classes inheriting this class.
	 * 
	 * @param isEnabled
	 *            Whether or not this compnent should be enabled.
	 */
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	/**
	 * Gets whether or not this component should be enabled. Not directly used
	 * by this class, but is used by classes inheriting this class.
	 * 
	 * @return True iff this component is enabled.
	 */
	public boolean isEnabled() {
		return isEnabled;
	}

	public void setVisible(boolean isVisible) {
		this.isVisible = isVisible;
	}

	public boolean isVisible() {
		return isVisible;
	}
}
