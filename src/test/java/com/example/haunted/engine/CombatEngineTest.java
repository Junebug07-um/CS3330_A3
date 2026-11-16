package com.example.haunted.engine;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.haunted.events.CombatResult;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Monster;
import com.example.haunted.model.Player;
import com.example.haunted.model.Potion;
import com.example.haunted.model.Quest;
import com.example.haunted.model.Room;
import com.example.haunted.rules.DamageCalculator;
import com.example.haunted.rules.QuestTracker;

public class CombatEngineTest {

    private CombatEngine engine;
    private Player player;
    private Quest quest;
    private Room room;

    @BeforeEach
    void setUp() {
        engine = new CombatEngine(new DamageCalculator(), new QuestTracker());
        quest = new Quest("Escape", "Get out");
        room = new Room("r1", "Test Room", "A test room.");
        player = new Player("Hero", 50, 7, 2, new Inventory(5));
        player.setCurrentRoom(room);
    }

    @Test
    void testNullMonster() {
        CombatResult r = engine.attack(player, quest, null);

        assertFalse(r.isSuccess());
        assertEquals(0, r.getDamageToMonster());
        assertEquals(0, r.getDamageToPlayer());
    }

    @Test
    @DisplayName("A defeated player cannot attack")
    void testDeadPlayer() {
        player.takeDamage(50);
        Monster goblin = new Monster("Goblin", 20, 6, 1, List.of());

        CombatResult r = engine.attack(player, quest, goblin);

        assertFalse(r.isSuccess());
        assertEquals(20, goblin.getHealth());
    }

    @Test
    @DisplayName("An already-defeated monster fails and drops no loot")
    void testDeadMonster() {
        Monster goblin = new Monster("Goblin", 10, 6, 1, List.of(new Potion("Tonic", "A tonic", 5)));
        goblin.takeDamage(100);

        CombatResult r = engine.attack(player, quest, goblin);

        assertFalse(r.isSuccess());
        assertTrue(r.isMonsterDefeated());
        assertTrue(room.getItems().isEmpty());
    }

    @Test
    @DisplayName("Non-lethal attack applies damage to the monster and the counterattack to the player")
    void testNonLethalAttack() {
        Monster goblin = new Monster("Goblin", 100, 6, 1, List.of());

        CombatResult r = engine.attack(player, quest, goblin);

        assertTrue(r.isSuccess());
        assertFalse(r.isMonsterDefeated());
        assertEquals(100 - r.getDamageToMonster(), goblin.getHealth());
        assertEquals(50 - r.getDamageToPlayer(), player.getHealth());
        assertTrue(r.getDamageToPlayer() > 0);
    }

    @Test
    @DisplayName("A kill drops loot in the room and the monster does not counterattack")
    void testKillDropsLoot() {
        Monster goblin = new Monster("Goblin", 1, 6, 1, List.of(new Potion("Tonic", "A tonic", 5)));

        CombatResult r = engine.attack(player, quest, goblin);

        assertTrue(r.isMonsterDefeated());
        assertEquals(1, r.getDroppedItems().size());
        assertTrue(room.findItem("Tonic").isPresent());
        assertEquals(0, r.getDamageToPlayer());
        assertEquals(50, player.getHealth());
    }

    @Test
    @DisplayName("Killing the Final Exam Phantom updates the quest")
    void testKillingPhantomUpdatesQuest() {
        Monster phantom = new Monster("Final Exam Phantom", 1, 10, 4, List.of());

        engine.attack(player, quest, phantom);

        assertTrue(quest.isPhantomDefeated());
    }

    @Test
    @DisplayName("The player can die from the counterattack")
    void testPlayerDiesFromCounterattack() {
        player.takeDamage(47); // 3 HP left
        Monster goblin = new Monster("Goblin", 100, 6, 1, List.of());

        CombatResult r = engine.attack(player, quest, goblin);

        assertTrue(r.isSuccess());
        assertFalse(player.isAlive());
    }
}