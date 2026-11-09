package com.example.haunted.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class QuestTest {

    @Test
    @DisplayName("Quest constructor initializes default status and null validation")
    void testQuestInitialization() {
        Quest quest = new Quest("Main Quest", "Survive and escape");

        assertEquals("Main Quest", quest.getName());
        assertEquals("Survive and escape", quest.getDescription());
        assertEquals(QuestStatus.NOT_STARTED, quest.getStatus());
        assertFalse(quest.isGradebookRecovered());
        assertFalse(quest.isPhantomDefeated());

        assertThrows(NullPointerException.class, () -> new Quest(null, "Description"));
        assertThrows(NullPointerException.class, () -> new Quest("Name", null));
    }

    @Test
    @DisplayName("Quest marks gradebook recovered and phantom defeated flags")
    void testQuestFlagsAndStatus() {
        Quest quest = new Quest("Escape", "Description");

        quest.markGradebookRecovered();
        assertTrue(quest.isGradebookRecovered());

        quest.markPhantomDefeated();
        assertTrue(quest.isPhantomDefeated());
    }

    @Test
    @DisplayName("QuestStatus enum values and valueOf coverage")
    void testQuestStatusEnum() {
        assertEquals(QuestStatus.NOT_STARTED, QuestStatus.valueOf("NOT_STARTED"));
        assertTrue(QuestStatus.values().length > 0);
    }
}