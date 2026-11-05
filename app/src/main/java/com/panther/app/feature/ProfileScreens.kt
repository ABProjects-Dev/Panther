package com.panther.app.feature

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.panther.app.R
import com.panther.app.core.designsystem.components.*
import com.panther.app.core.designsystem.theme.PantherSpacing
import com.panther.app.core.settings.ThemeChoice
import com.panther.app.domain.model.UserProfile
import com.panther.app.domain.model.WorkoutValidation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileAvatar(uri: String?) {
    val bitmap by produceState<android.graphics.Bitmap?>(null, uri) {
        value = withContext(Dispatchers.IO) {
            try { uri?.let { Uri.parse(it).path }?.let { BitmapFactory.decodeFile(it, BitmapFactory.Options().apply { inSampleSize = 8 }) } } catch (_: Exception) { null }
        }
    }
    val image = bitmap
    if (image != null) Image(image.asImageBitmap(), contentDescription = stringResource(R.string.avatar), modifier = Modifier.size(72.dp).clip(CircleShape), contentScale = ContentScale.Crop)
    else Box(Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.profile).take(1), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
    }
}

private fun normalizedNumber(value: String): String = value.trim().map { c ->
    when (c) { in '۰'..'۹' -> '0' + (c - '۰'); in '٠'..'٩' -> '0' + (c - '٠'); '٫', ',' -> '.'; else -> c }
}.joinToString("")

@Composable
fun ProfileScreen(viewModel: ProfileViewModel, onSettings: () -> Unit) {
    val state by viewModel.profile.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val profile = state.value
    var name by rememberSaveable(profile) { mutableStateOf(profile?.name.orEmpty()) }
    var age by rememberSaveable(profile) { mutableStateOf(profile?.age?.toString().orEmpty()) }
    var height by rememberSaveable(profile) { mutableStateOf(profile?.heightCm?.toString().orEmpty()) }
    var weight by rememberSaveable(profile) { mutableStateOf(profile?.weightKg?.toString().orEmpty()) }
    var selectedImage by rememberSaveable { mutableStateOf<String?>(null) }
    var invalid by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(profile?.profileImageUri) { selectedImage = null }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri -> if (uri != null) selectedImage = uri.toString() }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(PantherSpacing.medium), verticalArrangement = Arrangement.spacedBy(PantherSpacing.medium)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            PantherSectionHeader(stringResource(R.string.profile))
            TextButton(onClick = onSettings) { Text(stringResource(R.string.settings)) }
        }
        if (state.loading) Text(stringResource(R.string.loading))
        if (state.error || error) Text(stringResource(R.string.error_generic), color = MaterialTheme.colorScheme.error)
        if (!state.loading && !state.error) {
            if (profile == null) Text(stringResource(R.string.profile_setup))
            ProfileAvatar(profile?.profileImageUri)
            if (selectedImage != null) Text(stringResource(R.string.photo_selected))
            TextButton(onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) { Text(stringResource(R.string.choose_photo)) }
            PantherTextField(name, { name = it }, stringResource(R.string.name), Modifier.fillMaxWidth())
            PantherTextField(age, { age = it }, stringResource(R.string.age), Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            PantherTextField(height, { height = it }, stringResource(R.string.height), Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            PantherTextField(weight, { weight = it }, stringResource(R.string.weight), Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
            if (invalid) Text(stringResource(R.string.error_profile), color = MaterialTheme.colorScheme.error)
            PantherButton(stringResource(R.string.save), {
                val a = normalizedNumber(age); val h = normalizedNumber(height); val w = normalizedNumber(weight)
                val parsedAge = a.toIntOrNull(); val parsedHeight = h.toDoubleOrNull(); val parsedWeight = w.toDoubleOrNull()
                val candidate = UserProfile(name, profile?.profileImageUri, parsedAge, parsedHeight, parsedWeight)
                invalid = (a.isNotEmpty() && parsedAge == null) || (h.isNotEmpty() && parsedHeight == null) || (w.isNotEmpty() && parsedWeight == null) || !WorkoutValidation.profile(candidate)
                if (!invalid) viewModel.save(candidate, selectedImage?.let(Uri::parse))
            }, Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun SettingsScreen(viewModel: SettingsViewModel, onBack: () -> Unit) {
    val selected by viewModel.theme.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val pendingBackup by viewModel.pendingBackup.collectAsStateWithLifecycle()
    val operationMessage by viewModel.operationMessage.collectAsStateWithLifecycle()
    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri -> if (uri != null) viewModel.exportBackup(uri) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) viewModel.validateImport(uri) }
    LaunchedEffect(operationMessage) { if (operationMessage != null) { kotlinx.coroutines.delay(3500); viewModel.clearMessage() } }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(PantherSpacing.medium), verticalArrangement = Arrangement.spacedBy(PantherSpacing.small)) {
        TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
        PantherSectionHeader(stringResource(R.string.settings))
        Text(stringResource(R.string.theme), style = MaterialTheme.typography.titleMedium)
        ThemeChoice.entries.forEach { choice ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = selected == choice, onClick = { viewModel.setTheme(choice) })
                Text(stringResource(when (choice) { ThemeChoice.DARK -> R.string.dark; ThemeChoice.LIGHT -> R.string.light; ThemeChoice.SYSTEM -> R.string.system }))
            }
        }
        HorizontalDivider(Modifier.padding(vertical = PantherSpacing.small))
        Text(stringResource(R.string.backup_section), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.backup_description), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        PantherButton(stringResource(R.string.export_backup), { exportLauncher.launch("panther-backup.json") }, Modifier.fillMaxWidth())
        OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json", "text/*", "application/octet-stream")) }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.import_backup)) }
        if (error) Text(stringResource(R.string.error_generic), color = MaterialTheme.colorScheme.error)
        operationMessage?.let { Text(stringResource(it), color = if (it == R.string.error_generic || it == R.string.backup_invalid) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary) }
    }
    if (pendingBackup != null) AlertDialog(
        onDismissRequest = { viewModel.cancelImport() },
        title = { Text(stringResource(R.string.confirm_import_title)) },
        text = { Text(stringResource(R.string.confirm_import, pendingBackup!!.programs.size)) },
        confirmButton = { TextButton(onClick = { viewModel.restorePendingBackup() }) { Text(stringResource(R.string.import_replace)) } },
        dismissButton = { TextButton(onClick = { viewModel.cancelImport() }) { Text(stringResource(R.string.cancel)) } },
    )
}
