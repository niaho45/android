package com.example.gym.data;

import com.example.gym.model.Exercise;
import com.example.gym.util.TrainingCycle;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ExerciseLibrary {

    private static final List<Exercise> EXERCISES = Collections.unmodifiableList(Arrays.asList(
            new Exercise("杠铃卧推", TrainingCycle.CHEST, "杠铃"),
            new Exercise("哑铃卧推", TrainingCycle.CHEST, "哑铃"),
            new Exercise("上斜哑铃卧推", TrainingCycle.CHEST, "哑铃"),
            new Exercise("蝴蝶机夹胸", TrainingCycle.CHEST, "器械"),
            new Exercise("俯卧撑", TrainingCycle.CHEST, "自重"),
            new Exercise("双杠臂屈伸", TrainingCycle.CHEST, "自重"),

            new Exercise("引体向上", TrainingCycle.BACK, "自重"),
            new Exercise("高位下拉", TrainingCycle.BACK, "器械"),
            new Exercise("杠铃划船", TrainingCycle.BACK, "杠铃"),
            new Exercise("坐姿划船", TrainingCycle.BACK, "器械"),
            new Exercise("单臂哑铃划船", TrainingCycle.BACK, "哑铃"),
            new Exercise("硬拉", TrainingCycle.BACK, "杠铃"),

            new Exercise("杠铃推举", TrainingCycle.SHOULDER, "杠铃"),
            new Exercise("哑铃推举", TrainingCycle.SHOULDER, "哑铃"),
            new Exercise("哑铃侧平举", TrainingCycle.SHOULDER, "哑铃"),
            new Exercise("哑铃前平举", TrainingCycle.SHOULDER, "哑铃"),
            new Exercise("面拉", TrainingCycle.SHOULDER, "绳索"),
            new Exercise("反向飞鸟", TrainingCycle.SHOULDER, "器械"),

            new Exercise("杠铃深蹲", TrainingCycle.LEGS, "杠铃"),
            new Exercise("腿举", TrainingCycle.LEGS, "器械"),
            new Exercise("俯卧腿弯举", TrainingCycle.LEGS, "器械"),
            new Exercise("腿屈伸", TrainingCycle.LEGS, "器械"),
            new Exercise("哑铃箭步蹲", TrainingCycle.LEGS, "哑铃"),
            new Exercise("站姿提踵", TrainingCycle.LEGS, "自重")
    ));

    private ExerciseLibrary() {
    }

    public static List<Exercise> getAll() {
        return EXERCISES;
    }
}
