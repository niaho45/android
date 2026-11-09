package com.example.gym.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.gym.model.CustomExercise;

import java.util.List;

@Dao
public interface CustomExerciseDao {

    @Insert
    long insert(CustomExercise exercise);

    @Query("SELECT * FROM custom_exercises WHERE userId = :userId ORDER BY id ASC")
    List<CustomExercise> getForUser(long userId);
}
