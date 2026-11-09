package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TrapTest {

    @Test
    @DisplayName("Trap instantiates and returns correct attributes")
    void testTrapAttributes() {
        Trap trap = new Trap("Live Wire", TrapType.ELECTRIC, 20, true, true);

        assertEquals("Live Wire", trap.getName());
        assertEquals(TrapType.ELECTRIC, trap.getType());
        assertEquals(20, trap.getDamage());
        assertTrue(trap.isArmed());
        assertTrue(trap.isOneTimeTrigger());
    }

    @Test
    @DisplayName("Trap disarm sets armed flag to false")
    void testDisarm() {
        Trap trap = new Trap("Hot Steam Vent", TrapType.STEAM, 5, true, false);
        assertTrue(trap.isArmed());
        trap.disarm();
        assertFalse(trap.isArmed());
    }

    @Test
    @DisplayName("Trap constructor rejects null name or type")
    void testNullConstructorArguments() {
        assertThrows(NullPointerException.class, () -> new Trap(null, TrapType.STEAM, 10, true, true));
        assertThrows(NullPointerException.class, () -> new Trap("Steam Vent", null, 10, true, true));
    }

    @Test
    @DisplayName("TrapType enum values and valueOf coverage")
    void testTrapTypeEnumCoverage() {
        assertEquals(TrapType.ELECTRIC, TrapType.valueOf("ELECTRIC"));
        assertEquals(TrapType.STEAM, TrapType.valueOf("STEAM"));
        assertEquals(2, TrapType.values().length);
    }
}
