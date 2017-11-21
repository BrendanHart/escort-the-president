package escort.server.game.ai;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import escort.common.game.entities.units.Assassin;
import escort.common.game.entities.units.Police;
import escort.common.game.entities.units.Unit;
import escort.common.game.map.GameMap;
import escort.common.game.map.MapLoader;
import escort.server.game.Game;
import escort.server.lobby.LobbySettings;

public class AICombatLogicTest {
	private Game game;

	@Before
	/**
	 * @author Kwong-Hei Tsang
	 * @author James Birch
	 * @author Edward Dean
	 * @author Ahmed Bhallo
	 */
	public void setUp() {
		LobbySettings settings = new LobbySettings();
		settings.numAssassinsAI = 1;
		settings.numPoliceAI = 1;
		settings.numCivilianAI = 0;
		try {
			(new MapLoader()).load();
		} catch (Exception e) {
		}
		game = new Game(new ArrayList<>(), null, 1, settings);
		game.setStarted(true);
	}

	@Test
	/**
	 * Only passes iff: - Targets the closest enemy out of a sequence of enemies
	 * we define.
	 * 
	 * @author James Birch
	 * @author Edward Dean
	 */
	public void testFindClosestEnemy() {
		// Obstacle positions
		// Police 1: (645.0, 405.0) (UnitID 0)
		// Assassin 1: (645.0, 580.0) (UnitID 2)
		// Assassin 2: (20.0, 405.0) (UnitID 3)
		// Assassin 3: (789.0, 496.0) (UnitID 4)
		// Assassin 4: (733.0, 946.0) (UnitID 5)
		// Police 1 should target Assassin 1

		// President in position 0, Police in position 1,
		// Assassin in positions 2,3,4,5.
		ArrayList<Unit> units = new ArrayList<>(game.getGameData().getUnits().values());
		for (Unit unit : units) {
			// Order of if statements below is important!
			if (unit.getUnitType() == Unit.PRESIDENT_TYPE) {
				// don't want to include President in this test
				GameMap map = game.getGameData().getMap();
				unit.setX(map.getWidthInPx() - 10.0);
				unit.setY(map.getHeightInPx() - 10.0);
				continue;
			} else if (unit.getUnitType() == Unit.POLICE_TYPE) {
				unit.setX(645.0);
				unit.setY(405.0);
			} else if (unit.getUnitType() == Unit.ASSASSIN_TYPE) {
				switch (unit.getUnitID()) {
				case 2:
					// should target this one
					unit.setX(645.0);
					unit.setY(580.0);
					break;
				case 3:
					unit.setX(20.0);
					unit.setY(405.0);
					break;
				case 4:
					unit.setX(789.0);
					unit.setY(496.0);
					break;
				case 5:
					unit.setX(733.0);
					unit.setY(946.0);
					break;
				default:
					break; // should not enter default
				}
			}
		}

		// Police in index 1 of units list
		// Assassin which should be targeted in index 2 of units list
		Police police = (Police) units.get(1);
		Assassin assassin = (Assassin) units.get(2);
		PoliceController policeAI = (PoliceController) police.getUnitController();
		Unit enemy = policeAI.findClosestEnemy();
		assertTrue(enemy.getUnitID() == assassin.getUnitID());
	}

	@Test
	/**
	 * @author Edward Dean
	 */
	public void testFollowAndShootTarget() {
		Collection<Unit> units = game.getGameData().getUnits().values();
		Police police = null;
		Assassin assassin = null;

		for (Unit u : units) {
			if (u.getUnitType() == Unit.POLICE_TYPE) {
				police = (Police) u;
			}
			if (u.getUnitType() == Unit.ASSASSIN_TYPE) {
				assassin = (Assassin) u;
			}
		}

		PoliceController policeAI = (PoliceController) police.getUnitController();
		police.setUnitController(null);

		policeAI.target = policeAI.findClosestEnemy();
		if (policeAI.target == null) {
			policeAI.target = assassin;
		}

		policeAI.followAndShootTarget();

		if (police.distance(policeAI.target) <= AIController.SHOOTING_RANGE
				&& game.getGameData().getMap().lineOfSight(police.getHitbox(), policeAI.target.getHitbox())) {
			double minOffset = -AIController.MAX_OFFSET;
			double maxoffset = (-AIController.MAX_OFFSET) + (2 * AIController.MAX_OFFSET);

			double dir = police.getDir();
			assertTrue(police.angleFromPoint(policeAI.target.getCenterPoint()) + minOffset <= dir);
			assertTrue(police.angleFromPoint(policeAI.target.getCenterPoint()) + maxoffset > dir);
		} else if (police.distance(policeAI.target) <= AIController.VISION_RANGE) {
			assertTrue(!policeAI.getRoute().isEmpty());
		}
	}

	@Test
	/**
	 * Only passes iff: - Two routes are different after running newRoute and
	 * generateRoute.
	 * 
	 * @author James Birch
	 */
	public void testNewRoutes() {
		ArrayList<Unit> units = new ArrayList<>(game.getGameData().getUnits().values());

		AIController aiUnit = (AIController) units.get(0).getUnitController();
		aiUnit.newRoute(units.get(1));
		List<Rectangle> route = aiUnit.getRoute();
		aiUnit.newRoute(units.get(2));
		List<Rectangle> newRoute = aiUnit.getRoute();

		assertFalse(route == newRoute);
		assertFalse(route.equals(newRoute));

		aiUnit.generateRoute(aiUnit.getRandomTile());
		List<Rectangle> route1 = aiUnit.getRoute();
		aiUnit.generateRoute(aiUnit.getRandomTile());
		List<Rectangle> newRoute1 = aiUnit.getRoute();

		assertFalse(route1 == newRoute1);
		assertFalse(route1.equals(newRoute1));
	}
	
	@Test
	/**
	 * @author Edward Dean
	 */
	public void testFollowUnit() {
		Collection<Unit> units = game.getGameData().getUnits().values();
		Police police = null;
		Assassin assassin = null;

		for (Unit u : units) {
			if (u.getUnitType() == Unit.POLICE_TYPE) {
				police = (Police) u;
			}
			if (u.getUnitType() == Unit.ASSASSIN_TYPE) {
				assassin = (Assassin) u;
			}
		}

		PoliceController policeAI = (PoliceController) police.getUnitController();
		police.setUnitController(null);

		policeAI.followUnit(assassin);

		assertTrue(!police.getAbsoluteBoundsInTiles().equals(assassin.getAbsoluteBoundsInTiles()));

		assertTrue(!policeAI.getRoute().isEmpty());

		while (!policeAI.getRoute().isEmpty()) {
			policeAI.traverseRoute();
			police.update();
		}

		assertTrue(police.getAbsoluteBoundsInTiles().equals(assassin.getAbsoluteBoundsInTiles()));

		policeAI.followUnit(assassin);

		assertTrue(police.getAbsoluteBoundsInTiles().equals(assassin.getAbsoluteBoundsInTiles()));

		assertTrue(policeAI.getRoute().isEmpty());
	}

	
	@Test
	/**
	 * Only passes iff: - Gun switches to Pistol when MG bullets is zero. - Gun
	 * switches back to MG when MG bullets is not zero. - Reloads correctly.
	 * 
	 * @author James Birch
	 */
	public void testReloadSwitch() throws InterruptedException {
		ArrayList<Unit> units = new ArrayList<>(game.getGameData().getUnits().values());

		Unit unit = units.get(1); // id 0 is the president
		AIController aiUnit = (AIController) unit.getUnitController();

		// prevent the unit from moving
		unit.setUnitController(null);
		// try and remove from any action
		Rectangle rect = (Rectangle) game.getGameData().getMap().getEndZones().toArray()[0];
		unit.setX(rect.getX());
		unit.setY(rect.getY());
		// Set any velocity to zero
		unit.setXVel(0.0);
		unit.setYVel(0.0);
		// start with MG
		unit.setWeapon(Unit.MACHINE_GUN);
		unit.getMG().setMagBullets(0);
		assertTrue(unit.getMG().getBulletsInMag() == 0);
		aiUnit.reloadAndSwitch();

		// buffer the timer
		Thread.sleep(unit.getMG().getReloadSpeed() + 500);

		// should have reloaded
		assertTrue(unit.getMG().getBulletsInMag() == unit.getMG().getFullMag());
		assertTrue(unit.getWeapon() == Unit.MACHINE_GUN); // should stay on MG

		unit.getMG().setTotalBullets(0);
		aiUnit.reloadAndSwitch();

		Thread.sleep(unit.getMG().getReloadSpeed() + 500);
		// should still be zero
		assertTrue(unit.getMG().getBulletsInBag() == 0);
		// should have changed weapon to pistol
		assertTrue(unit.getWeapon() == Unit.PISTOL);

		unit.getMG().setTotalBullets(100);
		unit.getMG().setMagBullets(unit.getMG().getFullMag());
		aiUnit.reloadAndSwitch();

		Thread.sleep(unit.getMG().getReloadSpeed() + 500);
		// should still be 100 as magazine bullets wasn't depleted
		assertTrue(unit.getMG().getBulletsInBag() == 100);
		// should switch back to MG
		assertTrue(unit.getWeapon() == Unit.MACHINE_GUN);
	}
}
