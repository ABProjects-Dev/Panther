package com.panther.app.domain.model

data class UserProfile(val name: String, val profileImageUri: String? = null, val age: Int? = null, val heightCm: Double? = null, val weightKg: Double? = null)
data class WorkoutProgram(val id: Long, val name: String, val createdAt: Long, val isActive: Boolean, val isArchived: Boolean, val dayCount: Int, val exerciseCount: Int)
data class TrainingDay(val id: Long, val programId: Long, val name: String, val sortOrder: Int)
data class Exercise(val id: Long, val trainingDayId: Long, val name: String, val sortOrder: Int)
enum class TargetType { REPS, DURATION }
data class ExerciseSet(val id: Long = 0, val exerciseId: Long, val setNumber: Int, val targetType: TargetType, val targetValue: Int? = null, val targetMinutes: Int? = null, val targetSeconds: Int? = null)

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
}
