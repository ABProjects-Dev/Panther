package com.panther.app.feature

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panther.app.core.settings.ThemeChoice
import com.panther.app.core.settings.ThemePreferences
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
    fun days(id: Long) = repository.observeDays(id)
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
                        try { context.contentResolver.openInputStream(selectedImage).use { input -> requireNotNull(input); output.outputStream().use { input.copyTo(it) } } } catch (e: Exception) { output.delete(); throw e }
                        output
                    }
                    profile.copy(profileImageUri = Uri.fromFile(newFile).toString())
                } else profile
                saveProfile(savedProfile)
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
class SettingsViewModel @Inject constructor(private val preferences: ThemePreferences) : ViewModel() {
    val theme = preferences.choice.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeChoice.DARK)
    private val _error = MutableStateFlow(false)
    val error = _error.asStateFlow()
    fun setTheme(choice: ThemeChoice) { viewModelScope.launch { try { preferences.set(choice); _error.value = false } catch (e: Exception) { if (e is kotlinx.coroutines.CancellationException) throw e; _error.value = true } } }
}
