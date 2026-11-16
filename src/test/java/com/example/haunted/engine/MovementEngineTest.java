package com.example.haunted.engine;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import com.example.haunted.events.MoveResult;
import com.example.haunted.model.Direction;
import com.example.haunted.model.Inventory;
import com.example.haunted.model.Player;
import com.example.haunted.model.Room;
import com.example.haunted.model.Trap;
import com.example.haunted.model.TrapType;
import com.example.haunted.rules.TrapResolver;

public class MovementEngineTest {

    private MovementEngine engine;
    private Player player;
    private Room start;

    @BeforeEach
    void setUp() {
        engine = new MovementEngine(new TrapResolver());
        start = new Room("start", "Start Room", "Where you begin.");
        player = new Player("Hero", 50, 7, 2, new Inventory(5));
        player.setCurrentRoom(start);
    }

    private Room roomNorth(Trap trap) {
        Room target = new Room("elevator", "Elevator", "A shaft.");
        target.setTrap(trap);
        start.connect(Direction.NORTH, target);
        return target;
    }

    @ParameterizedTest
    @EnumSource(Direction.class)
    @DisplayName("Moving where there is no exit fails and the player stays put")
    void testNoExit(Direction direction) {
        MoveResult r = engine.move(player, direction);

        assertFalse(r.isSuccess());
        assertFalse(r.isTrapTriggered());
        assertSame(start, player.getCurrentRoom());
    }

    @Test
    @DisplayName("A locked room blocks movement, even one with a trap")
    void testLockedRoom() {
        Room target = roomNorth(new Trap("Wires", TrapType.ELECTRIC, 8, true, true));
        target.setLocked(true, "Key");

        MoveResult r = engine.move(player, Direction.NORTH);

        assertFalse(r.isSuccess());
        assertSame(start, player.getCurrentRoom());
        assertEquals(50, player.getHealth());
    }

    @Test
    void testPlainMove() {
        Room target = roomNorth(null);

        MoveResult r = engine.move(player, Direction.NORTH);

        assertTrue(r.isSuccess());
        assertFalse(r.isTrapTriggered());
        assertEquals(0, r.getTrapDamage());
        assertSame(target, player.getCurrentRoom());
    }

    @Test
    @DisplayName("Entering a room with an armed trap triggers it")
    void testArmedTrap() {
        Room target = roomNorth(new Trap("Wires", TrapType.ELECTRIC, 8, true, true));

        MoveResult r = engine.move(player, Direction.NORTH);

        assertTrue(r.isSuccess());
        assertTrue(r.isTrapTriggered());
        assertEquals(8, r.getTrapDamage());
        assertEquals(42, player.getHealth());
        assertSame(target, player.getCurrentRoom());
    }

    @Test
    @DisplayName("A disarmed trap does nothing")
    void testDisarmedTrap() {
        roomNorth(new Trap("Wires", TrapType.ELECTRIC, 8, false, true));

        MoveResult r = engine.move(player, Direction.NORTH);

        assertTrue(r.isSuccess());
        assertFalse(r.isTrapTriggered());
        assertEquals(50, player.getHealth());
    }
}