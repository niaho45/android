package com.example.gym.db;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.gym.model.CustomExercise;
import com.example.gym.model.TrainingRecord;
import com.example.gym.model.User;
import com.example.gym.model.UserProfile;

@Database(
        entities = {User.class, UserProfile.class, TrainingRecord.class, CustomExercise.class},
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE `training_records` ADD COLUMN "
                    + "`equipment` TEXT NOT NULL DEFAULT ''");
            db.execSQL("ALTER TABLE `user_profiles` ADD COLUMN "
                    + "`cycleCount` INTEGER NOT NULL DEFAULT 1");
            db.execSQL("ALTER TABLE `user_profiles` ADD COLUMN "
                    + "`startDate` TEXT NOT NULL DEFAULT ''");
            db.execSQL("CREATE TABLE IF NOT EXISTS `custom_exercises` ("
                    + "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`userId` INTEGER NOT NULL, "
                    + "`name` TEXT NOT NULL, "
                    + "`equipment` TEXT NOT NULL, "
                    + "`bodyPart` TEXT NOT NULL, "
                    + "FOREIGN KEY(`userId`) REFERENCES `users`(`id`) "
                    + "ON UPDATE NO ACTION ON DELETE CASCADE )");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_custom_exercises_userId` "
                    + "ON `custom_exercises` (`userId`)");
        }
    };

    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE `training_records` ADD COLUMN "
                    + "`weight` REAL NOT NULL DEFAULT 0");
        }
    };

    private static volatile AppDatabase instance;

    public abstract UserDao userDao();

    public abstract UserProfileDao userProfileDao();

    public abstract TrainingRecordDao trainingRecordDao();

    public abstract CustomExerciseDao customExerciseDao();

    public static AppDatabase get(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "fitness.db"
                    ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build();
                }
            }
        }
        return instance;
    }
}
