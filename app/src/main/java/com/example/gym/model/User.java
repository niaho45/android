package com.example.gym.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "users", indices = {@Index(value = {"account"}, unique = true)})
public class User {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String account;

    @NonNull
    public String password;

    @NonNull
    public String securityQuestion;

    @NonNull
    public String securityAnswer;

    public User() {
    }

    @Ignore
    public User(@NonNull String account, @NonNull String password,
                @NonNull String securityQuestion, @NonNull String securityAnswer) {
        this.account = account;
        this.password = password;
        this.securityQuestion = securityQuestion;
        this.securityAnswer = securityAnswer;
    }
}
