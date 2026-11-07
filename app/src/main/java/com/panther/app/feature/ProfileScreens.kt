package com.panther.app.feature

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panther.app.R
import com.panther.app.core.designsystem.components.*
import com.panther.app.core.designsystem.theme.LocalPantherColors
import com.panther.app.core.designsystem.theme.PantherSpacing
import com.panther.app.core.format.PantherDateFormat
import com.panther.app.core.settings.ThemeChoice
import com.panther.app.domain.model.UserProfile
import com.panther.app.domain.model.WorkoutValidation
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onSettings: () -> Unit) {
    val state by viewModel.profile.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val saved by viewModel.saved.collectAsStateWithLifecycle()
    val profile = state.value

    var name by rememberSaveable(profile) { mutableStateOf(profile?.name.orEmpty()) }
    var age by rememberSaveable(profile) { mutableStateOf(profile?.age?.toString().orEmpty()) }
    var height by rememberSaveable(profile) { mutableStateOf(profile?.heightCm?.toString().orEmpty()) }
    var weight by rememberSaveable(profile) { mutableStateOf(profile?.weightKg?.toString().orEmpty()) }
    var invalid by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(saved) {
        if (saved) {
            delay(2500)
            viewModel.clearSaved()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PantherSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(PantherSpacing.medium),
    ) {
        PantherBrandHeader(
            subtitle = stringResource(R.string.profile),
            trailing = {
                OutlinedButton(
                    onClick = onSettings,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.settings))
                }
            },
        )

        if (state.loading) {
            Text(stringResource(R.string.loading))
        }
        if (state.error || error) {
            Text(stringResource(R.string.error_generic), color = MaterialTheme.colorScheme.error)
        }
        if (!state.loading && !state.error) {
            if (profile == null) {
                PantherCard {
                    Text(
                        text = stringResource(R.string.profile_setup),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            PantherCard {
                Column(verticalArrangement = Arrangement.spacedBy(PantherSpacing.compact)) {
                    PantherTextField(
                        value = name,
                        onValueChange = { name = it; invalid = false },
                        label = stringResource(R.string.name),
                        modifier = Modifier.fillMaxWidth(),
                        isError = invalid && !WorkoutValidation.name(name),
                    )
                    PantherTextField(
                        value = age,
                        onValueChange = { age = it; invalid = false },
                        label = stringResource(R.string.age),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    PantherTextField(
                        value = height,
                        onValueChange = { height = it; invalid = false },
                        label = stringResource(R.string.height),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    PantherTextField(
                        value = weight,
                        onValueChange = { weight = it; invalid = false },
                        label = stringResource(R.string.weight),
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    )
                    if (invalid) {
                        Text(
                            text = stringResource(R.string.error_profile),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    if (saved) {
                        Text(
                            text = stringResource(R.string.profile_saved),
                            color = LocalPantherColors.current.activeGreen,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Spacer(Modifier.height(PantherSpacing.extraSmall))
                    PantherButton(
                        text = stringResource(R.string.save),
                        onClick = {
                            val a = PantherDateFormat.asciiDigits(age).trim()
                            val h = PantherDateFormat.asciiDigits(height).trim()
                            val w = PantherDateFormat.asciiDigits(weight).trim()
                            val parsedAge = a.toIntOrNull()
                            val parsedHeight = h.toDoubleOrNull()
                            val parsedWeight = w.toDoubleOrNull()
                            val candidate = UserProfile(
                                name = name,
                                age = parsedAge,
                                heightCm = parsedHeight,
                                weightKg = parsedWeight,
                            )
                            invalid = (a.isNotEmpty() && parsedAge == null) ||
                                (h.isNotEmpty() && parsedHeight == null) ||
                                (w.isNotEmpty() && parsedWeight == null) ||
                                !WorkoutValidation.profile(candidate)
                            if (!invalid) viewModel.save(candidate)
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val selected by viewModel.theme.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val pendingBackup by viewModel.pendingBackup.collectAsStateWithLifecycle()
    val operationMessage by viewModel.operationMessage.collectAsStateWithLifecycle()
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) viewModel.exportBackup(uri)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.validateImport(uri)
    }
    LaunchedEffect(operationMessage) {
        if (operationMessage != null) {
            delay(3500)
            viewModel.clearMessage()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PantherSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(PantherSpacing.medium),
    ) {
        PantherBrandHeader(
            subtitle = stringResource(R.string.settings),
            trailing = {
                TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
            },
        )
        PantherCard {
            Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(PantherSpacing.small))
            ThemeChoice.entries.forEach { choice ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selected == choice, onClick = { viewModel.setTheme(choice) })
                    Text(
                        stringResource(
                            when (choice) {
                                ThemeChoice.DARK -> R.string.dark
                                ThemeChoice.LIGHT -> R.string.light
                                ThemeChoice.SYSTEM -> R.string.system
                            },
                        ),
                    )
                }
            }
        }
        PantherCard {
            Text(stringResource(R.string.backup_section), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(PantherSpacing.extraSmall))
            Text(
                text = stringResource(R.string.backup_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(PantherSpacing.compact))
            PantherButton(
                text = stringResource(R.string.export_backup),
                onClick = { exportLauncher.launch("panther-backup.json") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(PantherSpacing.small))
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.import_backup))
            }
            if (error) {
                Spacer(Modifier.height(PantherSpacing.small))
                Text(stringResource(R.string.error_generic), color = MaterialTheme.colorScheme.error)
            }
            operationMessage?.let { msg ->
                Spacer(Modifier.height(PantherSpacing.small))
                Text(
                    text = stringResource(msg),
                    color = if (msg == R.string.error_generic || msg == R.string.backup_invalid) {
                        MaterialTheme.colorScheme.error
                    } else {
                        LocalPantherColors.current.activeGreen
                    },
                )
            }
        }
    }
    if (pendingBackup != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelImport() },
            title = { Text(stringResource(R.string.confirm_import_title)) },
            text = { Text(stringResource(R.string.confirm_import, pendingBackup!!.programs.size)) },
            confirmButton = {
                TextButton(onClick = { viewModel.restorePendingBackup() }) {
                    Text(stringResource(R.string.import_replace))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelImport() }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}
