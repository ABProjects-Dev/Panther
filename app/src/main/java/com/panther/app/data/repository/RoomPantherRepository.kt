package com.panther.app.data.repository

import androidx.room.withTransaction
import com.panther.app.data.local.*
import com.panther.app.domain.model.*
import com.panther.app.domain.repository.PantherRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomPantherRepository @Inject constructor(private val db: PantherDatabase) : PantherRepository {
    private val dao get() = db.dao()
    override fun observeProfile() = dao.profile().map { it?.let { p -> UserProfile(p.name, p.profileImageUri, p.age, p.heightCm, p.weightKg) } }
    override suspend fun saveProfile(profile: UserProfile) {
        require(WorkoutValidation.profile(profile))
        dao.saveProfile(UserProfileEntity(name = profile.name.trim(), profileImageUri = profile.profileImageUri, age = profile.age, heightCm = profile.heightCm, weightKg = profile.weightKg))
    }
    override fun observePrograms() = dao.programs().map { list -> list.map { WorkoutProgram(it.id, it.name, it.createdAt, it.isActive, it.isArchived, it.dayCount, it.exerciseCount) } }
    override suspend fun createProgram(name: String): Long { require(WorkoutValidation.name(name)); return dao.insertProgram(WorkoutProgramEntity(name = name.trim(), createdAt = System.currentTimeMillis())) }
    override suspend fun renameProgram(id: Long, name: String) { require(WorkoutValidation.name(name)); check(dao.renameProgram(id, name.trim()) == 1) }
    override suspend fun deleteProgram(id: Long) { check(dao.deleteProgram(id) == 1) }
    override suspend fun activateProgram(id: Long) = db.withTransaction {
        val program = checkNotNull(dao.program(id)); check(!program.isArchived)
        dao.clearActive(); check(dao.setActive(id) == 1)
    }
    override suspend fun deactivateProgram(id: Long) { check(dao.deactivate(id) == 1) }
    override suspend fun archiveProgram(id: Long, archived: Boolean) { check(dao.setArchived(id, archived) == 1) }
    override fun observeDays(programId: Long) = dao.days(programId).map { it.map { d -> TrainingDay(d.id, d.programId, d.name, d.sortOrder) } }
    override fun observeExercises(dayId: Long) = dao.exercises(dayId).map { it.map { e -> Exercise(e.id, e.trainingDayId, e.name, e.sortOrder) } }
    override fun observeSets(exerciseId: Long) = dao.sets(exerciseId).map { it.map { s -> ExerciseSet(s.id, s.exerciseId, s.setNumber, TargetType.valueOf(s.targetType), s.targetValue, s.targetMinutes, s.targetSeconds) } }
    override suspend fun addDay(programId: Long, name: String, order: Int): Long { require(WorkoutValidation.name(name) && order >= 0); return dao.insertDay(TrainingDayEntity(programId = programId, name = name.trim(), sortOrder = order)) }
    override suspend fun renameDay(id: Long, name: String) { require(WorkoutValidation.name(name)); check(dao.renameDay(id, name.trim()) == 1) }
    override suspend fun deleteDay(id: Long) { check(dao.deleteDay(id) == 1) }
    override suspend fun reorderDays(programId: Long, ids: List<Long>) = db.withTransaction {
        val existing = dao.dayIds(programId)
        require(ids.size == existing.size && ids.toSet() == existing.toSet())
        ids.forEachIndexed { index, id -> check(dao.orderDay(programId, id, index) == 1) }
    }
    override suspend fun addExercise(dayId: Long, name: String, order: Int): Long { require(WorkoutValidation.name(name) && order >= 0); return dao.insertExercise(ExerciseEntity(trainingDayId = dayId, name = name.trim(), sortOrder = order)) }
    override suspend fun renameExercise(id: Long, name: String) { require(WorkoutValidation.name(name)); check(dao.renameExercise(id, name.trim()) == 1) }
    override suspend fun deleteExercise(id: Long) { check(dao.deleteExercise(id) == 1) }
    override suspend fun reorderExercises(dayId: Long, ids: List<Long>) = db.withTransaction {
        val existing = dao.exerciseIds(dayId)
        require(ids.size == existing.size && ids.toSet() == existing.toSet())
        ids.forEachIndexed { index, id -> check(dao.orderExercise(dayId, id, index) == 1) }
    }
    override suspend fun replaceSets(exerciseId: Long, sets: List<ExerciseSet>) {
        require(sets.all { it.exerciseId == exerciseId } && WorkoutValidation.sets(sets))
        db.withTransaction {
            dao.deleteSets(exerciseId)
            dao.insertSets(sets.map { ExerciseSetEntity(exerciseId = exerciseId, setNumber = it.setNumber, targetType = it.targetType.name, targetValue = it.targetValue, targetMinutes = it.targetMinutes, targetSeconds = it.targetSeconds) })
        }
    }
}
