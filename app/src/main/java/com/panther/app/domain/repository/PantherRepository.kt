package com.panther.app.domain.repository

import com.panther.app.domain.model.*
import kotlinx.coroutines.flow.Flow

interface PantherRepository {
    fun observeProfile(): Flow<UserProfile?>
    suspend fun saveProfile(profile: UserProfile)
    fun observePrograms(): Flow<List<WorkoutProgram>>
    suspend fun createProgram(name: String): Long
    /**
     * Creates the room for a new program before its name is chosen. The row carries
     * [WorkoutDefaults.PENDING_PROGRAM_NAME] until the user names it, and is deleted otherwise.
     */
    suspend fun createPendingProgram(): Long
    suspend fun renameProgram(id: Long, name: String)
    suspend fun deleteProgram(id: Long)
    suspend fun activateProgram(id: Long)
    suspend fun deactivateProgram(id: Long)
    fun observeDays(programId: Long): Flow<List<TrainingDay>>
    fun observeExercises(dayId: Long): Flow<List<Exercise>>
    fun observeExercisesWithSets(dayId: Long): Flow<List<ExerciseWithSets>>
    fun observeSets(exerciseId: Long): Flow<List<ExerciseSet>>
    suspend fun addDay(programId: Long, name: String, order: Int): Long
    suspend fun renameDay(id: Long, name: String)
    suspend fun deleteDay(id: Long)
    suspend fun reorderDays(programId: Long, ids: List<Long>)
    suspend fun saveExercise(dayId: Long, exerciseId: Long?, name: String, order: Int?, sets: List<ExerciseSet>)
    suspend fun deleteExercise(id: Long)
    suspend fun reorderExercises(dayId: Long, ids: List<Long>)
    /** Device-only exercise-name catalogue; never part of [PantherBackup]. */
    fun observeExerciseNames(): Flow<List<String>>
    suspend fun exerciseNamesNow(): List<String>
    /** Records [name] unless it is blank or already known (case- and space-insensitive). */
    suspend fun rememberExerciseName(name: String)
    suspend fun createProgramBackup(programId: Long): ProgramBackup?
    suspend fun createBackup(): PantherBackup
    suspend fun restoreBackup(backup: PantherBackup)
}
