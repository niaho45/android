package com.example.gym.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "custom_exercises",
        indices = {@Index("userId")},
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        )
)
public class CustomExercise {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long userId;

    @NonNull
    public String name;

    @NonNull
    public String equipment;

    @NonNull
    public String bodyPart;

    public CustomExercise() {
    }

    @Ignore
    public CustomExercise(long userId, @NonNull String name,
                          @NonNull String equipment, @NonNull String bodyPart) {
        this.userId = userId;
        this.name = name;
        this.equipment = equipment;
        this.bodyPart = bodyPart;
    }
}
