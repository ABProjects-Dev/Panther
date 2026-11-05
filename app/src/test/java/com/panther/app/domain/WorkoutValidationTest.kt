package com.panther.app.domain

import com.panther.app.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class WorkoutValidationTest {
    @Test fun namesAndProfileAreValidated() {
        assertFalse(WorkoutValidation.name("  "))
        assertTrue(WorkoutValidation.name("Push"))
        assertFalse(WorkoutValidation.profile(UserProfile("A", age = -1)))
        assertFalse(WorkoutValidation.profile(UserProfile("A", heightCm = Double.NaN)))
    }
    @Test fun repTargetsMustBePositiveAndNotContainDuration() {
        assertTrue(WorkoutValidation.sets(listOf(ExerciseSet(exerciseId = 1, setNumber = 1, targetType = TargetType.REPS, targetValue = 10))))
        assertFalse(WorkoutValidation.sets(listOf(ExerciseSet(exerciseId = 1, setNumber = 1, targetType = TargetType.REPS, targetValue = 0))))
        assertFalse(WorkoutValidation.sets(listOf(ExerciseSet(exerciseId = 1, setNumber = 1, targetType = TargetType.REPS, targetValue = 5, targetSeconds = 3))))
    }
    @Test fun durationMustBeNonZeroAndSecondsInRange() {
        fun set(m: Int, s: Int) = ExerciseSet(exerciseId = 1, setNumber = 1, targetType = TargetType.DURATION, targetMinutes = m, targetSeconds = s)
        assertTrue(WorkoutValidation.sets(listOf(set(1, 0))))
        assertTrue(WorkoutValidation.sets(listOf(set(0, 59))))
        assertFalse(WorkoutValidation.sets(listOf(set(0, 0))))
        assertFalse(WorkoutValidation.sets(listOf(set(1, 60))))
        assertFalse(WorkoutValidation.sets(listOf(set(-1, 10))))
        assertFalse(WorkoutValidation.sets(listOf(set(1, 0), set(2, 0))))
    }
}
