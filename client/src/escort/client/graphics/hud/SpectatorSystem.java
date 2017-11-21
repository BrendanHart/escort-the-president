package escort.client.graphics.hud;

import java.util.List;
import java.util.ArrayList;

import escort.common.game.entities.units.Unit;
import escort.client.game.GameManager;
import escort.client.input.Inputs;
import escort.client.ui.components.panels.Panel;
import escort.client.ui.components.text.TextLabel;

/**
 * Provides the functionality of spectating on death.
 */
public class SpectatorSystem {

	private int spectating;
	private long setTime;

	private GameManager gameManager;
	private Panel container;
	private List<Unit> units;
	private Unit lastSpectating;

	private SpectatorBar bar;
	private TextLabel countdown;

	private boolean timerSet = false;

    /**
     * Create a new spectator system.
     * @param gameManager The game manager.
     * @param inputs The client inputs.
     * @param container The container to add the components.
     */
	public SpectatorSystem(GameManager gameManager, Inputs inputs, Panel container) {
		units = new ArrayList<>(gameManager.getGameData().getUnits().values());
		spectating = 0;
		lastSpectating = units.get(0);
		countdown = new TextLabel("", inputs);
		bar = new SpectatorBar(inputs, gameManager.getClient().getGameWidth(), 100, this);
		this.gameManager = gameManager;
		this.container = container;
	}

    /**
     * Spectate the unit to the left of the current index.
     */
	public void moveLeft() {

		spectating--;
		if (spectating < 0) {
			spectating = units.size() - 1;
		}
	}

    /**
     * Spectate the unit to the right of the current index.
     */
	public void moveRight() {
		spectating++;
		if (spectating >= units.size()) {
			spectating = 0;
		}
	}

    /**
     * Update the spectator system.
     */
	public void update() {
		if (gameManager.getClientUnit().isDead()) {
			Unit currentlySpectating = units.get(spectating);
			if (currentlySpectating.getUnitID() != lastSpectating.getUnitID()) {
				gameManager.getCamera().setFocussedUnit(currentlySpectating);
				gameManager.getCamera().setLocked(true);
			}

			if (!timerSet) {
				setRespawn();
				addGUI();
			}

			int timeUntilRespawn = (int) Math
					.ceil(gameManager.getClientUnit().getSpawnTime() - ((System.nanoTime() - setTime) / 1000000000.0));
			if (timeUntilRespawn < 0) {
				timeUntilRespawn = 0;
				this.setTime = -1;
			}
			countdown.setText("Respawn in " + timeUntilRespawn, true);
			container.add(countdown, (container.getWidth() - countdown.getWidth()) / 2, 0);
			bar.setName("" + units.get(spectating).getUsername());

			lastSpectating = currentlySpectating;

		} else {
			timerSet = false;
			container.remove(bar);
			container.remove(countdown);
			gameManager.getCamera().setFocussedUnit(gameManager.getClientUnit());
		}

	}

    /**
     * Add the GUI to the container.
     */
	public void addGUI() {
		container.add(bar, (gameManager.getClient().getGameWidth() - bar.getWidth()) / 2,
				gameManager.getClient().getGameHeight() / 10);
		container.add(countdown, (container.getWidth() - countdown.getWidth()) / 2, 0);
	}

    /**
     * Set the respawn timer and also change the camera.
     */
	public void setRespawn() {
		this.setTime = System.nanoTime();
		this.timerSet = true;
		gameManager.getCamera().setFocussedUnit(lastSpectating);
	}

}
