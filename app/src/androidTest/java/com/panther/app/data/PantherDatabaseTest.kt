package com.panther.app.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.panther.app.data.local.PantherDatabase
import com.panther.app.data.repository.RoomPantherRepository
import com.panther.app.domain.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PantherDatabaseTest {
    private lateinit var db: PantherDatabase
    private lateinit var repository: RoomPantherRepository
    @Before fun setup() {
        db = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getInstrumentation().targetContext, PantherDatabase::class.java).build()
        repository = RoomPantherRepository(db)
    }
    @After fun close() = db.close()
    @Test fun programsCanBeCreatedUpdatedActivatedSwitchedArchivedAndDeleted() = runBlocking {
        val first = repository.createProgram("First")
        val second = repository.createProgram("Second")
        repository.renameProgram(first, "Updated")
        assertEquals("Updated", repository.observePrograms().first().first { it.id == first }.name)
        repository.activateProgram(first)
        repository.activateProgram(second)
        var programs = repository.observePrograms().first()
        assertEquals(listOf(second), programs.filter { it.isActive }.map { it.id })
        repository.archiveProgram(second, true)
        try { repository.activateProgram(second); fail("Archived program should not activate") } catch (_: IllegalStateException) { }
        programs = repository.observePrograms().first()
        assertTrue(programs.first { it.id == second }.isArchived)
        assertFalse(programs.any { it.isActive })
        repository.deleteProgram(first)
        assertFalse(repository.observePrograms().first().any { it.id == first })
    }
    @Test fun cascadesAndIndependentSets() = runBlocking {
        val program = repository.createProgram("Plan")
        val day = repository.addDay(program, "Day", 0)
        val exercise = repository.addExercise(day, "Exercise", 0)
        repository.replaceSets(exercise, listOf(
            ExerciseSet(exerciseId = exercise, setNumber = 1, targetType = TargetType.REPS, targetValue = 8),
            ExerciseSet(exerciseId = exercise, setNumber = 2, targetType = TargetType.REPS, targetValue = 12),
        ))
        assertEquals(listOf(8, 12), repository.observeSets(exercise).first().map { it.targetValue })
        val summary = repository.observePrograms().first().single()
        assertEquals(1, summary.dayCount); assertEquals(1, summary.exerciseCount)
        repository.deleteProgram(program)
        assertTrue(repository.observeDays(program).first().isEmpty())
        assertTrue(repository.observeExercises(day).first().isEmpty())
        assertTrue(repository.observeSets(exercise).first().isEmpty())
    }
    @Test fun orderingIsTransactional() = runBlocking {
        val program = repository.createProgram("Plan")
        val a = repository.addDay(program, "A", 0)
        val b = repository.addDay(program, "B", 1)
        repository.reorderDays(program, listOf(b, a))
        assertEquals(listOf(b, a), repository.observeDays(program).first().map { it.id })
        try { repository.reorderDays(program, listOf(a, a)); fail("Invalid order") } catch (_: IllegalArgumentException) { }
        assertEquals(listOf(b, a), repository.observeDays(program).first().map { it.id })
    }
    @Test fun backupRestoreReplacesWholeDatasetAtomicallyAndPreservesTargets() = runBlocking {
        val original = repository.createProgram("Old")
        repository.saveProfile(UserProfile("Old User"))
        val replacement = PantherBackup(
            profile = UserProfile("Restored", age = 31),
            programs = listOf(ProgramBackup("New plan", System.currentTimeMillis(), true, false, listOf(
                DayBackup("Day A", 0, listOf(ExerciseBackup("Squat", 0, listOf(
                    SetBackup(1, TargetType.REPS, 8, null, null),
                    SetBackup(2, TargetType.DURATION, null, 1, 30),
                ))))
            )))
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

    @Test fun saveExerciseReplacesNameAndEachIndependentTargetInOneWrite() = runBlocking {
        val program = repository.createProgram("Plan")
        val day = repository.addDay(program, "Day", 0)
        repository.saveExercise(day, null, "Press", 0, listOf(
            ExerciseSet(exerciseId = 0, setNumber = 1, targetType = TargetType.REPS, targetValue = 5),
            ExerciseSet(exerciseId = 0, setNumber = 2, targetType = TargetType.REPS, targetValue = 9),
        ))
        val exercise = repository.observeExercises(day).first().single()
        repository.saveExercise(day, exercise.id, "Bench press", 0, listOf(
            ExerciseSet(exerciseId = exercise.id, setNumber = 1, targetType = TargetType.REPS, targetValue = 7),
            ExerciseSet(exerciseId = exercise.id, setNumber = 2, targetType = TargetType.REPS, targetValue = 11),
        ))
        assertEquals("Bench press", repository.observeExercises(day).first().single().name)
        assertEquals(listOf(7, 11), repository.observeSets(exercise.id).first().map { it.targetValue })
    }

    @Test fun profilePersists() = runBlocking {
        val profile = UserProfile("User", age = 30, heightCm = 180.0, weightKg = 80.0)
        repository.saveProfile(profile)
        assertEquals(profile, repository.observeProfile().first())
    }
}
