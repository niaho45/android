package com.example.gym.db;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.gym.model.CustomExercise;
import com.example.gym.model.Exercise;
import com.example.gym.model.HomeData;
import com.example.gym.model.TrainingItem;
import com.example.gym.model.TrainingRecord;
import com.example.gym.model.User;
import com.example.gym.model.UserProfile;
import com.example.gym.util.Md5Utils;
import com.example.gym.util.TrainingCycle;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FitnessRepository {

    public interface Callback<T> {
        void onResult(T result);
    }

    public static final double DEFAULT_WEIGHT = 60.0;
    public static final double DAILY_CALORIE_MULTIPLIER = 30.0;
    public static final double CALORIE_COEFFICIENT = 0.02;

    private static volatile FitnessRepository instance;

    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private FitnessRepository(Context context) {
        db = AppDatabase.get(context);
    }

    public static FitnessRepository get(Context context) {
        if (instance == null) {
            synchronized (FitnessRepository.class) {
                if (instance == null) {
                    instance = new FitnessRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public void login(String account, String rawPassword, Callback<User> callback) {
        executor.execute(() -> {
            User user = db.userDao().login(account, Md5Utils.md5(rawPassword));
            deliver(callback, user);
        });
    }

    public void findUserByAccount(String account, Callback<User> callback) {
        executor.execute(() -> deliver(callback, db.userDao().findByAccount(account)));
    }

    public void findUserById(long userId, Callback<User> callback) {
        executor.execute(() -> deliver(callback, db.userDao().findById(userId)));
    }

    public void register(String account, String rawPassword, String question, String answer,
                         Callback<Long> callback) {
        executor.execute(() -> {
            try {
                long userId = db.runInTransaction(() -> {
                    if (db.userDao().findByAccount(account) != null) {
                        return -1L;
                    }
                    User user = new User(account, Md5Utils.md5(rawPassword), question, answer);
                    long id = db.userDao().insert(user);
                    UserProfile profile = new UserProfile(id, DEFAULT_WEIGHT);
                    profile.cycleCount = 1;
                    profile.startDate = LocalDate.now().toString();
                    db.userProfileDao().upsert(profile);
                    return id;
                });
                deliver(callback, userId);
            } catch (Exception e) {
                deliver(callback, -1L);
            }
        });
    }

    public void updatePassword(long userId, String rawPassword, Callback<Boolean> callback) {
        executor.execute(() -> {
            try {
                int rows = db.userDao().updatePassword(userId, Md5Utils.md5(rawPassword));
                deliver(callback, rows > 0);
            } catch (Exception e) {
                deliver(callback, false);
            }
        });
    }

    public void updateWeight(long userId, double weight, Callback<Boolean> callback) {
        executor.execute(() -> {
            try {
                int rows = db.userProfileDao().updateWeight(userId, weight);
                if (rows == 0) {
                    db.userProfileDao().upsert(new UserProfile(userId, weight));
                }
                deliver(callback, true);
            } catch (Exception e) {
                deliver(callback, false);
            }
        });
    }

    public void getWeight(long userId, Callback<Double> callback) {
        executor.execute(() -> {
            try {
                UserProfile profile = db.userProfileDao().getProfile(userId);
                if (profile == null) {
                    profile = new UserProfile(userId, DEFAULT_WEIGHT);
                    db.userProfileDao().upsert(profile);
                }
                deliver(callback, profile.currentWeight);
            } catch (Exception e) {
                deliver(callback, null);
            }
        });
    }

    public void loadHome(long userId, Callback<HomeData> callback) {
        executor.execute(() -> {
            try {
                UserProfile profile = db.userProfileDao().getProfile(userId);
                if (profile == null) {
                    profile = new UserProfile(userId, DEFAULT_WEIGHT);
                    db.userProfileDao().upsert(profile);
                }
                boolean changed = false;
                if (profile.startDate == null || profile.startDate.isEmpty()) {
                    profile.startDate = LocalDate.now().toString();
                    changed = true;
                }
                if (profile.cycleCount < 1) {
                    profile.cycleCount = 1;
                    changed = true;
                }
                if (changed) {
                    db.userProfileDao().upsert(profile);
                }
                TrainingRecord lastRecord = db.trainingRecordDao().getLastRecord(userId);
                String bodyPart = TrainingCycle.resolve(
                        lastRecord == null ? null : lastRecord.bodyPart,
                        lastRecord == null ? null : lastRecord.date,
                        LocalDate.now()
                );
                String today = LocalDate.now().toString();
                boolean trainedToday =
                        db.trainingRecordDao().isTrainedToday(userId, today) > 0;
                double todayCalories =
                        db.trainingRecordDao().getTodayTotalCalories(userId, today);
                HomeData data = new HomeData(
                        profile.currentWeight,
                        bodyPart,
                        profile.currentWeight * DAILY_CALORIE_MULTIPLIER,
                        TrainingCycle.isRest(bodyPart),
                        profile.cycleCount,
                        trainedToday,
                        todayCalories
                );
                deliver(callback, data);
            } catch (Exception e) {
                deliver(callback, null);
            }
        });
    }

    public void loadLastTraining(long userId, String bodyPart,
                                 Callback<List<TrainingItem>> callback) {
        executor.execute(() -> {
            try {
                List<TrainingRecord> records =
                        db.trainingRecordDao().getLatestRecordsForBodyPart(userId, bodyPart);
                List<TrainingItem> items = new ArrayList<>();
                for (TrainingRecord record : records) {
                    items.add(new TrainingItem(record.exerciseName,
                            record.equipment == null ? "" : record.equipment,
                            record.getWeight(), record.sets, record.reps));
                }
                deliver(callback, items);
            } catch (Exception e) {
                deliver(callback, new ArrayList<>());
            }
        });
    }

    public void loadLastExerciseValues(long userId, String exerciseName,
                                       Callback<TrainingItem> callback) {
        executor.execute(() -> {
            try {
                TrainingRecord record =
                        db.trainingRecordDao().getLatestRecordForExercise(userId, exerciseName);
                if (record == null) {
                    deliver(callback, null);
                } else {
                    deliver(callback, new TrainingItem(record.exerciseName,
                            record.equipment == null ? "" : record.equipment,
                            record.getWeight(), record.sets, record.reps));
                }
            } catch (Exception e) {
                deliver(callback, null);
            }
        });
    }

    public void loadCustomExercises(long userId, Callback<List<Exercise>> callback) {
        executor.execute(() -> {
            try {
                List<CustomExercise> rows = db.customExerciseDao().getForUser(userId);
                List<Exercise> exercises = new ArrayList<>();
                for (CustomExercise row : rows) {
                    exercises.add(new Exercise(row.name, row.bodyPart, row.equipment));
                }
                deliver(callback, exercises);
            } catch (Exception e) {
                deliver(callback, new ArrayList<>());
            }
        });
    }

    public void saveCustomExercise(long userId, String name, String equipment, String bodyPart,
                                   Callback<Boolean> callback) {
        executor.execute(() -> {
            try {
                db.customExerciseDao().insert(
                        new CustomExercise(userId, name, equipment, bodyPart));
                deliver(callback, true);
            } catch (Exception e) {
                deliver(callback, false);
            }
        });
    }

    public void saveTraining(long userId, String bodyPart, List<TrainingItem> items,
                             double weight, Callback<Double> callback) {
        List<TrainingItem> snapshot = new ArrayList<>(items);
        executor.execute(() -> {
            try {
                String date = LocalDate.now().toString();
                List<TrainingRecord> records = new ArrayList<>();
                double totalCalories = 0.0;
                for (TrainingItem item : snapshot) {
                    double calories = item.sets * item.reps * weight * CALORIE_COEFFICIENT;
                    totalCalories += calories;
                    records.add(new TrainingRecord(
                            userId,
                            date,
                            bodyPart,
                            item.name,
                            item.equipment == null ? "" : item.equipment,
                            item.weight,
                            item.sets,
                            item.reps,
                            calories
                    ));
                }
                db.runInTransaction(() -> {
                    boolean alreadyCompletedLegsToday = TrainingCycle.LEGS.equals(bodyPart)
                            && db.trainingRecordDao()
                            .countRecordsForDateAndPart(userId, bodyPart, date) > 0;
                    db.trainingRecordDao().insertAll(records);
                    if (TrainingCycle.LEGS.equals(bodyPart) && !alreadyCompletedLegsToday) {
                        db.userProfileDao().incrementCycleCount(userId);
                    }
                });
                deliver(callback, totalCalories);
            } catch (Exception e) {
                deliver(callback, -1.0);
            }
        });
    }

    private <T> void deliver(Callback<T> callback, T value) {
        if (callback == null) {
            return;
        }
        mainHandler.post(() -> callback.onResult(value));
    }
}
