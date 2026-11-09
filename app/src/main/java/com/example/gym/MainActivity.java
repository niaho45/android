package com.example.gym;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.example.gym.db.FitnessRepository;
import com.example.gym.model.HomeData;
import com.example.gym.util.AppDialog;
import com.example.gym.util.Prefs;
import com.example.gym.util.TrainingCycle;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private long userId;
    private double currentWeight;
    private String currentBodyPart;
    private boolean restDay = true;
    private boolean trainedToday;
    private double todayCalories;

    private TextView tvDate;
    private TextView tvTrainingState;
    private TextView tvBodyPart;
    private TextView tvWeight;
    private TextView tvDailyCalories;
    private TextView tvRestHint;
    private TextView tvCycleCount;
    private TextView tvTodayCalories;
    private View rowTodayCalories;
    private AppCompatButton btnStartTraining;
    private FitnessRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userId = Prefs.getUserId(this);
        if (userId <= 0) {
            goToLogin();
            return;
        }

        setContentView(R.layout.activity_main);
        repository = FitnessRepository.get(this);

        tvDate = findViewById(R.id.tvDate);
        tvTrainingState = findViewById(R.id.tvTrainingState);
        tvBodyPart = findViewById(R.id.tvBodyPart);
        tvWeight = findViewById(R.id.tvWeight);
        tvDailyCalories = findViewById(R.id.tvDailyCalories);
        tvRestHint = findViewById(R.id.tvRestHint);
        tvCycleCount = findViewById(R.id.tvCycleCount);
        tvTodayCalories = findViewById(R.id.tvTodayCalories);
        rowTodayCalories = findViewById(R.id.rowTodayCalories);
        btnStartTraining = findViewById(R.id.btnStartTraining);
        MaterialButton btnEditWeight = findViewById(R.id.btnEditWeight);
        TextView tvLogout = findViewById(R.id.tvLogout);

        updateDate();
        btnStartTraining.setEnabled(false);

        btnEditWeight.setOnClickListener(v -> showEditWeightDialog());
        btnStartTraining.setOnClickListener(v -> startTraining());
        tvLogout.setOnClickListener(v -> showLogoutDialog());
    }

    @Override
    protected void onResume() {
        super.onResume();
        userId = Prefs.getUserId(this);
        if (userId <= 0) {
            goToLogin();
            return;
        }
        updateDate();
        loadDashboard();
    }

    private void loadDashboard() {
        repository.loadHome(userId, data -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (data == null) {
                Toast.makeText(this, R.string.toast_operation_failed, Toast.LENGTH_SHORT).show();
                return;
            }
            applyDashboard(data);
        });
    }

    private void applyDashboard(HomeData data) {
        currentWeight = data.weight;
        currentBodyPart = data.bodyPart;
        restDay = data.restDay;
        trainedToday = data.trainedToday;
        todayCalories = data.todayCalories;

        tvWeight.setText(getString(R.string.format_weight, data.weight));
        tvDailyCalories.setText(getString(R.string.format_calories, data.dailyCalories));
        tvCycleCount.setText(getString(R.string.format_cycle, data.cycleCount));

        if (trainedToday) {
            tvTrainingState.setText(R.string.today_completed);
            showTomorrowTarget(data.bodyPart);
            tvRestHint.setVisibility(View.GONE);
            tvTodayCalories.setText(getString(R.string.format_calories, data.todayCalories));
            rowTodayCalories.setVisibility(View.VISIBLE);
            btnStartTraining.setEnabled(false);
            btnStartTraining.setText(R.string.action_training_finished);
        } else {
            tvTrainingState.setText(R.string.today_training_goal);
            tvBodyPart.setText(data.bodyPart);
            tvRestHint.setVisibility(restDay ? View.VISIBLE : View.GONE);
            rowTodayCalories.setVisibility(View.GONE);
            btnStartTraining.setEnabled(!restDay);
            btnStartTraining.setText(R.string.action_start_training);
        }
    }

    private void showTomorrowTarget(String currentPart) {
        String prefix = getString(R.string.tomorrow_prefix);
        String nextPart = getNextBodyPart(currentPart);
        SpannableString text = new SpannableString(prefix + nextPart);
        int prefixEnd = prefix.length();
        text.setSpan(new RelativeSizeSpan(0.55f), 0, prefixEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(
                        ContextCompat.getColor(this, R.color.text_secondary)),
                0, prefixEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        text.setSpan(new ForegroundColorSpan(
                        ContextCompat.getColor(this, R.color.green)),
                prefixEnd, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvBodyPart.setText(text);
    }

    private String getNextBodyPart(String currentPart) {
        return TrainingCycle.nextPart(currentPart);
    }

    private void updateDate() {
        LocalDate now = LocalDate.now();
        String date = now.format(DateTimeFormatter.ofPattern("yyyy年M月d日", Locale.CHINA));
        String weekday = now.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.CHINA);
        tvDate.setText(date + " " + weekday);
    }

    private void startTraining() {
        if (restDay || trainedToday || currentBodyPart == null) {
            return;
        }
        Intent intent = new Intent(this, TrainingActivity.class);
        intent.putExtra(TrainingActivity.EXTRA_BODY_PART, currentBodyPart);
        startActivity(intent);
    }

    private void showEditWeightDialog() {
        if (currentWeight <= 0) {
            Toast.makeText(this, R.string.toast_weight_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_weight, null);
        EditText input = view.findViewById(R.id.etWeightInput);
        TextView confirm = view.findViewById(R.id.btnDialogConfirm);
        input.setText(String.format(Locale.US, "%.1f", currentWeight));
        input.setSelection(input.getText().length());

        Dialog dialog = AppDialog.show(this, view);
        view.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());
        confirm.setOnClickListener(v -> {
            double weight;
            try {
                weight = Double.parseDouble(input.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(this, R.string.toast_weight_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
            if (weight < 20 || weight > 300) {
                Toast.makeText(this, R.string.toast_weight_invalid, Toast.LENGTH_SHORT).show();
                return;
            }

            setConfirmEnabled(confirm, false);
            repository.updateWeight(userId, weight, success -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                setConfirmEnabled(confirm, true);
                if (Boolean.TRUE.equals(success)) {
                    dialog.dismiss();
                    Toast.makeText(this, R.string.toast_weight_updated, Toast.LENGTH_SHORT).show();
                    loadDashboard();
                } else {
                    Toast.makeText(this, R.string.toast_operation_failed, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void showLogoutDialog() {
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.logout_title)
                .setMessage(R.string.logout_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_exit, (d, which) -> {
                    Prefs.clear(this);
                    goToLogin();
                })
                .create();
        dialog.show();
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85f);
            window.setAttributes(params);
        }
    }

    private void setConfirmEnabled(TextView confirm, boolean enabled) {
        confirm.setEnabled(enabled);
        confirm.setAlpha(enabled ? 1f : 0.5f);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
