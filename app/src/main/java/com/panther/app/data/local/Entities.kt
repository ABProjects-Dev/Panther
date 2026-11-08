package com.panther.app.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

/**
 * Note: `profileImageUri` and `isArchived` remain in schema v1 for backward-compatible
 * SQLite storage without requiring a destructive migration; domain models ignore them.
 */
@Entity(tableName = "profile")
data class UserProfileEntity(@PrimaryKey val id: Int = 1, val name: String, val profileImageUri: String?, val age: Int?, val heightCm: Double?, val weightKg: Double?)
@Entity(tableName = "programs")
data class WorkoutProgramEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val createdAt: Long, val isActive: Boolean = false, val isArchived: Boolean = false)
@Entity(tableName = "training_days", foreignKeys = [ForeignKey(entity = WorkoutProgramEntity::class, parentColumns = ["id"], childColumns = ["programId"], onDelete = ForeignKey.CASCADE)], indices = [Index("programId")])
data class TrainingDayEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val programId: Long, val name: String, val sortOrder: Int)
@Entity(tableName = "exercises", foreignKeys = [ForeignKey(entity = TrainingDayEntity::class, parentColumns = ["id"], childColumns = ["trainingDayId"], onDelete = ForeignKey.CASCADE)], indices = [Index("trainingDayId")])
data class ExerciseEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val trainingDayId: Long, val name: String, val sortOrder: Int)
@Entity(tableName = "exercise_sets", foreignKeys = [ForeignKey(entity = ExerciseEntity::class, parentColumns = ["id"], childColumns = ["exerciseId"], onDelete = ForeignKey.CASCADE)], indices = [Index(value = ["exerciseId", "setNumber"], unique = true)])
data class ExerciseSetEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val exerciseId: Long, val setNumber: Int, val targetType: String, val targetValue: Int?, val targetMinutes: Int?, val targetSeconds: Int?)

data class ProgramSummaryRow(val id: Long, val name: String, val createdAt: Long, val isActive: Boolean, val dayCount: Int, val exerciseCount: Int)

data class ExerciseWithSetsRow(
    @Embedded val exercise: ExerciseEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "exerciseId",
    )
    val sets: List<ExerciseSetEntity>,
)
