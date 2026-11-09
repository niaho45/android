package com.example.gym.model;

import java.io.Serializable;

public class TrainingItem implements Serializable {

    private static final long serialVersionUID = 1L;

    public final String name;
    public final String equipment;
    public final float weight;
    public final int sets;
    public final int reps;

    public TrainingItem(String name, String equipment, float weight, int sets, int reps) {
        this.name = name;
        this.equipment = equipment;
        this.weight = weight;
        this.sets = sets;
        this.reps = reps;
    }
}
