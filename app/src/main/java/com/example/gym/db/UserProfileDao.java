package com.example.gym.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.gym.model.UserProfile;

@Dao
public interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(UserProfile profile);

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    UserProfile getProfile(long userId);

    @Query("UPDATE user_profiles SET currentWeight = :weight WHERE userId = :userId")
    int updateWeight(long userId, double weight);

    @Query("UPDATE user_profiles SET cycleCount = cycleCount + 1 WHERE userId = :userId")
    int incrementCycleCount(long userId);
}
