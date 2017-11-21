package escort.client.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import escort.client.game.GameManager;
import escort.client.main.Scale;
import escort.client.res.PowerUpSprites;
import escort.client.res.WeaponSprites;
import escort.client.ui.utils.Colors;
import escort.common.game.entities.units.Unit;
import escort.common.game.map.GameMap;
import escort.common.game.map.Tile;
import escort.common.game.weapons.Bullet;
import escort.common.game.weapons.Grenade;
import escort.common.powerups.PowerUp;
import escort.common.systime.SystemTime;

/**
 * Manages and renderings the graphics of the game. Renders the map of the game.
 * Renders all units using their unit models. Handles animation.
 * 
 * @author Ahmed Bhallo
 *
 */
public class GameRenderer {

	/**
	 * The game manager object.
	 */
	private final GameManager gameManager;

	/**
	 * The camera.
	 */
	private final Camera camera;

	/**
	 * The game map.
	 */
	private final GameMap gameMap;

	/**
	 * A map of unit to unit model of all players in the game.
	 */
	private final Map<Unit, UnitModel> unitModelMap = new ConcurrentHashMap<>();

	/**
	 * A map of grenade explosion position to time when explode happened. Used
	 * for rendering explosion.
	 */
	private final Map<Point, Long> grenadeExplosionMap = new ConcurrentHashMap<>();

	/**
	 * The duration of a grenade explosion.
	 */
	private int GRENADE_EXPLOSION_DURATION = 500;

	/**
	 * Renderings all visible tiles in the map.
	 */
	private final MapRenderer mapRenderer;

	/**
	 * Instantiates a new game renderer. Iterates through the unit collection
	 * from the game manager and creates a new unit model based on the unit.
	 * 
	 * @param gameManager
	 *            The game manager.
	 * @param gameMap
	 *            The map of the game.
	 */
	public GameRenderer(GameManager gameManager, GameMap gameMap) {
		this.gameManager = gameManager;
		this.gameMap = gameMap;
		camera = gameManager.getCamera();
		for (Unit unit : gameManager.getUnits()) {
			// Creates a new unit model object from the unit and adds it to the
			// model map.
			unitModelMap.put(unit, new UnitModel(unit, camera));
		}
		mapRenderer = new MapRenderer(camera, gameMap);
	}

	/**
	 * Updates all models used for rendering.
	 */
	public void update() {
		// Updates all unit models.
		for (UnitModel model : unitModelMap.values()) {
			model.update();
		}

		for (Entry<Point, Long> entry : grenadeExplosionMap.entrySet()) {
			if (SystemTime.milliTime() - entry.getValue() >= GRENADE_EXPLOSION_DURATION) {
				grenadeExplosionMap.remove(entry.getKey());
			}
		}
	}

	/**
	 * Renders the map and calls render method in all the entitiy models.
	 * 
	 * @param g
	 */
	public void render(Graphics2D g) {
		// Renders only the visible tiles of the map.
		renderVisibleTiles(g);

		// Renders all units and entities.
		renderAllProjectiles(g);

		for (Entry<Point, Long> entry : grenadeExplosionMap.entrySet()) {
			renderGrenadeExplosion(g, entry.getKey(), entry.getValue());
		}
	}

	/**
	 * Renders the visible tiles of the map based on the x and y offset of the
	 * camera.
	 * 
	 * @param ga
	 */
	private void renderVisibleTiles(Graphics2D g) {
		// Store the map data from the gameMap
		int[][] mapData = gameMap.getMapData();

		// Store the x and y offsets of the camera
		int xOffset = camera.getxOffset();
		int yOffset = camera.getyOffset();

		// Start at the (offset values / tile length) tile. If this is < 0,
		// start at tile 0.
		int xStart = (int) Math.floor(Math.max(0, (double) (xOffset / mapRenderer.TILE_DISPLAY_W)));
		int yStart = (int) Math.floor(Math.max(0, (double) (yOffset / mapRenderer.TILE_DISPLAY_H)));

		// End at the (client's size dimension / tile length) tile. If this is >
		// the length of the map, start the length of the map.
		int xEnd = (int) Math.floor(Math.min(gameMap.getWidthInTiles(),
				(gameManager.getClient().getGameWidth() + xOffset) / mapRenderer.TILE_DISPLAY_W));
		int yEnd = (int) Math.floor(Math.min(gameMap.getHeightInTiles(),
				(gameManager.getClient().getGameWidth() + yOffset) / mapRenderer.TILE_DISPLAY_H));

		// Iterate through the visible tiles and render them.
		for (int j = yStart; j <= yEnd; j++) {
			for (int i = xStart; i <= xEnd; i++) {
				// Continue in the event of indices being out of bounds.
				if (j >= mapData.length || i >= mapData[0].length) {
					continue;
				}

				// Gets the tileID to be rendered from the map data array.
				int tileID = mapData[j][i];

				// Get the tile object from the tile map based on the ID.
				Tile t = Tile.ALL_TILES.get(tileID);

				// If the tile is not in the map, do not render it.
				if (t == null) {
					continue;
				}

				// Render the tile based.
				mapRenderer.renderTile(g, i, j);

				for (PowerUp powerUp : gameMap.getPowerUps()) {
					Rectangle bounds = powerUp.getAbsoluteBoundsInTiles();
					if (bounds.x == i && bounds.y == j) {
						renderPowerUp(g, powerUp);
					}
				}
			}

			// Render the units on this j value.

			for (Unit unit : unitModelMap.keySet()) {
				int bottomY = (int) ((unit.getY() + unit.getHeight()) / (double) Tile.TILE_HEIGHT);
				if (bottomY != j) {
					continue;
				}
				unitModelMap.get(unit).render(g);
			}

		}

	}

	/**
	 * Render a power-up icon.
	 * 
	 * @param g
	 *            Graphics object
	 * @param powerUp
	 *            The power-up to render.
	 */
	private void renderPowerUp(Graphics2D g, PowerUp powerUp) {
		if (!powerUp.isActive()) {
			// Don't render the icon if the power-up is not active.
			return;
		}

		// Get the correct image based on the type of the power-up.
		BufferedImage img = null;
		switch (powerUp.getType()) {
		case PowerUp.REPLENISH_HEALTH:
			img = PowerUpSprites.HP_IMAGE;
			break;
		case PowerUp.EXTRA_GRENADES:
			img = PowerUpSprites.GRENADE_IMAGE;
			break;
		case PowerUp.EXTRA_MAGS:
			img = PowerUpSprites.AMMO_IMAGE;
			break;
		}

		int renderX = (int) ((powerUp.getX() + (Tile.TILE_WIDTH - PowerUpSprites.WIDTH) / 2) * Scale.factor
				- camera.getxOffset());
		int renderY = (int) ((powerUp.getY() + (Tile.TILE_HEIGHT - PowerUpSprites.HEIGHT) / 2) * Scale.factor
				- camera.getyOffset());
		g.drawImage(img, renderX, renderY, PowerUpSprites.WIDTH * Scale.factor, PowerUpSprites.HEIGHT * Scale.factor,
				null);
	}

	/**
	 * Renders all of the units and their possessive entities (such as grenades
	 * and bullets).
	 * 
	 * @param g
	 */
	private void renderAllProjectiles(Graphics2D g) {
		for (Unit unit : unitModelMap.keySet()) {
			Grenade heldGrenade = unit.getHeldGrenade();
			if (heldGrenade != null) {
				renderGrenade(g, heldGrenade);
			}
			for (Grenade thrownGrenades : unit.getAirborneGrenades()) {
				renderGrenade(g, thrownGrenades);
			}
			if (unit.getPistol() != null) {
				for (Bullet bullet : unit.getPistol().getFiredBullets()) {
					renderPistolBullet(g, bullet);
				}
			}
			if (unit.getMG() != null) {
				for (Bullet bullet : unit.getMG().getFiredBullets()) {
					renderMGBullet(g, bullet);
				}
			}
		}
	}

	/**
	 * Render a grenade object.
	 * 
	 * @param g
	 *            The graphics object to draw on.
	 * @param grenade
	 *            The grenade object to draw.
	 */
	private void renderGrenade(Graphics2D g, Grenade grenade) {
		int renderX = (int) (grenade.getX() * Scale.factor - camera.getxOffset());
		int renderY = (int) (grenade.getY() * Scale.factor - camera.getyOffset());
		g.drawImage(WeaponSprites.GRENADE_ICON, renderX, renderY, grenade.getWidth() * Scale.factor,
				grenade.getHeight() * Scale.factor, null);
	}

	/**
	 * Renders the explosion of a grenade.
	 * 
	 * @param g
	 *            The grenade object.
	 * @param grenadePoint
	 *            The point of explosion.
	 * @param timeWhenExploded
	 *            The time when the grenade epxloded.
	 */
	private void renderGrenadeExplosion(Graphics2D g, Point grenadePoint, long timeWhenExploded) {
		// Calculate the x position to render.
		int renderX = (int) (grenadePoint.x * Scale.factor - camera.getxOffset());

		// Calculate the y position to render.
		int renderY = (int) (grenadePoint.y * Scale.factor - camera.getyOffset());

		// Calculate the duration ratio based on the duration of an explosion.
		double durationRatio = (SystemTime.milliTime() - timeWhenExploded) / (double) GRENADE_EXPLOSION_DURATION;
		durationRatio = Math.max(0, Math.min(1, durationRatio));

		// Calculate the color values and set the graphics object accordingly
		int colorValue = (int) (255 - 100 * durationRatio);
		int alphaValue = (int) (255 - 255 * durationRatio);
		g.setColor(new Color(colorValue, colorValue, colorValue, alphaValue));

		// Calculate the rendered proximity
		int proximity = (int) (Grenade.GRENADE_EFFECT_AREA * Scale.factor);

		// Multiply the proximity by the duration ratio.
		proximity *= durationRatio;

		// Fill an oval to render the explosion.
		g.fillOval(renderX - proximity / 2, renderY - proximity / 2, proximity, proximity);
	}

	/**
	 * Render a bullet object from a pistol.
	 * 
	 * @param g
	 *            The graphics object to draw on.
	 * @param bullet
	 *            The bullet to draw.
	 */
	private void renderPistolBullet(Graphics2D g, Bullet bullet) {
		g.setColor(Colors.PISTOL_BULLET_COLOR);
		g.fillRect((int) (bullet.getX() * Scale.factor - camera.getxOffset()),
				(int) (bullet.getY() * Scale.factor - camera.getyOffset()), bullet.getWidth()*Scale.factor, bullet.getHeight()*Scale.factor);
	}

	/**
	 * Render a bullet object from a MG., b
	 * 
	 * @param g
	 *            The graphics object to draw on.
	 * @param bullet
	 *            The bullet to draw.
	 */
	private void renderMGBullet(Graphics2D g, Bullet bullet) {
		g.setColor(Colors.MG_BULLET_COLOR);
		g.fillRect((int) (bullet.getX() * Scale.factor - camera.getxOffset()),
				(int) (bullet.getY() * Scale.factor - camera.getyOffset()), bullet.getWidth()*Scale.factor, bullet.getHeight()*Scale.factor);
	}

	/**
	 * Add an explosion to the map.
	 * 
	 * @param centerPoint
	 *            The point of explosion.
	 * @param milliTime
	 *            The time when the grenade exploded.
	 */
	public void addExplosion(Point centerPoint, long milliTime) {
		grenadeExplosionMap.put(centerPoint, milliTime);
	}
}
