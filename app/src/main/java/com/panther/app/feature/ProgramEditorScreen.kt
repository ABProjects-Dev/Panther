package com.panther.app.feature

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panther.app.R
import com.panther.app.core.designsystem.components.*
import com.panther.app.core.designsystem.theme.LocalPantherColors
import com.panther.app.core.designsystem.theme.PantherSpacing
import com.panther.app.core.format.PantherDateFormat
import com.panther.app.domain.model.PantherBackup
import com.panther.app.domain.model.TrainingDay
import com.panther.app.domain.model.WorkoutProgram
import com.panther.app.domain.model.WorkoutValidation
import kotlinx.coroutines.delay

/**
 * The program page: header, the program's own settings, one independent card per training day, then
 * the page-level destructive action. Each day opens its own screen (`day/{dayId}`) instead of a tab.
 */
@Composable
fun ProgramEditorScreen(
    viewModel: ProgramsViewModel,
    settings: SettingsViewModel,
    programId: Long,
    onBack: () -> Unit,
    onOpenDay: (dayId: Long, programId: Long) -> Unit,
) {
    val programsState by viewModel.programs.collectAsStateWithLifecycle()
    val daysFlow = remember(programId) { viewModel.days(programId) }
    val daysState by daysFlow.collectAsStateWithLifecycle(initialValue = DataState(emptyList(), loading = true))
    val vmError by viewModel.error.collectAsStateWithLifecycle()
    val pdfMessage by settings.pdfMessage.collectAsStateWithLifecycle()
    val program = programsState.value.firstOrNull { it.id == programId }

    // `editor/{id}` can outlive the program it points at (deleted from another screen, a stale
    // deep link, or a malformed id). Show an explicit message instead of an empty editor whose
    // actions would all target a row that does not exist.
    if (editorAvailability(programsState, programId) == EditorAvailability.MISSING) {
        ProgramNotFoundScreen(onBack = onBack)
        return
    }

    val activeColors = LocalPantherColors.current

    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) settings.exportProgramPdf(programId, uri)
    }

    LaunchedEffect(vmError) {
        if (vmError) {
            delay(4000)
            viewModel.clearError()
        }
    }

    LaunchedEffect(pdfMessage) {
        if (pdfMessage != null) {
            delay(3500)
            settings.clearPdfMessage()
        }
    }

    val daysList = daysState.value
    val canAddDay = daysList.size < PantherBackup.MAX_DAYS_PER_PROGRAM

    // A program created as an empty "room" first asks for its name. Leaving without choosing one
    // discards the room instead of keeping a nameless program (task 5).
    var nameChosen by rememberSaveable(programId) { mutableStateOf(program?.name != PENDING_PROGRAM_NAME) }
    LaunchedEffect(program?.name) {
        val currentName = program?.name
        if (currentName != null && currentName != PENDING_PROGRAM_NAME) nameChosen = true
    }
    val exitWithoutSaving: () -> Unit = {
        val current = programsState.value.firstOrNull { it.id == programId }
        if (current != null && current.name == PENDING_PROGRAM_NAME) {
            viewModel.delete(programId, onDeleted = onBack)
        } else {
            onBack()
        }
    }

    var showAddDayDialog by rememberSaveable { mutableStateOf(false) }
    var newDayName by rememberSaveable { mutableStateOf("") }
    var showProgramSettings by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PantherSpacing.medium, vertical = PantherSpacing.small),
        verticalArrangement = Arrangement.spacedBy(PantherSpacing.compact),
    ) {
        if (program == null) {
            // Only the first, still-loading emission reaches this point; a program that is really
            // gone is a terminal state rendered by `ProgramNotFoundScreen` above.
            PantherTopBar(
                title = stringResource(R.string.loading),
                onBack = exitWithoutSaving,
            )
            return@Column
        }

        PantherTopBar(
            title = if (program.name == PENDING_PROGRAM_NAME) {
                stringResource(R.string.program_name_unset)
            } else {
                program.name
            },
            subtitle = stringResource(
                R.string.counts,
                PantherDateFormat.persianNumber(program.dayCount),
                PantherDateFormat.persianNumber(program.exerciseCount),
            ),
            onBack = exitWithoutSaving,
            actions = {
                IconButton(onClick = { showProgramSettings = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
        )

        if (!nameChosen) {
            // The card stays until the rename actually lands in the database, so a failed save keeps
            // asking for the name instead of silently leaving an unnamed program behind.
            ProgramNameCard(onSaveName = { chosen -> viewModel.rename(programId, chosen) })
        }

        pdfMessage?.let { msg ->
            Text(
                text = stringResource(msg),
                style = MaterialTheme.typography.bodySmall,
                color = if (msg == R.string.error_generic) {
                    MaterialTheme.colorScheme.error
                } else {
                    activeColors.activeGreen
                },
            )
        }

        if (daysState.error || vmError) {
            Text(stringResource(R.string.error_generic), color = MaterialTheme.colorScheme.error)
        }

        val toggleDesc = stringResource(R.string.toggle_program_active, program.name)
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
                        text = stringResource(R.string.created_at, PantherDateFormat.medium(program.createdAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(PantherSpacing.small))
                Switch(
                    checked = program.isActive,
                    onCheckedChange = { active -> viewModel.setActive(program.id, active) },
                    modifier = Modifier.semantics { contentDescription = toggleDesc },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = activeColors.onActiveGreen,
                        checkedTrackColor = activeColors.activeGreen,
                        checkedBorderColor = activeColors.activeGreen,
                    ),
                )
            }
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
                enabled = canAddDay,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.add_day))
            }
        }

        if (!canAddDay) {
            Text(
                text = stringResource(R.string.limit_days_reached),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Scroll area: the day cards, plus the page-level destructive action at the very end — the
        // button stays reachable by scrolling even when the cards are taller than the viewport.
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(PantherSpacing.small),
        ) {
            if (daysState.loading && daysList.isEmpty()) {
                Text(stringResource(R.string.loading))
            } else if (daysList.isEmpty()) {
                PantherCard {
                    Text(
                        text = stringResource(R.string.no_days),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                daysList.forEachIndexed { index, day ->
                    key(day.id) {
                        TrainingDayCard(
                            index = index,
                            day = day,
                            isProgramActive = program.isActive,
                            showReorder = daysList.size > 1,
                            canMoveEarlier = index > 0,
                            canMoveLater = index < daysList.lastIndex,
                            onOpen = { onOpenDay(day.id, programId) },
                            onMoveEarlier = {
                                val reordered = daysList.map { it.id }.toMutableList().apply {
                                    val moved = removeAt(index)
                                    add(index - 1, moved)
                                }
                                viewModel.reorderDays(programId, reordered)
                            },
                            onMoveLater = {
                                val reordered = daysList.map { it.id }.toMutableList().apply {
                                    val moved = removeAt(index)
                                    add(index + 1, moved)
                                }
                                viewModel.reorderDays(programId, reordered)
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(PantherSpacing.small))
            PantherDeleteButton(
                text = stringResource(R.string.delete_program_action),
                confirmTitle = stringResource(R.string.delete_program),
                confirmMessage = stringResource(R.string.confirm_delete),
                onConfirm = { viewModel.delete(programId, onDeleted = onBack) },
            )
            Spacer(Modifier.height(PantherSpacing.small))
        }
    }

    if (showProgramSettings) {
        ProgramSettingsDialog(
            program = program,
            onDismiss = { showProgramSettings = false },
            onSave = { newName ->
                viewModel.rename(program.id, newName)
                showProgramSettings = false
            },
            onExportPdf = {
                showProgramSettings = false
                pdfLauncher.launch("${program.name}.pdf")
            },
        )
    }

    if (showAddDayDialog) {
        var invalid by rememberSaveable { mutableStateOf(false) }
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
                            // Open the freshly created day straight away: the repository returns the
                            // generated id, so no name matching or pending state is needed.
                            viewModel.addDay(
                                onCreated = { newDayId -> onOpenDay(newDayId, programId) },
                                programId = programId,
                                name = newDayName,
                                order = daysList.size,
                            )
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

/** One training day as its own card. Shared by the program page and the read-only home dashboard. */
@Composable
internal fun TrainingDayCard(
    index: Int,
    day: TrainingDay,
    isProgramActive: Boolean,
    onOpen: () -> Unit,
    showReorder: Boolean = false,
    canMoveEarlier: Boolean = false,
    canMoveLater: Boolean = false,
    onMoveEarlier: () -> Unit = {},
    onMoveLater: () -> Unit = {},
) {
    val activeColors = LocalPantherColors.current
    val openLabel = stringResource(R.string.open_day)
    val numberLabel = stringResource(R.string.exercise_number_desc, PantherDateFormat.persianNumber(index + 1))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .semantics { contentDescription = "$openLabel — ${day.name}, $numberLabel" },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            if (isProgramActive) {
                activeColors.activeGreen.copy(alpha = 0.45f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PantherSpacing.medium, vertical = PantherSpacing.compact),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(PantherSpacing.small),
            ) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (isProgramActive) {
                        activeColors.activeGreen.copy(alpha = 0.16f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                ) {
                    Text(
                        text = PantherDateFormat.persianNumber(index + 1),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = day.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = openLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showReorder) {
                    DayMoveControls(
                        canMoveEarlier = canMoveEarlier,
                        canMoveLater = canMoveLater,
                        onMoveEarlier = onMoveEarlier,
                        onMoveLater = onMoveLater,
                    )
                }
                // Mirrored chevron: points at the start of the reading direction (RTL: left).
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}

@Composable
private fun DayMoveControls(
    canMoveEarlier: Boolean,
    canMoveLater: Boolean,
    onMoveEarlier: () -> Unit,
    onMoveLater: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onMoveEarlier, enabled = canMoveEarlier) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = stringResource(R.string.move_day_earlier),
                modifier = Modifier.size(18.dp),
            )
        }
        IconButton(onClick = onMoveLater, enabled = canMoveLater) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = stringResource(R.string.move_day_later),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

/** Program-level settings kept off the main flow: rename and PDF export. */
@Composable
private fun ProgramSettingsDialog(
    program: WorkoutProgram,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onExportPdf: () -> Unit,
) {
    var name by rememberSaveable(program.id) { mutableStateOf(program.name) }
    var invalid by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_program)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(PantherSpacing.small)) {
                PantherTextField(
                    value = name,
                    onValueChange = { name = it; invalid = false },
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
                OutlinedButton(onClick = onExportPdf, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.export_pdf))
                }
                Text(
                    text = stringResource(R.string.delete_program_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (!WorkoutValidation.name(name)) invalid = true else onSave(name)
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
}

/**
 * Inline "choose the program name" card for a freshly created room. Saving the name keeps the
 * program; going back without saving deletes it, so no nameless program is ever stored (task 5).
 */
@Composable
private fun ProgramNameCard(onSaveName: (String) -> Unit) {
    var value by rememberSaveable { mutableStateOf("") }
    var invalid by rememberSaveable { mutableStateOf(false) }

    PantherCard {
        Text(
            text = stringResource(R.string.program_name_prompt),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(PantherSpacing.small))
        PantherTextField(
            value = value,
            onValueChange = { value = it; invalid = false },
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
        Spacer(Modifier.height(PantherSpacing.small))
        Button(
            onClick = {
                if (!WorkoutValidation.name(value)) invalid = true else onSaveName(value)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.save_program_name), fontWeight = FontWeight.SemiBold)
        }
    }
}

/** Terminal state for `editor/{id}` when the program behind the id does not exist. */
@Composable
fun ProgramNotFoundScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = PantherSpacing.medium, vertical = PantherSpacing.small),
        verticalArrangement = Arrangement.spacedBy(PantherSpacing.compact),
    ) {
        PantherTopBar(
            title = stringResource(R.string.program_not_found),
            onBack = onBack,
        )
        PantherCard {
            // The message itself is the header title; this card carries the recovery hint. Keeping
            // one node per string also keeps the instrumented assertion unambiguous.
            Text(
                text = stringResource(R.string.program_not_found_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
