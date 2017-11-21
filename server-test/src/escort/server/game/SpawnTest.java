package escort.server.game;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.Rectangle;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import escort.common.game.entities.units.Unit;
import escort.common.game.map.MapLoader;
import escort.common.game.weapons.BlastShield;
import escort.server.lobby.LobbySettings;
import escort.server.network.Player;
import escort.server.network.ServerSide;

import static org.junit.Assert.assertTrue;

public class SpawnTest {

    private Game game;

    @Before
    public void setUp() {
        ServerSide ss = new ServerSide(null);
        ArrayList<Player> players = new ArrayList<Player>();
        players.add(new Player(null, ss));
        players.add(new Player(null, ss));
        LobbySettings settings = new LobbySettings();
        settings.numAssassinsAI = 0;
        settings.numPoliceAI = 1;
        settings.numCivilianAI = 1;
        try {
            (new MapLoader()).load();
        } catch (Exception e) {
        }
        game = new Game(players, ss.getLobbyManagement(), 1, settings);
        Set<Integer> tests = new HashSet<Integer>();
        for(Unit u : game.getGameData().getUnits().values()) {
            assertTrue(!tests.contains(u.getUnitType()));
            tests.add(u.getUnitType());
        }
        assertTrue(tests.size() == 5);
    }

    @After
    public void tearDown() {
        game = null;
    }

    @Test
    public void testThatSpawnsAreValid() {
        for(int i = 0; i < 10000; i++) {
            for(Unit u : game.getGameData().getUnits().values()) {
                game.spawn(u);
                int x = (int)u.getAbsoluteBounds().x; 
                int y = (int)u.getAbsoluteBounds().y; 
                switch(u.getUnitType()) {
                    case Unit.CIVILIAN_TYPE:
                        assertTrue(game.getGameData().getMap().walkable(x,y,0,0));
                        break;
                    case Unit.POLICE_TYPE:
                        assertTrue(game.getGameData().getMap().walkable(x,y,0,0));
                        break;
                    case Unit.ASSASSIN_TYPE:
                        assertTrue(findSpawn(x, y, game.getGameData().getMap().getAssassinSpawns()));
                        break;
                    case Unit.PRESIDENT_TYPE:
                        assertTrue(findSpawn(x, y, game.getGameData().getMap().getPresidentSpawns()));
                        break;
                    case Unit.ESCORT_TYPE:
                        assertTrue(findSpawn(x, y, game.getGameData().getMap().getEscortSpawns()));
                        break;
                }
            }
        }
    }

    @Test
    public void testRespawnResetsValues() {
        // Get units then make a new game with no units
        // since otherwise respawn sends positions to players (null sender)
        List<Unit> units = new ArrayList<>(game.getGameData().getUnits().values());
        ServerSide ss = new ServerSide(null);
        ArrayList<Player> players = new ArrayList<Player>();
        LobbySettings settings = new LobbySettings();
        settings.numAssassinsAI = 0;
        settings.numPoliceAI = 0;
        settings.numCivilianAI = 0;
        try {
            (new MapLoader()).load();
        } catch (Exception e) {
        }
        game = new Game(players, ss.getLobbyManagement(), 1, settings);

        for(Unit u : units) {
            // Simulate death for unit
            u.setHP(0);
            u.died();
            
            // Test respawn
            game.respawn(u);
            assertTrue(u.getXVel() == 0);
            assertTrue(u.getYVel() == 0);
            assertTrue(u.getHP() == u.getSpawnHealth());
            assertTrue(u.getGrenadesLeft() == 2);
            if(u.getBlastShield() != null) {
                assertTrue(u.getBlastShield().getHP() == BlastShield.MAX_HP);
            }
            if(u.getPistol() != null) {
                assertTrue(u.getPistol().getFullMag() == u.getPistol().getBulletsInMag());
            }
            if(u.getMG() != null) {
                System.out.println(u.getMG().getBulletsInBag() + " " + u.getMG().getFullMag());
                assertTrue(u.getMG().getBulletsInBag() == u.getMG().getFullMag() * 4);
            }
        }
    }

    public boolean findSpawn(int x, int y, Set<Rectangle> spawns) {
        for(Rectangle spawn : spawns) {
            if(spawn.x <= x && x <= spawn.x + spawn.width && spawn.y <= y && y <= spawn.y + spawn.height) {
                return true;
            }
        }
        return false;
    }

}
