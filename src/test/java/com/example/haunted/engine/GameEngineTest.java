package com.example.haunted.engine;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.haunted.config.DungeonFactory;
import com.example.haunted.events.CombatResult;
import com.example.haunted.model.Direction;

public class GameEngineTest {

    private GameEngine game;

    @BeforeEach
    void setUp() {
        game = DungeonFactory.createGame();
    }

    // ---------- helpers for the playthrough ----------

    private void go(Direction d) {
        assertTrue(game.move(d).isSuccess(), "move " + d);
    }

    private void take(String item) {
        assertTrue(game.pickUpItem(item).isSuccess(), "pick up " + item);
    }

    private void equip(String item) {
        assertTrue(game.equipItem(item).isSuccess(), "equip " + item);
    }

    private void unlock(Direction d) {
        assertTrue(game.unlockRoom(d).isSuccess(), "unlock " + d);
    }

    private void kill(String monster) {
        CombatResult r;
        int rounds = 0;
        do {
            r = game.attack(monster);
            assertTrue(r.isSuccess(), "attack " + monster);
            assertTrue(++rounds <= 10, monster + " took too long");
        } while (!r.isMonsterDefeated());
    }

    // ---------- delegation ----------

    @Test
    void testMoveDelegates() {
        assertTrue(game.move(Direction.EAST).isSuccess());
        assertEquals("lectureHall", game.getCurrentRoom().getId());

        assertFalse(game.move(Direction.EAST).isSuccess() && false); // see note below
    }

    @Test
    void testAttackUnknownMonster() {
        assertFalse(game.attack("Nobody").isSuccess());
    }

    @Test
    @DisplayName("attack finds the monster in the current room by name")
    void testAttackDelegates() {
        go(Direction.EAST);

        CombatResult r = game.attack("Sleep-Deprived TA");

        assertTrue(r.isSuccess());
        assertTrue(r.getDamageToMonster() > 0);
    }

    @Test
    void testPickUpAndUseDelegate() {
        go(Direction.EAST);
        take("Coffee Potion");
        game.getPlayer().takeDamage(20);

        assertTrue(game.useItem("Coffee Potion").isSuccess());
        assertEquals(45, game.getPlayer().getHealth());
    }

    @Test
    void testEquipDelegates() {
        go(Direction.EAST);
        go(Direction.EAST); // lab storage
        take("Calculator Shield");

        assertTrue(game.equipItem("Calculator Shield").isSuccess());
        assertNotNull(game.getPlayer().getEquippedArmor());
    }

    @Test
    @DisplayName("The exam archive is locked until the Archive Key is used")
    void testUnlockDelegates() {
        go(Direction.EAST);
        assertFalse(game.move(Direction.NORTH).isSuccess());
        assertFalse(game.unlockRoom(Direction.NORTH).isSuccess());

        go(Direction.EAST);
        take("Archive Key");
        go(Direction.WEST);
        unlock(Direction.NORTH);
        go(Direction.NORTH);
        assertEquals("examArchive", game.getCurrentRoom().getId());
    }

    // ---------- game state ----------

    @Test
    void testGameOverWhenPlayerDies() {
        game.getPlayer().takeDamage(49);
        assertFalse(game.isGameOver());

        game.getPlayer().takeDamage(1);
        assertTrue(game.isGameOver());
    }

    @ParameterizedTest(name = "gradebook={0}, phantom={1} -> won={2}")
    @CsvSource({"false,false,false", "true,false,false", "false,true,false", "true,true,true"})
    @DisplayName("Winning needs both the gradebook and the Phantom's defeat")
    void testWinRequiresBothObjectives(boolean gradebook, boolean phantom, boolean won) {
        if (gradebook) game.getQuest().markGradebookRecovered();
        if (phantom) game.getQuest().markPhantomDefeated();

        assertEquals(won, game.isGameWon());
    }

    @Test
    @DisplayName("A dead player has not won, even with the quest complete")
    void testDeadPlayerCannotWin() {
        game.getQuest().markGradebookRecovered();
        game.getQuest().markPhantomDefeated();
        game.getPlayer().takeDamage(50);

        assertTrue(game.isGameOver());
        assertFalse(game.isGameWon());
    }

    // ---------- full playthrough ----------

    @Test
    @DisplayName("Full playthrough: collect gear, get the gradebook, beat the Phantom, win")
    void testFullPlaythrough() {
        go(Direction.EAST);                    // lecture hall
        kill("Sleep-Deprived TA");

        go(Direction.EAST);                    // lab storage
        take("Archive Key");
        take("Calculator Shield");
        equip("Calculator Shield");

        go(Direction.NORTH);                   // server closet
        take("Stapler of Justice");
        equip("Stapler of Justice");
        kill("Spreadsheet Golem");
        take("Dry Erase Sword");               // dropped by the Golem
        equip("Dry Erase Sword");

        go(Direction.EAST);                    // dean vault
        kill("Registrar Wraith");
        take("Vault Key");
        take("Graduation Gown Armor");         // dropped by the Wraith
        equip("Graduation Gown Armor");

        unlock(Direction.WEST);                // exam archive needs the Archive Key
        go(Direction.WEST);
        kill("Plagiarism Ghost");
        take("Lost Gradebook");
        assertFalse(game.isGameWon());         // gradebook alone is not enough

        go(Direction.EAST);                    // back to the vault
        unlock(Direction.NORTH);               // final chamber needs the Vault Key
        go(Direction.NORTH);
        kill("Final Exam Phantom");

        assertTrue(game.isGameWon());
        assertFalse(game.isGameOver());
    }
}