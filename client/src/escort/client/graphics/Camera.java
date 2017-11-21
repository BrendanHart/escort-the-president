package escort.client.graphics;

import java.awt.Point;

import escort.client.game.GameManager;
import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.common.game.entities.units.Unit;

/**
 * The camera object stores and updates x and y offset values. These define
 * which position on the map should be displayed on the upper-left corner of the
 * game screen.
 * 
 * @author Ahmed Bhallo
 *
 */
public class Camera {

	/**
	 * The x offset of the camera.
	 */
	private int xOffset = 0;

	/**
	 * The y offset of the camera.
	 */
	private int yOffset = 0;

	/**
	 * The width of the game in pixels.
	 */
	private final int widthInPx;

	/**
	 * The height of the game in pixels.
	 */
	private final int heightInPx;

	/**
	 * The game manager object.
	 */
	private final GameManager gameManager;

	/**
	 * The client's input keys.
	 */
	private Inputs inputs;

	/**
	 * Whether or not the camera is locked.
	 */
	private boolean locked = true;

	/**
	 * The unit to be focussed if the camera is locked. Normally it is the
	 * player's controlling unit, but if the player is dead then it could be
	 * another unit that is being spectated.
	 */
	private Unit focussedUnit;

	private static final double CAMERA_SLIDE_FACTOR = 0.1;
	private static final double CAMERA_PAN_AMOUNT = 20;
	private final int cameraBorderThickness = 60 * Scale.factor;

	/**
	 * Instantiates a new camera object.
	 * 
	 * @param mapWidthInPx
	 *            The width of the game map in pixels.
	 * @param mapHeightInPx
	 *            The height of the game map in pixels.
	 */
	public Camera(Inputs keys, int mapWidthInPx, int mapHeightInPx, GameManager gameManager) {
		this.inputs = keys;
		this.widthInPx = mapWidthInPx * Scale.factor;
		this.heightInPx = mapHeightInPx * Scale.factor;
		this.gameManager = gameManager;
	}

	/**
	 * Called to update the x and y offsets of the camera. Repositions the
	 * camera to ensure it is not out of the map.
	 */
	public void update() {
		// Detect input for locking and unlocking the camera.
		detectInput();
		if (locked) {
			// If the camera is locked, follow the focussed unit.
			focusOnUnit();
		} else {
			// If not, detect mouse input on the edge of the screen to move the
			// camera.
			detectCameraPan();
		}
		// Reposition the camera so it is not out of bounds.
		reposition();
	}

	/**
	 * Detects if the user pressed the camera lock key.
	 */
	private void detectInput() {
		if (inputs.cameraLock.isPressed()) {
			locked = !locked;
			inputs.cameraLock.setPressed(false);
		}
	}

	/**
	 * Follows the focussedUnit.
	 */
	private void focusOnUnit() {
		// Follow the focussed unit based on their center point.
		Point unitCenter = focussedUnit.getCenterPoint();

		// Update the x and y offsets accordingly
		int newXOffset = unitCenter.x * Scale.factor - gameManager.getClient().getGameWidth() / 2;
		int newYOffset = unitCenter.y * Scale.factor - gameManager.getClient().getGameHeight() / 2;
		xOffset += (newXOffset - xOffset) * CAMERA_SLIDE_FACTOR;
		yOffset += (newYOffset - yOffset) * CAMERA_SLIDE_FACTOR;
	}

	/**
	 * Detects if the player is scrolling the screen with their mouse. In which
	 * case, track their input and move the camera accordingly.
	 */
	private void detectCameraPan() {
		double horizontal = 0;
		double vertical = 0;

		if (inputs.mouseX <= cameraBorderThickness) {
			horizontal = -1;
		}

		if (inputs.mouseX >= gameManager.getClient().getGameWidth() - cameraBorderThickness) {
			horizontal = 1;
		}

		if (inputs.mouseY <= cameraBorderThickness) {
			vertical = -1;
		}

		if (inputs.mouseY >= gameManager.getClient().getGameHeight() - cameraBorderThickness) {
			vertical = 1;
		}

		if (horizontal != 0 && vertical != 0) {
			horizontal *= 0.707;
			vertical *= 0.707;
		}

		xOffset += (int) (CAMERA_PAN_AMOUNT * horizontal);
		yOffset += (int) (CAMERA_PAN_AMOUNT * vertical);
	}

	/**
	 * Ensures the camera is within the bounds of the map. If it is not,
	 * repositions it.
	 */
	private void reposition() {
		xOffset = Math.max(0, xOffset);
		xOffset = Math.min(widthInPx - gameManager.getClient().getGameWidth(), xOffset);
		yOffset = Math.max(0, yOffset);
		yOffset = Math.min(heightInPx - gameManager.getClient().getGameHeight(), yOffset);
	}

	/**
	 * Gets the x offset of the camera.
	 * 
	 * @return The camera's offset in the x direction.
	 */
	public int getxOffset() {
		return xOffset;
	}

	/**
	 * Gets the y offset of the camera.
	 * 
	 * @return The camera's offset in the x direction.
	 */
	public int getyOffset() {
		return yOffset;
	}

	/**
	 * Returns whether or not the camera is locked.
	 * 
	 * @return True iff the player wants the camera to be focussed on the
	 *         focussed unit.
	 */
	public boolean isLocked() {
		return locked;
	}

	/**
	 * Sets whether or not the camera should be locked onto the focussed unit.
	 * 
	 * @param locked
	 *            Should the camera be locked?
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	/**
	 * Returns the focussed unit.
	 * 
	 * @return The unit that is focussed by the camera.
	 */
	public Unit getFocussedUnit() {
		return focussedUnit;
	}

	/**
	 * Sets the focussed unit to be followed by the camera if the camera is
	 * locked.
	 * 
	 * @param focussedUnit
	 *            The unit to be focussed.
	 */
	public void setFocussedUnit(Unit focussedUnit) {
		this.focussedUnit = focussedUnit;
	}

}
