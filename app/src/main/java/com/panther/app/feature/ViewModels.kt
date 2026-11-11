package com.panther.app.feature

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panther.app.R
import com.panther.app.core.settings.ThemeChoice
import com.panther.app.core.settings.ThemePreferences
import com.panther.app.data.backup.PantherBackupJson
import com.panther.app.data.backup.ProgramPdfExporter
import com.panther.app.data.backup.SafeUriOutput
import com.panther.app.domain.model.*
import com.panther.app.domain.repository.PantherRepository
import com.panther.app.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DataState<T>(val value: T, val loading: Boolean = false, val error: Boolean = false)

private fun logRecoverable(tag: String, throwable: Throwable) {
    runCatching {
        Log.w(tag, "Operation failed: ${throwable::class.java.simpleName}")
    }
}

private suspend fun removeLegacyProfileImages(context: Context) {
    withContext(Dispatchers.IO) {
        runCatching {
            val directory = File(context.filesDir, "profile_images")
            if (directory.exists()) directory.deleteRecursively()
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
) : ViewModel() {
    val programs = observePrograms().map { DataState(it, loading = false) }
        .catch { e ->
            if (e is CancellationException) throw e
            logRecoverable("ProgramsViewModel", e)
            emit(DataState(emptyList(), loading = false, error = true))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DataState(emptyList(), loading = true))
    private val _error = MutableStateFlow(false)
    val error = _error.asStateFlow()

    fun clearError() { _error.value = false }

    private fun execute(onComplete: (() -> Unit)? = null, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
                _error.value = false
                onComplete?.invoke()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                logRecoverable("ProgramsViewModel", e)
                _error.value = true
            }
        }
    }

    fun create(name: String) = execute { createProgram(name) }
    fun rename(id: Long, name: String) = execute { updateProgram(id, name) }
    fun delete(id: Long, onDeleted: (() -> Unit)? = null) = execute(onComplete = onDeleted) { deleteProgram(id) }
    fun activate(id: Long) = execute { activateProgram(id) }
    fun deactivate(id: Long) = execute { repository.deactivateProgram(id) }
    fun setActive(id: Long, active: Boolean) = if (active) activate(id) else deactivate(id)

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
        .catch { e ->
            if (e is CancellationException) throw e
            logRecoverable("ProgramsViewModel", e)
            emit(DataState(emptyList(), loading = false, error = true))
        }

    fun exercises(dayId: Long) = repository.observeExercises(dayId)
        .map { DataState(it, loading = false) }
        .catch { e ->
            if (e is CancellationException) throw e
            logRecoverable("ProgramsViewModel", e)
            emit(DataState(emptyList(), loading = false, error = true))
        }

    fun exercisesWithSets(dayId: Long) = repository.observeExercisesWithSets(dayId)
        .map { DataState(it, loading = false) }
        .catch { e ->
            if (e is CancellationException) throw e
            logRecoverable("ProgramsViewModel", e)
            emit(DataState(emptyList(), loading = false, error = true))
        }

    fun sets(exerciseId: Long) = repository.observeSets(exerciseId)
        .catch { e ->
            if (e is CancellationException) throw e
            logRecoverable("ProgramsViewModel", e)
            emit(emptyList())
        }
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeProfile: ObserveUserProfileUseCase,
    private val saveProfile: SaveUserProfileUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val profile = observeProfile().map { DataState(it, loading = false) }
        .catch { e ->
            if (e is CancellationException) throw e
            logRecoverable("ProfileViewModel", e)
            emit(DataState(null, loading = false, error = true))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DataState<UserProfile?>(null, loading = true))
    private val _error = MutableStateFlow(false)
    val error = _error.asStateFlow()
    private val _saved = MutableStateFlow(false)
    val saved = _saved.asStateFlow()

    fun clearSaved() { _saved.value = false }
    fun clearError() { _error.value = false }

    fun save(profile: UserProfile) {
        viewModelScope.launch {
            try {
                require(WorkoutValidation.profile(profile))
                saveProfile(profile)
                removeLegacyProfileImages(context)
                _error.value = false
                _saved.value = true
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                logRecoverable("ProfileViewModel", e)
                _error.value = true
                _saved.value = false
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
    private val _pdfMessage = MutableStateFlow<Int?>(null)
    val pdfMessage = _pdfMessage.asStateFlow()
    private val _pendingBackup = MutableStateFlow<PantherBackup?>(null)
    val pendingBackup = _pendingBackup.asStateFlow()

    fun clearMessage() {
        _operationMessage.value = null
        _error.value = false
    }

    fun clearPdfMessage() {
        _pdfMessage.value = null
    }

    fun setTheme(choice: ThemeChoice) {
        viewModelScope.launch {
            try {
                preferences.set(choice)
                _error.value = false
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                logRecoverable("SettingsViewModel", e)
                _error.value = true
                _operationMessage.value = R.string.error_generic
            }
        }
    }

    fun exportBackup(uri: Uri) = runOperation(R.string.backup_exported) {
        val snapshot = repository.createBackup()
        val json = withContext(Dispatchers.IO) { PantherBackupJson.encode(snapshot) }
        writeAtomically(uri) { file -> file.writeText(json) }
    }

    fun validateImport(uri: Uri) {
        viewModelScope.launch {
            try {
                val candidate = withContext(Dispatchers.IO) {
                    val input = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalArgumentException("Invalid import URI")
                    val bytes = input.use { it.readLimited(PantherBackup.MAX_BACKUP_BYTES + 1) }
                    PantherBackupJson.decode(bytes)
                }
                _pendingBackup.value = candidate
                _error.value = false
                _operationMessage.value = null
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                logRecoverable("SettingsViewModel", e)
                _pendingBackup.value = null
                _error.value = true
                _operationMessage.value = R.string.backup_invalid
            }
        }
    }

    fun cancelImport() { _pendingBackup.value = null }

    /**
     * Best-effort snapshot of the current data, written before a restore replaces it so that an
     * accidental restore can still be undone manually from `filesDir/pre-restore`. A failure here
     * is logged and must never block the restore the user explicitly confirmed.
     */
    private suspend fun writePreRestoreSnapshot() {
        withContext(Dispatchers.IO) {
            runCatching {
                val snapshot = PantherBackupJson.encode(repository.createBackup())
                val directory = File(context.filesDir, "pre-restore").apply { mkdirs() }
                File(directory, "backup-before-restore.json").writeText(snapshot)
            }.onFailure { logRecoverable("SettingsViewModel", it) }
        }
    }

    fun restorePendingBackup() {
        val backup = _pendingBackup.value ?: return
        viewModelScope.launch {
            try {
                writePreRestoreSnapshot()
                repository.restoreBackup(backup)
                _pendingBackup.value = null
                removeLegacyProfileImages(context)
                _error.value = false
                _operationMessage.value = R.string.backup_imported
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                logRecoverable("SettingsViewModel", e)
                _error.value = true
                _operationMessage.value = R.string.error_generic
            }
        }
    }

    fun exportProgramPdf(programId: Long, uri: Uri) {
        viewModelScope.launch {
            try {
                val program = repository.createProgramBackup(programId) ?: error("Program not found")
                writeAtomically(uri) { file ->
                    file.outputStream().use { output -> ProgramPdfExporter.write(context, program, output) }
                }
                _pdfMessage.value = R.string.pdf_exported
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                logRecoverable("SettingsViewModel", e)
                _pdfMessage.value = R.string.error_generic
            }
        }
    }

    /**
     * Renders the export into a cache file first and only then copies it into the document the user
     * picked, so a failure while generating a PDF or encoding JSON cannot leave a truncated file at
     * the destination ([SAFE-08]).
     */
    private suspend fun writeAtomically(uri: Uri, produce: (File) -> Unit) = withContext(Dispatchers.IO) {
        val temp = File.createTempFile("panther-export", ".tmp", context.cacheDir)
        try {
            produce(temp)
            SafeUriOutput.openTruncated(context.contentResolver, uri).use { output ->
                temp.inputStream().use { input -> input.copyTo(output) }
            }
        } finally {
            temp.delete()
        }
    }

    private fun runOperation(successMessage: Int, operation: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                operation()
                _error.value = false
                _operationMessage.value = successMessage
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                logRecoverable("SettingsViewModel", e)
                _error.value = true
                _operationMessage.value = R.string.error_generic
            }
        }
    }
}
