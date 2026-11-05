package com.panther.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserProfileEntity::class, WorkoutProgramEntity::class, TrainingDayEntity::class, ExerciseEntity::class, ExerciseSetEntity::class], version = 1, exportSchema = true)
abstract class PantherDatabase : RoomDatabase() {
    abstract fun dao(): PantherDao
}
