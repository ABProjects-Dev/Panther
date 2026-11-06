package com.panther.app.domain.usecase

import com.panther.app.domain.model.UserProfile
import com.panther.app.domain.repository.PantherRepository
import javax.inject.Inject

class ObserveUserProfileUseCase @Inject constructor(private val repository: PantherRepository) { operator fun invoke() = repository.observeProfile() }
class SaveUserProfileUseCase @Inject constructor(private val repository: PantherRepository) { suspend operator fun invoke(profile: UserProfile) = repository.saveProfile(profile) }
class ObserveWorkoutProgramsUseCase @Inject constructor(private val repository: PantherRepository) { operator fun invoke() = repository.observePrograms() }
class CreateWorkoutProgramUseCase @Inject constructor(private val repository: PantherRepository) { suspend operator fun invoke(name: String) = repository.createProgram(name) }
class UpdateWorkoutProgramUseCase @Inject constructor(private val repository: PantherRepository) { suspend operator fun invoke(id: Long, name: String) = repository.renameProgram(id, name) }
class DeleteWorkoutProgramUseCase @Inject constructor(private val repository: PantherRepository) { suspend operator fun invoke(id: Long) = repository.deleteProgram(id) }
class ActivateWorkoutProgramUseCase @Inject constructor(private val repository: PantherRepository) { suspend operator fun invoke(id: Long) = repository.activateProgram(id) }
class ArchiveWorkoutProgramUseCase @Inject constructor(private val repository: PantherRepository) { suspend operator fun invoke(id: Long, archived: Boolean = true) = repository.archiveProgram(id, archived) }
