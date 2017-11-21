package escort.server.game;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import escort.common.game.Outcomes;
import escort.common.game.entities.units.Unit;
import escort.common.game.weapons.Bullet;
import escort.server.lobby.LobbySettings;
import escort.server.network.Player;
import escort.server.network.ServerSide;

/**
 *  JUnit tests for weapons.
 */
public class WeaponsTest {

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
     *  - Pistol and MG firing decrements magazine.
     *  - Pistol and MG reloading resets magazine bullets and sets total bullets appropriately.
     *  - MG maintains zero values when reloading with no bullets at all.
     *  @author James Birch
     */
    public void shootTest() throws InterruptedException {
        System.out.println("RUNNING SHOOT TEST.");
        int initialMGTotal;
        // Tests the capacity for any escort or assassin to shoot
        for (Unit unit : game.getGameData().getUnits().values()) {
            if(unit.getUnitType() != Unit.ESCORT_TYPE || unit.getUnitType() != Unit.ASSASSIN_TYPE) {
                continue;
            }

            // Ensure pistol is set
            unit.setWeapon(Unit.PISTOL);
            assertTrue(unit.getWeapon() == Unit.PISTOL);

            // Before any bullets are fired this should be true
            assertTrue(unit.getPistol().getBulletsInMag() == unit.getPistol().getFullMag());

            // Shoot pistol
            unit.getPistol().fire(new Bullet(game.getGameData(), unit, 0));

            // A bullet has been fired
            assertTrue(unit.getPistol().getBulletsInMag() == unit.getPistol().getFullMag() - 1);

            // Reload pistol
            unit.reload();

            System.out.println("Waiting for pistol to reload");
            Thread.sleep(unit.getPistol().getReloadSpeed() * 2);

            // Should have a full magazine again, total bullets should remain negative
            assertTrue(unit.getPistol().getBulletsInMag() == unit.getPistol().getFullMag());
            assertTrue(unit.getPistol().getBulletsInBag() < 0);

            // Switch to MG
            unit.setWeapon(Unit.MACHINE_GUN);
            assertTrue(unit.getWeapon() == Unit.MACHINE_GUN);
            initialMGTotal = unit.getMG().getBulletsInBag();

            // Before any bullets are fired this should be true
            assertTrue(unit.getMG().getBulletsInMag() == unit.getMG().getFullMag());

            // Shoot MG 3 times
            unit.getMG().fire(new Bullet(game.getGameData(), unit, 0));
            unit.getMG().fire(new Bullet(game.getGameData(), unit, 0));
            unit.getMG().fire(new Bullet(game.getGameData(), unit, 0));

            // Check that 3 bullets have been fired
            assertTrue(unit.getMG().getBulletsInMag() == unit.getMG().getFullMag() - 3);

            // Reload MG
            unit.reload();

            System.out.println("Waiting for MG to reload");
            Thread.sleep(unit.getMG().getReloadSpeed() * 2);

            // Should have a full magazine again, total bullets should be down by 3
            assertTrue(unit.getMG().getBulletsInMag() == unit.getMG().getFullMag());
            assertTrue(unit.getMG().getBulletsInBag() == initialMGTotal - 3);

            // Empty all bullets
            unit.getMG().setMagBullets(0);
            unit.getMG().setTotalBullets(0);

            // Reloading the MG should maintain magazine bullets and total bullets at zero.
            assertTrue(unit.getMG().getBulletsInMag() == 0);
            assertTrue(unit.getMG().getBulletsInBag() == 0);
        }
    }

}
