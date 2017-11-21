package escort.client.graphics.hud;

import java.awt.Color;
import java.awt.Graphics2D;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.res.WeaponSprites;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.utils.Colors;
import escort.common.game.entities.units.Unit;
import escort.common.game.weapons.BlastShield;

/**
 * Container and handler for the weapon icons. Displays the weapon icons and
 * displays correct information.
 * 
 * @author Ahmed Bhallo
 *
 */
public class WeaponsPanel extends Panel {

	private final WeaponIcon mgIcon;
	private final WeaponIcon pistolIcon;
	private final WeaponIcon shieldIcon;
	private final WeaponIcon grenadeIcon;
	private final Unit clientUnit;

	private static final Color DESELECTED_COLOR = Colors.WEAPON_PANEL;
	private static final Color SELECTED_COLOR = Colors.BLUE;

	/**
	 * Instantiates a new weaponspanel
	 * 
	 * @param hud
	 *            The hud manager
	 * @param inputs
	 *            The inputs object
	 */
	public WeaponsPanel(HUDManager hud, Inputs inputs) {
		super(inputs, 0, 0);
		clientUnit = hud.getGameManager().getClientUnit();

		final int separation = 8 * Scale.factor;
		int x = 0;

		mgIcon = new WeaponIcon(inputs, WeaponSprites.MG_ICON);
		add(mgIcon, x, 0);

		x += separation + mgIcon.getWidth();

		pistolIcon = new WeaponIcon(inputs, WeaponSprites.PISTOL_ICON);
		add(pistolIcon, x, 0);

		x += separation + pistolIcon.getWidth();

		shieldIcon = new WeaponIcon(inputs, WeaponSprites.SHIELD_ICON);
		add(shieldIcon, x, 0);

		x += separation + pistolIcon.getWidth();
		x += separation * 2;

		grenadeIcon = new WeaponIcon(inputs, WeaponSprites.GRENADE_ICON);
		grenadeIcon.setBackground(DESELECTED_COLOR);
		add(grenadeIcon, x, 0);

		pack();
	}

	/**
	 * Displays the appropriate information based on the state of the client's
	 * unit. Sets color for background of weapon icon appropriately.
	 */
	@Override
	public void update() {
		super.update();
		mgIcon.updateInformation(clientUnit.getMG().getBulletsInMag() + "/" + clientUnit.getMG().getBulletsInBag());
		pistolIcon.updateInformation(clientUnit.getPistol().getBulletsInMag() + "/-");
		shieldIcon.updateInformation(clientUnit.getBlastShield().getHP() + "/" + BlastShield.MAX_HP);
		grenadeIcon.updateInformation("" + clientUnit.getGrenadesLeft() + "/" + Unit.MAX_NUM_GRENADES);
		mgIcon.setBackground(DESELECTED_COLOR);
		pistolIcon.setBackground(DESELECTED_COLOR);
		shieldIcon.setBackground(DESELECTED_COLOR);

		switch (clientUnit.getWeapon()) {
		case Unit.MACHINE_GUN:
			mgIcon.setBackground(SELECTED_COLOR);
			break;
		case Unit.PISTOL:
			pistolIcon.setBackground(SELECTED_COLOR);
			break;
		case Unit.SHIELD:
			shieldIcon.setBackground(SELECTED_COLOR);
			break;
		}
		grenadeIcon.setBackground(clientUnit.hasHeldGrenade() ? SELECTED_COLOR : DESELECTED_COLOR);
	}

	/**
	 * Only render if the unit is not dead.
	 */
	@Override
	public void render(Graphics2D g) {
		if (!clientUnit.isDead()) {
			super.render(g);
		}
	}
}