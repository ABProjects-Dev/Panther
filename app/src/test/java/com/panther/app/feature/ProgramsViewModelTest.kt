package com.panther.app.feature

import com.panther.app.data.backup.PantherBackupJson
import com.panther.app.domain.model.*
import com.panther.app.domain.repository.PantherRepository
import com.panther.app.domain.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProgramsViewModelTest {
    private lateinit var repo: FakePantherRepository
    private lateinit var viewModel: ProgramsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        repo = FakePantherRepository()
        viewModel = ProgramsViewModel(
            repository = repo,
            observePrograms = ObserveWorkoutProgramsUseCase(repo),
            createProgram = CreateWorkoutProgramUseCase(repo),
            updateProgram = UpdateWorkoutProgramUseCase(repo),
            deleteProgram = DeleteWorkoutProgramUseCase(repo),
            activateProgram = ActivateWorkoutProgramUseCase(repo),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createActivateRenameAndDeleteProgramUpdatesStateAndInvokesCallback() = runTest {
        viewModel.create("برنامه قدرتی")
        var list = viewModel.programs.first { !it.loading }.value
        assertEquals(1, list.size)
        val id = list.single().id

        viewModel.setActive(id, true)
        assertTrue(repo.observePrograms().first().single().isActive)

        viewModel.rename(id, "برنامه حجم")
        assertEquals("برنامه حجم", repo.observePrograms().first().single().name)

        var deletedCallbackFired = false
        viewModel.delete(id) { deletedCallbackFired = true }
        assertTrue(deletedCallbackFired)
        assertTrue(repo.observePrograms().first().isEmpty())
    }

    @Test
    fun dayReorderAndMiddleDeleteKeepBackupExportValid() = runTest {
        val programId = repo.createProgram("Plan")
        viewModel.addDay(programId, "روز اول", 0)
        viewModel.addDay(programId, "روز دوم", 1)
        viewModel.addDay(programId, "روز سوم", 2)

        val initialDays = viewModel.days(programId).first().value
        assertEquals(listOf("روز اول", "روز دوم", "روز سوم"), initialDays.map { it.name })

        viewModel.reorderDays(programId, listOf(initialDays[2].id, initialDays[0].id, initialDays[1].id))
        val reordered = viewModel.days(programId).first().value
        assertEquals(listOf("روز سوم", "روز اول", "روز دوم"), reordered.map { it.name })

        // Delete middle day and add a new day; backup JSON encoding must remain valid.
        viewModel.deleteDay(reordered[1].id)
        viewModel.addDay(programId, "روز چهارم", 2)

        val backup = repo.createBackup()
        val json = PantherBackupJson.encode(backup)
        val decoded = PantherBackupJson.decode(json)
        assertEquals(listOf(0, 1, 2), decoded.programs.single().days.map { it.sortOrder })
    }

    @Test
    fun saveExerciseInPlaceAndClearErrorBehavePredictably() = runTest {
        val programId = repo.createProgram("Plan")
        val dayId = repo.addDay(programId, "Day 1", 0)
        viewModel.saveExercise(
            dayId = dayId,
            exerciseId = null,
            name = "اسکوات",
            order = 0,
            sets = listOf(ExerciseSet(exerciseId = 0, setNumber = 1, targetType = TargetType.REPS, targetValue = 10)),
        )
        val created = viewModel.exercisesWithSets(dayId).first().value.single()
        assertEquals("اسکوات", created.exercise.name)
        assertEquals(listOf(10), created.sets.map { it.targetValue })

        // Simulated simulated rotation-safe edit: exerciseId is preserved -> updates in place without duplicating.
        viewModel.saveExercise(
            dayId = dayId,
            exerciseId = created.exercise.id,
            name = "اسکوات از جلو",
            order = 0,
            sets = listOf(
                ExerciseSet(exerciseId = created.exercise.id, setNumber = 1, targetType = TargetType.REPS, targetValue = 8),
                ExerciseSet(exerciseId = created.exercise.id, setNumber = 2, targetType = TargetType.REPS, targetValue = 6),
            ),
        )
        val updatedList = viewModel.exercisesWithSets(dayId).first().value
        assertEquals(1, updatedList.size)
        assertEquals("اسکوات از جلو", updatedList.single().exercise.name)
        assertEquals(listOf(8, 6), updatedList.single().sets.map { it.targetValue })

        // Invalid program name sets error, and clearError resets it.
        viewModel.create("   ")
        assertTrue(viewModel.error.value)
        viewModel.clearError()
        assertFalse(viewModel.error.value)
    }
}

private class FakePantherRepository : PantherRepository {
    private var nextId = 1L
    private val profileFlow = MutableStateFlow<UserProfile?>(null)
    private val programsFlow = MutableStateFlow<List<WorkoutProgram>>(emptyList())
    private val daysFlow = MutableStateFlow<List<TrainingDay>>(emptyList())
    private val exercisesFlow = MutableStateFlow<List<Exercise>>(emptyList())
    private val setsFlow = MutableStateFlow<List<ExerciseSet>>(emptyList())

    override fun observeProfile(): Flow<UserProfile?> = profileFlow

    override suspend fun saveProfile(profile: UserProfile) {
        require(WorkoutValidation.profile(profile))
        profileFlow.value = profile.copy(name = profile.name.trim())
    }

    override fun observePrograms(): Flow<List<WorkoutProgram>> = programsFlow

    override suspend fun createProgram(name: String): Long {
        require(WorkoutValidation.name(name))
        require(programsFlow.value.size < PantherBackup.MAX_PROGRAMS)
        val id = nextId++
        val program = WorkoutProgram(id, name.trim(), 1_700_000_000_000L, false, 0, 0)
        programsFlow.value = listOf(program) + programsFlow.value
        return id
    }

    override suspend fun renameProgram(id: Long, name: String) {
        require(WorkoutValidation.name(name))
        check(programsFlow.value.any { it.id == id })
        programsFlow.value = programsFlow.value.map { if (it.id == id) it.copy(name = name.trim()) else it }
    }

    override suspend fun deleteProgram(id: Long) {
        check(programsFlow.value.any { it.id == id })
        val removedDayIds = daysFlow.value.filter { it.programId == id }.map { it.id }.toSet()
        val removedExIds = exercisesFlow.value.filter { it.trainingDayId in removedDayIds }.map { it.id }.toSet()
        setsFlow.value = setsFlow.value.filterNot { it.exerciseId in removedExIds }
        exercisesFlow.value = exercisesFlow.value.filterNot { it.id in removedExIds }
        daysFlow.value = daysFlow.value.filterNot { it.id in removedDayIds }
        programsFlow.value = programsFlow.value.filterNot { it.id == id }
    }

    override suspend fun activateProgram(id: Long) {
        check(programsFlow.value.any { it.id == id })
        programsFlow.value = programsFlow.value.map { it.copy(isActive = it.id == id) }
    }

    override suspend fun deactivateProgram(id: Long) {
        check(programsFlow.value.any { it.id == id })
        programsFlow.value = programsFlow.value.map { if (it.id == id) it.copy(isActive = false) else it }
    }

    override fun observeDays(programId: Long): Flow<List<TrainingDay>> =
        daysFlow.map { list -> list.filter { it.programId == programId }.sortedWith(compareBy({ it.sortOrder }, { it.id })) }

    override fun observeExercises(dayId: Long): Flow<List<Exercise>> =
        exercisesFlow.map { list -> list.filter { it.trainingDayId == dayId }.sortedWith(compareBy({ it.sortOrder }, { it.id })) }

    override fun observeExercisesWithSets(dayId: Long): Flow<List<ExerciseWithSets>> =
        exercisesFlow.map { list ->
            list.filter { it.trainingDayId == dayId }
                .sortedWith(compareBy({ it.sortOrder }, { it.id }))
                .map { ex ->
                    ExerciseWithSets(
                        exercise = ex,
                        sets = setsFlow.value.filter { it.exerciseId == ex.id }.sortedBy { it.setNumber },
                    )
                }
        }

    override fun observeSets(exerciseId: Long): Flow<List<ExerciseSet>> =
        setsFlow.map { list -> list.filter { it.exerciseId == exerciseId }.sortedBy { it.setNumber } }

    override suspend fun addDay(programId: Long, name: String, order: Int): Long {
        require(WorkoutValidation.name(name) && order >= 0)
        check(programsFlow.value.any { it.id == programId })
        val existing = daysFlow.value.filter { it.programId == programId }.sortedWith(compareBy({ it.sortOrder }, { it.id }))
        require(existing.size < PantherBackup.MAX_DAYS_PER_PROGRAM)
        val compacted = existing.mapIndexed { idx, d -> d.copy(sortOrder = idx) }
        val newDay = TrainingDay(nextId++, programId, name.trim(), compacted.size)
        daysFlow.value = daysFlow.value.filterNot { it.programId == programId } + compacted + newDay
        recount(programId)
        return newDay.id
    }

    override suspend fun renameDay(id: Long, name: String) {
        require(WorkoutValidation.name(name))
        daysFlow.value = daysFlow.value.map { if (it.id == id) it.copy(name = name.trim()) else it }
    }

    override suspend fun deleteDay(id: Long) {
        val target = checkNotNull(daysFlow.value.firstOrNull { it.id == id })
        val exIds = exercisesFlow.value.filter { it.trainingDayId == id }.map { it.id }.toSet()
        setsFlow.value = setsFlow.value.filterNot { it.exerciseId in exIds }
        exercisesFlow.value = exercisesFlow.value.filterNot { it.id in exIds }
        val remaining = daysFlow.value
            .filter { it.programId == target.programId && it.id != id }
            .sortedWith(compareBy({ it.sortOrder }, { it.id }))
            .mapIndexed { idx, d -> d.copy(sortOrder = idx) }
        daysFlow.value = daysFlow.value.filterNot { it.programId == target.programId } + remaining
        recount(target.programId)
    }

    override suspend fun reorderDays(programId: Long, ids: List<Long>) {
        val existing = daysFlow.value.filter { it.programId == programId }
        require(ids.size == existing.size && ids.toSet() == existing.map { it.id }.toSet())
        val byId = existing.associateBy { it.id }
        val reordered = ids.mapIndexed { idx, id -> byId.getValue(id).copy(sortOrder = idx) }
        daysFlow.value = daysFlow.value.filterNot { it.programId == programId } + reordered
    }

    override suspend fun saveExercise(dayId: Long, exerciseId: Long?, name: String, order: Int, sets: List<ExerciseSet>) {
        require(WorkoutValidation.name(name) && order >= 0)
        require(sets.all { it.exerciseId == (exerciseId ?: 0L) } && WorkoutValidation.sets(sets))
        val day = checkNotNull(daysFlow.value.firstOrNull { it.id == dayId })
        val id = if (exerciseId == null) {
            val existing = exercisesFlow.value.filter { it.trainingDayId == dayId }.sortedWith(compareBy({ it.sortOrder }, { it.id }))
            require(existing.size < PantherBackup.MAX_EXERCISES_PER_DAY)
            val compacted = existing.mapIndexed { idx, e -> e.copy(sortOrder = idx) }
            val created = Exercise(nextId++, dayId, name.trim(), compacted.size)
            exercisesFlow.value = exercisesFlow.value.filterNot { it.trainingDayId == dayId } + compacted + created
            created.id
        } else {
            exercisesFlow.value = exercisesFlow.value.map { if (it.id == exerciseId) it.copy(name = name.trim()) else it }
            exerciseId
        }
        setsFlow.value = setsFlow.value.filterNot { it.exerciseId == id } + sets.mapIndexed { idx, s ->
            s.copy(id = nextId++, exerciseId = id, setNumber = idx + 1)
        }
        recount(day.programId)
    }

    override suspend fun deleteExercise(id: Long) {
        val ex = checkNotNull(exercisesFlow.value.firstOrNull { it.id == id })
        val day = checkNotNull(daysFlow.value.firstOrNull { it.id == ex.trainingDayId })
        setsFlow.value = setsFlow.value.filterNot { it.exerciseId == id }
        val remaining = exercisesFlow.value
            .filter { it.trainingDayId == day.id && it.id != id }
            .sortedWith(compareBy({ it.sortOrder }, { it.id }))
            .mapIndexed { idx, e -> e.copy(sortOrder = idx) }
        exercisesFlow.value = exercisesFlow.value.filterNot { it.trainingDayId == day.id } + remaining
        recount(day.programId)
    }

    override suspend fun reorderExercises(dayId: Long, ids: List<Long>) {
        val existing = exercisesFlow.value.filter { it.trainingDayId == dayId }
        require(ids.size == existing.size && ids.toSet() == existing.map { it.id }.toSet())
        val byId = existing.associateBy { it.id }
        val reordered = ids.mapIndexed { idx, id -> byId.getValue(id).copy(sortOrder = idx) }
        exercisesFlow.value = exercisesFlow.value.filterNot { it.trainingDayId == dayId } + reordered
    }

    override suspend fun createProgramBackup(programId: Long): ProgramBackup? {
        val p = programsFlow.value.firstOrNull { it.id == programId } ?: return null
        val days = daysFlow.value.filter { it.programId == p.id }
            .sortedWith(compareBy({ it.sortOrder }, { it.id }))
            .mapIndexed { dIdx, d ->
                DayBackup(
                    name = d.name,
                    sortOrder = dIdx,
                    exercises = exercisesFlow.value.filter { it.trainingDayId == d.id }
                        .sortedWith(compareBy({ it.sortOrder }, { it.id }))
                        .mapIndexed { eIdx, e ->
                            ExerciseBackup(
                                name = e.name,
                                sortOrder = eIdx,
                                sets = setsFlow.value.filter { it.exerciseId == e.id }
                                    .sortedBy { it.setNumber }
                                    .mapIndexed { sIdx, s ->
                                        SetBackup(sIdx + 1, s.targetType, s.targetValue, s.targetMinutes, s.targetSeconds)
                                    },
                            )
                        },
                )
            }
        return ProgramBackup(p.name, p.createdAt, p.isActive, days, p.id)
    }

    override suspend fun createBackup(): PantherBackup = PantherBackup(
        profile = profileFlow.value,
        programs = programsFlow.value.mapNotNull { createProgramBackup(it.id) },
    )

    override suspend fun restoreBackup(backup: PantherBackup) {
        require(WorkoutValidation.backup(backup))
        profileFlow.value = backup.profile
    }

    private fun recount(programId: Long) {
        val dIds = daysFlow.value.filter { it.programId == programId }.map { it.id }.toSet()
        val exCount = exercisesFlow.value.count { it.trainingDayId in dIds }
        programsFlow.value = programsFlow.value.map {
            if (it.id == programId) it.copy(dayCount = dIds.size, exerciseCount = exCount) else it
        }
    }
}
