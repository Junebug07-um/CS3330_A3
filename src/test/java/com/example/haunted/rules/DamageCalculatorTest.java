package com.example.haunted.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.haunted.model.BossMonster;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Monster;
import com.example.haunted.model.Player;

public class DamageCalculatorTest {

    private DamageCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DamageCalculator();
    }

    @ParameterizedTest(name = "Player Atk={0}, Monster Def={1} -> Expected Damage={2}")
    @CsvSource({
        "15, 5, 10",   // Standard damage (Atk > Def)
        "10, 9, 1",    // Boundary diff = 1
        "10, 10, 1",   // Atk == Def (tests Math.max floor of 1)
        "5, 12, 1",    // Atk < Def (tests negative diff clamped to 1)
        "0, 10, 1"     // Zero attack clamped to 1
    })
    @DisplayName("Calculate Player Damage across distinct attack and defense boundaries")
    void testCalculatePlayerDamage(int playerAttack, int monsterDefense, int expectedDamage) {
        Player player = new Player("Hero", 100, playerAttack, 0, new Inventory(5));
        Monster monster = new Monster("Ghoul", 50, 10, monsterDefense, Collections.emptyList());

        int damage = calculator.calculatePlayerDamage(player, monster);
        assertEquals(expectedDamage, damage);
    }

    @ParameterizedTest(name = "Monster Atk={0}, Player Def={1} -> Expected Damage={2}")
    @CsvSource({
        "20, 5, 15",   // Standard damage (Atk > Def)
        "10, 9, 1",    // Boundary diff = 1
        "10, 10, 1",   // Atk == Def (tests Math.max floor of 1)
        "4, 10, 1",    // Atk < Def (tests negative diff clamped to 1)
        "0, 5, 1"      // Zero attack clamped to 1
    })
    @DisplayName("Calculate Normal Monster Damage against Player")
    void testCalculateNormalMonsterDamage(int monsterAttack, int playerDefense, int expectedDamage) {
        Monster monster = new Monster("Sleep-Deprived TA", 40, monsterAttack, 5, Collections.emptyList());
        Player player = new Player("Hero", 100, 10, playerDefense, new Inventory(5));

        int damage = calculator.calculateMonsterDamage(monster, player);
        assertEquals(expectedDamage, damage);
    }

    @Test
    @DisplayName("BossMonster normal state (HP > 50%) does not receive enraged bonus")
    void testBossMonsterNormalDamage() {
        // Boss maxHealth=100, health=100 (100 > 50), attack=15, bonus=10. Player defense=5
        BossMonster boss = new BossMonster("Final Exam Phantom", 100, 15, 5, Collections.emptyList(), 10);
        Player player = new Player("Hero", 100, 10, 5, new Inventory(5));

        // Expected attack is 15. Damage = 15 - 5 = 10
        int damage = calculator.calculateMonsterDamage(boss, player);
        assertEquals(10, damage);
    }

    @Test
    @DisplayName("BossMonster enraged boundary exactly at maxHealth / 2")
    void testBossMonsterEnragedAtExactHalfHealth() {
        // Boss maxHealth=100, set current health to 50 via taking damage if needed, or instantiate
        BossMonster boss = new BossMonster("Final Exam Phantom", 100, 15, 5, Collections.emptyList(), 10);
        boss.takeDamage(50); // Brings HP to 50 (50 <= 50 -> Enraged!)

        Player player = new Player("Hero", 100, 10, 5, new Inventory(5));

        // Enraged attack = 15 + 10 = 25. Damage = 25 - 5 = 20
        int damage = calculator.calculateMonsterDamage(boss, player);
        assertEquals(20, damage);
    }

    @Test
    @DisplayName("BossMonster enraged below maxHealth / 2")
    void testBossMonsterEnragedBelowHalfHealth() {
        BossMonster boss = new BossMonster("Final Exam Phantom", 100, 15, 5, Collections.emptyList(), 10);
        boss.takeDamage(75); // HP is 25 (25 <= 50 -> Enraged!)

        Player player = new Player("Hero", 100, 10, 5, new Inventory(5));

        // Enraged attack = 15 + 10 = 25. Damage = 25 - 5 = 20
        int damage = calculator.calculateMonsterDamage(boss, player);
        assertEquals(20, damage);
    }
}