package com.example.gym.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.gym.model.User;

@Dao
public interface UserDao {

    @Insert
    long insert(User user);

    @Query("SELECT * FROM users WHERE account = :account LIMIT 1")
    User findByAccount(String account);

    @Query("SELECT * FROM users WHERE account = :account AND password = :password LIMIT 1")
    User login(String account, String password);

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    User findById(long userId);

    @Query("UPDATE users SET password = :newPassword WHERE id = :userId")
    int updatePassword(long userId, String newPassword);
}
