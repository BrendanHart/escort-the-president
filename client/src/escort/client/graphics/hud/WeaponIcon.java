package escort.client.graphics.hud;

import java.awt.image.BufferedImage;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.ui.components.panels.IconPanel;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.text.TextLabel;

/**
 * An icon for a weapon in the weapons panel. Displays the image and information of the weapon.
 * @author Ahmed Bhallo
 *
 */
public class WeaponIcon extends Panel {

	private final IconPanel iconPanel;
	private final TextLabel bulletInformation;

	/**
	 * Instantiates a new weapon icon.
	 * @param inputs The inputs object
	 * @param icon The image of the icon
	 */
	public WeaponIcon(Inputs inputs, BufferedImage icon) {
		super(inputs, 48 * Scale.factor, 36 * Scale.factor);
		iconPanel = new IconPanel(inputs, icon, 2*Scale.factor);
		bulletInformation = new TextLabel("2/2", inputs);
		bulletInformation.setPadding(Scale.factor, Scale.factor);
		add(iconPanel, center(iconPanel).x, 4*Scale.factor);
		add(bulletInformation, center(bulletInformation).x, getHeight() - bulletInformation.getHeight());
	}

	/**
	 * Update the information on the weapon.
	 * @param info
	 */
	public void updateInformation(String info){
		bulletInformation.setText(info, true);
		add(bulletInformation, center(bulletInformation).x, getHeight() - bulletInformation.getHeight());
	}
	
}
