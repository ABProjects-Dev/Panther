package com.panther.app.feature

import com.panther.app.domain.model.WorkoutProgram
import org.junit.Assert.assertEquals
import org.junit.Test

class EditorAvailabilityTest {
    private fun program(id: Long) = WorkoutProgram(
        id = id,
        name = "Plan $id",
        createdAt = 1L,
        isActive = false,
        dayCount = 0,
        exerciseCount = 0,
    )

    @Test
    fun foundWhenTheListStillContainsTheProgram() {
        val state = DataState(listOf(program(1), program(2)))

        assertEquals(EditorAvailability.FOUND, editorAvailability(state, 2))
    }

    @Test
    fun loadingWhileTheFirstEmissionIsPending() {
        val state = DataState(emptyList<WorkoutProgram>(), loading = true)

        assertEquals(EditorAvailability.LOADING, editorAvailability(state, 1))
    }

    @Test
    fun missingOnceTheListLoadedWithoutTheProgram() {
        val state = DataState(listOf(program(1)))

        assertEquals(EditorAvailability.MISSING, editorAvailability(state, 99))
    }

    @Test
    fun aZeroFallbackIdIsMissingRatherThanAnEmptyEditor() {
        val state = DataState(listOf(program(1)))

        assertEquals(EditorAvailability.MISSING, editorAvailability(state, 0L))
    }

    @Test
    fun aProgramThatArrivesLateIsFoundEvenIfTheListWasEmptyBefore() {
        val loaded = DataState(listOf(program(7)))

        assertEquals(EditorAvailability.FOUND, editorAvailability(loaded, 7))
    }
}
