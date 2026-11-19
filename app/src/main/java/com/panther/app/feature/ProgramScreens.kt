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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.AbsoluteAlignment
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
import com.panther.app.domain.model.*
import kotlinx.coroutines.delay

/**
 * Placeholder stored while a new program has no name yet. It is what the editor treats as "empty",
 * and a program that still carries it when the user leaves is deleted instead of saved.
 */
internal const val PENDING_PROGRAM_NAME = "—"

@Composable
private fun ErrorNotice(error: Boolean) {
    if (error) Text(stringResource(R.string.error_generic), color = MaterialTheme.colorScheme.error)
}

@Composable
fun HomeScreen(
    programs: ProgramsViewModel,
    profile: ProfileViewModel,
    onOpenDay: (dayId: Long, programId: Long) -> Unit,
) {
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
                        onOpenDay = onOpenDay,
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
    onOpenDay: (dayId: Long, programId: Long) -> Unit,
) {
    val activeColors = LocalPantherColors.current
    val daysFlow = remember(program.id) { viewModel.days(program.id) }
    val daysState by daysFlow.collectAsStateWithLifecycle(initialValue = DataState(emptyList(), loading = true))
    val daysList = daysState.value

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
            // Each training day is its own card and opens its own screen, exactly like the program.
            Column(verticalArrangement = Arrangement.spacedBy(PantherSpacing.small)) {
                daysList.forEachIndexed { index, day ->
                    key(day.id) {
                        TrainingDayCard(
                            index = index,
                            day = day,
                            isProgramActive = true,
                            onOpen = { onOpenDay(day.id, program.id) },
                        )
                    }
                }
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
    // Creating a program no longer asks for a name here: it opens the room (the program page) and
    // the name is chosen there. A program whose name was never chosen is discarded on exit (task 5).
    var pendingProgramId by rememberSaveable { mutableStateOf<Long?>(null) }
    var pendingFailed by rememberSaveable { mutableStateOf(false) }
    val canCreateProgram = state.value.size < PantherBackup.MAX_PROGRAMS

    LaunchedEffect(error) {
        if (error) {
            delay(4000)
            viewModel.clearError()
        }
    }

    LaunchedEffect(pendingProgramId, state.value, state.loading, state.error) {
        val target = pendingProgramId ?: return@LaunchedEffect
        if (state.value.any { it.id == target }) {
            pendingProgramId = null
            onDetails(target)
        } else if (!state.loading && !state.error) {
            // Loaded without the new row: the insert failed. Clear the placeholder instead of
            // leaving the button stuck in a "creating" state.
            pendingProgramId = null
            pendingFailed = true
        }
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
            if (state.error || error || pendingFailed) {
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
                    pendingFailed = false
                    // Placeholder name, replaced by the user on the program page. It is never kept
                    // if the user leaves without choosing one.
                    viewModel.createPendingProgram { createdId -> pendingProgramId = createdId }
                },
                modifier = Modifier
                    .align(AbsoluteAlignment.BottomLeft)
                    .padding(PantherSpacing.medium),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(PantherSpacing.small))
                Text(stringResource(R.string.new_program), fontWeight = FontWeight.SemiBold)
            }
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
                        text = if (program.name == PENDING_PROGRAM_NAME) {
                            stringResource(R.string.program_name_unset)
                        } else {
                            program.name
                        },
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
