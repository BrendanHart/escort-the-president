package escort.client.ui.components;

import java.util.ArrayList;
import java.util.List;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.ui.components.panels.Panel;

/**
 * A radio button ground that contains toggleable buttons where only 1 can
 * remain on.
 * 
 * @author Ahmed Bhallo
 *
 */
public class RadioButtonGroup extends Panel {

	/**
	 * The list of buttons.
	 */
	private final List<ToggleButton> buttons = new ArrayList<>();

	
	/**
	 * Instantiates a new radio button group.
	 * 
	 * @param inputs
	 *            The inputs object.
	 */
	public RadioButtonGroup(Inputs inputs) {
		// There are no toggle button yet, so width and height are 0.
		super(inputs, 0, 0);
	}

	/**
	 * Adds a radio button to this group.
	 * 
	 * @param The
	 *            text of the label for the button.
	 */
	public void addRadioButton(String label) {
		// Create the button.
		ToggleButton button = new ToggleButton(inputs, label) {
			@Override
			public void toggle() {
				// Override the toggle method so that it deselects all buttons,
				// and then sets this one as selected.
				buttons.forEach(b -> b.setSelected(false));
				setSelected(true);
			}
		};

		// Add the button to the bottom of the group.
		add(button, 0, getHeight());
		buttons.add(button);

		// Update the width so it can contain this button.
		setWidth(Math.max(getWidth(), button.getWidth()));

		// Increment the height and add some vertical padding underneath.
		setHeight(getHeight() + button.getHeight() + 2 * Scale.factor);

		// If this was the first button added, set it to be selected.
		if (buttons.size() == 1) {
			button.setSelected(true);
		}
	}

	/**
	 * Adds radio buttons to this group.
	 * 
	 * @param labels
	 *            An array of labels to be added to this group.
	 */
	public void addRadioButtons(String... labels) {
		for (String label : labels) {
			addRadioButton(label);
		}
	}

	/**
	 * Gets the index of the current selected button. The index indices of
	 * toggle buttons added to this group is dictates by the order in which they
	 * were added, with 0 being the first button added. Returns -1 if no button
	 * is select, however this will only be returned when no toggle buttons are
	 * in this group.
	 * 
	 * @return The index of the current select button.
	 */
	public int getSelectedIndex() {
		for (int i = 0; i < buttons.size(); i++) {
			if (buttons.get(i).isSelected()) {
				return i;
			}
		}
		return -1;
	}
}
