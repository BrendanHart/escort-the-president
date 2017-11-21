package escort.server.game;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import escort.common.game.entities.units.Unit;
import escort.common.game.map.MapLoader;
import escort.server.lobby.LobbySettings;
import escort.server.network.Player;
import escort.server.network.ServerSide;

import static org.junit.Assert.assertTrue;

public class GameTest {

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
    }

    @After
    public void tearDown() {
        game = null;
    }

    @Test
    public void testThatGameIsSetUpWithOneOfEach() {

        ServerSide ss = new ServerSide(null);
        ArrayList<Player> players = new ArrayList<Player>();
        players.add(new Player(null, ss));
        players.add(new Player(null, ss));
        LobbySettings settings = new LobbySettings();
        settings.numAssassinsAI = 0;
        settings.numPoliceAI = 1;
        settings.numCivilianAI = 1;

        game = new Game(players, ss.getLobbyManagement(), 1, settings);

        Set<Integer> tests = new HashSet<Integer>();
        for(Unit u : game.getGameData().getUnits().values()) {
            assertTrue(!tests.contains(u.getUnitType()));
            tests.add(u.getUnitType());
        }
        assertTrue(tests.size() == 5);

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
                        System.out.println(x  +"  " + y);
                        System.out.println(game.getGameData().getMap().getPresidentSpawns());
                        assertTrue(findSpawn(x, y, game.getGameData().getMap().getPresidentSpawns()));
                        break;
                    case Unit.ESCORT_TYPE:
                        assertTrue(findSpawn(x, y, game.getGameData().getMap().getEscortSpawns()));
                        break;
                }
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
