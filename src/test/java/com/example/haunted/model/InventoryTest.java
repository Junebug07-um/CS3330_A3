package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class InventoryTest {

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory(2);
    }

    @Test
    @DisplayName("Inventory initial state matches capacity")
    void testInitialState() {
        assertEquals(2, inventory.getCapacity());
        assertFalse(inventory.isFull());
        assertEquals(0, inventory.getItems().size());
    }

    @Test
    @DisplayName("Adding items up to capacity and hitting boundary")
    void testAddItemCapacityBoundary() {
        Item item1 = new QuestItem("Note", "A scrap of paper");
        Item item2 = new QuestItem("Pencil", "A yellow pencil");
        Item overflow = new QuestItem("Eraser", "A pink eraser");

        assertTrue(inventory.addItem(item1));
        assertFalse(inventory.isFull());

        assertTrue(inventory.addItem(item2));
        assertTrue(inventory.isFull()); // Size == capacity

        // Attempting to exceed capacity
        assertFalse(inventory.addItem(overflow));
        assertEquals(2, inventory.getItems().size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Flashlight", "flashlight", "FLASHLIGHT"})
    @DisplayName("Find and contains item matches case-insensitively")
    void testFindAndContainsCaseInsensitive(String queryName) {
        Item item = new QuestItem("Flashlight", "A heavy flashlight");
        inventory.addItem(item);

        assertTrue(inventory.contains(queryName));
        Optional<Item> found = inventory.findItem(queryName);
        assertTrue(found.isPresent());
        assertEquals("Flashlight", found.get().getName());
    }

    @Test
    @DisplayName("Find and contains item return negative when item absent")
    void testFindAndContainsMissingItem() {
        assertFalse(inventory.contains("Ghost Radar"));
        assertTrue(inventory.findItem("Ghost Radar").isEmpty());
    }

    @Test
    @DisplayName("Remove item successfully returns item and decreases inventory size")
    void testRemoveExistingItem() {
        Item item = new QuestItem("Key", "Brass key");
        inventory.addItem(item);

        Item removed = inventory.removeItem("key");
        assertNotNull(removed);
        assertEquals("Key", removed.getName());
        assertFalse(inventory.contains("Key"));
        assertEquals(0, inventory.getItems().size());
    }

    @Test
    @DisplayName("Remove absent item returns null without altering state")
    void testRemoveNonExistentItem() {
        Item item = new QuestItem("Key", "Brass key");
        inventory.addItem(item);

        Item removed = inventory.removeItem("Missing");
        assertNull(removed);
        assertEquals(1, inventory.getItems().size());
    }

    @Test
    @DisplayName("getItems() returns an unmodifiable list")
    void testUnmodifiableItemsList() {
        assertThrows(UnsupportedOperationException.class, () -> inventory.getItems().add(null));
    }
}