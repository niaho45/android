package com.example.gym.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.gym.model.TrainingRecord;

import java.util.List;

@Dao
public interface TrainingRecordDao {

    @Insert
    void insertAll(List<TrainingRecord> records);

    @Query("SELECT * FROM training_records WHERE userId = :userId ORDER BY date DESC, id DESC LIMIT 1")
    TrainingRecord getLastRecord(long userId);

    @Query("SELECT * FROM training_records WHERE userId = :userId AND bodyPart = :bodyPart "
            + "AND date = (SELECT MAX(date) FROM training_records "
            + "WHERE userId = :userId AND bodyPart = :bodyPart) ORDER BY id ASC")
    List<TrainingRecord> getLatestRecordsForBodyPart(long userId, String bodyPart);

    @Query("SELECT * FROM training_records WHERE userId = :userId AND exerciseName = :exerciseName "
            + "ORDER BY date DESC, id DESC LIMIT 1")
    TrainingRecord getLatestRecordForExercise(long userId, String exerciseName);

    @Query("SELECT COUNT(*) FROM training_records "
            + "WHERE userId = :userId AND bodyPart = :bodyPart AND date = :date")
    int countRecordsForDateAndPart(long userId, String bodyPart, String date);

    @Query("SELECT COUNT(*) FROM training_records "
            + "WHERE userId = :userId AND date = :date")
    int isTrainedToday(long userId, String date);

    @Query("SELECT COALESCE(SUM(caloriesBurned), 0) FROM training_records "
            + "WHERE userId = :userId AND date = :date")
    double getTodayTotalCalories(long userId, String date);
}
