package escort.client.graphics.hud;

import escort.client.input.Inputs;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.text.TextButton;
import escort.client.ui.components.text.TextLabel;

/**
 * Represents the username to be displayed for the Spectator system.
 * Also contains the left and right change buttons.
 */
public class SpectatorBar extends Panel {

    private TextButton left;
    private TextButton right;
    private TextLabel name;

    /**
     * Create a new SpectatorBar.
     * @param inputs The client inputs.
     * @param width The width of the bar.
     * @param height The height of the bar.
     * @param specSystem The spectator system containing this bar.
     */
    public SpectatorBar(Inputs inputs, int width, int height, SpectatorSystem specSystem) {
        super(inputs, width, height);
        left = new TextButton("<", inputs);
        left.addListener( e -> specSystem.moveLeft());
        right = new TextButton(">", inputs);
        right.addListener( e -> specSystem.moveRight() );
        name = new TextLabel(" Spectating: 1234567890123", inputs);
        add(left, ((width - name.getWidth()) / 2) - left.getWidth(), 0);
        add(name, (width - name.getWidth()) / 2, 0);
        add(right,(width + name.getWidth())/2, 0);
    }

    /**
     * Set the username.
     * @param username The username which is being spectated.
     */
    public void setName(String username) {
        name.setText("Spectating: " + username,false);
    }

}
