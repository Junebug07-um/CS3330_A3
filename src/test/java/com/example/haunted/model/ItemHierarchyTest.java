package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class ItemHierarchyTest {

    @Test
    @DisplayName("Weapon initializes, exposes attack bonus, and enforces null guards")
    void testWeapon() {
        Weapon sword = new Weapon("Steel Sword", "A sharp blade", 15);

        assertEquals("Steel Sword", sword.getName());
        assertEquals("A sharp blade", sword.getDescription());
        assertEquals(15, sword.getAttackBonus());
        assertEquals("Steel Sword", sword.toString());
        assertTrue(sword instanceof Equippable);
        assertTrue(sword instanceof Item);

        assertThrows(NullPointerException.class, () -> new Weapon(null, "A sharp blade", 15));
        assertThrows(NullPointerException.class, () -> new Weapon("Steel Sword", null, 15));
    }

    @Test
    @DisplayName("Armor initializes, exposes defense bonus, and enforces null guards")
    void testArmor() {
        Armor shield = new Armor("Iron Shield", "A heavy shield", 10);

        assertEquals("Iron Shield", shield.getName());
        assertEquals("A heavy shield", shield.getDescription());
        assertEquals(10, shield.getDefenseBonus());
        assertEquals("Iron Shield", shield.toString());
        assertTrue(shield instanceof Equippable);
        assertTrue(shield instanceof Item);

        assertThrows(NullPointerException.class, () -> new Armor(null, "A heavy shield", 10));
        assertThrows(NullPointerException.class, () -> new Armor("Iron Shield", null, 10));
    }

    @ParameterizedTest(name = "Initial HP=50, Max HP=100, Heal={0} -> Result HP={1}")
    @CsvSource({
        "20, 70",
        "50, 100",
        "75, 100"
    })
    @DisplayName("Potion heals player accurately via Usable interface")
    void testPotionUse(int healAmount, int expectedHp) {
        Potion potion = new Potion("Health Elixir", "Restores HP", healAmount);

        assertEquals("Health Elixir", potion.getName());
        assertEquals("Restores HP", potion.getDescription());
        assertEquals(healAmount, potion.getHealingAmount());
        assertEquals("Health Elixir", potion.toString());
        assertTrue(potion instanceof Usable);
        assertTrue(potion instanceof Item);

        Player player = new Player("Hero", 100, 10, 5, new Inventory(5));
        player.takeDamage(50);
        potion.use(player);
        assertEquals(expectedHp, player.getHealth());

        assertThrows(NullPointerException.class, () -> new Potion(null, "Restores HP", healAmount));
        assertThrows(NullPointerException.class, () -> new Potion("Health Elixir", null, healAmount));
    }

    @Test
    @DisplayName("Key initializes, validates getters, and enforces null guards")
    void testKey() {
        Key key = new Key("Master Key", "Opens dungeon exit");

        assertEquals("Master Key", key.getName());
        assertEquals("Opens dungeon exit", key.getDescription());
        assertEquals("Master Key", key.toString());
        assertTrue(key instanceof Item);

        assertThrows(NullPointerException.class, () -> new Key(null, "Opens dungeon exit"));
        assertThrows(NullPointerException.class, () -> new Key("Master Key", null));
    }

    @Test
    @DisplayName("QuestItem initializes, validates getters, and enforces null guards")
    void testQuestItem() {
        QuestItem gradebook = new QuestItem("Lost Gradebook", "Contains vital course records");

        assertEquals("Lost Gradebook", gradebook.getName());
        assertEquals("Contains vital course records", gradebook.getDescription());
        assertEquals("Lost Gradebook", gradebook.toString());
        assertTrue(gradebook instanceof Item);

        assertThrows(NullPointerException.class, () -> new QuestItem(null, "Description"));
        assertThrows(NullPointerException.class, () -> new QuestItem("Lost Gradebook", null));
    }
}