package com.example.gym;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gym.adapter.TrainingAdapter;
import com.example.gym.db.FitnessRepository;
import com.example.gym.model.TrainingItem;
import com.example.gym.util.AppDialog;
import com.example.gym.util.Prefs;
import com.example.gym.util.TrainingCycle;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrainingActivity extends AppCompatActivity {

    public static final String EXTRA_BODY_PART = "extra_body_part";

    private static final String STATE_ITEMS = "state_training_items";

    private final List<TrainingItem> items = new ArrayList<>();

    private ActivityResultLauncher<Intent> exerciseLauncher;
    private TrainingAdapter adapter;
    private RecyclerView rvTraining;
    private View emptyTrainingView;
    private MaterialButton btnSaveTraining;

    private String bodyPart;
    private long userId;
    private boolean saving;

    @Override
    @SuppressWarnings({"deprecation", "unchecked"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userId = Prefs.getUserId(this);
        bodyPart = getIntent().getStringExtra(EXTRA_BODY_PART);
        if (userId <= 0 || bodyPart == null || bodyPart.isEmpty()
                || TrainingCycle.isRest(bodyPart)) {
            finish();
            return;
        }

        setContentView(R.layout.activity_training);

        TextView tvTrainingTitle = findViewById(R.id.tvTrainingTitle);
        tvTrainingTitle.setText(getString(R.string.format_training_title, bodyPart));

        rvTraining = findViewById(R.id.rvTraining);
        emptyTrainingView = findViewById(R.id.emptyTrainingView);
        btnSaveTraining = findViewById(R.id.btnSaveTraining);

        adapter = new TrainingAdapter(items, bodyPart,
                this::showEditExerciseDialog,
                position -> {
                    if (position < 0 || position >= items.size()) {
                        return;
                    }
                    items.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateEmptyState();
                    Toast.makeText(this, R.string.toast_exercise_removed,
                            Toast.LENGTH_SHORT).show();
                });
        rvTraining.setLayoutManager(new LinearLayoutManager(this));
        rvTraining.setAdapter(adapter);

        if (savedInstanceState != null) {
            ArrayList<TrainingItem> restored =
                    (ArrayList<TrainingItem>) savedInstanceState.getSerializable(STATE_ITEMS);
            if (restored != null) {
                items.addAll(restored);
                adapter.notifyDataSetChanged();
            }
        }
        updateEmptyState();

        exerciseLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        addExerciseFromResult(result.getData());
                    }
                });

        findViewById(R.id.btnBackTraining).setOnClickListener(v -> finish());
        findViewById(R.id.btnAddExercise).setOnClickListener(v -> openExerciseLibrary());
        btnSaveTraining.setOnClickListener(v -> saveTraining());

        if (items.isEmpty()) {
            loadHistory();
        }
    }

    private void loadHistory() {
        FitnessRepository.get(this).loadLastTraining(userId, bodyPart, history -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (history == null || history.isEmpty() || !items.isEmpty()) {
                return;
            }
            items.addAll(history);
            adapter.notifyDataSetChanged();
            updateEmptyState();
        });
    }

    private void addExerciseFromResult(Intent data) {
        String name = data.getStringExtra(ExerciseSelectActivity.EXTRA_NAME);
        String equipment = data.getStringExtra(ExerciseSelectActivity.EXTRA_EQUIPMENT);
        float weight = data.getFloatExtra(ExerciseSelectActivity.EXTRA_WEIGHT, 0f);
        int sets = data.getIntExtra(ExerciseSelectActivity.EXTRA_SETS, 0);
        int reps = data.getIntExtra(ExerciseSelectActivity.EXTRA_REPS, 0);
        if (name == null || name.trim().isEmpty() || sets <= 0 || reps <= 0) {
            return;
        }
        items.add(new TrainingItem(name, equipment == null ? "" : equipment,
                weight, sets, reps));
        adapter.notifyItemInserted(items.size() - 1);
        updateEmptyState();
        rvTraining.scrollToPosition(items.size() - 1);
    }

    private void showEditExerciseDialog(int position) {
        if (position < 0 || position >= items.size()) {
            return;
        }
        TrainingItem item = items.get(position);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_exercise, null);
        TextView title = view.findViewById(R.id.tvDialogTitle);
        TextView subtitle = view.findViewById(R.id.tvDialogSubtitle);
        EditText etSets = view.findViewById(R.id.etSets);
        EditText etReps = view.findViewById(R.id.etReps);
        EditText etWeight = view.findViewById(R.id.etWeight);
        TextView confirm = view.findViewById(R.id.btnDialogConfirm);

        title.setText(item.name);
        subtitle.setText(R.string.edit_sets_reps_subtitle);
        etSets.setText(String.valueOf(item.sets));
        etReps.setText(String.valueOf(item.reps));
        if (item.weight > 0) {
            etWeight.setText(String.format(Locale.US, "%.1f", item.weight));
        }

        Dialog dialog = AppDialog.show(this, view);
        view.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());
        confirm.setOnClickListener(v -> {
            Integer sets = parsePositiveInt(etSets);
            Integer reps = parsePositiveInt(etReps);
            Float weight = parseWeight(etWeight);
            if (sets == null || reps == null || sets > 20 || reps > 100) {
                Toast.makeText(this, R.string.toast_sets_reps_invalid, Toast.LENGTH_SHORT).show();
                return;
            }
            if (weight == null) {
                Toast.makeText(this, R.string.toast_weight_value_invalid,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            items.set(position, new TrainingItem(item.name, item.equipment,
                    weight, sets, reps));
            adapter.notifyItemChanged(position);
            dialog.dismiss();
        });
    }

    private void openExerciseLibrary() {
        Intent intent = new Intent(this, ExerciseSelectActivity.class);
        intent.putExtra(ExerciseSelectActivity.EXTRA_CURRENT_BODY_PART, bodyPart);
        exerciseLauncher.launch(intent);
    }

    private void saveTraining() {
        if (saving) {
            return;
        }
        if (items.isEmpty()) {
            Toast.makeText(this, R.string.toast_empty_training, Toast.LENGTH_SHORT).show();
            return;
        }

        saving = true;
        btnSaveTraining.setEnabled(false);
        FitnessRepository.get(this).getWeight(userId, weight -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (weight == null || weight <= 0) {
                saving = false;
                btnSaveTraining.setEnabled(true);
                Toast.makeText(this, R.string.toast_weight_unavailable, Toast.LENGTH_SHORT).show();
                return;
            }
            FitnessRepository.get(this).saveTraining(userId, bodyPart, items, weight, total -> {
                if (isFinishing() || isDestroyed()) {
                    return;
                }
                saving = false;
                btnSaveTraining.setEnabled(true);
                if (total == null || total < 0) {
                    Toast.makeText(this, R.string.toast_operation_failed, Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(this,
                        getString(R.string.toast_training_saved, total),
                        Toast.LENGTH_LONG).show();
                finish();
            });
        });
    }

    private Integer parsePositiveInt(EditText input) {
        try {
            int value = Integer.parseInt(input.getText().toString().trim());
            return value > 0 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Float parseWeight(EditText input) {
        String text = input.getText().toString().trim();
        if (text.isEmpty()) {
            return 0f;
        }
        try {
            float value = Float.parseFloat(text);
            if (!Float.isFinite(value) || value < 0) {
                return null;
            }
            return value;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void updateEmptyState() {
        boolean empty = items.isEmpty();
        emptyTrainingView.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvTraining.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_ITEMS, new ArrayList<>(items));
    }
}
