package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

public class RoomTest {

    private Room room;

    @BeforeEach
    void setUp() {
        room = new Room("R1", "Basement Hallway", "A cold, damp hallway.");
    }

    @Test
    @DisplayName("Room constructor initializes fields and checks null constraints")
    void testRoomInitialization() {
        assertEquals("R1", room.getId());
        assertEquals("Basement Hallway", room.getName());
        assertEquals("A cold, damp hallway.", room.getDescription());
        assertFalse(room.isLocked());
        assertNull(room.getRequiredKeyName());
        assertNull(room.getTrap());
        assertTrue(room.getExits().isEmpty());
        assertTrue(room.getItems().isEmpty());
        assertTrue(room.getMonsters().isEmpty());
        assertFalse(room.hasLivingMonsters());

        assertThrows(NullPointerException.class, () -> new Room(null, "Name", "Desc"));
        assertThrows(NullPointerException.class, () -> new Room("ID", null, "Desc"));
        assertThrows(NullPointerException.class, () -> new Room("ID", "Name", null));
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    @DisplayName("Connect and get exits across all cardinal directions")
    void testConnectAndGetExits(Direction direction) {
        Room adjacent = new Room("R2", "Adjacent Room", "Connected room");
        room.connect(direction, adjacent);

        assertEquals(adjacent, room.getExit(direction));
        assertEquals(1, room.getExits().size());
        assertTrue(room.getExits().containsKey(direction));

        // Ensure defensive unmodifiable map
        assertThrows(UnsupportedOperationException.class, () -> room.getExits().put(direction, adjacent));
    }

    @Test
    @DisplayName("Connect rejects null direction or room")
    void testConnectNullConstraints() {
        Room adjacent = new Room("R2", "Adjacent", "Desc");
        assertThrows(NullPointerException.class, () -> room.connect(null, adjacent));
        assertThrows(NullPointerException.class, () -> room.connect(Direction.NORTH, null));
    }

    @Test
    @DisplayName("Item operations: add, find, remove, and unmodifiable list")
    void testRoomItems() {
        Item key = new QuestItem("Rusty Key", "Opens an old door");
        room.addItem(key);

        assertEquals(1, room.getItems().size());
        assertTrue(room.findItem("Rusty Key").isPresent());
        assertTrue(room.findItem("rusty key").isPresent()); // case-insensitive
        assertTrue(room.findItem("NonExistent").isEmpty());

        // Remove item
        Item removed = room.removeItemByName("RUSTY KEY");
        assertNotNull(removed);
        assertEquals("Rusty Key", removed.getName());
        assertEquals(0, room.getItems().size());

        // Remove non-existent returns null
        assertNull(room.removeItemByName("Rusty Key"));

        // Defensive copy
        assertThrows(UnsupportedOperationException.class, () -> room.getItems().add(key));
        assertThrows(NullPointerException.class, () -> room.addItem(null));
    }

    @Test
    @DisplayName("Monster operations: add, find, and living monsters check")
    void testRoomMonsters() {
        Monster ta = new Monster("Sleep-Deprived TA", 30, 10, 2, Collections.emptyList());
        room.addMonster(ta);

        assertEquals(1, room.getMonsters().size());
        assertTrue(room.findMonster("sleep-deprived ta").isPresent());
        assertTrue(room.findMonster("Ghost").isEmpty());
        assertTrue(room.hasLivingMonsters());

        // Defeat monster and re-check living status
        ta.takeDamage(30);
        assertFalse(ta.isAlive());
        assertFalse(room.hasLivingMonsters());

        // Defensive copy and null checks
        assertThrows(UnsupportedOperationException.class, () -> room.getMonsters().add(ta));
        assertThrows(NullPointerException.class, () -> room.addMonster(null));
    }

    @Test
    @DisplayName("Locking and unlocking logic with matching, non-matching, and null keys")
    void testLockAndUnlock() {
        // Unlocked room unlocks trivially
        assertTrue(room.unlock("AnyKey"));

        // Lock room with required key
        room.setLocked(true, "Master Key");
        assertTrue(room.isLocked());
        assertEquals("Master Key", room.getRequiredKeyName());

        // Failed unlock attempts
        assertFalse(room.unlock("Wrong Key"));
        assertFalse(room.unlock(null));
        assertTrue(room.isLocked());

        // Successful unlock (case-insensitive)
        assertTrue(room.unlock("master key"));
        assertFalse(room.isLocked());
    }

    @Test
    @DisplayName("Locked room with null requiredKeyName cannot be unlocked")
    void testLockedWithNullKey() {
        room.setLocked(true, null);
        assertTrue(room.isLocked());
        assertFalse(room.unlock("SomeKey"));
        assertFalse(room.unlock(null));
    }

    @Test
    @DisplayName("Trap attachment and retrieval")
    void testTrapAttachment() {
        Trap trap = new Trap("Steam Pipe", TrapType.STEAM, 15, true, true);
        room.setTrap(trap);
        assertEquals(trap, room.getTrap());
    }

    @Test
    @DisplayName("Direction enum coverage")
    void testDirectionEnum() {
        for (Direction dir : Direction.values()) {
            assertEquals(dir, Direction.valueOf(dir.name()));
        }
    }
}