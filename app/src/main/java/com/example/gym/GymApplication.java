package com.example.gym;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

public class GymApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }
}
