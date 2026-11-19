package com.panther.app.feature

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panther.app.R
import com.panther.app.core.designsystem.components.*
import com.panther.app.core.designsystem.theme.LocalPantherColors
import com.panther.app.core.designsystem.theme.PantherSpacing
import com.panther.app.core.format.PantherDateFormat
import com.panther.app.core.format.durationLabel
import com.panther.app.core.format.durationLabelResolver
import com.panther.app.domain.model.*
import java.util.ArrayList
import kotlinx.coroutines.delay

private val StringListSaver: Saver<List<String>, ArrayList<String>> = Saver(
    save = { ArrayList(it) },
    restore = { it.toList() },
)

private fun formatSetTarget(set: ExerciseSet): String =
    if (set.targetType == TargetType.REPS) {
        (set.targetValue ?: WorkoutDefaults.DEFAULT_REPS).toString()
    } else {
        "${set.targetMinutes ?: 0}:${(set.targetSeconds ?: 30).toString().padStart(2, '0')}"
    }

/**
 * One training day on its own screen (`day/{dayId}`).
 *
 * Ordering is locked by default: the drag handles and the move actions only appear while
 * "reorder mode" is on, and switching it off writes nothing — the persisted `sortOrder` is the only
 * source of the displayed 1..n numbers, so a move immediately renumbers the exercises.
 */
@Composable
fun TrainingDayScreen(
    viewModel: ProgramsViewModel,
    programId: Long,
    dayId: Long,
    onBack: () -> Unit,
    onDayDeleted: () -> Unit,
) {
    val programsState by viewModel.programs.collectAsStateWithLifecycle()
    val daysFlow = remember(programId) { viewModel.days(programId) }
    val daysState by daysFlow.collectAsStateWithLifecycle(initialValue = DataState(emptyList(), loading = true))
    val day = daysState.value.firstOrNull { it.id == dayId }
    val program = programsState.value.firstOrNull { it.id == programId }

    // Every hook below runs for all three states (`loading`, `missing`, `found`) so the composition
    // structure never changes between emissions; only the rendered content differs.
    val exercisesFlow = remember(dayId) { viewModel.exercisesWithSets(dayId) }
    val exercisesState by exercisesFlow.collectAsStateWithLifecycle(initialValue = DataState(emptyList(), loading = true))
    val exerciseNames by viewModel.exerciseNames.collectAsStateWithLifecycle()
    val vmError by viewModel.error.collectAsStateWithLifecycle()

    val orderedExercises = remember(dayId) { mutableStateListOf<ExerciseWithSets>() }
    var reorderMode by rememberSaveable(dayId) { mutableStateOf(false) }
    var draggedExerciseId by remember(dayId) { mutableStateOf<Long?>(null) }
    var dragOffsetY by remember(dayId) { mutableFloatStateOf(0f) }
    var dragAutoScrollSpeed by remember(dayId) { mutableFloatStateOf(0f) }
    val listState = rememberLazyListState()

    LaunchedEffect(dayId, exercisesState.value) {
        if (draggedExerciseId == null) {
            orderedExercises.clear()
            orderedExercises.addAll(exercisesState.value)
        }
    }

    // Leaving reorder mode must never strand a half-applied drag.
    LaunchedEffect(reorderMode) {
        if (!reorderMode) {
            draggedExerciseId = null
            dragOffsetY = 0f
            dragAutoScrollSpeed = 0f
        }
    }

    var showExerciseEditor by rememberSaveable(dayId) { mutableStateOf(false) }
    var editingExerciseId by rememberSaveable(dayId) { mutableStateOf<Long?>(null) }
    var exerciseToDeleteId by rememberSaveable(dayId) { mutableStateOf<Long?>(null) }
    var exerciseInvalid by rememberSaveable(dayId) { mutableStateOf(false) }
    var reorderHintShownAtLeastOnce by rememberSaveable(dayId) { mutableStateOf(false) }


    LaunchedEffect(vmError) {
        if (vmError) {
            delay(4000)
            viewModel.clearError()
        }
    }

    if (day == null) {
        if (daysState.loading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(PantherSpacing.medium),
            ) {
                Text(stringResource(R.string.loading))
            }
        } else {
            TrainingDayNotFoundScreen(onBack = onBack)
        }
        return
    }

    val currentExerciseItem = editingExerciseId?.let { targetId ->
        orderedExercises.firstOrNull { it.exercise.id == targetId }
            ?: exercisesState.value.firstOrNull { it.exercise.id == targetId }
    }
    val exerciseToDelete = exerciseToDeleteId?.let { targetId ->
        orderedExercises.firstOrNull { it.exercise.id == targetId }
            ?: exercisesState.value.firstOrNull { it.exercise.id == targetId }
    }
    val canAddExercise = orderedExercises.size < PantherBackup.MAX_EXERCISES_PER_DAY
    val canReorder = reorderMode && orderedExercises.size > 1

    fun handleDragStart(exerciseId: Long) {
        if (!reorderMode) return
        draggedExerciseId = exerciseId
        dragOffsetY = 0f
        dragAutoScrollSpeed = 0f
    }

    val activeDay = day
    fun handleDragStep(deltaY: Float) {
        if (!reorderMode || activeDay == null) return
        val activeId = draggedExerciseId ?: return
        dragOffsetY += deltaY
        val currentIndex = orderedExercises.indexOfFirst { it.exercise.id == activeId }
        if (currentIndex == -1) return
        val layoutInfo = listState.layoutInfo
        val visibleItems = layoutInfo.visibleItemsInfo
        val currentItemInfo = visibleItems.firstOrNull { it.index == currentIndex } ?: return
        val currentCenterY = currentItemInfo.offset + dragOffsetY + (currentItemInfo.size / 2f)

        val edgeThresholdPx = 110f
        dragAutoScrollSpeed = when {
            currentCenterY < layoutInfo.viewportStartOffset + edgeThresholdPx && listState.canScrollBackward -> -14f
            currentCenterY > layoutInfo.viewportEndOffset - edgeThresholdPx && listState.canScrollForward -> 14f
            else -> 0f
        }

        if (dragOffsetY > 0f && currentIndex < orderedExercises.lastIndex) {
            val nextItemInfo = visibleItems.firstOrNull { it.index == currentIndex + 1 }
            if (nextItemInfo != null && currentCenterY > nextItemInfo.offset + (nextItemInfo.size / 2f)) {
                val spacing = nextItemInfo.offset - (currentItemInfo.offset + currentItemInfo.size)
                val moved = orderedExercises.removeAt(currentIndex)
                orderedExercises.add(currentIndex + 1, moved)
                dragOffsetY -= (nextItemInfo.size + spacing).toFloat()
            }
        } else if (dragOffsetY < 0f && currentIndex > 0) {
            val prevItemInfo = visibleItems.firstOrNull { it.index == currentIndex - 1 }
            if (prevItemInfo != null && currentCenterY < prevItemInfo.offset + (prevItemInfo.size / 2f)) {
                val moved = orderedExercises.removeAt(currentIndex)
                orderedExercises.add(currentIndex - 1, moved)
                dragOffsetY += (currentItemInfo.offset - prevItemInfo.offset).toFloat()
            }
        }
    }

    fun handleDragFinish() {
        if (draggedExerciseId == null || activeDay == null) return
        draggedExerciseId = null
        dragOffsetY = 0f
        dragAutoScrollSpeed = 0f
        val newIds = orderedExercises.map { it.exercise.id }
        val persistedIds = exercisesState.value.map { it.exercise.id }
        if (newIds.size == persistedIds.size && newIds.toSet() == persistedIds.toSet() && newIds != persistedIds) {
            viewModel.reorderExercises(activeDay.id, newIds)
        } else if (newIds.toSet() != persistedIds.toSet()) {
            orderedExercises.clear()
            orderedExercises.addAll(exercisesState.value)
        }
    }

    LaunchedEffect(draggedExerciseId, dragAutoScrollSpeed) {
        while (draggedExerciseId != null && dragAutoScrollSpeed != 0f) {
            val consumed = listState.scrollBy(dragAutoScrollSpeed)
            if (consumed == 0f) {
                dragAutoScrollSpeed = 0f
                break
            }
            handleDragStep(0f)
            withFrameNanos { }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PantherSpacing.medium, vertical = PantherSpacing.small),
        verticalArrangement = Arrangement.spacedBy(PantherSpacing.small),
    ) {
        PantherTopBar(
            title = day.name,
            subtitle = program?.name,
            onBack = onBack,
            actions = {
                IconButton(
                    onClick = {
                        editingExerciseId = null
                        exerciseInvalid = false
                        showExerciseEditor = true
                    },
                    enabled = canAddExercise,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_exercise),
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
        )

        if (vmError) {
            Text(stringResource(R.string.error_generic), color = MaterialTheme.colorScheme.error)
        }

        if (!canAddExercise) {
            Text(
                text = stringResource(R.string.limit_exercises_reached),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            OutlinedButton(
                onClick = {
                    editingExerciseId = null
                    exerciseInvalid = false
                    showExerciseEditor = true
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 10.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.add_exercise))
            }
        }

        if (orderedExercises.size > 1) {
            PantherReorderToggle(
                active = reorderMode,
                onToggle = {
                    val next = !reorderMode
                    reorderMode = next
                    if (next) reorderHintShownAtLeastOnce = true
                },
            )
        }

        if (canReorder) {
            Text(
                text = stringResource(R.string.reorder_mode_on_hint),
                style = MaterialTheme.typography.labelSmall,
                color = LocalPantherColors.current.activeGreen,
            )
        } else if (!reorderMode && orderedExercises.size > 1 && reorderHintShownAtLeastOnce) {
            Text(
                text = stringResource(R.string.reorder_mode_off_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            if (exercisesState.loading && orderedExercises.isEmpty()) {
                Text(stringResource(R.string.loading))
            } else if (orderedExercises.isEmpty()) {
                PantherCard {
                    Text(
                        text = stringResource(R.string.no_exercises_in_day),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = PantherSpacing.small),
                    verticalArrangement = Arrangement.spacedBy(PantherSpacing.small),
                ) {
                    itemsIndexed(orderedExercises, key = { _, item -> item.exercise.id }) { index, item ->
                        val exercise = item.exercise
                        val isDragging = draggedExerciseId == exercise.id
                        ExerciseCard(
                            item = item,
                            position = index + 1,
                            highlightGreen = program?.isActive == true,
                            reorderMode = reorderMode,
                            isDragging = isDragging,
                            dragOffsetY = if (isDragging) dragOffsetY else 0f,
                            canMoveEarlier = index > 0,
                            canMoveLater = index < orderedExercises.lastIndex,
                            onMoveEarlier = {
                                if (index > 0) {
                                    val reordered = orderedExercises.map { it.exercise.id }.toMutableList().apply {
                                        val moved = removeAt(index)
                                        add(index - 1, moved)
                                    }
                                    viewModel.reorderExercises(day.id, reordered)
                                }
                            },
                            onMoveLater = {
                                if (index < orderedExercises.lastIndex) {
                                    val reordered = orderedExercises.map { it.exercise.id }.toMutableList().apply {
                                        val moved = removeAt(index)
                                        add(index + 1, moved)
                                    }
                                    viewModel.reorderExercises(day.id, reordered)
                                }
                            },
                            onDragStart = { handleDragStart(exercise.id) },
                            onDrag = { delta -> handleDragStep(delta) },
                            onDragEnd = { handleDragFinish() },
                            onEdit = {
                                editingExerciseId = exercise.id
                                exerciseInvalid = false
                                showExerciseEditor = true
                            },
                            onDelete = { exerciseToDeleteId = exercise.id },
                        )
                    }
                }
            }
        }

        // Page-level destructive action: the only way to delete the day itself.
        PantherDeleteButton(
            text = stringResource(R.string.delete_day_action),
            confirmTitle = stringResource(R.string.delete_day),
            confirmMessage = stringResource(R.string.confirm_delete_day),
            onConfirm = { viewModel.deleteDay(day.id, onDeleted = onDayDeleted) },
        )
    }

    if (exerciseToDelete != null) {
        AlertDialog(
            onDismissRequest = { exerciseToDeleteId = null },
            title = { Text(stringResource(R.string.delete_exercise_action)) },
            text = { Text(stringResource(R.string.confirm_delete_exercise)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        exerciseToDeleteId?.let { viewModel.deleteExercise(it) }
                        exerciseToDeleteId = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { exerciseToDeleteId = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showExerciseEditor) {
        ExerciseDialog(
            editingExerciseId = editingExerciseId,
            initialItem = currentExerciseItem,
            catalogNames = exerciseNames,
            invalid = exerciseInvalid,
            onInvalid = { exerciseInvalid = it },
            onDismiss = {
                showExerciseEditor = false
                editingExerciseId = null
            },
            onAddToCatalog = { viewModel.rememberExerciseName(it) },
            onSave = { name, sets ->
                // New exercises are appended (which numbers them last); edited ones keep their slot.
                val targetOrder = editingExerciseId?.let { id ->
                    orderedExercises.indexOfFirst { it.exercise.id == id }.takeIf { it >= 0 }
                }
                viewModel.saveExercise(day.id, editingExerciseId, name, targetOrder, sets)
                showExerciseEditor = false
                editingExerciseId = null
            },
        )
    }
}

/** Terminal state for `day/{dayId}` when the day behind the id does not exist. */
@Composable
fun TrainingDayNotFoundScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PantherSpacing.medium, vertical = PantherSpacing.small),
        verticalArrangement = Arrangement.spacedBy(PantherSpacing.compact),
    ) {
        PantherTopBar(
            title = stringResource(R.string.training_days),
            onBack = onBack,
        )
        PantherCard {
            Text(
                text = stringResource(R.string.day_not_found),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.program_not_found_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ExerciseCard(
    item: ExerciseWithSets,
    position: Int,
    highlightGreen: Boolean,
    reorderMode: Boolean,
    isDragging: Boolean,
    dragOffsetY: Float,
    canMoveEarlier: Boolean,
    canMoveLater: Boolean,
    onMoveEarlier: () -> Unit,
    onMoveLater: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val exercise = item.exercise
    val sets = item.sets
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)
    val earlierLabel = stringResource(R.string.exercise_reorder_earlier)
    val laterLabel = stringResource(R.string.exercise_reorder_later)
    val dragHandleLabel = stringResource(R.string.drag_handle_desc, exercise.name)
    val numberLabel = stringResource(R.string.exercise_number_desc, PantherDateFormat.persianNumber(position))

    val borderColor = when {
        isDragging -> MaterialTheme.colorScheme.primary
        highlightGreen -> LocalPantherColors.current.activeGreen.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (isDragging) 1f else 0f)
            .graphicsLayer {
                translationY = dragOffsetY
                scaleX = if (isDragging) 1.02f else 1f
                scaleY = if (isDragging) 1.02f else 1f
                shadowElevation = if (isDragging) 16f else 0f
            }
            .semantics {
                val actions = buildList {
                    if (canMoveEarlier) {
                        add(CustomAccessibilityAction(label = earlierLabel) { onMoveEarlier(); true })
                    }
                    if (canMoveLater) {
                        add(CustomAccessibilityAction(label = laterLabel) { onMoveLater(); true })
                    }
                }
                if (actions.isNotEmpty()) customActions = actions
            }
            // Long-press dragging is only attached while reorder mode is on.
            .then(
                if (reorderMode) {
                    Modifier.pointerInput(exercise.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { currentOnDragStart() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                currentOnDrag(dragAmount.y)
                            },
                            onDragEnd = { currentOnDragEnd() },
                            onDragCancel = { currentOnDragEnd() },
                        )
                    }
                } else {
                    Modifier
                },
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(if (isDragging) 1.5.dp else 1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PantherSpacing.compact),
            verticalArrangement = Arrangement.spacedBy(PantherSpacing.small),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PantherSpacing.small),
                ) {
                    // Position badge: the number is derived from the persisted order, so a move
                    // renumbers the list immediately.
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (highlightGreen) {
                            LocalPantherColors.current.activeGreen.copy(alpha = 0.16f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                    ) {
                        Text(
                            text = PantherDateFormat.persianNumber(position),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .semantics { contentDescription = numberLabel },
                        )
                    }
                    if (reorderMode) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .pointerInput(exercise.id, reorderMode) {
                                    detectDragGestures(
                                        onDragStart = { currentOnDragStart() },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            currentOnDrag(dragAmount.y)
                                        },
                                        onDragEnd = { currentOnDragEnd() },
                                        onDragCancel = { currentOnDragEnd() },
                                    )
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = dragHandleLabel,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (reorderMode) {
                        IconButton(onClick = onMoveEarlier, enabled = canMoveEarlier) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = earlierLabel,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                        IconButton(onClick = onMoveLater, enabled = canMoveLater) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = laterLabel,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
            if (sets.isNotEmpty()) {
                ExerciseSetsSquares(sets = sets, highlightGreen = highlightGreen)
            }
            // The delete entry point for a single exercise sits at the end of its own card.
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.delete_exercise_action))
            }
        }
    }
}

/**
 * Add/edit an exercise. The name field searches the device-only catalogue as the user types and, when
 * the typed name is not in it yet, offers "add to catalogue" right inside the field.
 */
@Composable
private fun ExerciseDialog(
    editingExerciseId: Long?,
    initialItem: ExerciseWithSets?,
    catalogNames: List<String>,
    invalid: Boolean,
    onInvalid: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onAddToCatalog: (String) -> Unit,
    onSave: (String, List<ExerciseSet>) -> Unit,
) {
    var name by rememberSaveable(editingExerciseId) { mutableStateOf(initialItem?.exercise?.name.orEmpty()) }
    var targetType by rememberSaveable(editingExerciseId) {
        mutableStateOf(initialItem?.sets?.firstOrNull()?.targetType ?: TargetType.REPS)
    }
    var targetStrings by rememberSaveable(editingExerciseId, stateSaver = StringListSaver) {
        mutableStateOf(
            initialItem?.sets?.map(::formatSetTarget)?.ifEmpty { null }
                ?: List(WorkoutDefaults.DEFAULT_SET_COUNT) { WorkoutDefaults.DEFAULT_REPS.toString() },
        )
    }
    var initializedFromDb by rememberSaveable(editingExerciseId) {
        mutableStateOf(editingExerciseId == null || initialItem != null)
    }
    var openDropdownSetIndex by rememberSaveable(editingExerciseId) { mutableStateOf<Int?>(null) }
    var addedToCatalog by rememberSaveable(editingExerciseId) { mutableStateOf(false) }

    LaunchedEffect(editingExerciseId, initialItem) {
        if (!initializedFromDb && editingExerciseId != null && initialItem != null) {
            name = initialItem.exercise.name
            if (initialItem.sets.isNotEmpty()) {
                targetType = initialItem.sets.first().targetType
                targetStrings = initialItem.sets.map(::formatSetTarget)
            }
            initializedFromDb = true
        }
    }

    val decreaseSetsDesc = stringResource(R.string.decrease_sets)
    val increaseSetsDesc = stringResource(R.string.increase_sets)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (editingExerciseId == null) R.string.add_exercise else R.string.edit_exercise))
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(PantherSpacing.small),
            ) {
                ExerciseNameField(
                    value = name,
                    onValueChange = { name = it; addedToCatalog = false; onInvalid(false) },
                    catalogNames = catalogNames,
                    onAddToCatalog = {
                        onAddToCatalog(ExerciseNameCatalog.normalize(name))
                        addedToCatalog = true
                    },
                    isError = invalid && !WorkoutValidation.name(name),
                )
                if (addedToCatalog) {
                    Text(
                        text = stringResource(R.string.exercise_name_added),
                        style = MaterialTheme.typography.labelSmall,
                        color = LocalPantherColors.current.activeGreen,
                    )
                }

                Text(stringResource(R.string.target_type), style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = targetType == TargetType.REPS,
                        onClick = {
                            if (targetType != TargetType.REPS) {
                                targetType = TargetType.REPS
                                openDropdownSetIndex = null
                                targetStrings = List(targetStrings.size) { WorkoutDefaults.DEFAULT_REPS.toString() }
                            }
                        },
                        label = { Text(stringResource(R.string.repetitions)) },
                    )
                    FilterChip(
                        selected = targetType == TargetType.DURATION,
                        onClick = {
                            if (targetType != TargetType.DURATION) {
                                targetType = TargetType.DURATION
                                openDropdownSetIndex = null
                                targetStrings = List(targetStrings.size) { WorkoutDefaults.DEFAULT_DURATION_RAW }
                            }
                        },
                        label = { Text(stringResource(R.string.duration)) },
                    )
                }

                // Set count stepper; each set below is a small numbered square.
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(stringResource(R.string.set_count_label))
                    IconButton(
                        onClick = {
                            if (targetStrings.size > 1) {
                                if (openDropdownSetIndex == targetStrings.lastIndex) openDropdownSetIndex = null
                                targetStrings = targetStrings.dropLast(1)
                            }
                        },
                        enabled = targetStrings.size > 1,
                        modifier = Modifier.semantics { contentDescription = decreaseSetsDesc },
                    ) {
                        Text("−")
                    }
                    Text(
                        text = PantherDateFormat.persianNumber(targetStrings.size),
                        fontWeight = FontWeight.Bold,
                    )
                    IconButton(
                        onClick = {
                            if (targetStrings.size < PantherBackup.MAX_SETS_PER_EXERCISE) {
                                targetStrings = targetStrings + if (targetType == TargetType.REPS) {
                                    WorkoutDefaults.DEFAULT_REPS.toString()
                                } else {
                                    WorkoutDefaults.DEFAULT_DURATION_RAW
                                }
                            }
                        },
                        enabled = targetStrings.size < PantherBackup.MAX_SETS_PER_EXERCISE,
                        modifier = Modifier.semantics { contentDescription = increaseSetsDesc },
                    ) {
                        Text("+")
                    }
                }

                FlowRowSets(
                    targetType = targetType,
                    targetStrings = targetStrings,
                    invalid = invalid,
                    onValueChange = { index, value ->
                        targetStrings = targetStrings.toMutableList().also { it[index] = value }
                        onInvalid(false)
                    },
                    onToggleSet = { index ->
                        if (targetType == TargetType.DURATION) {
                            openDropdownSetIndex = if (openDropdownSetIndex == index) null else index
                        }
                    },
                )

                if (invalid) {
                    Text(
                        text = stringResource(R.string.error_exercise),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    text = stringResource(R.string.individual_targets_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val targetExerciseId = editingExerciseId ?: 0L
                    val sets = targetStrings.mapIndexedNotNull { index, raw ->
                        if (targetType == TargetType.REPS) {
                            PantherDateFormat.asciiDigits(raw).trim().toIntOrNull()?.let { reps ->
                                ExerciseSet(
                                    exerciseId = targetExerciseId,
                                    setNumber = index + 1,
                                    targetType = targetType,
                                    targetValue = reps,
                                )
                            }
                        } else {
                            DurationCatalog.parse(raw)?.let { option ->
                                ExerciseSet(
                                    exerciseId = targetExerciseId,
                                    setNumber = index + 1,
                                    targetType = targetType,
                                    targetMinutes = option.minutes,
                                    targetSeconds = option.seconds,
                                )
                            }
                        }
                    }
                    val valid = WorkoutValidation.name(name) &&
                        sets.size == targetStrings.size &&
                        WorkoutValidation.sets(sets)
                    onInvalid(!valid)
                    if (valid) onSave(name, sets)
                },
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )

    val pickerIndex = openDropdownSetIndex
    if (pickerIndex != null && pickerIndex in targetStrings.indices && targetType == TargetType.DURATION) {
        val label = stringResource(R.string.set_duration_numbered, PantherDateFormat.persianNumber(pickerIndex + 1))
        DurationSearchablePickerDialog(
            title = label,
            selectedRaw = targetStrings[pickerIndex],
            onDismiss = { openDropdownSetIndex = null },
            onSelectOption = { option ->
                targetStrings = targetStrings.toMutableList().also { it[pickerIndex] = option.key }
                openDropdownSetIndex = null
                onInvalid(false)
            },
        )
    }
}

/**
 * The per-set targets as small squares with a set-number badge ([PantherSetSquareBox]). A square is
 * an inline editable field for repetitions and a picker for durations, so the row stays compact.
 */
@Composable
private fun FlowRowSets(
    targetType: TargetType,
    targetStrings: List<String>,
    invalid: Boolean,
    onValueChange: (Int, String) -> Unit,
    onToggleSet: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PantherSpacing.small)) {
        targetStrings.forEachIndexed { index, target ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PantherSpacing.small),
            ) {
                if (targetType == TargetType.REPS) {
                    EditableSetSquare(
                        setNumber = index + 1,
                        value = target,
                        isError = invalid,
                        onValueChange = { onValueChange(index, it) },
                    )
                } else {
                    SetSquareBox(
                        set = ExerciseSet(
                            exerciseId = 0,
                            setNumber = index + 1,
                            targetType = TargetType.DURATION,
                            targetMinutes = DurationCatalog.parse(target)?.minutes ?: 0,
                            targetSeconds = DurationCatalog.parse(target)?.seconds ?: 30,
                        ),
                        backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                        valueColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { onToggleSet(index) },
                    )
                }
                if (targetType == TargetType.DURATION) {
                    Text(
                        text = DurationCatalog.parse(target)?.let { option ->
                            durationLabel(LocalContext.current, option)
                        } ?: PantherDateFormat.persianDigits(target),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/** A set square whose value is edited in place; still a small square, not a full-width field. */
@Composable
private fun EditableSetSquare(
    setNumber: Int,
    value: String,
    isError: Boolean,
    onValueChange: (String) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val shape = MaterialTheme.shapes.small
    val activeColors = LocalPantherColors.current

    Box(modifier = Modifier.size(PantherSetSquareSize)) {
        if (expanded) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxSize(),
                isError = isError,
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = shape,
            )
        } else {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { expanded = true },
                shape = shape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                border = BorderStroke(
                    1.dp,
                    if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f),
                ),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = PantherDateFormat.persianNumber(
                            PantherDateFormat.asciiDigits(value).trim().toIntOrNull() ?: 0,
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isError) MaterialTheme.colorScheme.error else activeColors.activeGreen,
                        modifier = Modifier.align(Alignment.Center),
                    )
                }
            }
        }
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(2.dp),
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.85f),
            contentColor = MaterialTheme.colorScheme.surface,
        ) {
            Text(
                text = PantherDateFormat.persianNumber(setNumber),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
            )
        }
    }
}

/**
 * Free-text name field backed by the device-only catalogue: typing filters the suggestions, choosing
 * one fills the field, and a typed name that is not in the catalogue gets an inline "add" action.
 */
@Composable
private fun ExerciseNameField(
    value: String,
    onValueChange: (String) -> Unit,
    catalogNames: List<String>,
    onAddToCatalog: () -> Unit,
    isError: Boolean,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val normalizedValue = ExerciseNameCatalog.normalize(value)
    val suggestions = remember(value, catalogNames) {
        ExerciseNameCatalog.suggestions(catalogNames, if (expanded) normalizedValue else "")
            .filter { it.lowercase() != normalizedValue.lowercase() }
    }
    val isNewName = normalizedValue.isNotEmpty() && !ExerciseNameCatalog.contains(catalogNames, normalizedValue)
    val addLabel = stringResource(R.string.exercise_name_add_action)

    Column(verticalArrangement = Arrangement.spacedBy(PantherSpacing.extraSmall)) {
        OutlinedTextField(
            value = value,
            onValueChange = { onValueChange(it); expanded = true },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.exercise_name)) },
            isError = isError,
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange(""); expanded = true }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear_search),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
            },
        )
        Text(
            text = stringResource(R.string.exercise_name_hint),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (expanded && isNewName) {
            OutlinedButton(
                onClick = { onAddToCatalog(); onValueChange(normalizedValue); expanded = false },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(addLabel)
            }
        }

        if (expanded && suggestions.isNotEmpty()) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = stringResource(R.string.exercise_name_suggestions),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    )
                    LazyColumn(modifier = Modifier.heightIn(max = 160.dp)) {
                        itemsIndexed(suggestions, key = { _, item -> item }) { _, suggestion ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onValueChange(suggestion)
                                        expanded = false
                                    }
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(text = suggestion, style = MaterialTheme.typography.bodyMedium)
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }
        } else if (expanded && normalizedValue.isNotEmpty() && !isNewName) {
            Text(
                text = stringResource(R.string.exercise_name_no_results),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DurationSearchablePickerDialog(
    title: String,
    selectedRaw: String,
    onDismiss: () -> Unit,
    onSelectOption: (DurationOption) -> Unit,
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val labelOf = remember(context) { durationLabelResolver(context) }
    val selectedOption = remember(selectedRaw) { DurationCatalog.parse(selectedRaw) }
    val filteredOptions = remember(searchQuery, selectedRaw, labelOf) {
        DurationCatalog.search(searchQuery, extraRaw = selectedRaw, labelOf = labelOf)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PantherSpacing.small),
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = stringResource(R.string.duration_search_placeholder),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = if (searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = stringResource(R.string.clear_search),
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    } else {
                        null
                    },
                    shape = MaterialTheme.shapes.small,
                )

                if (filteredOptions.isEmpty()) {
                    Text(
                        text = stringResource(R.string.duration_no_results),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp),
                    ) {
                        itemsIndexed(filteredOptions, key = { _, opt -> opt.totalSeconds }) { _, option ->
                            val isSelected = selectedOption?.totalSeconds == option.totalSeconds
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectOption(option) }
                                    .padding(horizontal = 8.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = labelOf(option),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
    )
}
