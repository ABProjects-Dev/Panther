package com.panther.app.domain

import com.panther.app.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class WorkoutValidationTest {
    @Test
    fun namesAndProfileAreValidated() {
        assertFalse(WorkoutValidation.name("  "))
        assertTrue(WorkoutValidation.name("Push"))
        assertFalse(WorkoutValidation.profile(UserProfile("A", age = -1)))
        assertFalse(WorkoutValidation.profile(UserProfile("A", heightCm = Double.NaN)))
    }

    @Test
    fun repTargetsMustBePositiveAndNotContainDuration() {
        assertTrue(WorkoutValidation.sets(listOf(ExerciseSet(exerciseId = 1, setNumber = 1, targetType = TargetType.REPS, targetValue = 10))))
        assertFalse(WorkoutValidation.sets(listOf(ExerciseSet(exerciseId = 1, setNumber = 1, targetType = TargetType.REPS, targetValue = 0))))
        assertFalse(WorkoutValidation.sets(listOf(ExerciseSet(exerciseId = 1, setNumber = 1, targetType = TargetType.REPS, targetValue = 5, targetSeconds = 3))))
    }

    @Test
    fun backupValidationEnforcesSingleActiveProgramAndValidSetTargets() {
        val now = 1_700_000_000_000L
        fun program(name: String, active: Boolean) = ProgramBackup(name, now, active, emptyList())
        assertTrue(WorkoutValidation.backup(PantherBackup(profile = UserProfile("A"), programs = listOf(program("A", true), program("B", false)))))
        assertFalse(WorkoutValidation.backup(PantherBackup(profile = null, programs = listOf(program("A", true), program("B", true)))))
        val badExercise = ProgramBackup(
            "Plan",
            now,
            false,
            listOf(
                DayBackup(
                    "Day",
                    0,
                    listOf(
                        ExerciseBackup("Lift", 0, listOf(SetBackup(1, TargetType.REPS, 0, null, null))),
                    ),
                ),
            ),
        )
        assertFalse(WorkoutValidation.backup(PantherBackup(profile = null, programs = listOf(badExercise))))
    }

    @Test
    fun durationMustBeNonZeroAndSecondsInRange() {
        fun set(m: Int, s: Int) = ExerciseSet(exerciseId = 1, setNumber = 1, targetType = TargetType.DURATION, targetMinutes = m, targetSeconds = s)
        assertTrue(WorkoutValidation.sets(listOf(set(1, 0))))
        assertTrue(WorkoutValidation.sets(listOf(set(0, 59))))
        assertFalse(WorkoutValidation.sets(listOf(set(0, 0))))
        assertFalse(WorkoutValidation.sets(listOf(set(1, 60))))
        assertFalse(WorkoutValidation.sets(listOf(set(-1, 10))))
        assertFalse(WorkoutValidation.sets(listOf(set(1, 0), set(2, 0))))
    }

    @Test
    fun bmiCalculationClassifiesAndValidatesInputs() {
        assertNull(BmiCalculator.calculate(null, 75.0))
        assertNull(BmiCalculator.calculate(180.0, null))
        assertNull(BmiCalculator.calculate(10.0, 75.0))

        val underweight = BmiCalculator.calculate(180.0, 55.0)
        assertNotNull(underweight)
        assertEquals(17.0, underweight!!.value, 0.01)
        assertEquals(BmiCategory.UNDERWEIGHT, underweight.category)

        val normal = BmiCalculator.calculate(180.0, 75.0)
        assertNotNull(normal)
        assertEquals(23.1, normal!!.value, 0.01)
        assertEquals(BmiCategory.NORMAL, normal.category)

        val overweight = BmiCalculator.calculate(175.0, 82.0)
        assertNotNull(overweight)
        assertEquals(26.8, overweight!!.value, 0.01)
        assertEquals(BmiCategory.OVERWEIGHT, overweight.category)

        val obese = BmiCalculator.calculate(170.0, 95.0)
        assertNotNull(obese)
        assertEquals(32.9, obese!!.value, 0.01)
        assertEquals(BmiCategory.OBESE, obese.category)
    }

    @Test
    fun durationCatalogSupportsPersianAndAsciiSearch() {
        val byPersian = DurationCatalog.search("۱:۳۰")
        assertTrue(byPersian.any { it.minutes == 1 && it.seconds == 30 })

        val bySeconds = DurationCatalog.search("۴۵")
        assertTrue(bySeconds.any { it.minutes == 0 && it.seconds == 45 })

        val customIncluded = DurationCatalog.search("1:23", extraRaw = "1:23")
        assertEquals(1, customIncluded.size)
        assertEquals(83, customIncluded.single().totalSeconds)
    }
}
