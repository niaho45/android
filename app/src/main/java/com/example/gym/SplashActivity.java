package com.example.gym;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.gym.db.FitnessRepository;
import com.example.gym.util.Prefs;

public class SplashActivity extends AppCompatActivity {

    private static final long DELAY_MILLIS = 2000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable navigateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        hideSystemBars();

        View root = findViewById(R.id.rootSplash);
        AlphaAnimation animation = new AlphaAnimation(0f, 1f);
        animation.setDuration(1500);
        animation.setFillAfter(true);
        root.startAnimation(animation);

        navigateRunnable = this::navigateNext;
        handler.postDelayed(navigateRunnable, DELAY_MILLIS);
    }

    private void hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(
                getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.systemBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }

    private void navigateNext() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        long userId = Prefs.getUserId(this);
        if (userId <= 0) {
            goTo(LoginActivity.class);
            return;
        }
        FitnessRepository.get(this).findUserById(userId, user -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (user == null) {
                Prefs.clear(this);
                goTo(LoginActivity.class);
            } else {
                goTo(MainActivity.class);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void goTo(Class<?> target) {
        startActivity(new Intent(this, target));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (navigateRunnable != null) {
            handler.removeCallbacks(navigateRunnable);
        }
        super.onDestroy();
    }
}
