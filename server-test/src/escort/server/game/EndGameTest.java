package escort.server.game;

import static org.junit.Assert.assertTrue;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.common.game.Outcomes;
import escort.common.game.entities.units.President;
import escort.server.lobby.LobbySettings;
import escort.server.network.Player;
import escort.server.network.ServerSide;

/**
 *  JUnit tests for end game logic.
 */
public class EndGameTest {

    private Game game;
    private ServerSide ss;
    private List<Player> players;

    @Before
    /**
     * @author Kwong-Hei Tsang
     */
    public void setUp() {
        // construct the clients
        ss = new ServerSide(null);
        players = new ArrayList<Player>();
        Player player;
        for (int i = 0; i < 10; i++) {
            player = new Player(new FakeMessageControl(), ss);
            players.add(player);
            player.start();
        }

        // construct the game logic, and configure the lobby settings here
        LobbySettings settings = new LobbySettings();
        // disable AI for this test
        settings.numAssassinsAI= 0;
        settings.numPoliceAI = 0;
        game = new Game(players, ss.getLobbyManagement(), 1, settings);

        // start game
        game.startGame();
        assertTrue(game.hasStarted()); // Game should have started by this point
    }
    
    @After
    /**
     * @author James Birch
     */
    public void tearDown() {
        game.endGame(Outcomes.OUTCOME_DRAW); // Outcome doesn't really matter here
        ss.shutdownServer();
        game = null;
        ss = null;
        players = null;
    }

    @Test
    /**
     * Only passes iff:
     *  - isDead is true when President has zero HP.
     *  - Intersection with end zone is detected when a President walks on one.
     *  @author James Birch
     */
    public void endGameTest() {
        System.out.println("RUNNING END GAME CONDITIONS TEST");
        // End game is called if president isDead is true
        President pres = game.getGameData().getPresident();
        pres.setHP(0);
        assertTrue(pres.isDead());

        // End game is called if president reaches end zone
        pres.setHP(100);
        // hard code known end zone
        pres.setX(1664.0);
        pres.setY(140.0);
        Rectangle presBounds = pres.getCollisionBounds();
        presBounds.x += pres.getX();
        presBounds.y += pres.getY();
        boolean end = false;
        for(Rectangle r : game.getGameData().getMap().getEndZones()) {
            if(r.intersects(presBounds)) end = true; // one of these should match
        }
        assertTrue(end);
    }
}
