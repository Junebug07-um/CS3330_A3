package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class MonsterTest {

    @Test
    @DisplayName("Monster constructor initializes attributes and enforces null constraints")
    void testMonsterInitialization() {
        List<Item> loot = new ArrayList<>();
        Monster monster = new Monster("Sleep-Deprived TA", 45, 12, 4, loot);

        assertEquals("Sleep-Deprived TA", monster.getName());
        assertEquals(45, monster.getHealth());
        assertEquals(45, monster.getMaxHealth());
        assertEquals(12, monster.getAttack());
        assertEquals(4, monster.getDefense());
        assertTrue(monster.isAlive());
        assertEquals(0, monster.getLoot().size());

        assertThrows(NullPointerException.class, () -> new Monster(null, 45, 12, 4, loot));
        assertThrows(NullPointerException.class, () -> new Monster("TA", 45, 12, 4, null));
    }

    @ParameterizedTest(name = "Damage={0} -> ResultHealth={1}, Alive={2}")
    @CsvSource({
        "0, 50, true",     // Zero damage
        "-15, 50, true",   // Negative damage ignored by Math.max(0, amount)
        "20, 30, true",    // Standard damage
        "49, 1, true",     // Near-lethal boundary
        "50, 0, false",    // Exact lethal damage
        "80, 0, false"     // Overkill clamped to zero
    })
    @DisplayName("Monster takeDamage boundary analysis and alive evaluation")
    void testTakeDamage(int damage, int expectedHealth, boolean expectedAlive) {
        Monster monster = new Monster("Plagiarism Ghost", 50, 10, 2, Collections.emptyList());
        monster.takeDamage(damage);

        assertEquals(expectedHealth, monster.getHealth());
        assertEquals(expectedAlive, monster.isAlive());
    }

    @Test
    @DisplayName("Monster loot list is unmodifiable and defensively copied")
    void testMonsterLootImmutability() {
        List<Item> originalList = new ArrayList<>();
        Monster monster = new Monster("Ghoul", 30, 8, 2, originalList);

        assertThrows(UnsupportedOperationException.class, () -> monster.getLoot().add(null));
    }

    @Test
    @DisplayName("BossMonster enraged attack activates at and below half health")
    void testBossMonsterEnragedMechanic() {
        // maxHealth = 100, half = 50, base attack = 20, enraged bonus = 15
        BossMonster boss = new BossMonster("Final Exam Phantom", 100, 20, 5, Collections.emptyList(), 15);

        // Health > 50 -> Base attack
        assertEquals(20, boss.getCurrentAttack());

        // Health = 51 -> Base attack boundary
        boss.takeDamage(49);
        assertEquals(51, boss.getHealth());
        assertEquals(20, boss.getCurrentAttack());

        // Health = 50 -> Enraged trigger (50 <= 50)
        boss.takeDamage(1);
        assertEquals(50, boss.getHealth());
        assertEquals(35, boss.getCurrentAttack()); // 20 + 15

        // Health < 50 -> Enraged remains active
        boss.takeDamage(20);
        assertEquals(30, boss.getHealth());
        assertEquals(35, boss.getCurrentAttack());
    }
}