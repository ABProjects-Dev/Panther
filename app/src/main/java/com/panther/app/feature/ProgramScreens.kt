package com.panther.app.feature

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.panther.app.domain.model.*
import kotlinx.coroutines.delay

@Composable
private fun ErrorNotice(error: Boolean) {
    if (error) Text(stringResource(R.string.error_generic), color = MaterialTheme.colorScheme.error)
}

@Composable
fun HomeScreen(programs: ProgramsViewModel, profile: ProfileViewModel) {
    val programState by programs.programs.collectAsStateWithLifecycle()
    val profileState by profile.profile.collectAsStateWithLifecycle()
    val user = profileState.value
    val activeProgram = programState.value.firstOrNull { it.isActive }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(PantherSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(PantherSpacing.medium),
    ) {
        item {
            PantherBrandHeader(subtitle = stringResource(R.string.foundation_message))
        }
        item {
            Text(
                text = stringResource(R.string.greeting, user?.name ?: stringResource(R.string.guest)),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        item {
            BmiDashboardCard(heightCm = user?.heightCm, weightKg = user?.weightKg)
        }
        item {
            PantherSectionHeader(stringResource(R.string.active_program))
        }
        if (programState.loading || profileState.loading) {
            item { Text(stringResource(R.string.loading)) }
        }
        if (programState.error || profileState.error) {
            item { ErrorNotice(true) }
        }
        if (!programState.loading && !programState.error) {
            if (activeProgram == null) {
                item {
                    PantherCard {
                        Text(
                            text = stringResource(R.string.no_active),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                item(key = activeProgram.id) {
                    ReadOnlyActiveProgramSection(
                        program = activeProgram,
                        viewModel = programs,
                    )
                }
            }
        }
    }
}

@Composable
private fun BmiDashboardCard(heightCm: Double?, weightKg: Double?) {
    val bmi = remember(heightCm, weightKg) { BmiCalculator.calculate(heightCm, weightKg) }
    val activeColors = LocalPantherColors.current

    PantherCard(
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.bmi_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (bmi != null) {
                val categoryLabel = stringResource(
                    when (bmi.category) {
                        BmiCategory.UNDERWEIGHT -> R.string.bmi_underweight
                        BmiCategory.NORMAL -> R.string.bmi_normal
                        BmiCategory.OVERWEIGHT -> R.string.bmi_overweight
                        BmiCategory.OBESE -> R.string.bmi_obese
                    },
                )
                val badgeColor = when (bmi.category) {
                    BmiCategory.NORMAL -> activeColors.activeGreen
                    BmiCategory.UNDERWEIGHT -> MaterialTheme.colorScheme.primary
                    BmiCategory.OVERWEIGHT -> MaterialTheme.colorScheme.tertiary
                    BmiCategory.OBESE -> MaterialTheme.colorScheme.error
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = badgeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.55f)),
                ) {
                    Text(
                        text = categoryLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = badgeColor,
                    )
                }
            }
        }
        Spacer(Modifier.height(PantherSpacing.small))
        if (bmi == null) {
            Text(
                text = stringResource(R.string.bmi_missing),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = PantherDateFormat.persianDigits(String.format(java.util.Locale.US, "%.1f", bmi.value)),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(PantherSpacing.extraSmall))
            val formattedHeight = PantherDateFormat.persianDigits(formatDecimalTrimmed(heightCm ?: 0.0))
            val formattedWeight = PantherDateFormat.persianDigits(formatDecimalTrimmed(weightKg ?: 0.0))
            Text(
                text = stringResource(R.string.bmi_metrics, formattedHeight, formattedWeight),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatDecimalTrimmed(value: Double): String =
    if (value % 1.0 == 0.0) value.toLong().toString()
    else String.format(java.util.Locale.US, "%.1f", value)

@Composable
private fun ReadOnlyActiveProgramSection(
    program: WorkoutProgram,
    viewModel: ProgramsViewModel,
) {
    val activeColors = LocalPantherColors.current
    val daysFlow = remember(program.id) { viewModel.days(program.id) }
    val daysState by daysFlow.collectAsStateWithLifecycle(initialValue = DataState(emptyList(), loading = true))
    val daysList = daysState.value
    var selectedDayId by rememberSaveable(program.id) { mutableStateOf<Long?>(null) }
    val selectedIndex = remember(daysList, selectedDayId) {
        val idx = daysList.indexOfFirst { it.id == selectedDayId }
        if (idx >= 0) idx else 0
    }
    LaunchedEffect(daysList, selectedIndex) {
        if (daysList.isNotEmpty()) {
            selectedDayId = daysList[selectedIndex].id
        }
    }

    PantherCard(
        containerColor = activeColors.activeGreenContainer,
        border = BorderStroke(1.5.dp, activeColors.activeGreen),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = program.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(PantherSpacing.small))
            PantherActiveBadge()
        }
        Spacer(Modifier.height(PantherSpacing.extraSmall))
        Text(
            text = stringResource(
                R.string.counts,
                PantherDateFormat.persianNumber(program.dayCount),
                PantherDateFormat.persianNumber(program.exerciseCount),
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.created_at, PantherDateFormat.medium(program.createdAt)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(PantherSpacing.compact))
        if (daysState.loading) {
            Text(stringResource(R.string.loading), style = MaterialTheme.typography.bodySmall)
        } else if (daysList.isEmpty()) {
            Text(
                text = stringResource(R.string.no_days),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            ScrollableTabRow(
                selectedTabIndex = selectedIndex,
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                contentColor = activeColors.activeGreen,
                indicator = { tabPositions ->
                    if (selectedIndex in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                            color = activeColors.activeGreen,
                        )
                    }
                },
                divider = {
                    HorizontalDivider(color = activeColors.activeGreen.copy(alpha = 0.25f))
                },
            ) {
                daysList.forEachIndexed { index, day ->
                    Tab(
                        selected = selectedIndex == index,
                        onClick = { selectedDayId = day.id },
                        selectedContentColor = activeColors.activeGreen,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = {
                            Text(
                                text = day.name,
                                fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium,
                            )
                        },
                    )
                }
            }
            Spacer(Modifier.height(PantherSpacing.compact))
            val currentDay = daysList[selectedIndex]
            key(currentDay.id) {
                ReadOnlyTrainingDayExercises(day = currentDay, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun ReadOnlyTrainingDayExercises(day: TrainingDay, viewModel: ProgramsViewModel) {
    val exercisesFlow = remember(day.id) { viewModel.exercisesWithSets(day.id) }
    val exercisesState by exercisesFlow.collectAsStateWithLifecycle(initialValue = DataState(emptyList(), loading = true))
    if (exercisesState.loading) {
        Text(stringResource(R.string.loading), style = MaterialTheme.typography.bodySmall)
    } else if (exercisesState.value.isEmpty()) {
        Text(
            text = stringResource(R.string.no_exercises_in_day),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(PantherSpacing.small)) {
            exercisesState.value.forEach { item ->
                key(item.exercise.id) {
                    ReadOnlyExerciseItem(item = item)
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyExerciseItem(item: ExerciseWithSets) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, LocalPantherColors.current.activeGreen.copy(alpha = 0.25f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PantherSpacing.compact),
            verticalArrangement = Arrangement.spacedBy(PantherSpacing.small),
        ) {
            Text(
                text = item.exercise.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            if (item.sets.isNotEmpty()) {
                ExerciseSetsSquares(sets = item.sets, highlightGreen = true)
            }
        }
    }
}

@Composable
fun ProgramsScreen(
    viewModel: ProgramsViewModel,
    onDetails: (Long) -> Unit,
) {
    val state by viewModel.programs.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var newProgramName by rememberSaveable { mutableStateOf("") }
    val canCreateProgram = state.value.size < PantherBackup.MAX_PROGRAMS

    LaunchedEffect(error) {
        if (error) {
            delay(4000)
            viewModel.clearError()
        }
    }

    if (showCreateDialog) {
        var invalid by rememberSaveable { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text(stringResource(R.string.new_program)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(PantherSpacing.extraSmall)) {
                    PantherTextField(
                        value = newProgramName,
                        onValueChange = { newProgramName = it; invalid = false },
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
                        if (!WorkoutValidation.name(newProgramName)) {
                            invalid = true
                        } else {
                            viewModel.create(newProgramName)
                            showCreateDialog = false
                        }
                    },
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = PantherSpacing.medium,
                end = PantherSpacing.medium,
                top = PantherSpacing.medium,
                bottom = 92.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(PantherSpacing.small),
        ) {
            item {
                PantherBrandHeader(subtitle = stringResource(R.string.programs))
                Spacer(Modifier.height(PantherSpacing.small))
            }
            if (state.loading) {
                item { Text(stringResource(R.string.loading)) }
            }
            if (state.error || error) {
                item { ErrorNotice(true) }
            }
            if (!canCreateProgram) {
                item {
                    Text(
                        text = stringResource(R.string.limit_programs_reached),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (!state.loading && !state.error && state.value.isEmpty()) {
                item {
                    PantherCard {
                        Text(
                            text = stringResource(R.string.no_programs),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            items(state.value, key = { it.id }) { program ->
                CompactProgramCard(
                    program = program,
                    onClick = { onDetails(program.id) },
                    onToggleActive = { active -> viewModel.setActive(program.id, active) },
                )
            }
        }

        if (canCreateProgram) {
            ExtendedFloatingActionButton(
                onClick = {
                    newProgramName = ""
                    showCreateDialog = true
                },
                modifier = Modifier
                    .align(AbsoluteAlignment.BottomLeft)
                    .padding(PantherSpacing.medium),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.new_program), fontWeight = FontWeight.SemiBold) },
            )
        }
    }
}

@Composable
private fun CompactProgramCard(
    program: WorkoutProgram,
    onClick: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
) {
    val activeColors = LocalPantherColors.current
    val toggleLabel = stringResource(R.string.toggle_program_active, program.name)
    val containerColor = if (program.isActive) {
        activeColors.activeGreenContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    val border = if (program.isActive) {
        BorderStroke(1.5.dp, activeColors.activeGreen)
    } else {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = border,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = PantherSpacing.medium, vertical = PantherSpacing.compact),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(PantherSpacing.small),
                ) {
                    Text(
                        text = program.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (program.isActive) {
                        PantherActiveBadge()
                    }
                }
                Text(
                    text = stringResource(
                        R.string.counts,
                        PantherDateFormat.persianNumber(program.dayCount),
                        PantherDateFormat.persianNumber(program.exerciseCount),
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.created_at, PantherDateFormat.medium(program.createdAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(PantherSpacing.compact))
            Switch(
                checked = program.isActive,
                onCheckedChange = onToggleActive,
                modifier = Modifier.semantics { contentDescription = toggleLabel },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = activeColors.onActiveGreen,
                    checkedTrackColor = activeColors.activeGreen,
                    checkedBorderColor = activeColors.activeGreen,
                ),
            )
        }
    }
}
