package escort.client.ui.components;

/**
 * A listener for a ClickableComponent
 * 
 * @author Ahmed Bhallo
 *
 */
public interface ComponentListener {

	/**
	 * This method is called by a component when it has detected a mouse press
	 * by the client.
	 * 
	 * @param source
	 *            The component that has been clicked.
	 */
	public void componentClicked(Component source);

}
