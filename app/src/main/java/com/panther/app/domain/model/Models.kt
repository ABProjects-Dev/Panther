package com.panther.app.domain.model

data class UserProfile(val name: String, val profileImageUri: String? = null, val age: Int? = null, val heightCm: Double? = null, val weightKg: Double? = null)
data class WorkoutProgram(val id: Long, val name: String, val createdAt: Long, val isActive: Boolean, val isArchived: Boolean, val dayCount: Int, val exerciseCount: Int)
data class TrainingDay(val id: Long, val programId: Long, val name: String, val sortOrder: Int)
data class Exercise(val id: Long, val trainingDayId: Long, val name: String, val sortOrder: Int)
enum class TargetType { REPS, DURATION }
data class ExerciseSet(val id: Long = 0, val exerciseId: Long, val setNumber: Int, val targetType: TargetType, val targetValue: Int? = null, val targetMinutes: Int? = null, val targetSeconds: Int? = null)

data class ProgramBackup(val name: String, val createdAt: Long, val isActive: Boolean, val isArchived: Boolean, val days: List<DayBackup>, val originalId: Long = 0)
data class DayBackup(val name: String, val sortOrder: Int, val exercises: List<ExerciseBackup>)
data class ExerciseBackup(val name: String, val sortOrder: Int, val sets: List<SetBackup>)
data class SetBackup(val setNumber: Int, val targetType: TargetType, val targetValue: Int?, val targetMinutes: Int?, val targetSeconds: Int?)
data class PantherBackup(val version: Int = CURRENT_BACKUP_VERSION, val profile: UserProfile?, val programs: List<ProgramBackup>, val profileImageBase64: String? = null) {
    companion object {
        const val CURRENT_BACKUP_VERSION = 1
        const val MAX_PROGRAMS = 500
        const val MAX_DAYS_PER_PROGRAM = 100
        const val MAX_EXERCISES_PER_DAY = 500
        const val MAX_SETS_PER_EXERCISE = 100
        const val MAX_PROFILE_IMAGE_BYTES = 5_000_000
        const val MAX_PROFILE_IMAGE_BASE64_CHARS = 7_000_000
        const val MAX_BACKUP_BYTES = 10_000_000
    }
}

object WorkoutValidation {
    fun name(value: String) = value.isNotBlank() && value.trim().length <= 100
    fun profile(profile: UserProfile) = name(profile.name) && (profile.age == null || profile.age in 1..120) &&
        (profile.heightCm == null || profile.heightCm.isFinite() && profile.heightCm in 30.0..300.0) &&
        (profile.weightKg == null || profile.weightKg.isFinite() && profile.weightKg in 1.0..500.0)
    fun sets(sets: List<ExerciseSet>): Boolean = sets.isNotEmpty() &&
        sets.map { it.setNumber }.toSet().size == sets.size && sets.all { set ->
            set.setNumber > 0 && when (set.targetType) {
                TargetType.REPS -> set.targetValue != null && set.targetValue > 0 && set.targetMinutes == null && set.targetSeconds == null
                TargetType.DURATION -> set.targetValue == null && set.targetMinutes != null && set.targetMinutes >= 0 &&
                    set.targetSeconds != null && set.targetSeconds in 0..59 && (set.targetMinutes > 0 || set.targetSeconds > 0)
            }
        }

    fun backup(backup: PantherBackup): Boolean {
        if (backup.version != PantherBackup.CURRENT_BACKUP_VERSION) return false
        if (backup.profile != null && !profile(backup.profile)) return false
        if (backup.profileImageBase64 != null && (backup.profile == null || backup.profileImageBase64.length > PantherBackup.MAX_PROFILE_IMAGE_BASE64_CHARS)) return false
        if (backup.programs.count { it.isActive } > 1) return false
        if (backup.programs.any { it.isActive && it.isArchived }) return false
        return backup.programs.size <= PantherBackup.MAX_PROGRAMS && backup.programs.all { program ->
            name(program.name) && program.createdAt > 0 && program.originalId >= 0 && program.days.size <= PantherBackup.MAX_DAYS_PER_PROGRAM &&
                program.days.map { it.sortOrder }.toSet().size == program.days.size && program.days.all { day ->
                name(day.name) && day.sortOrder >= 0 && day.exercises.size <= PantherBackup.MAX_EXERCISES_PER_DAY &&
                    day.exercises.map { it.sortOrder }.toSet().size == day.exercises.size && day.exercises.all { exercise ->
                    name(exercise.name) && exercise.sortOrder >= 0 && exercise.sets.isNotEmpty() && exercise.sets.size <= PantherBackup.MAX_SETS_PER_EXERCISE &&
                        exercise.sets.map { it.setNumber }.toSet().size == exercise.sets.size &&
                        exercise.sets.all { set ->
                            set.setNumber > 0 && when (set.targetType) {
                                TargetType.REPS -> set.targetValue != null && set.targetValue > 0 && set.targetMinutes == null && set.targetSeconds == null
                                TargetType.DURATION -> set.targetValue == null && set.targetMinutes != null && set.targetMinutes >= 0 && set.targetSeconds != null && set.targetSeconds in 0..59 && (set.targetMinutes > 0 || set.targetSeconds > 0)
                            }
                        }
                }
            }
        }
    }
}
