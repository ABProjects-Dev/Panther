package com.panther.app.feature

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panther.app.core.settings.ThemeChoice
import com.panther.app.core.settings.ThemePreferences
import com.panther.app.data.backup.PantherBackupJson
import com.panther.app.data.backup.ProgramPdfExporter
import com.panther.app.domain.model.*
import com.panther.app.domain.repository.PantherRepository
import com.panther.app.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DataState<T>(val value: T, val loading: Boolean = true, val error: Boolean = false)

private suspend fun cleanupProfileImages(context: Context, keepUri: String?) {
    withContext(Dispatchers.IO) {
        runCatching {
            val directory = File(context.filesDir, "profile_images").canonicalFile
            val keepFile = keepUri?.let { Uri.parse(it) }?.takeIf { it.scheme == "file" }?.path?.let { File(it).canonicalFile }
            directory.listFiles().orEmpty().filter { file ->
                val ownedFile = file.canonicalFile
                ownedFile.parentFile == directory && ownedFile != keepFile
            }.forEach { it.delete() }
        }
    }
}

private fun java.io.InputStream.readLimited(maxBytes: Int): ByteArray {
    val output = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(8_192)
    var total = 0
    while (true) {
        val count = read(buffer)
        if (count < 0) break
        total += count
        require(total <= maxBytes) { "Input exceeds size limit" }
        output.write(buffer, 0, count)
    }
    return output.toByteArray()
}

@HiltViewModel
class ProgramsViewModel @Inject constructor(
    private val repository: PantherRepository,
    observePrograms: ObserveWorkoutProgramsUseCase,
    private val createProgram: CreateWorkoutProgramUseCase,
    private val updateProgram: UpdateWorkoutProgramUseCase,
    private val deleteProgram: DeleteWorkoutProgramUseCase,
    private val activateProgram: ActivateWorkoutProgramUseCase,
    private val archiveProgram: ArchiveWorkoutProgramUseCase,
) : ViewModel() {
    val programs = observePrograms().map { DataState(it, loading = false) }
        .catch { emit(DataState(emptyList(), loading = false, error = true)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DataState(emptyList()))
    private val _error = MutableStateFlow(false)
    val error = _error.asStateFlow()
    private fun execute(block: suspend () -> Unit) { viewModelScope.launch { try { block(); _error.value = false } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e; _error.value = true } } }
    fun clearError() { _error.value = false }
    fun create(name: String) = execute { createProgram(name) }
    fun rename(id: Long, name: String) = execute { updateProgram(id, name) }
    fun delete(id: Long) = execute { deleteProgram(id) }
    fun activate(id: Long) = execute { activateProgram(id) }
    fun deactivate(id: Long) = execute { repository.deactivateProgram(id) }
    fun archive(id: Long, archived: Boolean) = execute { archiveProgram(id, archived) }
    fun addDay(programId: Long, name: String, order: Int) = execute { repository.addDay(programId, name, order) }
    fun renameDay(id: Long, name: String) = execute { repository.renameDay(id, name) }
    fun deleteDay(id: Long) = execute { repository.deleteDay(id) }
    fun reorderDays(programId: Long, ids: List<Long>) = execute { repository.reorderDays(programId, ids) }
    fun saveExercise(dayId: Long, exerciseId: Long?, name: String, order: Int, sets: List<ExerciseSet>) =
        execute { repository.saveExercise(dayId, exerciseId, name, order, sets) }
    fun deleteExercise(id: Long) = execute { repository.deleteExercise(id) }
    fun reorderExercises(dayId: Long, ids: List<Long>) = execute { repository.reorderExercises(dayId, ids) }
    fun days(id: Long) = repository.observeDays(id)
        .map { DataState(it, loading = false) }
        .catch { emit(DataState(emptyList(), loading = false, error = true)) }
    fun exercises(dayId: Long) = repository.observeExercises(dayId)
        .map { DataState(it, loading = false) }
        .catch { emit(DataState(emptyList(), loading = false, error = true)) }
    fun sets(exerciseId: Long) = repository.observeSets(exerciseId)
        .catch { emit(emptyList()) }
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeProfile: ObserveUserProfileUseCase,
    private val saveProfile: SaveUserProfileUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val profile = observeProfile().map { DataState(it, loading = false) }
        .catch { emit(DataState(null, loading = false, error = true)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DataState<UserProfile?>(null))
    private val _error = MutableStateFlow(false)
    val error = _error.asStateFlow()
    fun clearError() { _error.value = false }
    fun save(profile: UserProfile, selectedImage: Uri?) {
        viewModelScope.launch {
            var newFile: File? = null
            try {
                require(WorkoutValidation.profile(profile))
                val savedProfile = if (selectedImage != null) {
                    newFile = withContext(Dispatchers.IO) {
                        val directory = File(context.filesDir, "profile_images").apply { mkdirs() }
                        val output = File(directory, "${UUID.randomUUID()}.image")
                        try {
                            val input = requireNotNull(context.contentResolver.openInputStream(selectedImage))
                            input.use { source -> output.outputStream().use { sink ->
                                val buffer = ByteArray(8_192)
                                var total = 0
                                while (true) {
                                    val count = source.read(buffer)
                                    if (count < 0) break
                                    total += count
                                    require(total <= PantherBackup.MAX_PROFILE_IMAGE_BYTES) { "Profile image is too large" }
                                    sink.write(buffer, 0, count)
                                }
                                require(total > 0) { "Empty profile image" }
                            } }
                        } catch (e: Exception) { output.delete(); throw e }
                        output
                    }
                    profile.copy(profileImageUri = Uri.fromFile(newFile).toString())
                } else profile
                saveProfile(savedProfile)
                cleanupProfileImages(context, savedProfile.profileImageUri)
                _error.value = false
            } catch (e: Exception) {
                // Keep a copied file on failure: a cancelled database write may already have committed.
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = true
            }
        }
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: ThemePreferences,
    private val repository: PantherRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val theme = preferences.choice.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeChoice.DARK)
    private val _error = MutableStateFlow(false)
    val error = _error.asStateFlow()
    private val _operationMessage = MutableStateFlow<Int?>(null)
    val operationMessage = _operationMessage.asStateFlow()
    private val _pendingBackup = MutableStateFlow<PantherBackup?>(null)
    val pendingBackup = _pendingBackup.asStateFlow()

    fun clearMessage() { _operationMessage.value = null }
    fun setTheme(choice: ThemeChoice) { viewModelScope.launch { try { preferences.set(choice); _error.value = false } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e; _error.value = true } } }

    fun exportBackup(uri: Uri) = runOperation(R.string.backup_exported) {
        val snapshot = repository.createBackup()
        val imageBase64 = snapshot.profile?.profileImageUri?.let { imageUri -> withContext(Dispatchers.IO) {
            val input = requireNotNull(context.contentResolver.openInputStream(Uri.parse(imageUri)))
            val imageBytes = input.use { it.readLimited(PantherBackup.MAX_PROFILE_IMAGE_BYTES + 1) }
            require(imageBytes.isNotEmpty() && imageBytes.size <= PantherBackup.MAX_PROFILE_IMAGE_BYTES) { "Profile image exceeds backup limit" }
            android.util.Base64.encodeToString(imageBytes, android.util.Base64.NO_WRAP)
        } }
        val json = withContext(Dispatchers.IO) { PantherBackupJson.encode(snapshot.copy(profile = snapshot.profile?.copy(profileImageUri = null), profileImageBase64 = imageBase64)) }
        withContext(Dispatchers.IO) {
            val output = requireNotNull(context.contentResolver.openOutputStream(uri, "wt"))
            output.bufferedWriter(Charsets.UTF_8).use { it.write(json) }
        }
    }

    fun validateImport(uri: Uri) {
        viewModelScope.launch {
            try {
                val candidate = withContext(Dispatchers.IO) {
                    val input = requireNotNull(context.contentResolver.openInputStream(uri))
                    val bytes = input.use { it.readLimited(PantherBackup.MAX_BACKUP_BYTES + 1) }
                    require(bytes.size <= PantherBackup.MAX_BACKUP_BYTES) { "Backup too large" }
                    PantherBackupJson.decode(bytes.toString(Charsets.UTF_8))
                }
                _pendingBackup.value = candidate
                _error.value = false
                _operationMessage.value = null
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _pendingBackup.value = null
                _error.value = true
                _operationMessage.value = R.string.backup_invalid
            }
        }
    }

    fun cancelImport() { _pendingBackup.value = null }
    fun restorePendingBackup() {
        val backup = _pendingBackup.value ?: return
        _pendingBackup.value = null
        runOperation(R.string.backup_imported) {
            val profile = backup.profile
            val restoredImageUri = backup.profileImageBase64?.let { encoded -> withContext(Dispatchers.IO) {
                val bytes = android.util.Base64.decode(encoded, android.util.Base64.DEFAULT)
                require(bytes.isNotEmpty() && bytes.size <= PantherBackup.MAX_PROFILE_IMAGE_BYTES) { "Invalid profile image" }
                val directory = File(context.filesDir, "profile_images").apply { check(isDirectory || mkdirs()) }
                val file = File(directory, "${UUID.randomUUID()}.image")
                file.outputStream().use { it.write(bytes) }
                Uri.fromFile(file).toString()
            } }
            val restoredProfile = profile?.let { it.copy(profileImageUri = restoredImageUri ?: it.profileImageUri) }
            repository.restoreBackup(backup.copy(profile = restoredProfile, profileImageBase64 = null))
            cleanupProfileImages(context, restoredProfile?.profileImageUri)
        }
    }

    fun exportProgramPdf(programId: Long, uri: Uri) = runOperation(R.string.pdf_exported) {
        val backup = repository.createBackup()
        val program = backup.programs.firstOrNull { it.originalId == programId } ?: error("Program not found")
        withContext(Dispatchers.IO) {
            val output = requireNotNull(context.contentResolver.openOutputStream(uri, "wt"))
            output.use { ProgramPdfExporter.write(context, program, it) }
        }
    }

    private fun runOperation(successMessage: Int, operation: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                operation()
                _error.value = false
                _operationMessage.value = successMessage
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _error.value = true
                _operationMessage.value = R.string.error_generic
            }
        }
    }
}
