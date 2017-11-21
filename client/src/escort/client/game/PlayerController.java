package escort.client.game;

import java.awt.Point;

import escort.client.graphics.Camera;
import escort.client.graphics.hud.ChatBox;
import escort.client.input.Inputs;
import escort.client.main.Client;
import escort.client.main.Scale;
import escort.common.game.entities.units.Escort;
import escort.common.game.entities.units.Unit;
import escort.common.game.entities.units.UnitController;

/**
 * A controller to be controlled by a human player. Detects keyboard inputs and
 * calls appropriate methods in the corresponding unit object. Does not and
 * should not perform any game logic operations. Simply calls methods in the
 * unit that it is controlling.
 * 
 * @author Ahmed Bhallo
 *
 */
public class PlayerController implements UnitController {
	/**
	 * The unit that is being controlled by this controller.
	 */
	private Unit unit;

	/**
	 * The player's input keys.
	 */
	private Inputs inputs;

	/**
	 * The render camera.
	 */
	private final Camera camera;

	/**
	 * Check if the mouse has been released so the pistol can be fired again.
	 */
	private boolean mouseReleased = true;

	/**
	 * Check if the grenade key is being held by the client.
	 */
	private boolean grenadeKeyHeld = false;

	/**
	 * The game manager.
	 */
	private final GameManager gameManager;

	/**
	 * The chat box. Used to check if input should be detected when chat is in
	 * use.
	 */
	private final ChatBox chatBox;

	/**
	 * Instantiates a new Player Controller.
	 * 
	 * @param client
	 *            The player's client.
	 * @param unit
	 *            The unit to be controller by this player.
	 * @param keys
	 *            The player's input keys.
	 */
	public PlayerController(Client client, Unit unit, Inputs keys) {
		this.unit = unit;
		this.inputs = keys;
		gameManager = client.getGameManager();
		camera = gameManager.getCamera();
		chatBox = gameManager.getHUDManager().getChat();
	}

	/**
	 * Called by the unit to be controlled. Detects any player movement and
	 * rotation based on mouse input. Detects if the player makes any combat
	 * actions such as shooting, grenade actions and reloading.
	 */
	@Override
	public void control() {
		if (chatBox.isFocussed() || gameManager.getHUDManager().isDisplaySettings()) {
			return;
		}
		// Detect if the player wants to move.
		detectMovement();

		// Detect follow key press.
		detectFollowKey();

		// Detect the unit's new direction based on the player's mouse position.
		updateRotation();

		// Detect reloading
		detectReloading();

		// Detect if the player is holding or releasing the grenade key.
		detectGrenadeAction();

		// Detect if the player is holding the shoot button.
		detectShooting();

		// Detect request to change weapon.
		detectWeaponToggle();
	}

    /**
     * Detects if follow key is pressed and carries out the correct action.
     */
	private void detectFollowKey() {
		if (inputs.followKey.isPressed()) {
			if (unit instanceof Escort) {
				if (((Escort) unit).isFollower()) {
					((Escort) unit).unfollow();
				} else {
					((Escort) unit).follow();
				}

			}
		}
		inputs.followKey.setPressed(false);
	}

	/**
	 * Detects if the movement direction keys have been pressed and
	 * appropriately sets the controlled unit's x and y velocities.
	 */
	private void detectMovement() {
		if (inputs.up.isPressed()) {
			unit.setYVel(-1);
		}
		if (inputs.down.isPressed()) {
			unit.setYVel(1);
		}
		if (inputs.left.isPressed()) {
			unit.setXVel(-1);
		}
		if (inputs.right.isPressed()) {
			unit.setXVel(1);
		}
	}

	/**
	 * Calculates the unit's new direction based on the angle between the
	 * player's mouse and the unit's position.
	 */
	private void updateRotation() {
		// Direction is based on the center point of the unit.
		Point center = unit.getCenterPoint();
		// Calculate the difference in the x and y directions between the
		// player's mouse postition and the center point of the unit.
		double dx = (inputs.mouseX + camera.getxOffset()) - (center.x * Scale.factor);
		double dy = (center.y * Scale.factor) - (inputs.mouseY + camera.getyOffset());
		// Calculate the theta component of the polar coordinate corresponding
		// to dx and dy.
		double newDir = Math.atan2(dx, dy);
		// Ensure that newDir is always >= 0.
		if (newDir < 0) {
			newDir += 2 * Math.PI;
		}
		unit.setDir(newDir);
	}

	/**
	 * Detects if the player chooses to hold or release a grenade. If so, calls
	 * method in the controlled unit accordingly.
	 */
	private void detectGrenadeAction() {
		if (inputs.grenade.isPressed() && !unit.hasHeldGrenade() && !grenadeKeyHeld && !unit.isBusy()) {
			if (unit.hasGrenadeStored()) {
				grenadeKeyHeld = true;
				// Tell the unit to hold a grenade if the unit has one and is
				// not already holding one.
				unit.holdGrenade();
			} else {
				// Notify the player that they have no more grenades left to
				// hold.
			}
		}

		if (!inputs.grenade.isPressed() && unit.hasHeldGrenade()) {
			// If the unit has a grenade and the player releases the grenade
			// key, tell the unit to throw the grenade.
			unit.releaseGrenade();
		}
		if (!inputs.grenade.isPressed() && grenadeKeyHeld) {
			grenadeKeyHeld = false;
		}
	}

	/**
	 * Detects whether or not the reload key has been pressed. If so, determines
	 * which gun the client wants to reload.
	 */
	private void detectReloading() {
		if (inputs.reload.isPressed() && !unit.isBusy()) {
			switch (unit.getWeapon()) {
			case Unit.MACHINE_GUN:
				if (unit.getMG().getBulletsInBag() > 0 && unit.getMG().getBulletsInMag() < unit.getMG().getFullMag()) {
					unit.requestReloadToServer();
				}
				break;
			case Unit.PISTOL:
				if (unit.getPistol().getBulletsInMag() < unit.getPistol().getFullMag()) {
					unit.requestReloadToServer();
				}
			default:
				break;
			}
		}
	}

	/**
	 * Detects if the player chooses to shoot. If so, calls the corresponding
	 * method.
	 */
	private void detectShooting() {
		if (inputs.leftClick.isPressed() && !unit.isBusy()) {
			if (unit.getWeapon() == Unit.PISTOL && mouseReleased) {
				if (unit.getPistol().getBulletsInMag() == 0 && unit.getPistol().getBulletsInBag() > 0) {
					unit.requestReloadToServer();
					return;
				}
				unit.shoot();
				mouseReleased = false;
			} else if (unit.getWeapon() == Unit.MACHINE_GUN) {
				if (unit.getMG().getBulletsInMag() == 0 && unit.getMG().getBulletsInBag() > 0) {
					unit.requestReloadToServer();
					return;
				}
				unit.shoot();
			}
		} else {
			mouseReleased = true;
		}
	}

	/**
	 * Changes the player's weapon.
	 */
	private void detectWeaponToggle() {
		if (inputs.pistolKey.isPressed()) {
			unit.switchWeaponServer(Unit.PISTOL);
			inputs.pistolKey.setPressed(false);
		} else if (inputs.mgKey.isPressed()) {
			unit.switchWeaponServer(Unit.MACHINE_GUN);
			inputs.mgKey.setPressed(false);
		} else if (inputs.shieldKey.isPressed()) {
			unit.switchWeaponServer(Unit.SHIELD);
			inputs.shieldKey.setPressed(false);
		}

		int weaponSlot = unit.getWeapon();

		if (inputs.weaponScrollUp.isPressed()) {
			inputs.weaponScrollUp.setPressed(false);
			weaponSlot++;
		} else if (inputs.weaponScrollDown.isPressed()) {
			inputs.weaponScrollDown.setPressed(false);
			weaponSlot--;
		} else {
			return;
		}

		if (weaponSlot < Unit.MACHINE_GUN) {
			weaponSlot = Unit.SHIELD;
		}

		if (weaponSlot > Unit.SHIELD) {
			weaponSlot = Unit.MACHINE_GUN;
		}

		unit.switchWeaponServer(weaponSlot);
	}

	/**
	 * Called when the unit has died.
	 */
	@Override
	public void unitDied() {
		// Do nothing.
	}
}
