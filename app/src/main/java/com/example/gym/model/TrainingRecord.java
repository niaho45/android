package com.example.gym.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "training_records",
        indices = {@Index(value = {"userId", "date"})},
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        )
)
public class TrainingRecord {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long userId;

    @NonNull
    public String date;

    @NonNull
    public String bodyPart;

    @NonNull
    public String exerciseName;

    @NonNull
    @ColumnInfo(defaultValue = "''")
    public String equipment = "";

    @ColumnInfo(defaultValue = "0")
    private float weight;

    public int sets;

    public int reps;

    public double caloriesBurned;

    public TrainingRecord() {
    }

    @Ignore
    public TrainingRecord(long userId, @NonNull String date, @NonNull String bodyPart,
                          @NonNull String exerciseName, @NonNull String equipment,
                          float weight, int sets, int reps, double caloriesBurned) {
        this.userId = userId;
        this.date = date;
        this.bodyPart = bodyPart;
        this.exerciseName = exerciseName;
        this.equipment = equipment;
        this.weight = weight;
        this.sets = sets;
        this.reps = reps;
        this.caloriesBurned = caloriesBurned;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }
}
