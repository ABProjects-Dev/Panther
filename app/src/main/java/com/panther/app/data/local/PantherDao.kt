package com.panther.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PantherDao {
    @Query("SELECT * FROM profile WHERE id = 1") fun profile(): Flow<UserProfileEntity?>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun saveProfile(profile: UserProfileEntity)
    @Query("""SELECT p.*, (SELECT COUNT(*) FROM training_days d WHERE d.programId=p.id) AS dayCount,
        (SELECT COUNT(*) FROM exercises e INNER JOIN training_days d ON e.trainingDayId=d.id WHERE d.programId=p.id) AS exerciseCount
        FROM programs p ORDER BY p.createdAt DESC, p.id DESC""") fun programs(): Flow<List<ProgramSummaryRow>>
    @Query("SELECT * FROM programs WHERE id = :id") suspend fun program(id: Long): WorkoutProgramEntity?
    @Insert suspend fun insertProgram(program: WorkoutProgramEntity): Long
    @Query("UPDATE programs SET name = :name WHERE id = :id") suspend fun renameProgram(id: Long, name: String): Int
    @Query("DELETE FROM programs WHERE id = :id") suspend fun deleteProgram(id: Long): Int
    @Query("UPDATE programs SET isActive = 0 WHERE isActive = 1") suspend fun clearActive()
    @Query("UPDATE programs SET isActive = 1 WHERE id = :id AND isArchived = 0") suspend fun setActive(id: Long): Int
    @Query("UPDATE programs SET isActive = 0 WHERE id = :id") suspend fun deactivate(id: Long): Int
    @Query("UPDATE programs SET isArchived = :archived, isActive = 0 WHERE id = :id") suspend fun setArchived(id: Long, archived: Boolean): Int
    @Query("SELECT * FROM training_days WHERE programId = :id ORDER BY sortOrder, id") fun days(id: Long): Flow<List<TrainingDayEntity>>
    @Query("SELECT * FROM exercises WHERE trainingDayId = :id ORDER BY sortOrder, id") fun exercises(id: Long): Flow<List<ExerciseEntity>>
    @Query("SELECT * FROM exercise_sets WHERE exerciseId = :id ORDER BY setNumber") fun sets(id: Long): Flow<List<ExerciseSetEntity>>
    @Insert suspend fun insertDay(day: TrainingDayEntity): Long
    @Query("UPDATE training_days SET name = :name WHERE id = :id") suspend fun renameDay(id: Long, name: String): Int
    @Query("DELETE FROM training_days WHERE id = :id") suspend fun deleteDay(id: Long): Int
    @Query("SELECT id FROM training_days WHERE programId = :programId") suspend fun dayIds(programId: Long): List<Long>
    @Query("UPDATE training_days SET sortOrder = :order WHERE id = :id AND programId = :programId") suspend fun orderDay(programId: Long, id: Long, order: Int): Int
    @Insert suspend fun insertExercise(exercise: ExerciseEntity): Long
    @Query("UPDATE exercises SET name = :name WHERE id = :id") suspend fun renameExercise(id: Long, name: String): Int
    @Query("DELETE FROM exercises WHERE id = :id") suspend fun deleteExercise(id: Long): Int
    @Query("SELECT id FROM exercises WHERE trainingDayId = :dayId") suspend fun exerciseIds(dayId: Long): List<Long>
    @Query("UPDATE exercises SET sortOrder = :order WHERE id = :id AND trainingDayId = :dayId") suspend fun orderExercise(dayId: Long, id: Long, order: Int): Int
    @Query("DELETE FROM exercise_sets WHERE exerciseId = :exerciseId") suspend fun deleteSets(exerciseId: Long)
    @Insert suspend fun insertSets(sets: List<ExerciseSetEntity>)
}
