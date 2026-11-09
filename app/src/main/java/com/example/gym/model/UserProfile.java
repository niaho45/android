package com.example.gym.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

@Entity(
        tableName = "user_profiles",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        )
)
public class UserProfile {

    @PrimaryKey
    public long userId;

    public double currentWeight;

    @ColumnInfo(defaultValue = "1")
    public int cycleCount = 1;

    @NonNull
    @ColumnInfo(defaultValue = "''")
    public String startDate = "";

    public UserProfile() {
    }

    @Ignore
    public UserProfile(long userId, double currentWeight) {
        this.userId = userId;
        this.currentWeight = currentWeight;
        this.cycleCount = 1;
        this.startDate = LocalDate.now().toString();
    }
}
