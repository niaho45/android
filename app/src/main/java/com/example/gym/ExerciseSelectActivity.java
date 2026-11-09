package com.example.gym;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gym.adapter.CategoryAdapter;
import com.example.gym.adapter.ExerciseAdapter;
import com.example.gym.data.ExerciseLibrary;
import com.example.gym.db.FitnessRepository;
import com.example.gym.model.Exercise;
import com.example.gym.util.AppDialog;
import com.example.gym.util.Prefs;
import com.example.gym.util.TrainingCycle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class ExerciseSelectActivity extends AppCompatActivity {

    public static final String EXTRA_CURRENT_BODY_PART = "extra_current_body_part";
    public static final String EXTRA_NAME = "extra_exercise_name";
    public static final String EXTRA_EQUIPMENT = "extra_exercise_equipment";
    public static final String EXTRA_WEIGHT = "extra_exercise_weight";
    public static final String EXTRA_SETS = "extra_exercise_sets";
    public static final String EXTRA_REPS = "extra_exercise_reps";

    private final List<Exercise> allExercises = new ArrayList<>();

    private EditText etSearchExercises;
    private TextView tvEmptyExercises;
    private ExerciseAdapter exerciseAdapter;
    private List<String> categories;
    private String currentCategory;
    private String currentBodyPart;
    private long userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_select);

        userId = Prefs.getUserId(this);
        if (userId <= 0) {
            finish();
            return;
        }

        findViewById(R.id.btnBackSelect).setOnClickListener(v -> finish());

        currentBodyPart = getIntent().getStringExtra(EXTRA_CURRENT_BODY_PART);
        categories = Arrays.asList(
                getString(R.string.category_all),
                getString(R.string.category_chest),
                getString(R.string.category_back),
                getString(R.string.category_shoulder),
                getString(R.string.category_legs)
        );

        int initialPosition = 0;
        if (currentBodyPart != null) {
            for (int i = 1; i < categories.size(); i++) {
                if (categories.get(i).equals(currentBodyPart)) {
                    initialPosition = i;
                    break;
                }
            }
        }
        currentCategory = categories.get(initialPosition);

        RecyclerView rvCategories = findViewById(R.id.rvCategories);
        CategoryAdapter categoryAdapter = new CategoryAdapter(categories, initialPosition,
                position -> {
                    currentCategory = categories.get(position);
                    applyFilter();
                });
        rvCategories.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(categoryAdapter);

        tvEmptyExercises = findViewById(R.id.tvEmptyExercises);
        etSearchExercises = findViewById(R.id.etSearchExercises);

        exerciseAdapter = new ExerciseAdapter(this::showSetsRepsDialog);
        RecyclerView rvExercises = findViewById(R.id.rvExercises);
        rvExercises.setLayoutManager(new GridLayoutManager(this, 2));
        rvExercises.setAdapter(exerciseAdapter);

        etSearchExercises.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                applyFilter();
            }
        });

        findViewById(R.id.btnCustomExercise).setOnClickListener(v -> showCustomExerciseDialog());

        allExercises.addAll(ExerciseLibrary.getAll());
        applyFilter();
        loadCustomExercises();
    }

    private void loadCustomExercises() {
        FitnessRepository.get(this).loadCustomExercises(userId, customExercises -> {
            if (isFinishing() || isDestroyed()) {
                return;
            }
            if (customExercises != null && !customExercises.isEmpty()) {
                allExercises.addAll(customExercises);
                applyFilter();
            }
        });
    }

    private void applyFilter() {
        String query = etSearchExercises.getText().toString().trim().toLowerCase(Locale.ROOT);
        String allCategory = getString(R.string.category_all);
        List<Exercise> filtered = new ArrayList<>();
        for (Exercise exercise : allExercises) {
            if (!allCategory.equals(currentCategory)
                    && !exercise.bodyPart.equals(currentCategory)) {
                continue;
            }
            if (!query.isEmpty()) {
                boolean nameMatches =
                        exercise.name.toLowerCase(Locale.ROOT).contains(query);
                boolean equipmentMatches =
                        exercise.equipment.toLowerCase(Locale.ROOT).contains(query);
                if (!nameMatches && !equipmentMatches) {
                    continue;
                }
            }
            filtered.add(exercise);
        }
        exerciseAdapter.submitList(filtered);
        tvEmptyExercises.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showSetsRepsDialog(Exercise exercise) {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_exercise, null);
        TextView title = view.findViewById(R.id.tvDialogTitle);
        EditText etSets = view.findViewById(R.id.etSets);
        EditText etReps = view.findViewById(R.id.etReps);
        EditText etWeight = view.findViewById(R.id.etWeight);
        TextView confirm = view.findViewById(R.id.btnDialogConfirm);
        title.setText(exercise.name);

        Dialog dialog = AppDialog.show(this, view);
        view.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());

        FitnessRepository.get(this).loadLastExerciseValues(userId, exercise.name, last -> {
            if (isFinishing() || isDestroyed() || !dialog.isShowing() || last == null) {
                return;
            }
            if (last.weight > 0 && etWeight.getText().toString().trim().isEmpty()) {
                etWeight.setText(String.format(Locale.US, "%.1f", last.weight));
                etWeight.selectAll();
            }
        });

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
            dialog.dismiss();
            returnExercise(exercise.name, exercise.equipment, weight, sets, reps);
        });
    }

    private void showCustomExerciseDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_custom_exercise, null);
        EditText etName = view.findViewById(R.id.etCustomName);
        EditText etEquipment = view.findViewById(R.id.etCustomEquipment);
        EditText etSets = view.findViewById(R.id.etCustomSets);
        EditText etReps = view.findViewById(R.id.etCustomReps);
        EditText etWeight = view.findViewById(R.id.etCustomWeight);
        TextView confirm = view.findViewById(R.id.btnDialogConfirm);

        TextView chipChest = view.findViewById(R.id.chipChest);
        TextView chipBack = view.findViewById(R.id.chipBack);
        TextView chipShoulder = view.findViewById(R.id.chipShoulder);
        TextView chipLegs = view.findViewById(R.id.chipLegs);

        String defaultPart = TrainingCycle.CHEST;
        if (isBodyPart(currentBodyPart)) {
            defaultPart = currentBodyPart;
        } else if (isBodyPart(currentCategory)) {
            defaultPart = currentCategory;
        }

        final String[] selectedPart = {defaultPart};
        TextView[] chips = {chipChest, chipBack, chipShoulder, chipLegs};
        String[] parts = {TrainingCycle.CHEST, TrainingCycle.BACK,
                TrainingCycle.SHOULDER, TrainingCycle.LEGS};
        for (int i = 0; i < chips.length; i++) {
            final int index = i;
            chips[i].setSelected(parts[i].equals(selectedPart[0]));
            chips[i].setOnClickListener(v -> {
                selectedPart[0] = parts[index];
                for (int j = 0; j < chips.length; j++) {
                    chips[j].setSelected(j == index);
                }
            });
        }

        Dialog dialog = AppDialog.show(this, view);
        view.findViewById(R.id.btnDialogCancel).setOnClickListener(v -> dialog.dismiss());
        confirm.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, R.string.toast_exercise_name_empty,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            String equipment = etEquipment.getText().toString().trim();
            if (equipment.isEmpty()) {
                equipment = getString(R.string.custom_equipment_default);
            }
            Integer sets = parsePositiveInt(etSets);
            Integer reps = parsePositiveInt(etReps);
            Float weight = parseWeight(etWeight);
            if (sets == null || reps == null || sets > 20 || reps > 100) {
                Toast.makeText(this, R.string.toast_sets_reps_invalid,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (weight == null) {
                Toast.makeText(this, R.string.toast_weight_value_invalid,
                        Toast.LENGTH_SHORT).show();
                return;
            }

            String finalEquipment = equipment;
            setConfirmEnabled(confirm, false);
            FitnessRepository.get(this).saveCustomExercise(
                    userId, name, finalEquipment, selectedPart[0], success -> {
                        if (isFinishing() || isDestroyed()) {
                            return;
                        }
                        if (Boolean.TRUE.equals(success)) {
                            dialog.dismiss();
                            returnExercise(name, finalEquipment, weight, sets, reps);
                        } else {
                            setConfirmEnabled(confirm, true);
                            Toast.makeText(this, R.string.toast_operation_failed,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }

    private boolean isBodyPart(String value) {
        return TrainingCycle.CHEST.equals(value)
                || TrainingCycle.BACK.equals(value)
                || TrainingCycle.SHOULDER.equals(value)
                || TrainingCycle.LEGS.equals(value);
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

    private void setConfirmEnabled(TextView confirm, boolean enabled) {
        confirm.setEnabled(enabled);
        confirm.setAlpha(enabled ? 1f : 0.5f);
    }

    private void returnExercise(String name, String equipment, float weight,
                                int sets, int reps) {
        Intent data = new Intent();
        data.putExtra(EXTRA_NAME, name);
        data.putExtra(EXTRA_EQUIPMENT, equipment);
        data.putExtra(EXTRA_WEIGHT, weight);
        data.putExtra(EXTRA_SETS, sets);
        data.putExtra(EXTRA_REPS, reps);
        setResult(RESULT_OK, data);
        finish();
    }
}
