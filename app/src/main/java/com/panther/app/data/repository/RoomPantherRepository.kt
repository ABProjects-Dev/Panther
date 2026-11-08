package com.panther.app.data.repository

import androidx.room.withTransaction
import com.panther.app.data.local.*
import com.panther.app.domain.model.*
import com.panther.app.domain.repository.PantherRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.map

class RoomPantherRepository @Inject constructor(private val db: PantherDatabase) : PantherRepository {
    private val dao get() = db.dao()

    override fun observeProfile() = dao.profile().map { it?.toDomain() }

    override suspend fun saveProfile(profile: UserProfile) {
        require(WorkoutValidation.profile(profile))
        dao.saveProfile(profile.toEntity())
    }

    override fun observePrograms() = dao.programs().map { list ->
        list.map { WorkoutProgram(it.id, it.name, it.createdAt, it.isActive, it.dayCount, it.exerciseCount) }
    }

    override suspend fun createProgram(name: String): Long = db.withTransaction {
        require(WorkoutValidation.name(name))
        require(dao.programCount() < PantherBackup.MAX_PROGRAMS) { "Maximum number of programs reached" }
        dao.insertProgram(WorkoutProgramEntity(name = name.trim(), createdAt = System.currentTimeMillis()))
    }

    override suspend fun renameProgram(id: Long, name: String) {
        require(WorkoutValidation.name(name))
        check(dao.renameProgram(id, name.trim()) == 1)
    }

    override suspend fun deleteProgram(id: Long) {
        check(dao.deleteProgram(id) == 1)
    }

    override suspend fun activateProgram(id: Long) = db.withTransaction {
        checkNotNull(dao.program(id))
        dao.clearActive()
        check(dao.setActive(id) == 1)
    }

    override suspend fun deactivateProgram(id: Long) {
        check(dao.deactivate(id) == 1)
    }

    override fun observeDays(programId: Long) =
        dao.days(programId).map { it.map { d -> TrainingDay(d.id, d.programId, d.name, d.sortOrder) } }

    override fun observeExercises(dayId: Long) =
        dao.exercises(dayId).map { it.map { e -> Exercise(e.id, e.trainingDayId, e.name, e.sortOrder) } }

    override fun observeExercisesWithSets(dayId: Long) =
        dao.exercisesWithSets(dayId).map { rows ->
            rows.map { row ->
                ExerciseWithSets(
                    exercise = Exercise(
                        id = row.exercise.id,
                        trainingDayId = row.exercise.trainingDayId,
                        name = row.exercise.name,
                        sortOrder = row.exercise.sortOrder,
                    ),
                    sets = row.sets.sortedBy { it.setNumber }.map { it.toDomain() },
                )
            }
        }

    override fun observeSets(exerciseId: Long) =
        dao.sets(exerciseId).map { it.map { s -> s.toDomain() } }

    override suspend fun addDay(programId: Long, name: String, order: Int): Long = db.withTransaction {
        require(WorkoutValidation.name(name) && order >= 0)
        checkNotNull(dao.program(programId))
        val existingIds = dao.dayIdsOrdered(programId)
        require(existingIds.size < PantherBackup.MAX_DAYS_PER_PROGRAM) { "Maximum number of training days reached" }
        existingIds.forEachIndexed { index, existingId -> dao.orderDay(programId, existingId, index) }
        dao.insertDay(TrainingDayEntity(programId = programId, name = name.trim(), sortOrder = existingIds.size))
    }

    override suspend fun renameDay(id: Long, name: String) {
        require(WorkoutValidation.name(name))
        check(dao.renameDay(id, name.trim()) == 1)
    }

    override suspend fun deleteDay(id: Long) = db.withTransaction {
        val existing = checkNotNull(dao.day(id))
        check(dao.deleteDay(id) == 1)
        dao.dayIdsOrdered(existing.programId).forEachIndexed { index, remainingId ->
            dao.orderDay(existing.programId, remainingId, index)
        }
    }

    override suspend fun reorderDays(programId: Long, ids: List<Long>) = db.withTransaction {
        val existing = dao.dayIdsOrdered(programId)
        require(ids.size == existing.size && ids.toSet() == existing.toSet())
        ids.forEachIndexed { index, id -> check(dao.orderDay(programId, id, index) == 1) }
    }

    override suspend fun saveExercise(dayId: Long, exerciseId: Long?, name: String, order: Int, sets: List<ExerciseSet>) {
        require(WorkoutValidation.name(name) && order >= 0)
        require(sets.all { it.exerciseId == (exerciseId ?: 0L) } && WorkoutValidation.sets(sets))
        db.withTransaction {
            checkNotNull(dao.day(dayId))
            val id = if (exerciseId == null) {
                val existingIds = dao.exerciseIdsOrdered(dayId)
                require(existingIds.size < PantherBackup.MAX_EXERCISES_PER_DAY) { "Maximum number of exercises reached" }
                existingIds.forEachIndexed { index, existingId -> dao.orderExercise(dayId, existingId, index) }
                dao.insertExercise(ExerciseEntity(trainingDayId = dayId, name = name.trim(), sortOrder = existingIds.size))
            } else {
                val current = checkNotNull(dao.exercise(exerciseId))
                require(current.trainingDayId == dayId) { "Exercise does not belong to day" }
                check(dao.renameExercise(exerciseId, name.trim()) == 1)
                exerciseId
            }
            dao.deleteSets(id)
            dao.insertSets(
                sets.mapIndexed { index, set ->
                    ExerciseSetEntity(
                        exerciseId = id,
                        setNumber = index + 1,
                        targetType = set.targetType.name,
                        targetValue = set.targetValue,
                        targetMinutes = set.targetMinutes,
                        targetSeconds = set.targetSeconds,
                    )
                },
            )
        }
    }

    override suspend fun deleteExercise(id: Long) = db.withTransaction {
        val existing = checkNotNull(dao.exercise(id))
        check(dao.deleteExercise(id) == 1)
        dao.exerciseIdsOrdered(existing.trainingDayId).forEachIndexed { index, remainingId ->
            dao.orderExercise(existing.trainingDayId, remainingId, index)
        }
    }

    override suspend fun reorderExercises(dayId: Long, ids: List<Long>) = db.withTransaction {
        val existing = dao.exerciseIdsOrdered(dayId)
        require(ids.size == existing.size && ids.toSet() == existing.toSet())
        ids.forEachIndexed { index, id -> check(dao.orderExercise(dayId, id, index) == 1) }
    }

    override suspend fun createProgramBackup(programId: Long): ProgramBackup? = db.withTransaction {
        val program = dao.program(programId) ?: return@withTransaction null
        buildProgramBackup(program)
    }

    override suspend fun createBackup(): PantherBackup = db.withTransaction {
        val profile = dao.profileNow()?.toDomain()
        val programs = dao.allPrograms().map { program -> buildProgramBackup(program) }
        PantherBackup(profile = profile, programs = programs)
    }

    private suspend fun buildProgramBackup(program: WorkoutProgramEntity): ProgramBackup {
        val days = dao.allDays(program.id).mapIndexed { dayIndex, day ->
            DayBackup(
                name = day.name,
                sortOrder = dayIndex,
                exercises = dao.allExercises(day.id).mapIndexed { exerciseIndex, exercise ->
                    ExerciseBackup(
                        name = exercise.name,
                        sortOrder = exerciseIndex,
                        sets = dao.allSets(exercise.id).mapIndexed { setIndex, set ->
                            SetBackup(
                                setNumber = setIndex + 1,
                                targetType = TargetType.valueOf(set.targetType),
                                targetValue = set.targetValue,
                                targetMinutes = set.targetMinutes,
                                targetSeconds = set.targetSeconds,
                            )
                        },
                    )
                },
            )
        }
        return ProgramBackup(program.name, program.createdAt, program.isActive, days, program.id)
    }

    override suspend fun restoreBackup(backup: PantherBackup) {
        require(WorkoutValidation.backup(backup))
        db.withTransaction {
            dao.clearSets()
            dao.clearExercises()
            dao.clearDays()
            dao.clearPrograms()
            dao.clearProfile()
            backup.profile?.let { dao.saveProfile(it.toEntity()) }
            backup.programs.forEach { program ->
                val programId = dao.insertProgram(
                    WorkoutProgramEntity(
                        name = program.name.trim(),
                        createdAt = program.createdAt,
                        isActive = program.isActive,
                        isArchived = false,
                    ),
                )
                program.days.sortedBy { it.sortOrder }.forEachIndexed { dayIndex, day ->
                    val dayId = dao.insertDay(
                        TrainingDayEntity(
                            programId = programId,
                            name = day.name.trim(),
                            sortOrder = dayIndex,
                        ),
                    )
                    day.exercises.sortedBy { it.sortOrder }.forEachIndexed { exerciseIndex, exercise ->
                        val exerciseId = dao.insertExercise(
                            ExerciseEntity(
                                trainingDayId = dayId,
                                name = exercise.name.trim(),
                                sortOrder = exerciseIndex,
                            ),
                        )
                        dao.insertSets(
                            exercise.sets.sortedBy { it.setNumber }.mapIndexed { setIndex, set ->
                                ExerciseSetEntity(
                                    exerciseId = exerciseId,
                                    setNumber = setIndex + 1,
                                    targetType = set.targetType.name,
                                    targetValue = set.targetValue,
                                    targetMinutes = set.targetMinutes,
                                    targetSeconds = set.targetSeconds,
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

private fun UserProfileEntity.toDomain() = UserProfile(name = name, age = age, heightCm = heightCm, weightKg = weightKg)
private fun UserProfile.toEntity() = UserProfileEntity(name = name.trim(), profileImageUri = null, age = age, heightCm = heightCm, weightKg = weightKg)
private fun ExerciseSetEntity.toDomain() = ExerciseSet(id, exerciseId, setNumber, TargetType.valueOf(targetType), targetValue, targetMinutes, targetSeconds)
