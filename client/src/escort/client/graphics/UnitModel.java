package escort.client.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import escort.client.main.Scale;
import escort.client.res.UnitSprites;
import escort.client.res.WeaponSprites;
import escort.client.ui.RenderUtils;
import escort.client.ui.utils.Colors;
import escort.client.ui.utils.Fonts;
import escort.common.game.entities.units.Unit;
import escort.common.systime.SystemTime;

/**
 * A model of the unit used for rendering and animation handling.
 * 
 * @author Ahmed Bhallo
 *
 */
public class UnitModel {

	/**
	 * The unit being modelled.
	 */
	private final Unit unit;

	/**
	 * The camera of the client.
	 */
	private final Camera camera;

	/**
	 * The Rotation index of the unit. Used to determine which sprite to render
	 * based on the unit's facing direction.
	 */
	private int rotationIndex;

	private BufferedImage[] head;
	private BufferedImage[] torso;
	private BufferedImage[] legs;

	private final int renderHPBarHeight = 10 * Scale.factor;
	private final int renderReloadBarHeight = 8 * Scale.factor;
	private final int renderHeadWidth = Unit.UNIT_HEAD_WIDTH * Scale.factor;
	private final int renderHeadHeight = Unit.UNIT_HEAD_HEIGHT * Scale.factor;
	private final int renderTorsoWidth = Unit.UNIT_TORSO_WIDTH * Scale.factor;
	private final int renderTorsoHeight = Unit.UNIT_TORSO_HEIGHT * Scale.factor;
	private final int renderLegsWidth = Unit.UNIT_LEGS_WIDTH * Scale.factor;
	private final int renderLegsHeight = Unit.UNIT_LEGS_HEIGHT * Scale.factor;
	private final int renderBodyOffset = (Unit.UNIT_WIDTH - Unit.UNIT_TORSO_WIDTH) / 2 * Scale.factor;
	private final int renderUnitHeight = Unit.UNIT_HEIGHT * Scale.factor;

	private final int barSeparation = 6 * Scale.factor;
	private final int barPadding = 2 * Scale.factor;
	private boolean renderUnit = true;

	private boolean unitDead = false;
	private long timeWhenDead = SystemTime.milliTime();
	private static final long DEATH_ANIMATION_DURATION = 1000;

	private boolean unitReloading = false;
	private long timeWhenReload = SystemTime.milliTime();
	private int reloadDuration;

	private int legsFrame = 0;
	private int headFrame = 0;

	/**
	 * Instantiates a new Unit Model object based on the given unit.
	 * 
	 * @param unit
	 *            The unit to be rendered.
	 * @param camera
	 *            The camera object of the client.
	 */
	public UnitModel(Unit unit, Camera camera) {
		this.unit = unit;
		this.camera = camera;
		switch (unit.getUnitType()) {
		case Unit.ESCORT_TYPE:
			head = UnitSprites.ESCORT_HEAD;
			torso = UnitSprites.ESCORT_TORSO;
			legs = UnitSprites.ESCORT_LEGS;
			break;
		case Unit.PRESIDENT_TYPE:
			head = UnitSprites.PRESIDENT_HEAD;
			torso = UnitSprites.PRESIDENT_TORSO;
			legs = UnitSprites.PRESIDENT_LEGS;
			break;
		case Unit.ASSASSIN_TYPE:
			head = UnitSprites.ASSASSIN_HEAD;
			torso = UnitSprites.ASSASSIN_TORSO;
			legs = UnitSprites.ASSASSIN_LEGS;
			break;
		case Unit.CIVILIAN_TYPE:
			head = UnitSprites.CIVILIAN_HEAD;
			torso = UnitSprites.CIVILIAN_TORSO;
			legs = UnitSprites.CIVILIAN_LEGS;
			break;
		case Unit.POLICE_TYPE:
			head = UnitSprites.POLICE_HEAD;
			torso = UnitSprites.POLICE_TORSO;
			legs = UnitSprites.POLICE_LEGS;
			break;
		default:
			throw new IllegalArgumentException("Invalid unit type");
		}
	}

	/**
	 * Calls methods to update rotation index of the model and animation
	 * updates.
	 */
	public void update() {
		updateRotationIndex();
		detectDeath();
		detectReload();
		updateWalkingFrame();
	}

	private void detectDeath() {
		if (!unitDead && unit.isDead()) {
			timeWhenDead = SystemTime.milliTime();
			unitDead = true;
			unitReloading = false;
		}

		if (unitDead) {
			if (renderUnit && SystemTime.milliTime() - timeWhenDead >= DEATH_ANIMATION_DURATION) {
				renderUnit = false;
			}
			if (!unit.isDead()) {
				renderUnit = true;
				unitDead = false;
			}
		}
	}

	private void detectReload() {
		if (!unitReloading && (unit.isReloadingMG() || unit.isReloadingPistol())) {
			timeWhenReload = SystemTime.milliTime();
			reloadDuration = unit.isReloadingMG() ? unit.getMG().getReloadSpeed() : unit.getPistol().getReloadSpeed();
			unitReloading = true;
		}

		if (unitReloading) {
			if (SystemTime.milliTime() - timeWhenReload > reloadDuration
					|| !(unit.isReloadingMG() || unit.isReloadingPistol())) {
				unitReloading = false;
			}
		}

	}

	private void updateWalkingFrame() {
		if (isWalking()) {
			legsFrame = ((int) (SystemTime.milliTime() / 250) % 2) + 1;
			headFrame = (int) (SystemTime.milliTime() / 250) % 2;
		} else {
			legsFrame = 0;
			headFrame = 0;
		}
	}

	private boolean isWalking() {
		return unit.getXVel() * unit.getXVel() + unit.getYVel() * unit.getYVel() > 0;
	}

	/**
	 * Updates the rotation index of the unit based on the unit's facing
	 * direction.
	 */
	private void updateRotationIndex() {
		// Converts the unit's direction into a rotation state.
		int rotation = (int) (unit.getDir() * 8 / Math.PI);

		// Based on the rotation state, updates the rotation index accordingly.
		if (rotation < 1) {
			rotationIndex = 4;
		} else if (rotation < 3) {
			rotationIndex = 3;
		} else if (rotation < 5) {
			rotationIndex = 2;
		} else if (rotation < 7) {
			rotationIndex = 1;
		} else if (rotation < 9) {
			rotationIndex = 0;
		} else if (rotation < 11) {
			rotationIndex = 7;
		} else if (rotation < 13) {
			rotationIndex = 6;
		} else if (rotation < 15) {
			rotationIndex = 5;
		} else {
			rotationIndex = 4;
		}
	}

	/**
	 * Gets the image of the head of the unit.
	 * 
	 * @return The head image of the unit.
	 */
	public BufferedImage getHead() {
		BufferedImage headImage = head[rotationIndex];
		return unitDead ? overlayDeathImage(headImage) : headImage;
	}

	/**
	 * Gets the image of the torso of the unit.
	 * 
	 * @return The torso image of the unit.
	 */
	public BufferedImage getTorso() {
		BufferedImage torsoImage = torso[rotationIndex];
		return unitDead ? overlayDeathImage(torsoImage) : torsoImage;
	}

	/**
	 * @return The image of the legs of the unit
	 */
	public BufferedImage getLegs() {
		BufferedImage legsImage = legs[legsFrame];
		return unitDead ? overlayDeathImage(legsImage) : legsImage;
	}

	/**
	 * Over lays an image with white based on how long the unit has been dead
	 * for
	 * 
	 * @param image
	 *            The image to be overlayed.
	 * @return The overlayed image.
	 */
	private BufferedImage overlayDeathImage(BufferedImage image) {
		float alpha = (SystemTime.milliTime() - timeWhenDead) / (float) DEATH_ANIMATION_DURATION;

		if (alpha > 1 || alpha < 0) {
			return null;
		}
		return RenderUtils.overlayImage(image, Colors.DARK_WHITE, alpha);
	}

	/**
	 * Renders the unit.
	 * 
	 * @param g
	 */
	public void render(Graphics2D g) {
		if (!renderUnit) {
			return;
		}
		if (facingFront()) {
			renderUnit(g);
			renderWeapon(g);
		} else {
			renderWeapon(g);
			renderUnit(g);
		}
		renderHealthBar(g);
		renderBlastShield(g);
		renderReloadBar(g);
		renderUsername(g);
	}

	/**
	 * @return true iff the unit is facing the front.
	 */
	private boolean facingFront() {
		switch (rotationIndex) {
		case 3:
		case 4:
		case 5:
			return false;
		default:
			return true;
		}
	}

	/**
	 * Renders the unit's head, torso and legs
	 * 
	 * @param g
	 */
	private void renderUnit(Graphics2D g) {
		int renderX = (int) (unit.getX() * Scale.factor - camera.getxOffset());
		int renderY = (int) (unit.getY() * Scale.factor - camera.getyOffset());
		g.drawImage(getHead(), renderX, renderY + headFrame * Scale.factor * 6, renderHeadWidth, renderHeadHeight,
				null);
		g.drawImage(getTorso(), renderX + renderBodyOffset, renderY + renderHeadHeight, renderTorsoWidth,
				renderTorsoHeight, null);
		g.drawImage(getLegs(), renderX + renderBodyOffset, renderY + renderHeadHeight + renderTorsoHeight,
				renderLegsWidth, renderLegsHeight, null);
	}

	/**
	 * Renders the unit's health bar
	 * 
	 * @param g
	 */
	private void renderHealthBar(Graphics2D g) {
		int renderX = (int) (unit.getX() * Scale.factor - camera.getxOffset());
		int renderY = (int) (unit.getY() * Scale.factor - camera.getyOffset()) - renderHPBarHeight - barSeparation;
		int barWidth = (int) ((unit.getHP() / (double) unit.getMaxHP()) * renderHeadWidth);
		g.setColor(Colors.BAR_OUTLINE);
		g.fillRect(renderX - barPadding, renderY - barPadding, renderHeadWidth + barPadding * 2,
				renderHPBarHeight + barPadding * 2);
		g.setColor(Colors.PRESIDENTIAL_RED);
		g.fillRect(renderX, renderY, barWidth, renderHPBarHeight);
		renderText(g, unit.getHP() + "/" + unit.getMaxHP(), Fonts.BODY, Colors.WHITE, renderX,
				renderY - barPadding + 3 * Scale.factor, renderHeadWidth);
	}

	/**
	 * Renders the unit's reload bar if the unit is realoading
	 * 
	 * @param g
	 */
	private void renderReloadBar(Graphics2D g) {
		if (!unitReloading) {
			return;
		}
		int renderX = (int) (unit.getX() * Scale.factor - camera.getxOffset());
		int renderY = (int) (unit.getY() * Scale.factor - camera.getyOffset()) - renderHPBarHeight
				- renderReloadBarHeight - barSeparation * 2;
		int barWidth = (int) (((SystemTime.milliTime() - timeWhenReload) / (double) reloadDuration) * renderHeadWidth);
		g.setColor(Colors.BAR_OUTLINE);
		g.fillRect(renderX - barPadding, renderY - barPadding, renderHeadWidth + barPadding * 2,
				renderReloadBarHeight + barPadding * 2);
		g.setColor(Colors.ORANGE);
		g.fillRect(renderX, renderY, barWidth, renderReloadBarHeight);
		renderText(g, "Reload", Fonts.BODY, Colors.WHITE, renderX, renderY - barPadding + 2 * Scale.factor,
				renderHeadWidth);
	}

	/**
	 * Renders the unit's blast shield if the unit is holding one.
	 * 
	 * @param g
	 */
	private void renderBlastShield(Graphics2D g) {
		if (unit.getWeapon() != Unit.SHIELD) {
			return;
		}
		int shieldOffset = 30 * Scale.factor;
		int renderX = (int) (unit.getX() * Scale.factor - camera.getxOffset());
		int renderY = (int) (unit.getY() * Scale.factor - camera.getyOffset());
		g.setColor(Colors.setAlpha(Colors.ORANGE, unit.getBlastShield().getHP()));
		g.fillRect(renderX + renderBodyOffset, renderY + shieldOffset, renderTorsoWidth,
				renderUnitHeight - shieldOffset);
		g.setColor(Colors.DARK_WHITE);
		RenderUtils.renderRectBorder(g, Colors.SHIELD_OUTLINE, renderX + renderBodyOffset, renderY + shieldOffset,
				renderTorsoWidth, renderUnitHeight - shieldOffset, 3 * Scale.factor);
	}

	/**
	 * Renders the unit's weapon if the unit is not dead.
	 * 
	 * @param g
	 */
	private void renderWeapon(Graphics2D g) {
		if (unitDead) {
			return;
		}
		BufferedImage gunImage = null;
		if (unit.getWeapon() == Unit.PISTOL) {
			gunImage = WeaponSprites.PISTOL_ICON;
		} else if (unit.getWeapon() == Unit.MACHINE_GUN) {
			gunImage = WeaponSprites.MG_ICON;
		} else {
			return;
		}

		int xOffset = 0;
		int yOffset = 4 * Scale.factor;
		int gunWidth = gunImage.getWidth() * Scale.factor * 2;
		int gunHeight = gunImage.getHeight() * Scale.factor * 2;
		switch (rotationIndex) {
		case 0:
			break;
		case 1:
			xOffset = 5 * Scale.factor;
			break;
		case 2:
			xOffset = 10 * Scale.factor;
			break;
		case 7:
			gunWidth *= -1;
			xOffset = -5 * Scale.factor;
			break;
		case 6:
			gunWidth *= -1;
			xOffset = -10 * Scale.factor;
			break;
		case 3:
			xOffset = 5 * Scale.factor;
			break;
		case 4:
			return;
		case 5:
			xOffset = -5 * Scale.factor;
			gunWidth *= -1;
			break;
		}

		int renderX = (int) (unit.getCenterPoint().x * Scale.factor - camera.getxOffset()) + xOffset;
		int renderY = (int) (unit.getCenterPoint().y * Scale.factor - camera.getyOffset()) + yOffset;
		g.drawImage(gunImage, renderX - gunWidth / 2, renderY - gunHeight / 2, gunWidth, gunHeight, null);
	}

	/**
	 * Renders the unit's username.
	 * 
	 * @param g
	 */
	private void renderUsername(Graphics2D g) {
		int renderX = (int) (unit.getX() * Scale.factor - camera.getxOffset());
		int renderY = (int) (unit.getY() * Scale.factor - camera.getyOffset());
		renderText(g, unit.getUsername(), Fonts.BODY, Colors.WHITE, renderX, renderY - 4 * Scale.factor,
				renderHeadWidth);
	}

	/**
	 * Helper method to render text on the screen.
	 * 
	 * @param g
	 *            The graphics object
	 * @param text
	 *            The text to be rendered
	 * @param font
	 *            The desired font of the text
	 * @param color
	 *            The desired color of the text
	 * @param x
	 *            The x position to render the text
	 * @param y
	 *            The y position to render the text
	 * @param width
	 *            The width of the text
	 */
	private void renderText(Graphics2D g, String text, Font font, Color color, int x, int y, int width) {
		g.setFont(font);
		FontMetrics metrics = g.getFontMetrics();
		x += (width - metrics.stringWidth(text)) / 2;
		y += metrics.getAscent() + 1;
		g.setColor(Colors.setAlpha(Colors.LIGHT_BLACK, 150));
		g.drawString(text, x + 2, y + 2);
		g.setColor(Colors.setAlpha(Colors.BLACK, 200));
		g.drawString(text, x + 1, y + 1);
		g.setColor(color);
		g.drawString(text, x, y);
	}
}