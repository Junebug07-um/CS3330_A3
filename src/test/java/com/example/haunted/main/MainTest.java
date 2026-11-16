package com.example.haunted.main;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

import com.example.haunted.Main;

public class MainTest {
	
    @Test
    void testMainPrintsStartRoomMoveAndPickup() {
        new Main();   // covers the implicit default constructor

        PrintStream original = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));
        try {
            Main.main(new String[0]);
        } finally {
            System.setOut(original);
        }

        String[] lines = captured.toString().split(System.lineSeparator());
        assertEquals(3, lines.length);
        assertEquals("Maintenance Stairwell", lines[0]);
        assertEquals("Moved to Abandoned Lecture Hall.", lines[1]);
        assertEquals("Picked up Coffee Potion.", lines[2]);
    }
}