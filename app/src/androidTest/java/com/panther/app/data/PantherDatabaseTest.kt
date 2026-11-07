package com.panther.app.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.panther.app.data.local.PantherDatabase
import com.panther.app.data.repository.RoomPantherRepository
import com.panther.app.domain.model.DayBackup
import com.panther.app.domain.model.ExerciseBackup
import com.panther.app.domain.model.ExerciseSet
import com.panther.app.domain.model.PantherBackup
import com.panther.app.domain.model.ProgramBackup
import com.panther.app.domain.model.SetBackup
import com.panther.app.domain.model.TargetType
import com.panther.app.domain.model.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PantherDatabaseTest {
    private lateinit var db: PantherDatabase
    private lateinit var repository: RoomPantherRepository

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            PantherDatabase::class.java,
        ).build()
        repository = RoomPantherRepository(db)
    }

    @After
    fun close() = db.close()

    @Test
    fun programsCanBeCreatedUpdatedActivatedSwitchedDeactivatedAndDeleted() = runTest {
        val first = repository.createProgram("First")
        val second = repository.createProgram("Second")
        repository.renameProgram(first, "Updated")
        assertEquals("Updated", repository.observePrograms().first().first { it.id == first }.name)

        repository.activateProgram(first)
        assertEquals(listOf(first), repository.observePrograms().first().filter { it.isActive }.map { it.id })

        // Activating a second program automatically deactivates the previous one.
        repository.activateProgram(second)
        var programs = repository.observePrograms().first()
        assertEquals(listOf(second), programs.filter { it.isActive }.map { it.id })

        repository.deactivateProgram(second)
        programs = repository.observePrograms().first()
        assertFalse(programs.any { it.isActive })

        repository.deleteProgram(first)
        assertFalse(repository.observePrograms().first().any { it.id == first })
    }

    @Test
    fun cascadesAndIndependentSets() = runTest {
        val program = repository.createProgram("Plan")
        val day = repository.addDay(program, "Day", 0)
        val exercise = repository.addExercise(day, "Exercise", 0)
        repository.replaceSets(
            exercise,
            listOf(
                ExerciseSet(exerciseId = exercise, setNumber = 1, targetType = TargetType.REPS, targetValue = 8),
                ExerciseSet(exerciseId = exercise, setNumber = 2, targetType = TargetType.REPS, targetValue = 12),
            ),
        )
        assertEquals(listOf(8, 12), repository.observeSets(exercise).first().map { it.targetValue })
        val summary = repository.observePrograms().first().single()
        assertEquals(1, summary.dayCount)
        assertEquals(1, summary.exerciseCount)

        repository.deleteProgram(program)
        assertTrue(repository.observeDays(program).first().isEmpty())
        assertTrue(repository.observeExercises(day).first().isEmpty())
        assertTrue(repository.observeSets(exercise).first().isEmpty())
    }

    @Test
    fun dayAndExerciseOrderingAreTransactionalAndPersistent() = runTest {
        val program = repository.createProgram("Plan")
        val a = repository.addDay(program, "A", 0)
        val b = repository.addDay(program, "B", 1)
        repository.reorderDays(program, listOf(b, a))
        assertEquals(listOf(b, a), repository.observeDays(program).first().map { it.id })
        try {
            repository.reorderDays(program, listOf(a, a))
            fail("Invalid order")
        } catch (_: IllegalArgumentException) {
        }
        assertEquals(listOf(b, a), repository.observeDays(program).first().map { it.id })

        val ex1 = repository.addExercise(a, "Squat", 0)
        val ex2 = repository.addExercise(a, "Lunge", 1)
        val ex3 = repository.addExercise(a, "Calf Raise", 2)
        repository.reorderExercises(a, listOf(ex3, ex1, ex2))
        assertEquals(listOf(ex3, ex1, ex2), repository.observeExercises(a).first().map { it.id })
    }

    /** Regression test for the originalId key mismatch that broke restore/export round-trips. */
    @Test
    fun createBackupKeepsProgramIdForPdfExport() = runTest {
        val program = repository.createProgram("Plan")
        val day = repository.addDay(program, "Day", 0)
        repository.addExercise(day, "Squat", 0)
        val backup = repository.createBackup()
        assertEquals(program, backup.programs.single().originalId)
    }

    @Test
    fun backupRestoreReplacesWholeDatasetAtomicallyAndPreservesTargets() = runTest {
        val original = repository.createProgram("Old")
        repository.saveProfile(UserProfile("Old User"))
        val replacement = PantherBackup(
            profile = UserProfile("Restored", age = 31),
            programs = listOf(
                ProgramBackup(
                    "New plan",
                    System.currentTimeMillis(),
                    true,
                    listOf(
                        DayBackup(
                            "Day A",
                            0,
                            listOf(
                                ExerciseBackup(
                                    "Squat",
                                    0,
                                    listOf(
                                        SetBackup(1, TargetType.REPS, 8, null, null),
                                        SetBackup(2, TargetType.DURATION, null, 1, 30),
                                    ),
                                ),
                            ),
                        ),
                    ),
                ),
            ),
        )
        repository.restoreBackup(replacement)
        assertEquals(UserProfile("Restored", age = 31), repository.observeProfile().first())
        val restored = repository.observePrograms().first().single()
        assertFalse(restored.id == original)
        assertEquals("New plan", restored.name)
        assertTrue(restored.isActive)
        val day = repository.observeDays(restored.id).first().single()
        val exercise = repository.observeExercises(day.id).first().single()
        assertEquals(listOf(8, null), repository.observeSets(exercise.id).first().map { it.targetValue })
        assertEquals(listOf(30), repository.observeSets(exercise.id).first().mapNotNull { it.targetSeconds })
    }

    @Test
    fun saveExerciseReplacesNameAndEachIndependentTargetInOneWrite() = runTest {
        val program = repository.createProgram("Plan")
        val day = repository.addDay(program, "Day", 0)
        repository.saveExercise(
            day,
            null,
            "Press",
            0,
            listOf(
                ExerciseSet(exerciseId = 0, setNumber = 1, targetType = TargetType.REPS, targetValue = 5),
                ExerciseSet(exerciseId = 0, setNumber = 2, targetType = TargetType.REPS, targetValue = 9),
            ),
        )
        val exercise = repository.observeExercises(day).first().single()
        repository.saveExercise(
            day,
            exercise.id,
            "Bench press",
            0,
            listOf(
                ExerciseSet(exerciseId = exercise.id, setNumber = 1, targetType = TargetType.REPS, targetValue = 7),
                ExerciseSet(exerciseId = exercise.id, setNumber = 2, targetType = TargetType.REPS, targetValue = 11),
            ),
        )
        assertEquals("Bench press", repository.observeExercises(day).first().single().name)
        assertEquals(listOf(7, 11), repository.observeSets(exercise.id).first().map { it.targetValue })
    }

    @Test
    fun profilePersists() = runTest {
        val profile = UserProfile("User", age = 30, heightCm = 180.0, weightKg = 80.0)
        repository.saveProfile(profile)
        assertEquals(profile, repository.observeProfile().first())
    }
}
