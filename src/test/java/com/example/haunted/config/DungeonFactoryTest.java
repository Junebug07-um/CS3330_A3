package com.example.haunted.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.haunted.engine.GameEngine;
import com.example.haunted.model.Direction;
import com.example.haunted.model.Player;
import com.example.haunted.model.Room;

public class DungeonFactoryTest {

    private GameEngine game;

    @BeforeEach
    void setUp() {
        game = DungeonFactory.createGame();
    }

    @Test
    void testStartState() {
        Player p = game.getPlayer();
        assertEquals("Student Explorer", p.getName());
        assertEquals(50, p.getHealth());
        assertEquals(8, p.getInventory().getCapacity());
        assertEquals("stairwell", game.getCurrentRoom().getId());
        assertFalse(game.isGameWon());
    }

    @Test
    void testWorldLayout() {
        Room hall = game.getCurrentRoom().getExit(Direction.EAST);
        assertEquals("lectureHall", hall.getId());
        assertTrue(hall.getExit(Direction.NORTH).isLocked());     // exam archive
        assertNotNull(hall.getExit(Direction.SOUTH).getTrap());   // broken elevator
        Room vault = hall.getExit(Direction.NORTH).getExit(Direction.EAST);
        assertTrue(vault.getExit(Direction.NORTH).isLocked());    // final chamber
        assertTrue(vault.findItem("Vault Key").isPresent());
    }

    @Test
    void testGamesAreIndependent() {
        GameEngine other = DungeonFactory.createGame();
        game.getPlayer().takeDamage(10);
        assertEquals(50, other.getPlayer().getHealth());
    }
    
    @Test
    @DisplayName("Every room has the expected way back")
    void testReverseExits() {
        Room stairwell = game.getCurrentRoom();
        Room hall = stairwell.getExit(Direction.EAST);
        Room lab = hall.getExit(Direction.EAST);
        Room elevator = hall.getExit(Direction.SOUTH);
        Room archive = hall.getExit(Direction.NORTH);
        Room server = lab.getExit(Direction.NORTH);
        Room vault = server.getExit(Direction.EAST);
        Room finalChamber = vault.getExit(Direction.NORTH);

        assertSame(stairwell, hall.getExit(Direction.WEST));
        assertSame(hall, lab.getExit(Direction.WEST));
        assertSame(hall, elevator.getExit(Direction.NORTH));
        assertSame(hall, archive.getExit(Direction.SOUTH));
        assertSame(lab, server.getExit(Direction.SOUTH));
        assertSame(server, vault.getExit(Direction.SOUTH));
        assertSame(vault, finalChamber.getExit(Direction.SOUTH));
    }
    
    @Test
    @DisplayName("DungeonFactory is a utility class with a private constructor")
    void testPrivateConstructor() throws Exception {
        java.lang.reflect.Constructor<DungeonFactory> c = DungeonFactory.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(c.getModifiers()));
        c.setAccessible(true);
        assertNotNull(c.newInstance());
    }
}