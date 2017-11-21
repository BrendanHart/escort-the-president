package escort.client.graphics.hud;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import escort.client.graphics.Camera;
import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.res.UnitSprites;
import escort.client.ui.RenderUtils;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.utils.Colors;
import escort.common.game.GameData;
import escort.common.game.entities.units.Unit;
import escort.common.game.map.GameMap;

/**
 * A container that renders the game map, all units in the game and the position
 * of the camera.
 * 
 * @author Ahmed Bhallo
 *
 */
public class Radar extends Panel {

	private static final Color PRESIDENT_COLOR = UnitSprites.PRESIDENT_LEGS_COLOR;
	private static final Color ESCORT_COLOR = UnitSprites.ESCORT_LEGS_COLOR;
	private static final Color ASSASSIN_COLOR = UnitSprites.ASSASSIN_LEGS_COLOR;
	private static final Color CIVILIAN_COLOR = UnitSprites.CIVILIAN_LEGS_COLOR;
	private static final Color POLICE_COLOR = UnitSprites.POLICE_LEGS_COLOR;

	private final GameData gameData;
	private final GameMap gameMap;
	private final BufferedImage map;
	private final Camera camera;
	private final int renderCameraWidth;
	private final int renderCameraHeight;

	private final int mapScale = 2 * Scale.factor;

	/**
	 * Instantiates a new radar object.
	 * 
	 * @param hud
	 *            The HUD Manager
	 * @param inputs
	 *            The inputs object
	 */
	public Radar(HUDManager hud, Inputs inputs) {
		super(inputs, 0, 0);
		switch (hud.getGameManager().getGameData().getMap().getMapID()) {
		case GameMap.HOTEL_ID:
			map = GameMap.HOTEL_CODE;
			break;
		default:
			map = GameMap.UNIVERSITY_CODE;
			break;
		}
		camera = hud.getGameManager().getCamera();
		gameData = hud.getGameManager().getGameData();
		gameMap = gameData.getMap();
		setWidth(map.getWidth() * mapScale);
		setHeight(map.getHeight() * mapScale);
		renderCameraWidth = convertHorizontal(hud.getGameManager().getClient().getGameWidth());
		renderCameraHeight = convertVertical(hud.getGameManager().getClient().getGameHeight());
	}

	/**
	 * Renders the map image. Calls methods to render units and the camera
	 * rectangle.
	 */
	@Override
	public void render(Graphics2D g) {
		super.render(g);
		g.drawImage(map, 0, 0, getWidth(), getHeight(), null);
		renderAllUnits(g);
		renderCameraRect(g);
	}

	/**
	 * Renders all units in the game based on their position and color.
	 * 
	 * @param g
	 */
	private void renderAllUnits(Graphics2D g) {
		for (Unit unit : gameData.getUnits().values()) {
			g.setColor(getUnitColor(unit));
			int x = convertHorizontal((int) unit.getX() + unit.getCollisionBounds().x);
			int y = convertVertical((int) unit.getY() + unit.getCollisionBounds().y);
			g.fillRect(x * Scale.factor, y * Scale.factor, mapScale*2, mapScale*2);
		}
	}

	/**
	 * Given a unit, switches on the unit type and returns their appropriate
	 * color.
	 * 
	 * @param unit
	 *            The unit
	 * @return The color code of that unit.
	 */
	private Color getUnitColor(Unit unit) {
		switch (unit.getUnitType()) {
		case Unit.PRESIDENT_TYPE:
			return PRESIDENT_COLOR;
		case Unit.ESCORT_TYPE:
			return ESCORT_COLOR;
		case Unit.ASSASSIN_TYPE:
			return ASSASSIN_COLOR;
		case Unit.CIVILIAN_TYPE:
			return CIVILIAN_COLOR;
		default:
			return POLICE_COLOR;
		}
	}

	/**
	 * Renders the rectangle of the camera.
	 * 
	 * @param g
	 */
	private void renderCameraRect(Graphics2D g) {
		int x = convertHorizontal(camera.getxOffset());
		int y = convertVertical(camera.getyOffset());
		RenderUtils.renderRectBorder(g, Colors.LIGHT_GRAY, x, y, renderCameraWidth, renderCameraHeight, Scale.factor);
	}

	/**
	 * Convert horizontal coordinates from ingame pixels to radar pixels
	 * 
	 * @param x
	 *            The x coordinate
	 * @return The converted x coordinate.
	 */
	private int convertHorizontal(int x) {
		return (int) Math.floor(x / (double) (gameMap.getWidthInPx() * Scale.factor) * getWidth());
	}

	/**
	 * Convert vertical coordinates from ingame pixels to radar pixels
	 * 
	 * @param x
	 *            The y coordinate
	 * @return The converted y coordinate.
	 */
	private int convertVertical(int y) {
		return (int) Math.floor(y / (double) (gameMap.getHeightInPx() * Scale.factor) * getHeight());
	}

}
