package com.example.gym.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * 打卡驱动的 5 天循环：胸 -> 背 -> 肩 -> 腿 -> 休息。
 * 未完成的部位会一直保留到完成打卡；练腿后的次日为休息日，休息日过后进入胸。
 */
public final class TrainingCycle {

    public static final String CHEST = "胸";
    public static final String BACK = "背";
    public static final String SHOULDER = "肩";
    public static final String LEGS = "腿";
    public static final String REST = "休息";

    private static final String[] CYCLE = {CHEST, BACK, SHOULDER, LEGS, REST};

    private TrainingCycle() {
    }

    public static String resolve(String lastBodyPart, String lastDate, LocalDate today) {
        if (lastBodyPart == null || lastDate == null) {
            return CHEST;
        }

        int lastIndex = indexOf(lastBodyPart);
        if (lastIndex < 0) {
            return CHEST;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(lastDate);
        } catch (DateTimeParseException e) {
            return CHEST;
        }

        long days = ChronoUnit.DAYS.between(date, today);
        if (days <= 0) {
            return lastIndex == CYCLE.length - 1 ? CHEST : lastBodyPart;
        }

        if (lastIndex == indexOf(LEGS)) {
            return days == 1 ? REST : CHEST;
        }

        return CYCLE[(lastIndex + 1) % CYCLE.length];
    }

    public static String nextPart(String bodyPart) {
        int index = indexOf(bodyPart);
        if (index < 0) {
            return CHEST;
        }
        return CYCLE[(index + 1) % CYCLE.length];
    }

    public static int indexOf(String bodyPart) {
        for (int i = 0; i < CYCLE.length; i++) {
            if (CYCLE[i].equals(bodyPart)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean isRest(String bodyPart) {
        return REST.equals(bodyPart);
    }
}
