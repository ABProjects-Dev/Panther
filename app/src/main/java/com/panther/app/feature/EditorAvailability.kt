package com.panther.app.feature

import com.panther.app.domain.model.WorkoutProgram

/**
 * What `editor/{id}` can show for a given program id. Kept as a pure function so the "the program is
 * gone" branch is testable without Compose.
 */
internal enum class EditorAvailability { LOADING, FOUND, MISSING }

internal fun editorAvailability(
    state: DataState<List<WorkoutProgram>>,
    programId: Long,
): EditorAvailability {
    if (state.value.any { it.id == programId }) return EditorAvailability.FOUND
    return if (state.loading) EditorAvailability.LOADING else EditorAvailability.MISSING
}
