package escort.common.game.entities;

import java.awt.Rectangle;

import escort.common.game.GameData;
import escort.common.game.map.GameMap;

/**
 * Any Entity that moves
 * 
 * @author Ahmed Bhallo
 *
 */
public abstract class Mob extends Entity {

	private double speed = 2;
	private final GameMap map;

	/**
	 * How much should the unit slide? Slide is when a unit continues to move
	 * for a short while after they have stopped moving.
	 */
	private double slideFactor = 0.9;

	/**
	 * -1 <= xMove <= 1 Determines how much of the speed to use in the x
	 * direction. Sign determines direction.
	 */
	private double xMove = 0;

	/**
	 * -1 <= yMove <= 1 Determines how much of the speed to use in the y
	 * direction. Sign determines direction.
	 */
	private double yMove = 0;

	private double smoothX = getX();
	private double smoothY = getY();

	/**
	 * Create a new mobile entity
	 * 
	 * @param data
	 *            The game data
	 * @param x
	 *            The x position of the entity
	 * @param y
	 *            The y position of the entity
	 * @param initWidth
	 *            The width of the entity
	 * @param initHeight
	 *            The height of the entity
	 */
	public Mob(GameData data, double x, double y, int initWidth, int initHeight) {
		super(x, y, initWidth, initHeight, data);
		this.map = data.getMap();
	}

	/**
	 * Calls the super update method and calls move.
	 */
	@Override
	public void update() {
		super.update();
		move();
	}

	/**
	 * Move the entity based on the x and y velocity
	 */
	private void move() {
		if (xMove > 1 || yMove > 1 || xMove < -1 || yMove < -1) {
			throw new RuntimeException("xMove and yMove must be <= 1 && >= -1");
		}

		// If moving diagonally, multiply x and y move by (sqrt 2) / 2
		if (Math.abs(xMove) == 1 && Math.abs(yMove) == 1) {
			xMove *= 0.707;
			yMove *= 0.707;
		}

		double oldSmoothX = smoothX;
		double oldSmoothY = smoothY;

		smoothX += (xMove * speed);
		smoothY += (yMove * speed);

		// Calculate new x and y values

		double newX = smoothX;
		double newY = smoothY;

		// Splitting the following checks will allow us to still move when
		// diagonal input is received even when movement is restricted on 1
		// dimension. It still treats the moveable dimension as a diagonal
		// movement (and therefore multiplied by root 2 / 2. But this is a
		// feature of entity movement.)

		Rectangle bounds = getCollisionBounds();

		boolean currentlyInWall = !map.walkable((int) (bounds.x + getX()), (int) (bounds.y + getY()), bounds.width,
				bounds.height);

		if (currentlyInWall
				|| map.walkable((int) (bounds.x + newX), (int) (bounds.y + getY()), bounds.width, bounds.height)) {
			setX(newX);
		} else {
			smoothX = oldSmoothX;
			horizontalCollision();
		}

		if (currentlyInWall
				|| map.walkable((int) (bounds.x + getX()), (int) (bounds.y + newY), bounds.width, bounds.height)) {
			setY(newY);
		} else {
			smoothY = oldSmoothY;
			verticalCollision();
		}

		// Multiply xMove and yMove by the slide factor
		xMove *= slideFactor;
		yMove *= slideFactor;

		// If xMove, yMove are close to (but not equal to) 0, set them exactly
		// to 0
		double threshold = 0.1;
		if (xMove != 0 && Math.abs(xMove) <= threshold) {
			xMove = 0;
		}
		if (yMove != 0 && Math.abs(yMove) <= threshold) {
			yMove = 0;
		}
	}

	/**
	 * What to do if hit a wall horizontally
	 */
	public abstract void horizontalCollision();

	/**
	 * What to do if hit a wall vertically
	 */
	public abstract void verticalCollision();

	/**
	 * Set the slide factor, how much should the unit slide? Slide is when a
	 * unit continues to move for a short while after they have stopped moving.
	 * 
	 * @param slideFactor
	 *            The slide factor (between 0 and 1) (1 = infinite slide)
	 */
	public void setSlideFactor(double slideFactor) {
		this.slideFactor = slideFactor;
	}

	/**
	 * Set the speed of the entity, how fast does it move across the map
	 * 
	 * @param speed
	 *            The speed
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	/**
	 * Get the speed of the object
	 * @return the speed of the object
	 */
	public double getSpeed(){
		return this.speed;
	}

	/**
	 * Set the x velocity (between 0 and 1)
	 * 
	 * @param xMove
	 *            The x velocity
	 */
	public void setXVel(double xMove) {
		this.xMove = xMove;
	}

	/**
	 * Set the y velocity (between 0 and 1)
	 * 
	 * @param yMove
	 *            The y velocity
	 */
	public void setYVel(double yMove) {
		this.yMove = yMove;
	}

	@Override
	public void setX(double x) {
		super.setX(x);
		if (Math.abs(smoothX - x) >= 5) {
			smoothX = x;
		}
	}

	@Override
	public void setY(double y) {
		super.setY(y);
		if (Math.abs(smoothY - y) >= 5) {
			smoothY = y;
		}
	}

	/**
	 * Get the x velocity of the entity
	 * 
	 * @return The x velocity
	 */
	public double getXVel() {
		return xMove;
	}

	/**
	 * Get the y velocity of the entity
	 * 
	 * @return The y velocity
	 */
	public double getYVel() {
		return yMove;
	}
}