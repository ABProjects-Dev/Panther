package com.panther.app.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panther.app.R
import com.panther.app.core.designsystem.components.*
import com.panther.app.core.designsystem.theme.PantherSpacing
import com.panther.app.domain.model.WorkoutProgram
import com.panther.app.domain.model.WorkoutValidation
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@Composable
private fun Screen(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = modifier.fillMaxSize().padding(horizontal = PantherSpacing.medium), content = content)
}

@Composable
private fun ProgramCard(program: WorkoutProgram, onClick: () -> Unit, actions: @Composable ColumnScope.() -> Unit = {}) {
    PantherCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Text(program.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(PantherSpacing.small))
        Text(stringResource(if (program.isArchived) R.string.status_archived else if (program.isActive) R.string.status_active else R.string.status_inactive), color = MaterialTheme.colorScheme.primary)
        Text(stringResource(R.string.counts, program.dayCount, program.exerciseCount), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.created_at, DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.forLanguageTag("fa-IR")).format(Date(program.createdAt))), style = MaterialTheme.typography.bodySmall)
        actions()
    }
}

@Composable
private fun ErrorNotice(error: Boolean) { if (error) Text(stringResource(R.string.error_generic), color = MaterialTheme.colorScheme.error) }

@Composable
fun HomeScreen(programs: ProgramsViewModel, profile: ProfileViewModel, onCreate: () -> Unit, onDetails: (Long) -> Unit) {
    val programState by programs.programs.collectAsStateWithLifecycle()
    val profileState by profile.profile.collectAsStateWithLifecycle()
    LazyColumn(contentPadding = PaddingValues(PantherSpacing.medium), verticalArrangement = Arrangement.spacedBy(PantherSpacing.medium)) {
        item {
            Text(stringResource(R.string.greeting, profileState.value?.name ?: stringResource(R.string.guest)), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.foundation_message), style = MaterialTheme.typography.bodyMedium)
        }
        item { ProfileAvatar(profileState.value?.profileImageUri) }
        item { PantherButton(stringResource(R.string.new_program), onCreate, Modifier.fillMaxWidth()) }
        item { PantherSectionHeader(stringResource(R.string.active_program)) }
        if (programState.loading || profileState.loading) item { Text(stringResource(R.string.loading)) }
        if (programState.error || profileState.error) item { ErrorNotice(true) }
        val active = programState.value.firstOrNull { it.isActive }
        if (!programState.loading && !programState.error) {
            if (active == null) item { Text(stringResource(R.string.no_active)) }
            else item { ProgramCard(active, { onDetails(active.id) }) }
        }
        item { PantherSectionHeader(stringResource(R.string.recent_programs)) }
        if (!programState.loading && !programState.error && programState.value.isEmpty()) item { Text(stringResource(R.string.no_programs)) }
        items(programState.value.take(5), key = { it.id }) { program -> ProgramCard(program, { onDetails(program.id) }) }
    }
}

@Composable
fun ProgramsScreen(viewModel: ProgramsViewModel, initiallyCreate: Boolean, onDetails: (Long) -> Unit) {
    val state by viewModel.programs.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    var editName by rememberSaveable { mutableStateOf("") }
    var showEditor by rememberSaveable { mutableStateOf(initiallyCreate) }
    var deleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    if (showEditor) {
        var invalid by remember { mutableStateOf(false) }
        AlertDialog(onDismissRequest = { showEditor = false }, title = { Text(stringResource(if (editingId == null) R.string.new_program else R.string.edit)) }, text = {
            Column {
                PantherTextField(editName, { editName = it; invalid = false }, stringResource(R.string.program_name), isError = invalid)
                if (invalid) Text(stringResource(R.string.error_name), color = MaterialTheme.colorScheme.error)
            }
        }, confirmButton = { TextButton(onClick = {
            if (!WorkoutValidation.name(editName)) invalid = true else {
                val id = editingId
                if (id == null) viewModel.create(editName) else viewModel.rename(id, editName)
                showEditor = false
            }
        }) { Text(stringResource(R.string.save)) } }, dismissButton = { TextButton(onClick = { showEditor = false }) { Text(stringResource(R.string.cancel)) } })
    }
    if (deleteId != null) AlertDialog(onDismissRequest = { deleteId = null }, title = { Text(stringResource(R.string.delete)) }, text = { Text(stringResource(R.string.confirm_delete)) }, confirmButton = { TextButton(onClick = { deleteId?.let(viewModel::delete); deleteId = null }) { Text(stringResource(R.string.delete)) } }, dismissButton = { TextButton(onClick = { deleteId = null }) { Text(stringResource(R.string.cancel)) } })
    LazyColumn(contentPadding = PaddingValues(PantherSpacing.medium), verticalArrangement = Arrangement.spacedBy(PantherSpacing.medium)) {
        item { PantherSectionHeader(stringResource(R.string.programs)) }
        item { PantherButton(stringResource(R.string.new_program), { editingId = null; editName = ""; showEditor = true }, Modifier.fillMaxWidth()) }
        if (state.loading) item { Text(stringResource(R.string.loading)) }
        if (state.error || error) item { ErrorNotice(true) }
        if (!state.loading && !state.error && state.value.isEmpty()) item { Text(stringResource(R.string.no_programs)) }
        items(state.value, key = { it.id }) { p ->
            ProgramCard(p, { onDetails(p.id) }) {
                // Wrapping rows prevents actions from overflowing on narrow screens.
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { editingId = p.id; editName = p.name; showEditor = true }) { Text(stringResource(R.string.edit)) }
                    TextButton(onClick = { deleteId = p.id }) { Text(stringResource(R.string.delete)) }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    if (!p.isArchived) TextButton(onClick = { if (p.isActive) viewModel.deactivate(p.id) else viewModel.activate(p.id) }) { Text(stringResource(if (p.isActive) R.string.deactivate else R.string.activate)) }
                    TextButton(onClick = { viewModel.archive(p.id, !p.isArchived) }) { Text(stringResource(if (p.isArchived) R.string.unarchive else R.string.archive)) }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(viewModel: ProgramsViewModel, onDetails: (Long) -> Unit) {
    val state by viewModel.programs.collectAsStateWithLifecycle()
    LazyColumn(contentPadding = PaddingValues(PantherSpacing.medium), verticalArrangement = Arrangement.spacedBy(PantherSpacing.medium)) {
        item { PantherSectionHeader(stringResource(R.string.history)) }
        if (state.loading) item { Text(stringResource(R.string.loading)) }
        if (state.error) item { ErrorNotice(true) }
        if (!state.loading && !state.error && state.value.isEmpty()) item { Text(stringResource(R.string.no_history)) }
        items(state.value, key = { it.id }) { p -> ProgramCard(p, { onDetails(p.id) }) }
    }
}

@Composable
fun ProgramDetailsScreen(viewModel: ProgramsViewModel, id: Long, onBack: () -> Unit) {
    val state by viewModel.programs.collectAsStateWithLifecycle()
    val daysFlow = remember(id) { viewModel.days(id) }
    val days by daysFlow.collectAsStateWithLifecycle(initialValue = DataState(emptyList()))
    val program = state.value.firstOrNull { it.id == id }
    Screen(modifier = Modifier.verticalScroll(rememberScrollState())) {
        TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
        PantherSectionHeader(stringResource(R.string.details))
        Spacer(Modifier.height(PantherSpacing.medium))
        if (state.loading) Text(stringResource(R.string.loading))
        if (state.error) ErrorNotice(true)
        if (program != null) {
            ProgramCard(program, {})
            Spacer(Modifier.height(PantherSpacing.medium))
            when {
                days.loading -> Text(stringResource(R.string.loading))
                days.error -> ErrorNotice(true)
                days.value.isEmpty() -> Text(stringResource(R.string.no_days))
                else -> days.value.forEach { Text(it.name, modifier = Modifier.padding(vertical = 8.dp)) }
            }
        }
    }
}
