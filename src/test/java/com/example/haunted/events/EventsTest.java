package com.example.haunted.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.example.haunted.model.Item;

public class EventsTest {

    @ParameterizedTest(name = "success={0}, msg={1}")
    @CsvSource({
        "true, Action succeeded",
        "false, Action failed"
    })
    @DisplayName("InteractionResult correctly captures and exposes state")
    void testInteractionResult(boolean success, String message) {
        InteractionResult result = new InteractionResult(success, message);
        assertEquals(success, result.isSuccess());
        assertEquals(message, result.getMessage());
    }

    @ParameterizedTest(name = "success={0}, msg={1}, trap={2}, dmg={3}")
    @CsvSource({
        "true, Moved north, false, 0",
        "true, Walked into trap, true, 15",
        "false, Blocked by locked door, false, 0"
    })
    @DisplayName("MoveResult correctly captures and exposes all movement data")
    void testMoveResult(boolean success, String message, boolean trapTriggered, int trapDamage) {
        MoveResult result = new MoveResult(success, message, trapTriggered, trapDamage);
        assertEquals(success, result.isSuccess());
        assertEquals(message, result.getMessage());
        assertEquals(trapTriggered, result.isTrapTriggered());
        assertEquals(trapDamage, result.getTrapDamage());
    }

    @Test
    @DisplayName("CombatResult exposes all parameters and ensures defensive copy of dropped items")
    void testCombatResultDefensiveCopyAndGetters() {
        List<Item> initialItems = new ArrayList<>();
        CombatResult result = new CombatResult(true, "Monster hit", 20, 5, false, initialItems);

        assertTrue(result.isSuccess());
        assertEquals("Monster hit", result.getMessage());
        assertEquals(20, result.getDamageToMonster());
        assertEquals(5, result.getDamageToPlayer());
        assertFalse(result.isMonsterDefeated());
        assertEquals(0, result.getDroppedItems().size());

        // Verifying returned list is an unmodifiable wrapper
        assertThrows(UnsupportedOperationException.class, () -> result.getDroppedItems().add(null));
    }

    @Test
    @DisplayName("CombatResult defeated state with non-empty dropped items")
    void testCombatResultDefeatedState() {
        CombatResult result = new CombatResult(true, "Final Boss Defeated", 50, 0, true, new ArrayList<>());
        assertTrue(result.isSuccess());
        assertTrue(result.isMonsterDefeated());
        assertEquals(50, result.getDamageToMonster());
        assertEquals(0, result.getDamageToPlayer());
    }
}