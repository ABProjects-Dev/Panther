package com.panther.app.domain.repository

import com.panther.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface PantherRepository {
    fun observeProfile(): Flow<UserProfile?>
    suspend fun saveProfile(profile: UserProfile)
    fun observePrograms(): Flow<List<WorkoutProgram>>
    suspend fun createProgram(name: String): Long
    suspend fun renameProgram(id: Long, name: String)
    suspend fun deleteProgram(id: Long)
    suspend fun activateProgram(id: Long)
    suspend fun deactivateProgram(id: Long)
    suspend fun archiveProgram(id: Long, archived: Boolean)
    fun observeDays(programId: Long): Flow<List<TrainingDay>>
    fun observeExercises(dayId: Long): Flow<List<Exercise>>
    fun observeSets(exerciseId: Long): Flow<List<ExerciseSet>>
    suspend fun addDay(programId: Long, name: String, order: Int): Long
    suspend fun renameDay(id: Long, name: String)
    suspend fun deleteDay(id: Long)
    suspend fun reorderDays(programId: Long, ids: List<Long>)
    suspend fun addExercise(dayId: Long, name: String, order: Int): Long
    suspend fun renameExercise(id: Long, name: String)
    suspend fun deleteExercise(id: Long)
    suspend fun reorderExercises(dayId: Long, ids: List<Long>)
    suspend fun replaceSets(exerciseId: Long, sets: List<ExerciseSet>)
}
