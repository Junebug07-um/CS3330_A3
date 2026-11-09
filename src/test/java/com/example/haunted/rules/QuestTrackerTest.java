package com.example.haunted.rules;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.example.haunted.model.Item;
import com.example.haunted.model.Monster;
import com.example.haunted.model.Quest;
import com.example.haunted.model.QuestItem;

public class QuestTrackerTest {

    private QuestTracker tracker;
    private Quest quest;

    @BeforeEach
    void setUp() {
        tracker = new QuestTracker();
        quest = new Quest("Escape Basement", "Recover gradebook and defeat the phantom");
    }

    @Test
    @DisplayName("Null item does not mark gradebook recovered")
    void testNullItemDoesNotAdvanceQuest() {
        tracker.updateQuestForItem(quest, null);
        assertFalse(quest.isGradebookRecovered());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Key", "Health Potion", "Old Textbook"})
    @DisplayName("Non-target items do not mark gradebook recovered")
    void testNonTargetItemsDoNotAdvanceQuest(String itemName) {
        Item item = new QuestItem(itemName, "A regular item");
        tracker.updateQuestForItem(quest, item);
        assertFalse(quest.isGradebookRecovered());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Lost Gradebook", "lost gradebook", "LOST GRADEBOOK"})
    @DisplayName("Recovering Lost Gradebook with case insensitivity updates quest")
    void testLostGradebookRecoversQuest(String itemName) {
        Item gradebook = new QuestItem(itemName, "The legendary book");
        tracker.updateQuestForItem(quest, gradebook);
        assertTrue(quest.isGradebookRecovered());
    }

    @Test
    @DisplayName("Null monster does not mark phantom defeated")
    void testNullMonsterDoesNotAdvanceQuest() {
        tracker.updateQuestForMonster(quest, null);
        assertFalse(quest.isPhantomDefeated());
    }

    @Test
    @DisplayName("Living Final Exam Phantom does not mark phantom defeated")
    void testLivingPhantomDoesNotAdvanceQuest() {
        Monster phantom = new Monster("Final Exam Phantom", 100, 20, 5, Collections.emptyList());
        tracker.updateQuestForMonster(quest, phantom);
        assertFalse(quest.isPhantomDefeated());
    }

    @Test
    @DisplayName("Defeating different monster does not mark phantom defeated")
    void testDefeatingOtherMonsterDoesNotAdvanceQuest() {
        Monster ta = new Monster("Sleep-Deprived TA", 30, 10, 2, Collections.emptyList());
        ta.takeDamage(30);
        assertFalse(ta.isAlive());

        tracker.updateQuestForMonster(quest, ta);
        assertFalse(quest.isPhantomDefeated());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Final Exam Phantom", "final exam phantom", "FINAL EXAM PHANTOM"})
    @DisplayName("Defeating Final Exam Phantom marks phantom defeated")
    void testDefeatedFinalExamPhantomAdvancesQuest(String monsterName) {
        Monster phantom = new Monster(monsterName, 100, 20, 5, Collections.emptyList());
        phantom.takeDamage(100);
        assertFalse(phantom.isAlive());

        tracker.updateQuestForMonster(quest, phantom);
        assertTrue(quest.isPhantomDefeated());
    }
}