package escort.client.res;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import escort.client.main.GameWindow;
import escort.client.main.Scale;
import escort.client.ui.menus.MenuManager;
import escort.client.ui.utils.Fonts;
import escort.common.game.map.MapLoader;
import escort.common.game.map.Tile;

/**
 * Used to load all resources when the client opens.
 * 
 * @author Ahmed Bhallo
 *
 */
public class ResourceLoader {

	private static final Color SPRITE_BORDER_COLOR = Color.decode("#FF00FF");
	private static final Color SPRITE_BACKGROUND_COLOR = Color.decode("#FF99FF");
	private static final Color BASE_LEGS_COLOR = Color.decode("#00FF00");

	private BufferedImage[] shadows = new BufferedImage[3];

	/**
	 * Instantiates a new resource loader object. Empty constructor.
	 */
	public ResourceLoader() {

	}

	/**
	 * Load all resources to be used by the client.
	 */
	public void load() {
		try {
			// Load the map and tile objects.
			new MapLoader().load();

			// Load shadow images.
			loadTileShadow();

			// Load the tile sprites.
			loadTileSprites();

			// Load fonts.
			loadAllFonts();

			// Load unit sprites.
			loadAllUnits();

			// Load all weapon sprites.
			loadAllWeaponSprites();

			// Load all power-ups.
			loadAllPowerUpSprites();

			// Load the menu images
			loadMenuImages();

			// Load thumbnail for JFrame
			loadThumbnail();
		} catch (IOException e) {
			// Swallow the IOException.
			e.printStackTrace();
		}
	}

	/**
	 * Loads all fonts.
	 * 
	 * @throws IOException
	 */
	public void loadAllFonts() throws IOException {
		try {
			Font codersCrux = loadFont("fonts/coders_crux.ttf");
			Fonts.HEADER = codersCrux.deriveFont(Font.PLAIN, Fonts.MEDIUM * Scale.factor);
			Fonts.BODY = codersCrux.deriveFont(Font.PLAIN, Fonts.SMALL * Scale.factor);
		} catch (FontFormatException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads all unit sprites from the sprite sheet.
	 * 
	 * @throws IOException
	 */
	private void loadAllUnits() throws IOException {
		loadAllHeads();
		loadAllTorsos();
		loadAllLegs();
	}

	private void loadAllHeads() throws IOException {
		final BufferedImage headSheet = loadBufferedImage("sprites/units_head_sheet.png");
		removeBackground(headSheet);
		final int w = UnitSprites.SPRITE_WIDTH;
		final int h = UnitSprites.HEAD_HEIGHT;
		for (int i = 0; i < 8; i++) {
			UnitSprites.PRESIDENT_HEAD[i] = cropImage(headSheet, i * w, h * 0, UnitSprites.SPRITE_WIDTH,
					UnitSprites.HEAD_HEIGHT);
			UnitSprites.ESCORT_HEAD[i] = cropImage(headSheet, i * w, h * 1, UnitSprites.SPRITE_WIDTH,
					UnitSprites.HEAD_HEIGHT);
			UnitSprites.ASSASSIN_HEAD[i] = cropImage(headSheet, i * w, h * 2, UnitSprites.SPRITE_WIDTH,
					UnitSprites.HEAD_HEIGHT);
			UnitSprites.MENACE_HEAD[i] = cropImage(headSheet, i * w, h * 3, UnitSprites.SPRITE_WIDTH,
					UnitSprites.HEAD_HEIGHT);
			UnitSprites.CIVILIAN_HEAD[i] = cropImage(headSheet, i * w, h * 4, UnitSprites.SPRITE_WIDTH,
					UnitSprites.HEAD_HEIGHT);
			UnitSprites.POLICE_HEAD[i] = cropImage(headSheet, i * w, h * 5, UnitSprites.SPRITE_WIDTH,
					UnitSprites.HEAD_HEIGHT);
		}
	}

	/**
	 * Loads all unit torsos to be rendered as their body
	 * 
	 * @throws IOException
	 */
	private void loadAllTorsos() throws IOException {
		final int w = UnitSprites.TORSO_WIDTH;
		final int h = UnitSprites.TORSO_HEIGHT;
		BufferedImage torsoSheet = loadBufferedImage("sprites/units_torso_sheet.png");
		removeBackground(torsoSheet);
		BufferedImage wrappedTorso = new BufferedImage(w * 3, torsoSheet.getHeight(), BufferedImage.TYPE_INT_ARGB);
		BufferedImage torsoFront = torsoSheet.getSubimage(0, 0, w, torsoSheet.getHeight());
		Graphics g = wrappedTorso.getGraphics();
		g.drawImage(torsoSheet, 0, 0, null);
		g.drawImage(torsoFront, w * 2, 0, null);

		for (int i = 0; i < 8; i++) {
			int x = 16 - i * 2;
			UnitSprites.PRESIDENT_TORSO[i] = cropImage(wrappedTorso, x, h * 0, UnitSprites.TORSO_WIDTH,
					UnitSprites.TORSO_HEIGHT);
			UnitSprites.ESCORT_TORSO[i] = cropImage(wrappedTorso, x, h * 1, UnitSprites.TORSO_WIDTH,
					UnitSprites.TORSO_HEIGHT);
			UnitSprites.ASSASSIN_TORSO[i] = cropImage(wrappedTorso, x, h * 2, UnitSprites.TORSO_WIDTH,
					UnitSprites.TORSO_HEIGHT);
			UnitSprites.MENACE_TORSO[i] = cropImage(wrappedTorso, x, h * 3, UnitSprites.TORSO_WIDTH,
					UnitSprites.TORSO_HEIGHT);
			UnitSprites.CIVILIAN_TORSO[i] = cropImage(wrappedTorso, x, h * 4, UnitSprites.TORSO_WIDTH,
					UnitSprites.TORSO_HEIGHT);
			UnitSprites.POLICE_TORSO[i] = cropImage(wrappedTorso, x, h * 5, UnitSprites.TORSO_WIDTH,
					UnitSprites.TORSO_HEIGHT);
		}
	}

	/**
	 * Loads all unit legs from the base sprite.
	 * 
	 * @throws IOException
	 */
	private void loadAllLegs() throws IOException {
		BufferedImage baseLegSheet = loadBufferedImage("sprites/units_leg_sheet.png");
		removeBackground(baseLegSheet);
		BufferedImage[] baseLegSplit = new BufferedImage[3];
		baseLegSplit[0] = cropImage(baseLegSheet, 0, 0, UnitSprites.LEG_WIDTH, UnitSprites.LEG_HEIGHT);
		baseLegSplit[1] = cropImage(baseLegSheet, UnitSprites.LEG_WIDTH, 0, UnitSprites.LEG_WIDTH,
				UnitSprites.LEG_HEIGHT);
		baseLegSplit[2] = cropImage(baseLegSheet, UnitSprites.LEG_WIDTH * 2, 0, UnitSprites.LEG_WIDTH,
				UnitSprites.LEG_HEIGHT);

		for (int i = 0; i < baseLegSplit.length; i++) {
			UnitSprites.PRESIDENT_LEGS[i] = replaceColor(baseLegSplit[i], BASE_LEGS_COLOR,
					UnitSprites.PRESIDENT_LEGS_COLOR);
		}

		for (int i = 0; i < baseLegSplit.length; i++) {
			UnitSprites.ESCORT_LEGS[i] = replaceColor(baseLegSplit[i], BASE_LEGS_COLOR, UnitSprites.ESCORT_LEGS_COLOR);
		}

		for (int i = 0; i < baseLegSplit.length; i++) {
			UnitSprites.ASSASSIN_LEGS[i] = replaceColor(baseLegSplit[i], BASE_LEGS_COLOR,
					UnitSprites.ASSASSIN_LEGS_COLOR);
		}

		for (int i = 0; i < baseLegSplit.length; i++) {
			UnitSprites.MENACE_LEGS[i] = replaceColor(baseLegSplit[i], BASE_LEGS_COLOR, UnitSprites.MENACE_LEGS_COLOR);
		}

		for (int i = 0; i < baseLegSplit.length; i++) {
			UnitSprites.CIVILIAN_LEGS[i] = replaceColor(baseLegSplit[i], BASE_LEGS_COLOR,
					UnitSprites.CIVILIAN_LEGS_COLOR);
		}

		for (int i = 0; i < baseLegSplit.length; i++) {
			UnitSprites.POLICE_LEGS[i] = replaceColor(baseLegSplit[i], BASE_LEGS_COLOR, UnitSprites.POLICE_LEGS_COLOR);
		}
	}

	/**
	 * Used to crop a subimage sprite from a spritesheet. Sets alpha value to
	 * null if a certain pixel of the sprite is one of the background colours.
	 * 
	 * @param sheet
	 *            The sprite sheet.
	 * @param x
	 *            The top left x position to crop.
	 * @param y
	 *            The top left y position to crop.
	 * @param w
	 *            The width of the sub image.
	 * @param h
	 *            The height of the sub image.
	 * @return The cropped sprite with an alpha background,
	 */
	public static BufferedImage cropImage(BufferedImage sheet, int x, int y, int w, int h) {
		BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		result.getGraphics().drawImage(sheet.getSubimage(x, y, w, h), 0, 0, null);
		return result;
	}

	/**
	 * Removes the background colors from an image.
	 * 
	 * @param image
	 */
	public static void removeBackground(BufferedImage image) {
		// Traverse the pixel data.
		for (int j = 0; j < image.getHeight(); j++) {
			for (int i = 0; i < image.getWidth(); i++) {
				Color pixel = new Color(image.getRGB(i, j));
				// If a certain pixel equals one of the background colours, set
				// its alpha value to 0.
				if (pixel.equals(SPRITE_BORDER_COLOR) || pixel.equals(SPRITE_BACKGROUND_COLOR)) {
					image.setRGB(i, j, 0);
				}
			}
		}
	}

	/**
	 * Replaces a color with a new one in an image. Does not modify the source.
	 * 
	 * @param img
	 *            The old image.
	 * @param oldColor
	 *            The color to be replaced.
	 * @param newColor
	 *            The color to replace the old color.
	 * @return The new image with the colors replaced.
	 */
	public static BufferedImage replaceColor(BufferedImage img, Color oldColor, Color newColor) {
		BufferedImage result = cropImage(img, 0, 0, img.getWidth(), img.getHeight());
		for (int j = 0; j < result.getHeight(); j++) {
			for (int i = 0; i < result.getWidth(); i++) {
				Color pixel = new Color(result.getRGB(i, j));
				if (pixel.equals(oldColor)) {
					result.setRGB(i, j, newColor.getRGB());
				}
			}
		}
		return result;
	}

	/**
	 * Loads the images used as shadow.
	 * 
	 * @throws IOException
	 */
	private void loadTileShadow() throws IOException {
		BufferedImage shadowSheet = loadBufferedImage("sprites/tile_shadow_sheet.png");
		shadows[0] = shadowSheet.getSubimage(0, 0, Tile.TILE_WIDTH, Tile.TILE_HEIGHT);
		shadows[1] = shadowSheet.getSubimage(Tile.TILE_WIDTH, 0, Tile.TILE_WIDTH, Tile.TILE_HEIGHT);
		shadows[2] = shadowSheet.getSubimage(Tile.TILE_WIDTH * 2, 0, Tile.TILE_WIDTH, Tile.TILE_HEIGHT);
	}

	/**
	 * Loads tiles from the tile sheet.
	 * 
	 * @throws IOException
	 */
	private void loadTileSprites() throws IOException {
		final BufferedImage sheet = loadBufferedImage("sprites/tile_sheet.png");
		loadTileSprite(Tile.BED, sheet, 3, 2);
		loadTileSprite(Tile.BLUE_CARPET, sheet, 3, 0);
		loadTileSprite(Tile.BRICK_WALL, sheet, 0, 2);
		loadTileSprite(Tile.CHECKERED_FLOOR, sheet, 6, 0);
		loadTileSprite(Tile.CHAIR, sheet, 5, 2);
		loadTileSprite(Tile.DINING_TABLE, sheet, 2, 2);
		loadTileSprite(Tile.GRASS, sheet, 1, 0);
		loadTileSprite(Tile.MUD, sheet, 9, 0);
		loadTileSprite(Tile.PAVEMENT, sheet, 3, 1);
		loadTileSprite(Tile.PINK_CARPET, sheet, 4, 0);
		loadTileSprite(Tile.RED_CARPET, sheet, 1, 1);
		loadTileSprite(Tile.STONE_WALL, sheet, 1, 2);
		loadTileSprite(Tile.TARMAC, sheet, 7, 0);
		loadTileSprite(Tile.WHITE_FLOOR, sheet, 8, 0);
		loadTileSprite(Tile.WOODEN_FLOOR, sheet, 2, 0);
		loadTileSprite(Tile.BED_PILLOW, sheet, 4, 2);
		loadTileSprite(Tile.HP_POWER_UP, sheet, 4, 1);
		loadTileSprite(Tile.AMMO_POWER_UP, sheet, 4, 1);
		loadTileSprite(Tile.GRENADE_POWER_UP, sheet, 4, 1);
		loadTileSprite(Tile.ESCORT_SPAWN, sheet, 5, 1);
		loadTileSprite(Tile.PRESIDENT_SPAWN, sheet, 6, 1);
		loadTileSprite(Tile.ASSASSIN_SPAWN, sheet, 7, 1);
		loadTileSprite(Tile.END_ZONE, sheet, 8, 1);
		loadTileSprite(Tile.STONE_BRICK, sheet, 6, 2);
	}

	/**
	 * Loads a tile sheet. If a given tile is unwalkable, also adds the base
	 * sheet.
	 * 
	 * @param tileID
	 *            The tile id of the tile.
	 * @param sheet
	 *            The tile spritesheet.
	 * @param i
	 *            The i index of the tile in the sheet.
	 * @param j
	 *            The j index of the tile in the sheet.
	 */
	private void loadTileSprite(int tileID, BufferedImage sheet, int i, int j) {
		Tile tile = Tile.ALL_TILES.get(tileID);
		int x = i * Tile.TILE_WIDTH;
		int y = j * Tile.TILE_HEIGHT;
		BufferedImage topImage = sheet.getSubimage(x, y, Tile.TILE_WIDTH, Tile.TILE_HEIGHT);
		tile.getImages().put(Tile.TOP_IMAGE, topImage);
		if (!tile.isWalkable()) {
			// If the tile is not walkable (a solid tile), add its bottom image.
			tile.getImages().put(Tile.BOTTOM_IMAGE,
					sheet.getSubimage(x, y + Tile.TILE_HEIGHT, Tile.TILE_WIDTH, Tile.TILE_DEPTH));
		} else {
			tile.getImages().put(Tile.TOP_RIGHT_SHADOW, mergeImages(topImage, shadows[0]));
			tile.getImages().put(Tile.BOTTOM_LEFT_SHADOW, mergeImages(topImage, shadows[1]));
			tile.getImages().put(Tile.FULL_SHADOW, mergeImages(topImage, shadows[2]));
		}
	}

	/**
	 * Loads all the weapon sprites to be rendered in game
	 * 
	 * @throws IOException
	 */
	private void loadAllWeaponSprites() throws IOException {
		WeaponSprites.PISTOL_ICON = loadBufferedImage("icons/pistol_icon.png");
		WeaponSprites.MG_ICON = loadBufferedImage("icons/mg_icon.png");
		WeaponSprites.SHIELD_ICON = loadBufferedImage("icons/shield_icon.png");
		WeaponSprites.GRENADE_ICON = loadBufferedImage("icons/grenade_icon.png");
	}

	/**
	 * Loads all power up sprites to be rendered on the tiles
	 * 
	 * @throws IOException
	 */
	private void loadAllPowerUpSprites() throws IOException {
		final BufferedImage sheet = loadBufferedImage("sprites/power_up_sheet.png");
		removeBackground(sheet);
		int w = PowerUpSprites.WIDTH;
		int h = PowerUpSprites.HEIGHT;
		PowerUpSprites.HP_IMAGE = sheet.getSubimage(0, 0, w, h);
		PowerUpSprites.GRENADE_IMAGE = sheet.getSubimage(w, 0, w, h);
		PowerUpSprites.AMMO_IMAGE = sheet.getSubimage(w * 2, 0, w, h);
	}

	/**
	 * Loads the client background and logo
	 * 
	 * @throws IOException
	 */
	private void loadMenuImages() throws IOException {
		MenuManager.MENU_BACKGROUND = loadBufferedImage("images/menu-background.png");
		MenuManager.LOGO = loadBufferedImage("images/logo.png");
	}

	/**
	 * Loads the thumbnail for the JFrame
	 * 
	 * @throws IOException
	 */
	private void loadThumbnail() throws IOException {
		GameWindow.THUMBNAIL = loadBufferedImage("icons/thumbnail.png");
	}

	/**
	 * Merges two images (puts one on top of the other). This is used for
	 * merging shadow sprites on floor tiles.
	 * 
	 * @param bottom
	 *            The bottom image
	 * @param top
	 *            The top iamge
	 * @return The new merged image.
	 */
	public static BufferedImage mergeImages(BufferedImage bottom, BufferedImage top) {
		BufferedImage result = new BufferedImage(bottom.getWidth(), bottom.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = result.getGraphics();
		g.drawImage(bottom, 0, 0, null);
		g.drawImage(top, 0, 0, null);
		return result;
	}

	/**
	 * Loads a buffered image from a path relative to this class.
	 * 
	 * @param path
	 *            The path to load the image from.
	 * @return The loaded image.
	 * @throws IOException
	 *             Thrown when image could not be loaded and/or read.
	 */
	public BufferedImage loadBufferedImage(String path) throws IOException {
		return ImageIO.read(getClass().getResourceAsStream(path));
	}

	/**
	 * Loads a font from a path relative to this class.
	 * 
	 * @param path
	 *            The path to load the font from.
	 * @return The loaded font.
	 * @throws FontFormatException
	 *             Thrown if the fontStream data does not contain the required
	 *             font tables for the specified format.
	 * @throws IOException
	 *             Thrown if the fontStream cannot be completely read.
	 */
	public Font loadFont(String path) throws FontFormatException, IOException {
		return Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream(path));
	}
}
