package com.example.gym.util;

import android.content.Context;
import android.content.SharedPreferences;

public final class Prefs {

    private static final String FILE_NAME = "fitness_prefs";
    private static final String KEY_USER_ID = "current_user_id";

    private Prefs() {
    }

    public static void saveUserId(Context context, long userId) {
        preferences(context).edit().putLong(KEY_USER_ID, userId).apply();
    }

    public static long getUserId(Context context) {
        return preferences(context).getLong(KEY_USER_ID, -1L);
    }

    public static void clear(Context context) {
        preferences(context).edit().clear().apply();
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }
}
