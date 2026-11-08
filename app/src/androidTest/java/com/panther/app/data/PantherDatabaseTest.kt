package com.panther.app.data

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.panther.app.data.backup.PantherBackupJson
import com.panther.app.data.backup.ProgramPdfExporter
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
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PantherDatabaseTest {
    @get:Rule
    val migrationHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        PantherDatabase::class.java,
    )

    private lateinit var db: PantherDatabase
    private lateinit var repository: RoomPantherRepository

    private fun defaultRepSet(reps: Int = 10, exerciseId: Long = 0L, setNumber: Int = 1) =
        ExerciseSet(exerciseId = exerciseId, setNumber = setNumber, targetType = TargetType.REPS, targetValue = reps)

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
        repository.saveExercise(
            dayId = day,
            exerciseId = null,
            name = "Exercise",
            order = 0,
            sets = listOf(
                defaultRepSet(reps = 8, setNumber = 1),
                defaultRepSet(reps = 12, setNumber = 2),
            ),
        )
        val exerciseWithSets = repository.observeExercisesWithSets(day).first().single()
        val exerciseId = exerciseWithSets.exercise.id
        assertEquals(listOf(8, 12), exerciseWithSets.sets.map { it.targetValue })
        val summary = repository.observePrograms().first().single()
        assertEquals(1, summary.dayCount)
        assertEquals(1, summary.exerciseCount)

        repository.deleteProgram(program)
        assertTrue(repository.observeDays(program).first().isEmpty())
        assertTrue(repository.observeExercises(day).first().isEmpty())
        assertTrue(repository.observeSets(exerciseId).first().isEmpty())
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

        repository.saveExercise(a, null, "Squat", 0, listOf(defaultRepSet(10)))
        repository.saveExercise(a, null, "Lunge", 1, listOf(defaultRepSet(12)))
        repository.saveExercise(a, null, "Calf Raise", 2, listOf(defaultRepSet(15)))
        val created = repository.observeExercises(a).first()
        val ex1 = created[0].id
        val ex2 = created[1].id
        val ex3 = created[2].id
        repository.reorderExercises(a, listOf(ex3, ex1, ex2))
        assertEquals(listOf(ex3, ex1, ex2), repository.observeExercises(a).first().map { it.id })
    }

    /** Regression test for L1-01: deleting a middle day/exercise then adding another must never break JSON backup export. */
    @Test
    fun deletingMiddleDayOrExerciseCompactsSortOrderAndExportsValidBackupJson() = runTest {
        val program = repository.createProgram("Full Body")
        val d0 = repository.addDay(program, "Day 1", 0)
        val d1 = repository.addDay(program, "Day 2", 1)
        repository.addDay(program, "Day 3", 2)

        repository.deleteDay(d1)
        // Simulate UI passing remaining list size (2) after deleting the middle day.
        repository.addDay(program, "Day 4", 2)

        repository.saveExercise(d0, null, "Squat", 0, listOf(defaultRepSet(8)))
        repository.saveExercise(d0, null, "Bench", 1, listOf(defaultRepSet(10)))
        repository.saveExercise(d0, null, "Row", 2, listOf(defaultRepSet(12)))
        val middleExId = repository.observeExercises(d0).first()[1].id
        repository.deleteExercise(middleExId)
        repository.saveExercise(d0, null, "Overhead Press", 2, listOf(defaultRepSet(8)))

        val backup = repository.createBackup()
        val encoded = PantherBackupJson.encode(backup)
        val decoded = PantherBackupJson.decode(encoded)
        val exportedProgram = decoded.programs.single()
        assertEquals(listOf(0, 1, 2), exportedProgram.days.map { it.sortOrder })
        assertEquals(listOf("Day 1", "Day 3", "Day 4"), exportedProgram.days.map { it.name })
        assertEquals(listOf(0, 1, 2), exportedProgram.days.first().exercises.map { it.sortOrder })
        assertEquals(listOf("Squat", "Row", "Overhead Press"), exportedProgram.days.first().exercises.map { it.name })
    }

    /** Regression test for single-program PDF snapshot and PDF header bytes. */
    @Test
    fun createBackupAndSingleProgramPdfSnapshotSucceed() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val programA = repository.createProgram("برنامه الف")
        repository.createProgram("برنامه ب")
        val day = repository.addDay(programA, "روز اول", 0)
        repository.saveExercise(day, null, "اسکوات", 0, listOf(defaultRepSet(10)))

        val fullBackup = repository.createBackup()
        assertEquals(programA, fullBackup.programs.first { it.name == "برنامه الف" }.originalId)
        assertTrue(PantherBackupJson.encode(fullBackup).isNotBlank())

        val single = repository.createProgramBackup(programA)
        assertNotNull(single)
        assertEquals("برنامه الف", single!!.name)
        val pdfBytes = ByteArrayOutputStream().also { out ->
            ProgramPdfExporter.write(context, single, out)
        }.toByteArray()
        assertTrue(pdfBytes.size > 100)
        assertEquals("%PDF-", String(pdfBytes, 0, 5, Charsets.US_ASCII))
    }

    @Test
    fun exportedRoomSchemaBaselineOpensCleanlyWithMigrationTestHelper() {
        migrationHelper.createDatabase("panther-migration-baseline-test", 1).close()
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
