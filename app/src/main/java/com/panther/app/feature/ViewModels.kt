package com.panther.app.feature

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panther.app.R
import com.panther.app.core.settings.ThemeChoice
import com.panther.app.core.settings.ThemePreferences
import com.panther.app.data.backup.PantherBackupJson
import com.panther.app.data.backup.ProgramPdfExporter
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
        .catch { emit(DataState(emptyList(), loading = false, error = true)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DataState(emptyList(), loading = true))
    private val _error = MutableStateFlow(false)
    val error = _error.asStateFlow()

    private fun execute(onComplete: (() -> Unit)? = null, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
                _error.value = false
                onComplete?.invoke()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
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
        .catch { emit(DataState(emptyList(), loading = false, error = true)) }

    fun exercises(dayId: Long) = repository.observeExercises(dayId)
        .map { DataState(it, loading = false) }
        .catch { emit(DataState(emptyList(), loading = false, error = true)) }

    fun sets(exerciseId: Long) = repository.observeSets(exerciseId)
        .catch { e -> if (e is CancellationException) throw e; emit(emptyList()) }
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    observeProfile: ObserveUserProfileUseCase,
    private val saveProfile: SaveUserProfileUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val profile = observeProfile().map { DataState(it, loading = false) }
        .catch { emit(DataState(null, loading = false, error = true)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DataState<UserProfile?>(null, loading = true))
    private val _error = MutableStateFlow(false)
    val error = _error.asStateFlow()
    private val _saved = MutableStateFlow(false)
    val saved = _saved.asStateFlow()

    fun clearSaved() { _saved.value = false }

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
    private val _pendingBackup = MutableStateFlow<PantherBackup?>(null)
    val pendingBackup = _pendingBackup.asStateFlow()

    fun clearMessage() { _operationMessage.value = null }

    fun setTheme(choice: ThemeChoice) {
        viewModelScope.launch {
            try {
                preferences.set(choice)
                _error.value = false
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _error.value = true
            }
        }
    }

    fun exportBackup(uri: Uri) = runOperation(R.string.backup_exported) {
        val snapshot = repository.createBackup()
        val json = withContext(Dispatchers.IO) { PantherBackupJson.encode(snapshot) }
        withContext(Dispatchers.IO) {
            val output = context.contentResolver.openOutputStream(uri, "wt")
                ?: throw IllegalArgumentException("Invalid export URI")
            output.bufferedWriter(Charsets.UTF_8).use { it.write(json) }
        }
    }

    fun validateImport(uri: Uri) {
        viewModelScope.launch {
            try {
                val candidate = withContext(Dispatchers.IO) {
                    val input = context.contentResolver.openInputStream(uri)
                        ?: throw IllegalArgumentException("Invalid import URI")
                    val bytes = input.use { it.readLimited(PantherBackup.MAX_BACKUP_BYTES + 1) }
                    require(bytes.size <= PantherBackup.MAX_BACKUP_BYTES) { "Backup too large" }
                    PantherBackupJson.decode(bytes.toString(Charsets.UTF_8))
                }
                _pendingBackup.value = candidate
                _error.value = false
                _operationMessage.value = null
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _pendingBackup.value = null
                _error.value = true
                _operationMessage.value = R.string.backup_invalid
            }
        }
    }

    fun cancelImport() { _pendingBackup.value = null }

    fun restorePendingBackup() {
        val backup = _pendingBackup.value ?: return
        viewModelScope.launch {
            try {
                repository.restoreBackup(backup)
                _pendingBackup.value = null
                removeLegacyProfileImages(context)
                _error.value = false
                _operationMessage.value = R.string.backup_imported
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _error.value = true
                _operationMessage.value = R.string.error_generic
            }
        }
    }

    fun exportProgramPdf(programId: Long, uri: Uri) = runOperation(R.string.pdf_exported) {
        val backup = repository.createBackup()
        val program = backup.programs.firstOrNull { it.originalId == programId } ?: error("Program not found")
        withContext(Dispatchers.IO) {
            val output = context.contentResolver.openOutputStream(uri, "wt")
                ?: throw IllegalArgumentException("Invalid export URI")
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
                if (e is CancellationException) throw e
                _error.value = true
                _operationMessage.value = R.string.error_generic
            }
        }
    }
}
