package com.panther.app.domain.model

import com.panther.app.core.format.PantherDateFormat
import kotlin.math.round

data class UserProfile(
    val name: String,
    val age: Int? = null,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
)

data class WorkoutProgram(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val isActive: Boolean,
    val dayCount: Int,
    val exerciseCount: Int,
)

data class TrainingDay(val id: Long, val programId: Long, val name: String, val sortOrder: Int)
data class Exercise(val id: Long, val trainingDayId: Long, val name: String, val sortOrder: Int)
enum class TargetType { REPS, DURATION }

data class ExerciseSet(
    val id: Long = 0,
    val exerciseId: Long,
    val setNumber: Int,
    val targetType: TargetType,
    val targetValue: Int? = null,
    val targetMinutes: Int? = null,
    val targetSeconds: Int? = null,
)

enum class BmiCategory { UNDERWEIGHT, NORMAL, OVERWEIGHT, OBESE }
data class BmiResult(val value: Double, val category: BmiCategory)

object BmiCalculator {
    fun calculate(heightCm: Double?, weightKg: Double?): BmiResult? {
        if (heightCm == null || weightKg == null) return null
        if (!heightCm.isFinite() || !weightKg.isFinite()) return null
        if (heightCm !in 30.0..300.0 || weightKg !in 1.0..500.0) return null
        val heightMeters = heightCm / 100.0
        val raw = weightKg / (heightMeters * heightMeters)
        if (!raw.isFinite() || raw <= 0.0) return null
        val rounded = round(raw * 10.0) / 10.0
        val category = when {
            rounded < 18.5 -> BmiCategory.UNDERWEIGHT
            rounded < 25.0 -> BmiCategory.NORMAL
            rounded < 30.0 -> BmiCategory.OVERWEIGHT
            else -> BmiCategory.OBESE
        }
        return BmiResult(value = rounded, category = category)
    }
}

data class DurationOption(val minutes: Int, val seconds: Int) {
    val totalSeconds: Int get() = minutes * 60 + seconds
    val key: String get() = "$minutes:${seconds.toString().padStart(2, '0')}"
    val compactLabel: String
        get() = PantherDateFormat.persianDigits(
            "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
        )
    val displayLabel: String
        get() = when {
            minutes == 0 -> "${PantherDateFormat.persianNumber(seconds)} ثانیه ($compactLabel)"
            seconds == 0 -> "${PantherDateFormat.persianNumber(minutes)} دقیقه ($compactLabel)"
            else -> "${PantherDateFormat.persianNumber(minutes)} دقیقه و ${PantherDateFormat.persianNumber(seconds)} ثانیه ($compactLabel)"
        }
}

object DurationCatalog {
    val presets: List<DurationOption> = buildList {
        for (sec in 5..55 step 5) add(DurationOption(0, sec))
        for (min in 1..2) {
            for (sec in listOf(0, 15, 30, 45)) add(DurationOption(min, sec))
        }
        for (min in 3..4) {
            add(DurationOption(min, 0))
            add(DurationOption(min, 30))
        }
        for (min in 5..20) add(DurationOption(min, 0))
        for (min in 25..60 step 5) add(DurationOption(min, 0))
    }

    fun parse(raw: String): DurationOption? {
        val parts = PantherDateFormat.asciiDigits(raw.trim()).split(":")
        if (parts.size != 2) return null
        val minutes = parts[0].toIntOrNull() ?: return null
        val seconds = parts[1].toIntOrNull() ?: return null
        if (minutes < 0 || seconds !in 0..59 || (minutes == 0 && seconds == 0)) return null
        return DurationOption(minutes, seconds)
    }

    fun optionsIncluding(extraRaw: String? = null): List<DurationOption> {
        val extra = extraRaw?.let(::parse)
        if (extra == null || presets.any { it.totalSeconds == extra.totalSeconds }) return presets
        return (presets + extra).sortedBy { it.totalSeconds }
    }

    fun search(query: String, extraRaw: String? = null): List<DurationOption> {
        val all = optionsIncluding(extraRaw)
        val normalizedQuery = PantherDateFormat.asciiDigits(query).trim().lowercase()
        if (normalizedQuery.isEmpty()) return all
        return all.filter { option ->
            val paddedKey = "${option.minutes.toString().padStart(2, '0')}:${option.seconds.toString().padStart(2, '0')}"
            val normalizedLabel = PantherDateFormat.asciiDigits(option.displayLabel).lowercase()
            normalizedLabel.contains(normalizedQuery) ||
                option.key.contains(normalizedQuery) ||
                paddedKey.contains(normalizedQuery) ||
                option.totalSeconds.toString() == normalizedQuery
        }
    }
}

data class ProgramBackup(
    val name: String,
    val createdAt: Long,
    val isActive: Boolean,
    val days: List<DayBackup>,
    val originalId: Long = 0,
)

data class DayBackup(val name: String, val sortOrder: Int, val exercises: List<ExerciseBackup>)
data class ExerciseBackup(val name: String, val sortOrder: Int, val sets: List<SetBackup>)
data class SetBackup(
    val setNumber: Int,
    val targetType: TargetType,
    val targetValue: Int?,
    val targetMinutes: Int?,
    val targetSeconds: Int?,
)

data class PantherBackup(
    val version: Int = CURRENT_BACKUP_VERSION,
    val profile: UserProfile?,
    val programs: List<ProgramBackup>,
) {
    companion object {
        const val CURRENT_BACKUP_VERSION = 1
        // Caps are chosen together with MAX_BACKUP_BYTES so that a backup can always be
        // materialised in memory without exhausting the process heap.
        const val MAX_PROGRAMS = 100
        const val MAX_DAYS_PER_PROGRAM = 50
        const val MAX_EXERCISES_PER_DAY = 50
        const val MAX_SETS_PER_EXERCISE = 30
        const val MAX_BACKUP_BYTES = 8_000_000
    }
}

object WorkoutValidation {
    fun name(value: String) = value.isNotBlank() && value.trim().length <= 100

    fun profile(profile: UserProfile) = name(profile.name) &&
        (profile.age == null || profile.age in 1..120) &&
        (profile.heightCm == null || (profile.heightCm.isFinite() && profile.heightCm in 30.0..300.0)) &&
        (profile.weightKg == null || (profile.weightKg.isFinite() && profile.weightKg in 1.0..500.0))

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
        if (backup.programs.count { it.isActive } > 1) return false
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
