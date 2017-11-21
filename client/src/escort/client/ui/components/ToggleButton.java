package escort.client.ui.components;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.text.TextLabel;
import escort.client.ui.utils.Colors;

/**
 * A button the can be toggled on and off. Contains a label and a button
 * component.
 * 
 * @author Ahmed Bhallo
 *
 */
public class ToggleButton extends Panel {

	/**
	 * Whether or not this button is toggled on.
	 */
	private boolean selected = false;

	/**
	 * The button component.
	 */
	private final Component button;

	/**
	 * The label of this button.
	 */
	private final TextLabel label;

	/**
	 * Instantiates a new toggle button. By defaut, it is toggled off.
	 * 
	 * @param inputs
	 *            The inputs object.
	 * @param labelText
	 *            The label of the toggle button.
	 */
	public ToggleButton(Inputs inputs, String labelText) {
		super(inputs, 0, 0);

		// Create the label.
		this.label = new TextLabel(labelText, inputs, Colors.LIGHT_BLUE);
		label.setPadding(2*Scale.factor, 0);

		// Create the button.
		button = new Component(inputs, label.getHeight(), label.getHeight());

		// Add the label and button, and pack this panel.
		add(button, 0, 0);
		add(label, label.getHeight() + 2 * Scale.factor, 0);
		pack();

		// Add a listener for toggling.
		addListener(e -> toggle());
	}

	/**
	 * Toggles this toggle button.
	 */
	public void toggle() {
		// If previously off, turn on, and vice-versa.
		setSelected(!isSelected());
	}

	/**
	 * Sets whether or not this button should be selected.
	 * 
	 * @param selected
	 *            The new toggle state.
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * Gets whether or ot this button is selected.
	 * 
	 * @return The toggle state.
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Gets the text label of this button.
	 * 
	 * @return The text label of this button.
	 */
	public String getLabel() {
		return label.getText();
	}

	/**
	 * Updates colours of the button depending on the state of the mouse on this
	 * component.
	 */
	@Override
	public void update() {
		super.update();
		if (isDown()) {
			button.setBorder(Colors.LIGHT_BLUE);
			button.setBackground(Colors.DARK_BLUE);
		} else if (isHovered()) {
			button.setBorder(Colors.LIGHT_BLUE);
			button.setBackground(Colors.LIGHT_BLUE);
		} else if (isSelected()) {
			button.setBorder(Colors.LIGHT_BLUE);
			button.setBackground(Colors.PALE_BLUE);
		} else {
			button.setBorder(Colors.BLUE);
			button.setBackground(null);
		}
	}
}