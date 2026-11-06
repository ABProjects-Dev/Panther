package com.panther.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Schema version 1. Exported schemas live in `app/schemas`; every future version bump
 * must ship an explicit migration and a schema JSON committed with the change.
 */
@Database(
    entities = [
        UserProfileEntity::class,
        WorkoutProgramEntity::class,
        TrainingDayEntity::class,
        ExerciseEntity::class,
        ExerciseSetEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class PantherDatabase : RoomDatabase() {
    abstract fun dao(): PantherDao
}
