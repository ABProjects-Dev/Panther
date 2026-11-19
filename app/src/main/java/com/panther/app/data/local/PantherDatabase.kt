package com.panther.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Schema version 2. Exported schemas live in `app/schemas`; every future version bump must ship an
 * explicit migration and a schema JSON committed with the change.
 *
 * `2` adds `exercise_catalog`, the device-only exercise-name catalogue. The migration is additive:
 * no existing table, column or row is touched, so upgrading and rolling back keep user data intact.
 */
@Database(
    entities = [
        UserProfileEntity::class,
        WorkoutProgramEntity::class,
        TrainingDayEntity::class,
        ExerciseEntity::class,
        ExerciseSetEntity::class,
        ExerciseCatalogEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class PantherDatabase : RoomDatabase() {
    abstract fun dao(): PantherDao
}

/** v1 → v2: create the catalogue table and its unique index. Additive and non-destructive. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `exercise_catalog` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`name` TEXT NOT NULL, `nameKey` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)",
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_exercise_catalog_nameKey` ON `exercise_catalog` (`nameKey`)",
        )
    }
}
