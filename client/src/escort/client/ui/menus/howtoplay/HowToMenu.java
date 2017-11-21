package escort.client.ui.menus.howtoplay;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import escort.client.input.Inputs;
import escort.client.main.Scale;
import escort.client.ui.components.Stepper;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.text.TextLabel;
import escort.client.ui.components.text.TextUtils;
import escort.client.ui.menus.AbstractMenu;
import escort.client.ui.menus.MenuManager;
import escort.client.ui.utils.Colors;
import escort.client.ui.utils.Fonts;

/**
 * Class for the How to section of the menus. Loads the pages from the txt files
 * and displays them as pages on the client.
 * 
 * @author Ahmed Bhallo
 * @author James Birch
 *
 */
public class HowToMenu extends AbstractMenu {

	private static final int NUM_PAGES = 5;

	private final HowToPage[] pages = new HowToPage[NUM_PAGES];

	private final Stepper pageStepper;
	private int currentIndex = 0;

	/**
	 * Instantiates a new how to menu
	 * 
	 * @param inputs
	 *            The inputs object
	 * @param menuManager
	 *            The menu manager
	 */
	public HowToMenu(Inputs inputs, MenuManager menuManager) {
		super(inputs, menuManager, "HOW TO PLAY");
		String[] pageNames = new String[NUM_PAGES];
		for (int i = 0; i < NUM_PAGES; i++) {
			pageNames[i] = "Page " + (i + 1);
		}
		pageStepper = new Stepper(inputs, 200*Scale.factor, 0, pageNames);
		pageStepper.addListener(e -> updatePage());
		add(pageStepper, center(pageStepper).x, getHeight() - pageStepper.getHeight());
		initPages();
		add(pages[0], 0, 0);
	}

	/**
	 * Initialises the pages and loads them onto the array.
	 */
	private void initPages() {
		int pageHeight = getHeight() - pageStepper.getHeight();
		// Files are relative to the content root which in this case is 'client'
		pages[0] = new HowToPage(inputs, getWidth(), pageHeight, "Objectives",
				"src/escort/client/ui/menus/howtoplay/Objectives.txt");
		pages[1] = new HowToPage(inputs, getWidth(), pageHeight, "Shooting",
				"src/escort/client/ui/menus/howtoplay/Shooting.txt");
		pages[2] = new HowToPage(inputs, getWidth(), pageHeight, "Grenades and Shields",
				"src/escort/client/ui/menus/howtoplay/GrenadesShields.txt");
		pages[3] = new HowToPage(inputs, getWidth(), pageHeight, "AI", "src/escort/client/ui/menus/howtoplay/AI.txt");
		pages[4] = new HowToPage(inputs, getWidth(), pageHeight, "Power-ups",
				"src/escort/client/ui/menus/howtoplay/Powerups.txt");
	}

	/**
	 * Called when page stepper has been updated. Updates the current page.
	 */
	private void updatePage() {
		remove(pages[currentIndex]);
		currentIndex = pageStepper.getIndex();
		add(pages[currentIndex], 0, 0);
	}

	/**
	 * Called when the back button is pressed.
	 */
	@Override
	public void goBack() {
		if (menuManager.getClient().getNetworkManager().isInLobby()) {
			menuManager.displayLobbyMenu();
		} else {
			menuManager.displayMainMenu();
		}
	}

	/**
	 * A page on the how to menu. Loads the content from the txt file and
	 * displays it.
	 * 
	 * @author Ahmed Bhallo
	 * @author James Birch
	 *
	 */
	private class HowToPage extends Panel {

		/**
		 * Generate a HowTo page.
		 * 
		 * @param inputs
		 *            Possible inputs.
		 * @param width
		 *            Width of page.
		 * @param height
		 *            Height of page.
		 * @param header
		 *            The page header.
		 * @param path
		 *            Path to a file that contains the body of the page.
		 * @author Ahmed Bhallo
		 * @author James Birch
		 */
		public HowToPage(Inputs inputs, int width, int height, String header, String path) {
			super(inputs, width, height);
			setBackground(Colors.BIG_STONE);
			TextLabel headerLabel = new TextLabel(header, inputs, Colors.DARK_WHITE);
			headerLabel.setPadding(0, 0);
			headerLabel.setFont(Fonts.HEADER);
			final int x = 20 * Scale.factor;
			add(headerLabel, center(headerLabel).x, 10 * Scale.factor);

			String line;
			try {
				// Read file from path given
				// FileReader fileReader = new FileReader(path);
				InputStreamReader r = new InputStreamReader(getClass().getResourceAsStream(path.substring(3)));

				// Wrap FileReader in BufferedReader
				BufferedReader bufferedReader = new BufferedReader(r);

				int y = 40 * Scale.factor;

				// Add each line of the file to the page
				while ((line = bufferedReader.readLine()) != null) {
					Panel container = TextUtils.wrappedTextLabel(inputs, line, 65, Colors.LIGHT_GRAY);
					add(container, x, y);
					y += container.getHeight() + 15 * Scale.factor;
				}

				bufferedReader.close();
			} catch (FileNotFoundException e) {
				System.err.println(String.format("File at path %s not found!", path));
				e.printStackTrace();
				System.exit(1);
			} catch (IOException e) {
				System.err.println("There was a problem with the buffered reader!");
				e.printStackTrace();
				System.exit(1);
			}
		}
	}

}