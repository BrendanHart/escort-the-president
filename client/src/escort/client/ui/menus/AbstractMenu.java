package escort.client.ui.menus;

import escort.client.input.Inputs;
import escort.client.ui.components.panels.Panel;

/**
 * An abstract menu on the Menu Manager
 * 
 * @author Ahmed Bhallo
 *
 */
public abstract class AbstractMenu extends Panel {

	protected MenuManager menuManager;
	protected boolean displayBackButton = true;
	private String menuName;

	/**
	 * Constructor for the abstract menu
	 * 
	 * @param inputs
	 *            The inputs object
	 * @param menuManager
	 *            The menu manager
	 * @param menuName
	 *            The name of the menu
	 */
	public AbstractMenu(Inputs inputs, MenuManager menuManager, String menuName) {
		super(inputs, menuManager.getClient().getGameWidth() - menuManager.getLeftMargin() * 2,
				menuManager.getClient().getGameHeight() - menuManager.getTopMargin() - menuManager.getBottomMargin());
		this.menuManager = menuManager;
		this.menuName = menuName;
	}

	/**
	 * @return The name of the menu.
	 */
	public String getMenuName() {
		return menuName;
	}

	/**
	 * @return Whether or not the back button should be disabled on the menu
	 *         manager.
	 */
	public boolean displayBackButton() {
		return displayBackButton;
	}

	/**
	 * Determines what to do when the back button is pressed on the menu
	 * manager.
	 */
	public abstract void goBack();

}
