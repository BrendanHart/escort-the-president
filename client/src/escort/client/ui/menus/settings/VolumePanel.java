package escort.client.ui.menus.settings;

import escort.client.input.Inputs;
import escort.client.ui.components.Slider;
import escort.client.ui.components.ToggleButton;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.text.TextLabel;

/**
 * A panel for volume changing used in the settings panel. Updates the
 * percentage of volume automatically.
 * 
 * @author Ahmed Bhallo
 *
 */
public class VolumePanel extends Panel {

	private int volume;
	private final Slider volSlider;
	private final ToggleButton mute;

	/**
	 * The volume panel
	 * 
	 * @param inputs
	 *            The inputs object
	 * @param width
	 *            The width of the panel
	 * @param height
	 *            The height of the panel
	 */
	public VolumePanel(Inputs inputs, int width, int height) {
		super(inputs, width, height);
		TextLabel value = new TextLabel("100%", inputs);
		mute = new ToggleButton(inputs, "Mute");
		// mute.setPadding(6 * Scale.factor, 6 * Scale.factor);
		volSlider = new Slider(inputs, width - value.getWidth() - mute.getWidth()) {
			@Override
			public void update() {
				super.update();
				volume = getValue();
				value.setText(volume + "%", false);
			}
		};
		add(volSlider, 0, center(volSlider).y);
		add(value, volSlider.getWidth(), center(value).y);
		add(mute, volSlider.getWidth() + value.getWidth(), center(mute).y);
	}

	/**
	 * @return The selected volume as a percentage
	 */
	public int getVolume() {
		return volume;
	}

	/**
	 * Sets the current volume
	 * 
	 * @param volume
	 *            The new volume as a percentage
	 */
	public void setVolume(int volume) {
		this.volume = volume;
		volSlider.setValue(volume);
	}

	/**
	 * Gets the mute button
	 * 
	 * @return The mute button
	 */
	public ToggleButton getMute() {
		return mute;
	}
}
