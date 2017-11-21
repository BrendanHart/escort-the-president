package escort.client.input;

/**
 * A mouse button object is a key object that contains information of where the
 * mouse was when it was clicked or released.
 * 
 * @author Ahmed Bhallo
 *
 */
public class MouseButton extends Key {

	/**
	 * The x position of where the mouse was when it was clicked.
	 */
	private int clickX = -1;
	
	/**
	 * The y position of where the mouse was when it was clicked.
	 */
	private int clickY = -1;

	/**
	 * The x position of where the mouse was when it was released.
	 */
	private int releaseX = -1;

	/**
	 * The y position of where the mouse was when it was released.
	 */
	private int releaseY = -1;

	/**
	 * Instantiates a new mouse button object.
	 * 
	 * @param name
	 *            The name of the mouse button.
	 */
	public MouseButton(String name) {
		super(name);
	}

	/**
	 * Called when this mouse button is clicked.
	 * 
	 * @param clickX
	 *            The x position of the mouse when the button was pressed.
	 * @param clickY
	 *            The y position of the mouse when the button was pressed.
	 */
	public void clicked(int clickX, int clickY) {
		this.clickX = clickX;
		this.clickY = clickY;
		setPressed(true);
	}

	/**
	 * Called when this mouse button is released.
	 * 
	 * @param releaseX
	 *            The x position of the mouse when the button was released.
	 * @param releaseY
	 *            The y position of the mouse when the button was released.
	 */
	public void released(int releaseX, int releaseY) {
		this.releaseX = releaseX;
		this.releaseY = releaseY;
		setPressed(false);
	}

	/**
	 * Gets the x position of the mouse when it was clicked.
	 * 
	 * @return The x position of the mouse when it was clicked.
	 */
	public int getClickX() {
		return clickX;
	}

	/**
	 * Gets the y position of the mouse when it was clicked.
	 * 
	 * @return The y position of the mouse when it was clicked.
	 */
	public int getClickY() {
		return clickY;
	}

	/**
	 * Gets the x position of the mouse when it was released.
	 * 
	 * @return The x position of the mouse when it was released.
	 */
	public int getReleaseX() {
		return releaseX;
	}

	/**
	 * Gets the y position of the mouse when it was released.
	 * 
	 * @return The y position of the mouse when it was released.
	 */
	public int getReleaseY() {
		return releaseY;
	}

}
