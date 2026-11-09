package com.example.haunted.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.example.haunted.events.InteractionResult;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Player;
import com.example.haunted.model.Trap;
import com.example.haunted.model.TrapType;

public class TrapResolverTest {

    private TrapResolver resolver;
    private Player player;

    @BeforeEach
    void setUp() {
        resolver = new TrapResolver();
        player = new Player("Hero", 100, 10, 5, new Inventory(5));
    }

    @Test
    @DisplayName("Null trap returns un-triggered interaction result")
    void testResolveNullTrap() {
        InteractionResult result = resolver.resolveTrap(player, null);
        assertFalse(result.isSuccess());
        assertEquals("No trap was triggered.", result.getMessage());
        assertEquals(100, player.getHealth());
    }

    @Test
    @DisplayName("Disarmed trap returns un-triggered interaction result")
    void testResolveDisarmedTrap() {
        Trap trap = new Trap("Exposed Wire", TrapType.ELECTRIC, 15, false, true);
        InteractionResult result = resolver.resolveTrap(player, trap);

        assertFalse(result.isSuccess());
        assertEquals("No trap was triggered.", result.getMessage());
        assertEquals(100, player.getHealth());
    }

    @Test
    @DisplayName("Armed one-time trigger trap deals damage and disarms itself")
    void testResolveOneTimeArmedTrap() {
        Trap trap = new Trap("Bursting Pipe", TrapType.STEAM, 25, true, true);
        InteractionResult result = resolver.resolveTrap(player, trap);

        assertTrue(result.isSuccess());
        assertEquals("Trap 'Bursting Pipe' triggered for 25 damage.", result.getMessage());
        assertEquals(75, player.getHealth());
        assertFalse(trap.isArmed());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 10, 30})
    @DisplayName("Armed recurring trap deals damage but remains armed")
    void testResolveRecurringArmedTrap(int damage) {
        Trap trap = new Trap("Live Conduit", TrapType.ELECTRIC, damage, true, false);
        InteractionResult result = resolver.resolveTrap(player, trap);

        assertTrue(result.isSuccess());
        assertEquals(String.format("Trap 'Live Conduit' triggered for %d damage.", damage), result.getMessage());
        assertEquals(100 - damage, player.getHealth());
        assertTrue(trap.isArmed());
    }
}