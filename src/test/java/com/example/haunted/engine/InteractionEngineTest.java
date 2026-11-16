package com.example.haunted.engine;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.example.haunted.events.InteractionResult;
import com.example.haunted.model.Armor;
import com.example.haunted.model.Direction;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Key;
import com.example.haunted.model.Player;
import com.example.haunted.model.Potion;
import com.example.haunted.model.Quest;
import com.example.haunted.model.QuestItem;
import com.example.haunted.model.Room;
import com.example.haunted.model.Weapon;
import com.example.haunted.rules.QuestTracker;

public class InteractionEngineTest {

    private InteractionEngine engine;
    private Player player;
    private Quest quest;
    private Room room;

    @BeforeEach
    void setUp() {
        engine = new InteractionEngine(new QuestTracker());
        quest = new Quest("Escape", "Get out");
        room = new Room("r1", "Test Room", "A test room.");
        player = new Player("Hero", 50, 5, 2, new Inventory(1));
        player.setCurrentRoom(room);
    }

    private Room lockedRoomNorth(String requiredKey) {
        Room target = new Room("vault", "Vault", "A vault.");
        target.setLocked(true, requiredKey);
        room.connect(Direction.NORTH, target);
        return target;
    }

    // ---------- pickUpItem ----------

    @Test
    @DisplayName("pickUpItem moves the item to the inventory and updates the quest")
    void testPickUpSuccess() {
        room.addItem(new QuestItem("Lost Gradebook", "The gradebook"));

        InteractionResult r = engine.pickUpItem(player, quest, "Lost Gradebook");

        assertTrue(r.isSuccess());
        assertTrue(player.getInventory().contains("Lost Gradebook"));
        assertTrue(room.findItem("Lost Gradebook").isEmpty());
        assertTrue(quest.isGradebookRecovered());
    }

    @Test
    @DisplayName("pickUpItem fails when the item is not in the room")
    void testPickUpMissing() {
        InteractionResult r = engine.pickUpItem(player, quest, "Nothing");

        assertFalse(r.isSuccess());
        assertTrue(player.getInventory().getItems().isEmpty());
    }

    @Test
    @DisplayName("Full inventory: pickup fails, item stays in the room, quest not updated")
    void testPickUpFullInventory() {
        player.getInventory().addItem(new Key("Old Key", "A key"));
        room.addItem(new QuestItem("Lost Gradebook", "The gradebook"));

        InteractionResult r = engine.pickUpItem(player, quest, "Lost Gradebook");

        assertFalse(r.isSuccess());
        assertTrue(room.findItem("Lost Gradebook").isPresent());
        assertFalse(quest.isGradebookRecovered());
    }

    // ---------- useItem ----------

    @Test
    void testUseMissingItem() {
        assertFalse(engine.useItem(player, "Nothing").isSuccess());
    }

    @Test
    @DisplayName("useItem rejects a non-potion and keeps it")
    void testUseNonPotion() {
        player.getInventory().addItem(new Key("Old Key", "A key"));

        InteractionResult r = engine.useItem(player, "Old Key");

        assertFalse(r.isSuccess());
        assertTrue(player.getInventory().contains("Old Key"));
    }

    @Test
    @DisplayName("useItem heals the player and consumes the potion")
    void testUsePotion() {
        player.takeDamage(10);
        player.getInventory().addItem(new Potion("Tonic", "A tonic", 5));

        InteractionResult r = engine.useItem(player, "Tonic");

        assertTrue(r.isSuccess());
        assertEquals(45, player.getHealth());
        assertFalse(player.getInventory().contains("Tonic"));
    }

    // ---------- equipItem ----------

    @Test
    void testEquipMissingItem() {
        assertFalse(engine.equipItem(player, "Nothing").isSuccess());
    }

    @Test
    void testEquipWeapon() {
        Weapon stapler = new Weapon("Stapler", "Heavy", 4);
        player.getInventory().addItem(stapler);

        assertTrue(engine.equipItem(player, "Stapler").isSuccess());
        assertSame(stapler, player.getEquippedWeapon());
        assertNull(player.getEquippedArmor());
    }

    @Test
    void testEquipArmor() {
        Armor shield = new Armor("Shield", "Sturdy", 3);
        player.getInventory().addItem(shield);

        assertTrue(engine.equipItem(player, "Shield").isSuccess());
        assertSame(shield, player.getEquippedArmor());
        assertNull(player.getEquippedWeapon());
    }

    @Test
    @DisplayName("equipItem rejects items that are neither weapons nor armor")
    void testEquipNeither() {
        player.getInventory().addItem(new Key("Old Key", "A key"));

        assertFalse(engine.equipItem(player, "Old Key").isSuccess());
        assertNull(player.getEquippedWeapon());
        assertNull(player.getEquippedArmor());
    }

    // ---------- unlockRoom ----------

    @ParameterizedTest
    @EnumSource(Direction.class)
    @DisplayName("unlockRoom fails when there is no exit that way")
    void testUnlockNoExit(Direction direction) {
        assertFalse(engine.unlockRoom(player, direction).isSuccess());
    }

    @Test
    void testUnlockAlreadyUnlocked() {
        room.connect(Direction.NORTH, new Room("hall", "Hall", "A hall."));

        assertTrue(engine.unlockRoom(player, Direction.NORTH).isSuccess());
    }

    @Test
    void testUnlockRoomWithNoRequiredKey() {
        Room target = lockedRoomNorth(null);

        assertFalse(engine.unlockRoom(player, Direction.NORTH).isSuccess());
        assertTrue(target.isLocked());
    }

    @Test
    void testUnlockWithoutKey() {
        Room target = lockedRoomNorth("Archive Key");

        assertFalse(engine.unlockRoom(player, Direction.NORTH).isSuccess());
        assertTrue(target.isLocked());
    }

    @Test
    @DisplayName("An item with the key's name that is not a Key does not unlock")
    void testUnlockWithNonKeyItem() {
        Room target = lockedRoomNorth("Archive Key");
        player.getInventory().addItem(new QuestItem("Archive Key", "Not a key"));

        assertFalse(engine.unlockRoom(player, Direction.NORTH).isSuccess());
        assertTrue(target.isLocked());
    }

    @Test
    void testUnlockSuccess() {
        Room target = lockedRoomNorth("Archive Key");
        player.getInventory().addItem(new Key("Archive Key", "A key"));

        assertTrue(engine.unlockRoom(player, Direction.NORTH).isSuccess());
        assertFalse(target.isLocked());
    }
}