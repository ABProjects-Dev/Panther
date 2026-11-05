package com.panther.app.feature

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panther.app.R
import com.panther.app.core.designsystem.components.PantherButton
import com.panther.app.core.designsystem.components.PantherCard
import com.panther.app.core.designsystem.components.PantherSectionHeader
import com.panther.app.core.designsystem.components.PantherTextField
import com.panther.app.core.designsystem.theme.PantherSpacing
import com.panther.app.domain.model.*

private fun normalizedTarget(value: String): String = value.map { digit ->
    when (digit) {
        in '۰'..'۹' -> '0' + (digit - '۰')
        in '٠'..'٩' -> '0' + (digit - '٠')
        '：' -> ':'
        else -> digit
    }
}.joinToString("")

@Composable
fun ProgramEditorScreen(viewModel: ProgramsViewModel, settings: SettingsViewModel, programId: Long, onBack: () -> Unit) {
    val programs by viewModel.programs.collectAsStateWithLifecycle()
    val days by viewModel.days(programId).collectAsStateWithLifecycle(initialValue = DataState(emptyList()))
    val program = programs.value.firstOrNull { it.id == programId }
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri -> if (uri != null) settings.exportProgramPdf(programId, uri) }
    var addDay by rememberSaveable { mutableStateOf(false) }
    var dayName by rememberSaveable { mutableStateOf("") }
    var invalid by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(PantherSpacing.medium), verticalArrangement = Arrangement.spacedBy(PantherSpacing.medium)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
            if (program != null) TextButton(onClick = { pdfLauncher.launch("${program.name}.pdf") }) { Text(stringResource(R.string.export_pdf)) }
        }
        if (program != null) {
            PantherSectionHeader(program.name)
            Text(stringResource(R.string.counts, program.dayCount, program.exerciseCount), style = MaterialTheme.typography.bodyMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(PantherSpacing.small)) {
                Text(stringResource(if (program.isArchived) R.string.status_archived else if (program.isActive) R.string.status_active else R.string.status_inactive), color = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.created_at, java.text.DateFormat.getDateInstance().format(java.util.Date(program.createdAt))), style = MaterialTheme.typography.bodySmall)
            }
        } else if (!programs.loading) Text(stringResource(R.string.program_not_found), color = MaterialTheme.colorScheme.error)
        if (days.loading) Text(stringResource(R.string.loading))
        if (days.error || viewModelError(viewModel)) Text(stringResource(R.string.error_generic), color = MaterialTheme.colorScheme.error)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            PantherSectionHeader(stringResource(R.string.training_days))
            TextButton(onClick = { dayName = ""; invalid = false; addDay = true }) { Text(stringResource(R.string.add_day)) }
        }
        if (days.value.isEmpty() && !days.loading) Text(stringResource(R.string.no_days))
        days.value.forEachIndexed { index, day ->
            TrainingDayEditorCard(day, index, days.value, viewModel)
        }
    }
    if (addDay) AlertDialog(
        onDismissRequest = { addDay = false },
        title = { Text(stringResource(R.string.add_day)) },
        text = { Column { PantherTextField(dayName, { dayName = it; invalid = false }, stringResource(R.string.day_name), Modifier.fillMaxWidth(), isError = invalid); if (invalid) Text(stringResource(R.string.error_name), color = MaterialTheme.colorScheme.error) } },
        confirmButton = { TextButton(onClick = { if (!WorkoutValidation.name(dayName)) invalid = true else { viewModel.addDay(programId, dayName, days.value.size); addDay = false } }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = { addDay = false }) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
private fun viewModelError(viewModel: ProgramsViewModel): Boolean {
    val error by viewModel.error.collectAsStateWithLifecycle()
    return error
}

@Composable
private fun TrainingDayEditorCard(day: TrainingDay, dayIndex: Int, allDays: List<TrainingDay>, viewModel: ProgramsViewModel) {
    val exercises by viewModel.exercises(day.id).collectAsStateWithLifecycle(initialValue = DataState(emptyList()))
    var editName by rememberSaveable(day.id) { mutableStateOf(day.name) }
    var renameDialog by rememberSaveable(day.id) { mutableStateOf(false) }
    var deleteDialog by rememberSaveable(day.id) { mutableStateOf(false) }
    var showExerciseEditor by rememberSaveable(day.id) { mutableStateOf(false) }
    var currentExercise by remember { mutableStateOf<Exercise?>(null) }
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }
    var invalid by remember { mutableStateOf(false) }

    PantherCard(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(day.name, style = MaterialTheme.typography.titleLarge)
            Row {
                IconButton(onClick = { if (dayIndex > 0) viewModel.reorderDays(day.programId, allDays.map { it.id }.toMutableList().apply { add(dayIndex - 1, removeAt(dayIndex)) }) }, enabled = dayIndex > 0) { Text("↑") }
                IconButton(onClick = { if (dayIndex < allDays.lastIndex) viewModel.reorderDays(day.programId, allDays.map { it.id }.toMutableList().apply { add(dayIndex + 1, removeAt(dayIndex)) }) }, enabled = dayIndex < allDays.lastIndex) { Text("↓") }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = { editName = day.name; invalid = false; renameDialog = true }) { Text(stringResource(R.string.edit)) }
            TextButton(onClick = { deleteDialog = true }) { Text(stringResource(R.string.delete)) }
            TextButton(onClick = { currentExercise = null; invalid = false; showExerciseEditor = true }) { Text(stringResource(R.string.add_exercise)) }
        }
        if (exercises.loading) Text(stringResource(R.string.loading))
        exercises.value.forEachIndexed { index, exercise ->
            ExerciseEditorRow(exercise, index, exercises.value, viewModel,
                onEdit = { currentExercise = exercise; invalid = false; showExerciseEditor = true },
                onDelete = { exerciseToDelete = exercise })
        }
    }
    if (renameDialog) AlertDialog(onDismissRequest = { renameDialog = false }, title = { Text(stringResource(R.string.edit_day)) }, text = {
        Column { PantherTextField(editName, { editName = it; invalid = false }, stringResource(R.string.day_name), Modifier.fillMaxWidth(), isError = invalid); if (invalid) Text(stringResource(R.string.error_name), color = MaterialTheme.colorScheme.error) }
    }, confirmButton = { TextButton(onClick = { if (!WorkoutValidation.name(editName)) invalid = true else { viewModel.renameDay(day.id, editName); renameDialog = false } }) { Text(stringResource(R.string.save)) } }, dismissButton = { TextButton(onClick = { renameDialog = false }) { Text(stringResource(R.string.cancel)) } })
    if (deleteDialog) AlertDialog(onDismissRequest = { deleteDialog = false }, title = { Text(stringResource(R.string.delete)) }, text = { Text(stringResource(R.string.confirm_delete_day)) }, confirmButton = { TextButton(onClick = { viewModel.deleteDay(day.id); deleteDialog = false }) { Text(stringResource(R.string.delete)) } }, dismissButton = { TextButton(onClick = { deleteDialog = false }) { Text(stringResource(R.string.cancel)) } })
    if (exerciseToDelete != null) AlertDialog(onDismissRequest = { exerciseToDelete = null }, title = { Text(stringResource(R.string.delete)) }, text = { Text(stringResource(R.string.confirm_delete_exercise)) }, confirmButton = { TextButton(onClick = { exerciseToDelete?.let { viewModel.deleteExercise(it.id) }; exerciseToDelete = null }) { Text(stringResource(R.string.delete)) } }, dismissButton = { TextButton(onClick = { exerciseToDelete = null }) { Text(stringResource(R.string.cancel)) } })
    if (showExerciseEditor) ExerciseDialog(
        initial = currentExercise,
        exerciseFlow = currentExercise?.let { viewModel.sets(it.id) },
        invalid = invalid,
        onInvalid = { invalid = it },
        onDismiss = { showExerciseEditor = false },
        onSave = { name, sets -> viewModel.saveExercise(day.id, currentExercise?.id, name, currentExercise?.sortOrder ?: exercises.value.size, sets); showExerciseEditor = false },
    )
}

@Composable
private fun ExerciseEditorRow(exercise: Exercise, index: Int, all: List<Exercise>, viewModel: ProgramsViewModel, onEdit: () -> Unit, onDelete: () -> Unit) {
    val sets by viewModel.sets(exercise.id).collectAsStateWithLifecycle(initialValue = emptyList())
    HorizontalDivider(Modifier.padding(vertical = 6.dp))
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(exercise.name, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.set_count, sets.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = { if (index > 0) viewModel.reorderExercises(exercise.trainingDayId, all.map { it.id }.toMutableList().apply { add(index - 1, removeAt(index)) }) }, enabled = index > 0) { Text("↑") }
        IconButton(onClick = { if (index < all.lastIndex) viewModel.reorderExercises(exercise.trainingDayId, all.map { it.id }.toMutableList().apply { add(index + 1, removeAt(index)) }) }, enabled = index < all.lastIndex) { Text("↓") }
    }
    Row { TextButton(onClick = onEdit) { Text(stringResource(R.string.edit)) }; TextButton(onClick = onDelete) { Text(stringResource(R.string.delete)) } }
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
    val initialSets by (exerciseFlow ?: remember { kotlinx.coroutines.flow.flowOf(emptyList()) }).collectAsStateWithLifecycle(initialValue = emptyList())
    var name by rememberSaveable(initial?.id) { mutableStateOf(initial?.name.orEmpty()) }
    var targetType by rememberSaveable(initial?.id) { mutableStateOf(initialSets.firstOrNull()?.targetType ?: TargetType.REPS) }
    var targetStrings by rememberSaveable(initial?.id) { mutableStateOf(initialSets.map { if (it.targetType == TargetType.REPS) it.targetValue.toString() else "${it.targetMinutes}:${it.targetSeconds.toString().padStart(2, '0')}" }.ifEmpty { listOf("10", "10", "10") }) }
    // Existing target data arrives asynchronously; initialize it once when editing an exercise.
    LaunchedEffect(initialSets) {
        if (initialSets.isNotEmpty() && targetStrings.size == 3 && initial != null) {
            targetType = initialSets.first().targetType
            targetStrings = initialSets.map { if (it.targetType == TargetType.REPS) it.targetValue.toString() else "${it.targetMinutes}:${it.targetSeconds.toString().padStart(2, '0')}" }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (initial == null) R.string.add_exercise else R.string.edit_exercise)) },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(PantherSpacing.small)) {
                PantherTextField(name, { name = it; onInvalid(false) }, stringResource(R.string.exercise_name), Modifier.fillMaxWidth(), isError = invalid)
                Text(stringResource(R.string.target_type), style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = targetType == TargetType.REPS, onClick = { if (targetType != TargetType.REPS) { targetType = TargetType.REPS; targetStrings = List(targetStrings.size) { "10" } } }, label = { Text(stringResource(R.string.repetitions)) })
                    FilterChip(selected = targetType == TargetType.DURATION, onClick = { if (targetType != TargetType.DURATION) { targetType = TargetType.DURATION; targetStrings = List(targetStrings.size) { "0:30" } } }, label = { Text(stringResource(R.string.duration)) })
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.set_count_label))
                    IconButton(onClick = { if (targetStrings.size > 1) targetStrings = targetStrings.dropLast(1) }, enabled = targetStrings.size > 1) { Text("−") }
                    Text(targetStrings.size.toString())
                    IconButton(onClick = { if (targetStrings.size < 30) targetStrings = targetStrings + if (targetType == TargetType.REPS) "10" else "0:30" }, enabled = targetStrings.size < 30) { Text("+") }
                }
                targetStrings.forEachIndexed { index, target ->
                    PantherTextField(
                        value = target,
                        onValueChange = { value -> targetStrings = targetStrings.toMutableList().also { it[index] = value }; onInvalid(false) },
                        label = stringResource(if (targetType == TargetType.REPS) R.string.set_repetitions else R.string.set_duration),
                        modifier = Modifier.fillMaxWidth(), isError = invalid,
                        keyboardOptions = KeyboardOptions(keyboardType = if (targetType == TargetType.REPS) KeyboardType.Number else KeyboardType.Ascii),
                    )
                }
                if (invalid) Text(stringResource(R.string.error_exercise), color = MaterialTheme.colorScheme.error)
                Text(stringResource(R.string.individual_targets_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = { TextButton(onClick = {
            val sets = targetStrings.mapIndexedNotNull { index, raw ->
                if (targetType == TargetType.REPS) normalizedTarget(raw).toIntOrNull()?.let { ExerciseSet(exerciseId = initial?.id ?: 0L, setNumber = index + 1, targetType = targetType, targetValue = it) }
                else {
                    val parts = normalizedTarget(raw).split(":")
                    val minutes = parts.getOrNull(0)?.toIntOrNull(); val seconds = parts.getOrNull(1)?.toIntOrNull()
                    if (parts.size == 2 && minutes != null && seconds != null) ExerciseSet(exerciseId = initial?.id ?: 0L, setNumber = index + 1, targetType = targetType, targetMinutes = minutes, targetSeconds = seconds) else null
                }
            }
            val valid = WorkoutValidation.name(name) && sets.size == targetStrings.size && WorkoutValidation.sets(sets)
            onInvalid(!valid)
            if (valid) onSave(name, sets)
        }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
