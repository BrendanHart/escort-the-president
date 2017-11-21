package escort.server.game;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.common.game.Outcomes;
import escort.common.game.entities.units.Unit;
import escort.server.lobby.LobbySettings;
import escort.server.network.Player;
import escort.server.network.ServerSide;

/**
 *  JUnit tests for assignment.
 */
public class AssignmentTest {

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
     *  - The correct proportion of assassins and escorts are assigned based
     *    on human player numbers. That is the number of escorts is ceil(numPlayers/3.0)
     *    and the number of assassins is numPlayers - numEscorts. numPresidents should be 1.
     * @author James Birch
     */
    public void assignmentTest() {
        System.out.println("RUNNING UNIT ASSIGNMENT TEST");
        int escorts = 0;
        int assassins = 0;
        int presidents = 0;
        for(Unit unit : game.getGameData().getUnits().values()) {
            switch(unit.getUnitType()) {
                case Unit.ESCORT_TYPE: escorts++;
                                       break;
                case Unit.ASSASSIN_TYPE: assassins++;
                                         break;
                case Unit.PRESIDENT_TYPE: presidents++;
                                          break;
                default: break;
            }
        }

        int numPlayers = players.size();
        assertTrue((int) Math.ceil(numPlayers / 3.0) == escorts);
        assertTrue(numPlayers - escorts == assassins);
        assertTrue(presidents == 1);
    }

}
