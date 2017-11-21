package escort.client.ui.components;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.text.TextButton;
import escort.client.ui.components.text.TextLabel;

/**
 * A stepper component where steps can be switched between using incrementing
 * and decrementing buttons.
 * 
 * @author Ahmed Bhallo
 *
 */
public class Stepper extends Panel {

	/**
	 * An array of all states/steps of the stepper.
	 */
	private String[] steps;

	/**
	 * The decrement index button.
	 */
	private TextButton decButton;

	/**
	 * The increment index button.
	 */
	private TextButton incButton;

	/**
	 * The label of the stepper.
	 */
	private TextLabel label;

	/**
	 * The index of the current step.
	 */
	private int index;

	/**
	 * Instantiates a new stepper object.
	 * 
	 * @param inputs
	 *            The inputs object.
	 * @param width
	 *            The width of the stepper.
	 * @param index
	 *            The initial index of the stepper
	 * @param steps
	 *            A list of steps/states.
	 */
	public Stepper(Inputs inputs, int width, int index, String... steps) {
		super(inputs, 0, 0);
		this.index = index;
		this.steps = steps;
		if (index < 0 || index >= steps.length) {
			throw new IllegalArgumentException("Index supplied must be within the index bounds of steps array");
		}

		// Create the decrement button.
		decButton = new TextButton("<", inputs);
		decButton.setPadding(4 * Scale.factor, 4 * Scale.factor);
		decButton.addListener(e -> decrement());

		// Create the increment button.
		incButton = new TextButton(">", inputs);
		incButton.setPadding(4 * Scale.factor, 4 * Scale.factor);
		incButton.addListener(e -> increment());

		// Create the label inbetween
		label = new TextLabel("", inputs);
		label.setWidth(width - decButton.getWidth() - incButton.getWidth());
		label.setCentered(true);
		add(decButton, 0, 0);
		pack();
		add(label, getWidth(), center(label).y);
		pack();
		add(incButton, getWidth(), center(incButton).y);
		pack();
		updateLabel();
		updateButtons();
	}

	/**
	 * Called when the decrement button is pressed. Decrements the current step
	 * by 1. Returns if the step would be out of bounds.
	 */
	public void decrement() {
		if (index == 0) {
			return;
		}

		// Decrement the index.
		index--;

		// Update label and buttons.
		updateLabel();
		updateButtons();

		notifyListeners();
	}

	/**
	 * Called when the increment button is pressed. Increments the current step
	 * by 1. Returns if the step would be out of bounds.
	 */
	public void increment() {
		if (index >= steps.length - 1) {
			return;
		}

		// Increment the index.
		index++;

		// Update label and buttons.
		updateLabel();
		updateButtons();

		notifyListeners();
	}

	/**
	 * Updates the label depending on the current index. Adds it to the center
	 * of this stepper panel.
	 */
	private void updateLabel() {
		label.setText(steps[index], false);
		add(label, center(label));
	}

	/**
	 * Updates whether or not a button should be enabled based on the value of
	 * the current index.
	 */
	private void updateButtons() {
		if (!isEnabled()) {
			return;
		}
		decButton.setEnabled(index > 0);
		incButton.setEnabled(index < steps.length - 1);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		super.setEnabled(isEnabled);
		incButton.setEnabled(isEnabled);
		decButton.setEnabled(isEnabled);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;

		// Update label and buttons.
		updateLabel();
		updateButtons();
	}

}
