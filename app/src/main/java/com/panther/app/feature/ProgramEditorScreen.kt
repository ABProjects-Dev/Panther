package com.panther.app.feature

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
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
import com.panther.app.domain.model.*

@Composable
fun ProgramEditorScreen(
    viewModel: ProgramsViewModel,
    settings: SettingsViewModel,
    programId: Long,
    onBack: () -> Unit,
) {
    val programsState by viewModel.programs.collectAsStateWithLifecycle()
    val daysState by viewModel.days(programId).collectAsStateWithLifecycle(initialValue = DataState(emptyList(), loading = true))
    val vmError by viewModel.error.collectAsStateWithLifecycle()
    val program = programsState.value.firstOrNull { it.id == programId }
    val activeColors = LocalPantherColors.current

    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) settings.exportProgramPdf(programId, uri)
    }

    var showRenameProgramDialog by rememberSaveable { mutableStateOf(false) }
    var renameProgramText by rememberSaveable { mutableStateOf("") }
    var showDeleteProgramDialog by rememberSaveable { mutableStateOf(false) }

    var showAddDayDialog by rememberSaveable { mutableStateOf(false) }
    var newDayName by rememberSaveable { mutableStateOf("") }

    val daysList = daysState.value
    var selectedDayId by rememberSaveable(programId) { mutableStateOf<Long?>(null) }
    var previousDayCount by rememberSaveable(programId) { mutableIntStateOf(0) }

    LaunchedEffect(daysList) {
        if (daysList.isNotEmpty()) {
            if (daysList.size > previousDayCount && previousDayCount > 0) {
                selectedDayId = daysList.last().id
            } else if (daysList.none { it.id == selectedDayId }) {
                selectedDayId = daysList.first().id
            }
        } else {
            selectedDayId = null
        }
        previousDayCount = daysList.size
    }

    val selectedTabIndex = remember(daysList, selectedDayId) {
        val index = daysList.indexOfFirst { it.id == selectedDayId }
        if (index >= 0) index else 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PantherSpacing.medium, vertical = PantherSpacing.small),
        verticalArrangement = Arrangement.spacedBy(PantherSpacing.compact),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onBack) {
                Text(stringResource(R.string.back))
            }
            if (program != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TextButton(
                        onClick = {
                            renameProgramText = program.name
                            showRenameProgramDialog = true
                        },
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.edit))
                    }
                    TextButton(onClick = { pdfLauncher.launch("${program.name}.pdf") }) {
                        Text(stringResource(R.string.export_pdf))
                    }
                    TextButton(
                        onClick = { showDeleteProgramDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.delete))
                    }
                }
            }
        }

        if (program == null) {
            if (!programsState.loading) {
                Text(stringResource(R.string.program_not_found), color = MaterialTheme.colorScheme.error)
            } else {
                Text(stringResource(R.string.loading))
            }
            return@Column
        }

        PantherCard(
            containerColor = if (program.isActive) activeColors.activeGreenContainer else MaterialTheme.colorScheme.surface,
            border = if (program.isActive) {
                BorderStroke(1.5.dp, activeColors.activeGreen)
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            },
            contentPadding = PantherSpacing.compact,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(PantherSpacing.small),
                    ) {
                        Text(
                            text = program.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false),
                        )
                        if (program.isActive) {
                            PantherActiveBadge()
                        } else {
                            Text(
                                text = stringResource(R.string.status_inactive),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        text = "${
                            stringResource(
                                R.string.counts,
                                PantherDateFormat.persianNumber(program.dayCount),
                                PantherDateFormat.persianNumber(program.exerciseCount),
                            )
                        } • ${stringResource(R.string.created_at, PantherDateFormat.medium(program.createdAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(PantherSpacing.small))
                Switch(
                    checked = program.isActive,
                    onCheckedChange = { active -> viewModel.setActive(program.id, active) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = activeColors.onActiveGreen,
                        checkedTrackColor = activeColors.activeGreen,
                        checkedBorderColor = activeColors.activeGreen,
                    ),
                )
            }
        }

        if (daysState.error || vmError) {
            Text(stringResource(R.string.error_generic), color = MaterialTheme.colorScheme.error)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.training_days),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            OutlinedButton(
                onClick = {
                    newDayName = ""
                    showAddDayDialog = true
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.add_day))
            }
        }

        if (daysState.loading) {
            Text(stringResource(R.string.loading))
        } else if (daysList.isEmpty()) {
            PantherCard {
                Text(
                    text = stringResource(R.string.no_days),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            val tabAccent = if (program.isActive) activeColors.activeGreen else MaterialTheme.colorScheme.primary
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                contentColor = tabAccent,
                indicator = { tabPositions ->
                    if (selectedTabIndex in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = tabAccent,
                        )
                    }
                },
                divider = {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                },
            ) {
                daysList.forEachIndexed { index, day ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedDayId = day.id },
                        selectedContentColor = tabAccent,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                text = day.name,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            )
                        },
                    )
                }
            }

            val currentDay = daysList[selectedTabIndex]
            key(currentDay.id) {
                TrainingDayDedicatedPage(
                    day = currentDay,
                    isProgramActive = program.isActive,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (showRenameProgramDialog && program != null) {
        var invalid by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showRenameProgramDialog = false },
            title = { Text(stringResource(R.string.edit_program)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(PantherSpacing.extraSmall)) {
                    PantherTextField(
                        value = renameProgramText,
                        onValueChange = { renameProgramText = it; invalid = false },
                        label = stringResource(R.string.program_name),
                        modifier = Modifier.fillMaxWidth(),
                        isError = invalid,
                    )
                    if (invalid) {
                        Text(
                            text = stringResource(R.string.error_name),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!WorkoutValidation.name(renameProgramText)) {
                            invalid = true
                        } else {
                            viewModel.rename(program.id, renameProgramText)
                            showRenameProgramDialog = false
                        }
                    },
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameProgramDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showDeleteProgramDialog && program != null) {
        AlertDialog(
            onDismissRequest = { showDeleteProgramDialog = false },
            title = { Text(stringResource(R.string.delete_program)) },
            text = { Text(stringResource(R.string.confirm_delete)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteProgramDialog = false
                        viewModel.delete(program.id, onDeleted = onBack)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteProgramDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showAddDayDialog) {
        var invalid by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showAddDayDialog = false },
            title = { Text(stringResource(R.string.add_day)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(PantherSpacing.extraSmall)) {
                    PantherTextField(
                        value = newDayName,
                        onValueChange = { newDayName = it; invalid = false },
                        label = stringResource(R.string.day_name),
                        modifier = Modifier.fillMaxWidth(),
                        isError = invalid,
                    )
                    if (invalid) {
                        Text(
                            text = stringResource(R.string.error_name),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!WorkoutValidation.name(newDayName)) {
                            invalid = true
                        } else {
                            viewModel.addDay(programId, newDayName, daysList.size)
                            showAddDayDialog = false
                        }
                    },
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDayDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun TrainingDayDedicatedPage(
    day: TrainingDay,
    isProgramActive: Boolean,
    viewModel: ProgramsViewModel,
    modifier: Modifier = Modifier,
) {
    val exercisesState by viewModel.exercises(day.id).collectAsStateWithLifecycle(initialValue = DataState(emptyList(), loading = true))
    val orderedExercises = remember(day.id) { mutableStateListOf<Exercise>() }
    var draggedExerciseId by remember(day.id) { mutableStateOf<Long?>(null) }
    var dragOffsetY by remember(day.id) { mutableFloatStateOf(0f) }
    val listState = rememberLazyListState()

    LaunchedEffect(day.id, exercisesState.value) {
        if (draggedExerciseId == null) {
            orderedExercises.clear()
            orderedExercises.addAll(exercisesState.value)
        }
    }

    var renameDayDialog by rememberSaveable(day.id) { mutableStateOf(false) }
    var editDayName by rememberSaveable(day.id) { mutableStateOf(day.name) }
    var deleteDayDialog by rememberSaveable(day.id) { mutableStateOf(false) }

    var showExerciseEditor by rememberSaveable(day.id) { mutableStateOf(false) }
    var currentExercise by remember(day.id) { mutableStateOf<Exercise?>(null) }
    var exerciseToDelete by remember(day.id) { mutableStateOf<Exercise?>(null) }
    var exerciseInvalid by remember(day.id) { mutableStateOf(false) }

    fun handleDragStart(exerciseId: Long) {
        draggedExerciseId = exerciseId
        dragOffsetY = 0f
    }

    fun handleDragStep(deltaY: Float) {
        val activeId = draggedExerciseId ?: return
        dragOffsetY += deltaY
        val currentIndex = orderedExercises.indexOfFirst { it.id == activeId }
        if (currentIndex == -1) return
        val visibleItems = listState.layoutInfo.visibleItemsInfo
        val currentItemInfo = visibleItems.firstOrNull { it.index == currentIndex } ?: return
        val currentCenterY = currentItemInfo.offset + dragOffsetY + (currentItemInfo.size / 2f)

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
        if (draggedExerciseId == null) return
        draggedExerciseId = null
        dragOffsetY = 0f
        val newIds = orderedExercises.map { it.id }
        val persistedIds = exercisesState.value.map { it.id }
        if (newIds.size == persistedIds.size && newIds.toSet() == persistedIds.toSet() && newIds != persistedIds) {
            viewModel.reorderExercises(day.id, newIds)
        } else if (newIds.toSet() != persistedIds.toSet()) {
            orderedExercises.clear()
            orderedExercises.addAll(exercisesState.value)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(PantherSpacing.small),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Button(
                    onClick = {
                        currentExercise = null
                        exerciseInvalid = false
                        showExerciseEditor = true
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.add_exercise), fontWeight = FontWeight.SemiBold)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = {
                        editDayName = day.name
                        renameDayDialog = true
                    },
                ) {
                    Text(stringResource(R.string.edit_day))
                }
                TextButton(
                    onClick = { deleteDayDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(R.string.delete_day))
                }
            }
        }

        if (orderedExercises.size > 1) {
            Text(
                text = stringResource(R.string.drag_hint),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

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
                contentPadding = PaddingValues(bottom = PantherSpacing.large),
                verticalArrangement = Arrangement.spacedBy(PantherSpacing.small),
            ) {
                itemsIndexed(orderedExercises, key = { _, ex -> ex.id }) { _, exercise ->
                    val isDragging = draggedExerciseId == exercise.id
                    DraggableExerciseCard(
                        exercise = exercise,
                        isDragging = isDragging,
                        dragOffsetY = if (isDragging) dragOffsetY else 0f,
                        highlightGreen = isProgramActive,
                        viewModel = viewModel,
                        onDragStart = { handleDragStart(exercise.id) },
                        onDrag = { delta -> handleDragStep(delta) },
                        onDragEnd = { handleDragFinish() },
                        onEdit = {
                            currentExercise = exercise
                            exerciseInvalid = false
                            showExerciseEditor = true
                        },
                        onDelete = { exerciseToDelete = exercise },
                    )
                }
            }
        }
    }

    if (renameDayDialog) {
        var invalid by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { renameDayDialog = false },
            title = { Text(stringResource(R.string.edit_day)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(PantherSpacing.extraSmall)) {
                    PantherTextField(
                        value = editDayName,
                        onValueChange = { editDayName = it; invalid = false },
                        label = stringResource(R.string.day_name),
                        modifier = Modifier.fillMaxWidth(),
                        isError = invalid,
                    )
                    if (invalid) {
                        Text(
                            text = stringResource(R.string.error_name),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (!WorkoutValidation.name(editDayName)) {
                            invalid = true
                        } else {
                            viewModel.renameDay(day.id, editDayName)
                            renameDayDialog = false
                        }
                    },
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { renameDayDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (deleteDayDialog) {
        AlertDialog(
            onDismissRequest = { deleteDayDialog = false },
            title = { Text(stringResource(R.string.delete_day)) },
            text = { Text(stringResource(R.string.confirm_delete_day)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteDay(day.id)
                        deleteDayDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteDayDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (exerciseToDelete != null) {
        AlertDialog(
            onDismissRequest = { exerciseToDelete = null },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(stringResource(R.string.confirm_delete_exercise)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        exerciseToDelete?.let { viewModel.deleteExercise(it.id) }
                        exerciseToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { exerciseToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (showExerciseEditor) {
        ExerciseDialog(
            initial = currentExercise,
            exerciseFlow = currentExercise?.let { viewModel.sets(it.id) },
            invalid = exerciseInvalid,
            onInvalid = { exerciseInvalid = it },
            onDismiss = { showExerciseEditor = false },
            onSave = { name, sets ->
                val targetOrder = currentExercise?.let { existing ->
                    orderedExercises.indexOfFirst { it.id == existing.id }.takeIf { it >= 0 } ?: existing.sortOrder
                } ?: orderedExercises.size
                viewModel.saveExercise(day.id, currentExercise?.id, name, targetOrder, sets)
                showExerciseEditor = false
            },
        )
    }
}

@Composable
private fun DraggableExerciseCard(
    exercise: Exercise,
    isDragging: Boolean,
    dragOffsetY: Float,
    highlightGreen: Boolean,
    viewModel: ProgramsViewModel,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val sets by viewModel.sets(exercise.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val currentOnDragStart by rememberUpdatedState(onDragStart)
    val currentOnDrag by rememberUpdatedState(onDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

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
            .pointerInput(exercise.id) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { currentOnDragStart() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentOnDrag(dragAmount.y)
                    },
                    onDragEnd = { currentOnDragEnd() },
                    onDragCancel = { currentOnDragEnd() },
                )
            },
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
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .pointerInput(exercise.id) {
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
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Spacer(Modifier.width(PantherSpacing.extraSmall))
                    Text(
                        text = exercise.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
            if (sets.isNotEmpty()) {
                ExerciseSetsSquares(sets = sets, highlightGreen = highlightGreen)
            }
        }
    }
}

@Composable
private fun ExerciseDialog(
    initial: Exercise?,
    exerciseFlow: kotlinx.coroutines.flow.Flow<List<ExerciseSet>>?,
    invalid: Boolean,
    onInvalid: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, List<ExerciseSet>) -> Unit,
) {
    val initialSets by (exerciseFlow ?: remember { kotlinx.coroutines.flow.flowOf(emptyList()) })
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var name by rememberSaveable(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var targetType by rememberSaveable(initial?.id) {
        mutableStateOf(initialSets.firstOrNull()?.targetType ?: TargetType.REPS)
    }
    var targetStrings by rememberSaveable(initial?.id) {
        mutableStateOf(
            initialSets.map {
                if (it.targetType == TargetType.REPS) {
                    it.targetValue.toString()
                } else {
                    "${it.targetMinutes}:${it.targetSeconds.toString().padStart(2, '0')}"
                }
            }.ifEmpty { listOf("10", "10", "10") },
        )
    }
    var initializedFromDb by rememberSaveable(initial?.id) { mutableStateOf(initial == null) }
    var openDropdownSetIndex by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(initialSets) {
        if (!initializedFromDb && initialSets.isNotEmpty()) {
            targetType = initialSets.first().targetType
            targetStrings = initialSets.map {
                if (it.targetType == TargetType.REPS) {
                    it.targetValue.toString()
                } else {
                    "${it.targetMinutes}:${it.targetSeconds.toString().padStart(2, '0')}"
                }
            }
            initializedFromDb = true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(if (initial == null) R.string.add_exercise else R.string.edit_exercise))
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(PantherSpacing.small),
            ) {
                PantherTextField(
                    value = name,
                    onValueChange = { name = it; onInvalid(false) },
                    label = stringResource(R.string.exercise_name),
                    modifier = Modifier.fillMaxWidth(),
                    isError = invalid && !WorkoutValidation.name(name),
                )
                Text(stringResource(R.string.target_type), style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = targetType == TargetType.REPS,
                        onClick = {
                            if (targetType != TargetType.REPS) {
                                targetType = TargetType.REPS
                                openDropdownSetIndex = null
                                targetStrings = List(targetStrings.size) { "10" }
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
                                targetStrings = List(targetStrings.size) { "0:30" }
                            }
                        },
                        label = { Text(stringResource(R.string.duration)) },
                    )
                }
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
                                targetStrings = targetStrings + if (targetType == TargetType.REPS) "10" else "0:30"
                            }
                        },
                        enabled = targetStrings.size < PantherBackup.MAX_SETS_PER_EXERCISE,
                    ) {
                        Text("+")
                    }
                }

                targetStrings.forEachIndexed { index, target ->
                    val setNum = PantherDateFormat.persianNumber(index + 1)
                    if (targetType == TargetType.REPS) {
                        PantherTextField(
                            value = target,
                            onValueChange = { value ->
                                targetStrings = targetStrings.toMutableList().also { it[index] = value }
                                onInvalid(false)
                            },
                            label = stringResource(R.string.set_repetitions_numbered, setNum),
                            modifier = Modifier.fillMaxWidth(),
                            isError = invalid,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                    } else {
                        DurationSearchableSelectField(
                            label = stringResource(R.string.set_duration_numbered, setNum),
                            selectedRaw = target,
                            expanded = openDropdownSetIndex == index,
                            onToggleExpanded = {
                                openDropdownSetIndex = if (openDropdownSetIndex == index) null else index
                            },
                        )
                    }
                }

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
                    val sets = targetStrings.mapIndexedNotNull { index, raw ->
                        if (targetType == TargetType.REPS) {
                            PantherDateFormat.asciiDigits(raw).trim().toIntOrNull()?.let { reps ->
                                ExerciseSet(
                                    exerciseId = initial?.id ?: 0L,
                                    setNumber = index + 1,
                                    targetType = targetType,
                                    targetValue = reps,
                                )
                            }
                        } else {
                            DurationCatalog.parse(raw)?.let { option ->
                                ExerciseSet(
                                    exerciseId = initial?.id ?: 0L,
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
        val setNum = PantherDateFormat.persianNumber(pickerIndex + 1)
        DurationSearchablePickerDialog(
            title = stringResource(R.string.set_duration_numbered, setNum),
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

@Composable
private fun DurationSearchableSelectField(
    label: String,
    selectedRaw: String,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
) {
    val selectedOption = remember(selectedRaw) { DurationCatalog.parse(selectedRaw) }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggleExpanded),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (expanded) 1.5.dp else 1.dp,
            color = if (expanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = selectedOption?.displayLabel ?: PantherDateFormat.persianDigits(selectedRaw),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
    val selectedOption = remember(selectedRaw) { DurationCatalog.parse(selectedRaw) }
    val filteredOptions = remember(searchQuery, selectedRaw) {
        DurationCatalog.search(searchQuery, extraRaw = selectedRaw)
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
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(18.dp))
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
                                    text = option.displayLabel,
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
