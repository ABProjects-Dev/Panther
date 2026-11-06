package com.panther.app.domain

import com.panther.app.domain.model.DayBackup
import com.panther.app.domain.model.ExerciseBackup
import com.panther.app.domain.model.PantherBackup
import com.panther.app.domain.model.ProgramBackup
import com.panther.app.domain.model.SetBackup
import com.panther.app.domain.model.TargetType
import com.panther.app.domain.model.UserProfile
import com.panther.app.domain.model.WorkoutValidation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guards the backup size/shape limits that keep restore operations inside the heap budget. */
class BackupLimitTest {

    private fun program(
        name: String = "P",
        days: Int = 0,
        exercisesPerDay: Int = 0,
        setsPerExercise: Int = 1,
    ) = ProgramBackup(name, 1_700_000_000_000, false, false, (0 until days).map { d ->
        DayBackup("Day $d", d, (0 until exercisesPerDay).map { e ->
            ExerciseBackup("Ex $e", e, (0 until setsPerExercise).map { s ->
                SetBackup(s + 1, TargetType.REPS, 5, null, null)
            })
        })
    })

    @Test
    fun acceptsBackupAtTheProgramLimit() {
        val programs = (0 until PantherBackup.MAX_PROGRAMS).map { program("P$it") }
        assertTrue(WorkoutValidation.backup(PantherBackup(profile = null, programs = programs)))
    }

    @Test
    fun rejectsBackupAboveTheProgramLimit() {
        val programs = (0 until PantherBackup.MAX_PROGRAMS + 1).map { program("P$it") }
        assertFalse(WorkoutValidation.backup(PantherBackup(profile = null, programs = programs)))
    }

    @Test
    fun rejectsTooManySetsPerExercise() {
        val p = program(days = 1, exercisesPerDay = 1, setsPerExercise = PantherBackup.MAX_SETS_PER_EXERCISE + 1)
        assertFalse(WorkoutValidation.backup(PantherBackup(profile = null, programs = listOf(p))))
    }

    @Test
    fun rejectsDuplicateDayOrder() {
        val p = ProgramBackup("P", 1_700_000_000_000, false, false, listOf(
            DayBackup("A", 0, emptyList()),
            DayBackup("B", 0, emptyList()),
        ))
        assertFalse(WorkoutValidation.backup(PantherBackup(profile = null, programs = listOf(p))))
    }

    @Test
    fun rejectsProfileImageWithoutProfile() {
        assertFalse(WorkoutValidation.backup(PantherBackup(profile = null, programs = emptyList(), profileImageBase64 = "abc")))
    }

    @Test
    fun acceptsProfileImageWithProfile() {
        assertTrue(
            WorkoutValidation.backup(
                PantherBackup(profile = UserProfile("A"), programs = emptyList(), profileImageBase64 = "abc"),
            ),
        )
    }
}
