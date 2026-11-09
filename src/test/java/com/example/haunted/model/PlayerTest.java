package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

public class PlayerTest {

    private Player player;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory(5);
        player = new Player("Hero", 100, 15, 5, inventory);
    }

    @Test
    @DisplayName("Player constructor initializes baseline state and validates null checks")
    void testPlayerInitialization() {
        assertEquals("Hero", player.getName());
        assertEquals(100, player.getHealth());
        assertEquals(100, player.getMaxHealth());
        assertEquals(15, player.getBaseAttack());
        assertEquals(5, player.getBaseDefense());
        assertEquals(inventory, player.getInventory());
        assertNull(player.getEquippedWeapon());
        assertNull(player.getEquippedArmor());
        assertNull(player.getCurrentRoom());
        assertTrue(player.isAlive());

        assertThrows(NullPointerException.class, () -> new Player(null, 100, 10, 5, inventory));
        assertThrows(NullPointerException.class, () -> new Player("Hero", 100, 10, 5, null));
    }

    @ParameterizedTest(name = "Damage={0} -> ResultHealth={1}, Alive={2}")
    @CsvSource({
        "0, 100, true",     // Zero damage
        "-10, 100, true",   // Negative damage ignored by Math.max(0, amount)
        "40, 60, true",     // Standard damage
        "99, 1, true",      // Boundary near zero
        "100, 0, false",    // Exact lethal damage
        "150, 0, false"     // Overkill clamped to 0
    })
    @DisplayName("Take damage applies clamped reductions and updates alive status")
    void testTakeDamage(int damage, int expectedHealth, boolean expectedAlive) {
        player.takeDamage(damage);
        assertEquals(expectedHealth, player.getHealth());
        assertEquals(expectedAlive, player.isAlive());
    }

    @ParameterizedTest(name = "Damage 50, Heal {0} -> Health {1}")
    @CsvSource({
        "20, 70",    // Partial heal
        "50, 100",   // Full heal to max
        "80, 100",   // Overheal clamped to maxHealth
        "0, 50",     // Zero heal
        "-20, 50"    // Negative heal ignored by Math.max(0, amount)
    })
    @DisplayName("Heal applies within [currentHealth, maxHealth] boundaries")
    void testHeal(int healAmount, int expectedHealth) {
        player.takeDamage(50);
        player.heal(healAmount);
        assertEquals(expectedHealth, player.getHealth());
    }

    @Test
    @DisplayName("Attack and Defense powers calculate correctly without and with equipment")
    void testEquipmentStatCalculations() {
        // Base stats without gear
        assertEquals(15, player.getAttackPower());
        assertEquals(5, player.getDefensePower());

        // Equip Weapon
        Weapon sword = new Weapon("Steel Sword", "A sharp blade", 10);
        player.equipWeapon(sword);
        assertEquals(sword, player.getEquippedWeapon());
        assertEquals(25, player.getAttackPower()); // 15 base + 10 bonus

        // Equip Armor
        Armor shield = new Armor("Iron Shield", "Sturdy guard", 8);
        player.equipArmor(shield);
        assertEquals(shield, player.getEquippedArmor());
        assertEquals(13, player.getDefensePower()); // 5 base + 8 bonus

        // Rejects null equipment
        assertThrows(NullPointerException.class, () -> player.equipWeapon(null));
        assertThrows(NullPointerException.class, () -> player.equipArmor(null));
    }

    @Test
    @DisplayName("Setting current room updates reference and rejects null")
    void testSetCurrentRoom() {
        Room room = new Room("R1", "Starting Hall", "Safe entrance");
        player.setCurrentRoom(room);
        assertEquals(room, player.getCurrentRoom());

        assertThrows(NullPointerException.class, () -> player.setCurrentRoom(null));
    }
}