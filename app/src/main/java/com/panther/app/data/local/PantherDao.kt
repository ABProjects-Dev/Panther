package com.panther.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface PantherDao {
    @Query("SELECT * FROM profile WHERE id = 1") fun profile(): Flow<UserProfileEntity?>
    @Query("SELECT * FROM profile WHERE id = 1") suspend fun profileNow(): UserProfileEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveProfile(profile: UserProfileEntity)

    @Query("""SELECT p.id, p.name, p.createdAt, p.isActive,
        (SELECT COUNT(*) FROM training_days d WHERE d.programId=p.id) AS dayCount,
        (SELECT COUNT(*) FROM exercises e INNER JOIN training_days d ON e.trainingDayId=d.id WHERE d.programId=p.id) AS exerciseCount
        FROM programs p ORDER BY p.createdAt DESC, p.id DESC""") fun programs(): Flow<List<ProgramSummaryRow>>
    @Query("SELECT * FROM programs WHERE id = :id") suspend fun program(id: Long): WorkoutProgramEntity?
    @Query("SELECT COUNT(*) FROM programs") suspend fun programCount(): Int
    @Insert suspend fun insertProgram(program: WorkoutProgramEntity): Long
    @Query("UPDATE programs SET name = :name WHERE id = :id") suspend fun renameProgram(id: Long, name: String): Int
    @Query("DELETE FROM programs WHERE id = :id") suspend fun deleteProgram(id: Long): Int
    @Query("UPDATE programs SET isActive = 0 WHERE isActive = 1") suspend fun clearActive()
    @Query("UPDATE programs SET isActive = 1, isArchived = 0 WHERE id = :id") suspend fun setActive(id: Long): Int
    @Query("UPDATE programs SET isActive = 0 WHERE id = :id") suspend fun deactivate(id: Long): Int

    @Query("SELECT * FROM training_days WHERE programId = :id ORDER BY sortOrder, id") fun days(id: Long): Flow<List<TrainingDayEntity>>
    @Query("SELECT * FROM training_days WHERE id = :id") suspend fun day(id: Long): TrainingDayEntity?
    @Query("SELECT id FROM training_days WHERE programId = :programId ORDER BY sortOrder, id") suspend fun dayIdsOrdered(programId: Long): List<Long>
    @Insert suspend fun insertDay(day: TrainingDayEntity): Long
    @Query("UPDATE training_days SET name = :name WHERE id = :id") suspend fun renameDay(id: Long, name: String): Int
    @Query("DELETE FROM training_days WHERE id = :id") suspend fun deleteDay(id: Long): Int
    @Query("UPDATE training_days SET sortOrder = :order WHERE id = :id AND programId = :programId") suspend fun orderDay(programId: Long, id: Long, order: Int): Int

    @Query("SELECT * FROM exercises WHERE trainingDayId = :id ORDER BY sortOrder, id") fun exercises(id: Long): Flow<List<ExerciseEntity>>
    @Transaction
    @Query("SELECT * FROM exercises WHERE trainingDayId = :dayId ORDER BY sortOrder, id")
    fun exercisesWithSets(dayId: Long): Flow<List<ExerciseWithSetsRow>>
    @Query("SELECT * FROM exercises WHERE id = :id") suspend fun exercise(id: Long): ExerciseEntity?
    @Query("SELECT id FROM exercises WHERE trainingDayId = :dayId ORDER BY sortOrder, id") suspend fun exerciseIdsOrdered(dayId: Long): List<Long>
    @Insert suspend fun insertExercise(exercise: ExerciseEntity): Long
    @Query("UPDATE exercises SET name = :name WHERE id = :id") suspend fun renameExercise(id: Long, name: String): Int
    @Query("DELETE FROM exercises WHERE id = :id") suspend fun deleteExercise(id: Long): Int
    @Query("UPDATE exercises SET sortOrder = :order WHERE id = :id AND trainingDayId = :dayId") suspend fun orderExercise(dayId: Long, id: Long, order: Int): Int

    @Query("SELECT * FROM exercise_sets WHERE exerciseId = :id ORDER BY setNumber") fun sets(id: Long): Flow<List<ExerciseSetEntity>>
    @Query("DELETE FROM exercise_sets WHERE exerciseId = :exerciseId") suspend fun deleteSets(exerciseId: Long)
    @Insert suspend fun insertSets(sets: List<ExerciseSetEntity>)

    @Query("SELECT * FROM programs ORDER BY createdAt, id") suspend fun allPrograms(): List<WorkoutProgramEntity>
    @Query("SELECT * FROM training_days WHERE programId = :programId ORDER BY sortOrder, id") suspend fun allDays(programId: Long): List<TrainingDayEntity>
    @Query("SELECT * FROM exercises WHERE trainingDayId = :dayId ORDER BY sortOrder, id") suspend fun allExercises(dayId: Long): List<ExerciseEntity>
    @Query("SELECT * FROM exercise_sets WHERE exerciseId = :exerciseId ORDER BY setNumber") suspend fun allSets(exerciseId: Long): List<ExerciseSetEntity>
    @Query("DELETE FROM exercise_sets") suspend fun clearSets()
    @Query("DELETE FROM exercises") suspend fun clearExercises()
    @Query("DELETE FROM training_days") suspend fun clearDays()
    @Query("DELETE FROM programs") suspend fun clearPrograms()
    @Query("DELETE FROM profile") suspend fun clearProfile()
}
