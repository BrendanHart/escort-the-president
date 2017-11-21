package escort.common.game.entities;

import java.awt.Point;
import java.awt.Rectangle;

import escort.common.game.GameData;
import escort.common.game.map.Tile;

/**
 * An Entity object.
 * 
 * @author Ahmed Bhallo
 * @author Brendan Hart
 * @author Edward Dean
 *
 */
public class Entity {

	private double x;
	private double y;
	private int width;
	private int height;
	private GameData gameData;

	/**
	 * Create an entity (thing) in the game
	 *
	 * @param x
	 *            The initial x position of the object
	 * @param y
	 *            The initial y position of the object
	 * @param width
	 *            The initial width of the object
	 * @param height
	 *            The initial height of the object
	 * @param gameData
	 *            The game data that contains all the players and the map
	 */
	public Entity(double x, double y, int width, int height, GameData gameData) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.gameData = gameData;
	}

	/**
	 * Get the game data for the game (contains the map, all units and the
	 * president)
	 * 
	 * @return The game data
	 */
	public GameData getGameData() {
		return gameData;
	}

	/**
	 * Update the entity on every game loop
	 */
	public void update() {
	}

	/**
	 * Get the centre point of the entity
	 * 
	 * @return The centre point of the entity
	 */
	public Point getCenterPoint() {
		int centerX = (int) (x) + width / 2;
		int centerY = (int) (y) + height / 2;
		return new Point(centerX, centerY);
	}

	/**
	 * Get the current x position of the entity
	 * 
	 * @return The x position of the entity
	 */
	public double getX() {
		return x;
	}

	/**
	 * Get the current y position of the entity
	 * 
	 * @return The y position of the entity
	 */
	public double getY() {
		return y;
	}

	/**
	 * Set the current x position of the entity
	 * 
	 * @param x
	 *            The x position of the entity to change
	 */
	public void setX(double x) {
		this.x = x;
	}

	/**
	 * Set the current y position of the entity
	 * 
	 * @param y
	 *            The y position of the entity to change
	 */
	public void setY(double y) {
		this.y = y;
	}

	/**
	 * Get the current width of the entity
	 * 
	 * @return The width of the entity
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Get the current height of the entity
	 * 
	 * @return The height of the entity
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Checks whether an entity is colliding with another entity
	 * 
	 * @param entity
	 *            The entity to check for collision
	 * @return Whether the two entities have collided
	 */
	public boolean collision(Entity entity) {
		return getAbsoluteBounds().intersects(entity.getAbsoluteBounds());
	}

	/**
	 * Return the hit box of the entity. (The area that other entities will be
	 * effect by)
	 * 
	 * @return The hit box of the entity
	 */
	public Rectangle getHitbox() {
		return new Rectangle((int) getX(), (int) getY(), getWidth(), getHeight());
	}

	/**
	 * Gets the boundaries of this entity, relative to this entity's top left
	 * position.
	 */
	public Rectangle getCollisionBounds() {
		return new Rectangle(0, 0, width, height);
	}

	/**
	 * Gets the absolute boundaries of this entity in tiles, relative to this
	 * entity's top left position.
	 * 
	 * @return The bounds in tiles of the entity
	 */
	public Rectangle getAbsoluteBoundsInTiles() {
		Rectangle bounds = getCollisionBounds();
		bounds.x = (int) Math.floor((bounds.x + x) / Tile.TILE_WIDTH);
		bounds.y = (int) Math.floor((bounds.y + y) / Tile.TILE_HEIGHT);
		bounds.width = (int) Math.floor(bounds.width / Tile.TILE_WIDTH);
		bounds.height = (int) Math.floor(bounds.height / Tile.TILE_HEIGHT);
		return bounds;
	}

	/**
	 * Gets the absolute boundaries of this entity in pixels
	 * 
	 * @return The hit box around the entity
	 */
	public Rectangle getAbsoluteBounds() {
		Rectangle bounds = getCollisionBounds();
		bounds.x = (int) Math.floor(bounds.x + x);
		bounds.y = (int) Math.floor(bounds.y + y);
		return bounds;
	}

}