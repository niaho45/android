package com.example.gym.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.time.LocalDate;

public class TrainingCycleTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 29);

    @Test
    public void noRecordStartsAtChest() {
        assertEquals(TrainingCycle.CHEST, TrainingCycle.resolve(null, null, TODAY));
    }

    @Test
    public void sameDayKeepsCompletedPart() {
        assertEquals(TrainingCycle.CHEST,
                TrainingCycle.resolve(TrainingCycle.CHEST, TODAY.toString(), TODAY));
    }

    @Test
    public void legsYesterdayIsRestDay() {
        assertEquals(TrainingCycle.REST,
                TrainingCycle.resolve(TrainingCycle.LEGS, TODAY.minusDays(1).toString(), TODAY));
    }

    @Test
    public void legsTwoDaysAgoStartsNextRoundAtChest() {
        assertEquals(TrainingCycle.CHEST,
                TrainingCycle.resolve(TrainingCycle.LEGS, TODAY.minusDays(2).toString(), TODAY));
    }

    @Test
    public void legsLongAgoStillStartsAtChest() {
        assertEquals(TrainingCycle.CHEST,
                TrainingCycle.resolve(TrainingCycle.LEGS, TODAY.minusDays(10).toString(), TODAY));
    }

    @Test
    public void noTrainingDayDoesNotAdvanceTheCycle() {
        assertEquals(TrainingCycle.BACK,
                TrainingCycle.resolve(TrainingCycle.CHEST, TODAY.minusDays(10).toString(), TODAY));
        assertEquals(TrainingCycle.LEGS,
                TrainingCycle.resolve(TrainingCycle.SHOULDER, TODAY.minusDays(5).toString(), TODAY));
    }

    @Test
    public void unknownPartFallsBackToChest() {
        assertEquals(TrainingCycle.CHEST,
                TrainingCycle.resolve("未知", TODAY.minusDays(1).toString(), TODAY));
    }

    @Test
    public void nextPartFollowsCycleOrder() {
        assertEquals(TrainingCycle.BACK, TrainingCycle.nextPart(TrainingCycle.CHEST));
        assertEquals(TrainingCycle.SHOULDER, TrainingCycle.nextPart(TrainingCycle.BACK));
        assertEquals(TrainingCycle.LEGS, TrainingCycle.nextPart(TrainingCycle.SHOULDER));
        assertEquals(TrainingCycle.REST, TrainingCycle.nextPart(TrainingCycle.LEGS));
        assertEquals(TrainingCycle.CHEST, TrainingCycle.nextPart(TrainingCycle.REST));
    }

    @Test
    public void unknownNextPartFallsBackToChest() {
        assertEquals(TrainingCycle.CHEST, TrainingCycle.nextPart("未知"));
    }
}
