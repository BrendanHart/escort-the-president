package escort.client.ui.components.panels;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.ui.components.Component;
import escort.client.ui.components.text.TextLabel;
import escort.client.ui.components.text.TextUtils;
import escort.client.ui.utils.Colors;

/**
 * A dialog panel. Set this as Client.setdialog to make it displyed and modal.
 * 
 * @author Ahmed Bhallo
 *
 */
public class DialogPanel extends Panel {

	/**
	 * Instantiates a new dialog panel
	 * 
	 * @param inputs
	 *            The inputs object
	 * @param width
	 *            The width of the client
	 * @param height
	 *            The height of the client
	 * @param title
	 *            The title of the dialog
	 * @param message
	 *            The message of the dialog
	 * @param content
	 *            The content component/panel on the dialog
	 */
	public DialogPanel(Inputs inputs, int width, int height, String title, String message, Component content) {
		super(inputs, width, height);
		setBackground(Colors.setAlpha(Colors.BLACK, 200));
		TextLabel titleLabel = new TextLabel(title, inputs, Colors.DARK_WHITE);
		Panel messagePanel = TextUtils.wrappedTextLabel(inputs, message, 40, Colors.LIGHT_GRAY);

		Panel container = new Panel(inputs, 280 * Scale.factor, 140 * Scale.factor);
		container.setBackground(Colors.setAlpha(Colors.BIG_STONE, 200));
		final int separation = 5 * Scale.factor;
		int yPos = separation;
		container.add(titleLabel, container.center(titleLabel).x, yPos);
		yPos += titleLabel.getHeight();
		yPos += separation;
		container.add(messagePanel, container.center(messagePanel).x, yPos);
		yPos += messagePanel.getHeight();
		yPos += separation;
		container.add(content, container.center(content).x, container.getHeight() - content.getHeight() - separation);
		add(container, center(container));
	}

}
