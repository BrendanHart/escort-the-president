package escort.common.game.entities;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.common.game.GameData;
import escort.common.game.entities.units.Assassin;
import escort.common.game.entities.units.Civilian;
import escort.common.game.entities.units.Escort;
import escort.common.game.entities.units.Police;
import escort.common.game.entities.units.President;
import escort.common.game.entities.units.Unit;
import escort.common.game.map.GameMap;
import escort.common.game.map.MapLoader;

public class UnitTest {

	private Unit unit1;
	private Unit unit2;
	private Unit unit3;
	private Unit unit4;
	private Unit unit5;
	private GameData gameData;

	@Before
	public void setUp() {
		try {
			new MapLoader().load();
		} catch (IOException e) {
			System.out.println("Oh");
			e.printStackTrace();
		}
		gameData = new GameData(GameMap.loadFromID(0), null);
		unit1 = new Escort(gameData, null, 0);
		unit2 = new Assassin(gameData, null, 1);
		unit3 = new Police(gameData, null, 2);
		unit4 = new Civilian(gameData, null, 3);
		unit5 = new President(gameData, null, 4);
	}

	@After
	public void tearDown() {
		unit1 = null;
		unit2 = null;
		unit3 = null;
		unit4 = null;
		unit5 = null;
		gameData = null;
	}

//	@Test
//	public void testDelayReload() {
//		assertTrue(false);
//	}

	@Test
	public void testGetBlastSheild() {
		assertTrue(unit1.getBlastShield() != null);
		assertTrue(unit2.getBlastShield() != null);
	}

	@Test
	public void testGetMaxHP() {
		assertTrue(unit1.getMaxHP() == Unit.ESCORT_HP);
		assertTrue(unit2.getMaxHP() == Unit.ASSASSIN_HP);
		assertTrue(unit3.getMaxHP() == Unit.POLICE_HP);
		assertTrue(unit4.getMaxHP() == Unit.CIVILIAN_HP);
		assertTrue(unit5.getMaxHP() == Unit.PRESIDENT_HP);
	}

	@Test
	public void testReduceHP() {
		unit1.reduceHP(Unit.ESCORT_HP / 2);
		assertTrue(!unit1.isDead());

		unit1.reduceHP((Unit.ESCORT_HP / 2) - 1);
		assertTrue(!unit1.isDead());

		unit1.reduceHP(1);
		assertTrue(unit1.isDead());

		unit1.reduceHP(1000);
		assertTrue(unit1.isDead());
		assertTrue(unit1.getHP() == 0);
	}
	
	@Test
	public void testSetNumberOfGrenades() {
		unit1.setNumberOfGrenades(2);
		assertTrue(unit1.getGrenadesLeft() == 2);
		
		unit1.setNumberOfGrenades(Unit.MAX_NUM_GRENADES - 1);
		assertTrue(unit1.getGrenadesLeft() == Unit.MAX_NUM_GRENADES - 1);
		
		unit1.setNumberOfGrenades(Unit.MAX_NUM_GRENADES);
		assertTrue(unit1.getGrenadesLeft() == Unit.MAX_NUM_GRENADES);
		
		unit1.setNumberOfGrenades(Unit.MAX_NUM_GRENADES + 1);
		assertTrue(unit1.getGrenadesLeft() == Unit.MAX_NUM_GRENADES);
		
		unit1.setNumberOfGrenades(Unit.MAX_NUM_GRENADES + 100);
		assertTrue(unit1.getGrenadesLeft() == Unit.MAX_NUM_GRENADES);
	}
	
	@Test
	public void testSetHP() {
		unit1.setHP(30);
		assertTrue(unit1.getHP() == 30);
		
		unit1.setHP(50);
		assertTrue(unit1.getHP() == 50);
		
		unit1.setHP(unit1.getMaxHP());
		assertTrue(unit1.getHP() == Unit.ESCORT_HP);
	}
	
	@Test
	public void testGetSpawnHealthAndTime() {
		assertTrue(unit1.getSpawnHealth() == Unit.ESCORT_HP);
		assertTrue(unit2.getSpawnHealth() == Unit.ASSASSIN_HP);
		assertTrue(unit3.getSpawnHealth() == Unit.POLICE_HP);
		assertTrue(unit4.getSpawnHealth() == Unit.CIVILIAN_HP);
		assertTrue(unit5.getSpawnHealth() == Unit.PRESIDENT_HP);
		
		assertTrue(unit1.getSpawnTime() == Unit.ESCORT_SPAWN_TIME);
		assertTrue(unit2.getSpawnTime() == Unit.ASSASSIN_SPAWN_TIME);
		assertTrue(unit3.getSpawnTime() == Unit.POLICE_SPAWN_TIME);
		assertTrue(unit4.getSpawnTime() == Unit.CIVILIAN_SPAWN_TIME);
	}
	
	@Test
	public void testDistance() {
		unit1.setX(100);
		unit1.setY(70);
		
		unit2.setX(50);
		unit2.setY(70);
		assertTrue(unit1.distance(unit2) == 50);
		
		unit1.setX(75);
		unit1.setY(75);
		
		unit2.setX(95);
		unit2.setY(174);
		assertTrue(unit1.distance(unit2) == 101);
		
		unit1.setX(45);
		unit1.setY(60);
		
		unit2.setX(164);
		unit2.setY(180);
		assertTrue(unit1.distance(unit2) == 169);
		
	}
	
	@Test
	public void testRespawn() {
		unit1.respawn();
		
		assertTrue(unit1.getGrenadesLeft() == 2);
		assertTrue(unit2.getPistol().getBulletsInMag() == unit1.getPistol().getFullMag());
		assertTrue(unit1.getMG().getBulletsInMag() == unit1.getMG().getFullMag());
	}
	
	@Test
	public void testDied() {
		unit1.setHP(0);
		unit1.died();
		
		assertTrue(unit1.getXVel() == 0);
		assertTrue(unit1.getYVel() == 0);
		assertTrue(unit1.getGrenadesLeft() == 0);
		assertTrue(unit1.getHeldGrenade() == null);
		assertTrue(unit1.getAirborneGrenades().isEmpty());
		assertTrue(unit1.getPistol().getFiredBullets().isEmpty());
		assertTrue(unit1.getMG().getFiredBullets().isEmpty());
		assertTrue(unit1.getPistol().getBulletsInBag() == 0);
		assertTrue(unit1.getMG().getBulletsInBag() == 0);
	}

	@Test
	public void testAngleFromPoint() {
		unit1.setX(10);
		unit1.setY(10);

		unit2.setX(20);
		unit2.setY(10);
		assertTrue(unit1.angleFromPoint(unit2.getCenterPoint()) == Math.PI / 2);

		unit1.setX(10);
		unit1.setY(10);

		unit2.setX(20);
		unit2.setY(20);
		assertTrue(unit1.angleFromPoint(unit2.getCenterPoint()) == 3 * Math.PI / 4);
		
		unit1.setX(15);
		unit1.setY(70);

		unit2.setX(15);
		unit2.setY(100);
		assertTrue(unit1.angleFromPoint(unit2.getCenterPoint()) == Math.PI);
	}
}