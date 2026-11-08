package com.panther.app.data.backup

import com.panther.app.domain.model.*
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

/** Versioned, fully validated JSON codec. Parsing is side-effect free; callers restore only after success. */
object PantherBackupJson {
    /** JSON key that carries [ProgramBackup.originalId] in both directions. */
    private const val KEY_ORIGINAL_ID = "id"

    fun encode(backup: PantherBackup): String {
        require(WorkoutValidation.backup(backup))
        val root = JSONObject().put("format", "panther-backup").put("version", backup.version)
        root.put(
            "profile",
            backup.profile?.let { profile ->
                JSONObject().put("name", profile.name)
                    .put("age", profile.age ?: JSONObject.NULL)
                    .put("heightCm", profile.heightCm ?: JSONObject.NULL)
                    .put("weightKg", profile.weightKg ?: JSONObject.NULL)
            } ?: JSONObject.NULL,
        )
        root.put(
            "programs",
            JSONArray().apply {
                backup.programs.forEach { program ->
                    put(
                        JSONObject()
                            .put(KEY_ORIGINAL_ID, program.originalId)
                            .put("name", program.name)
                            .put("createdAt", program.createdAt)
                            .put("active", program.isActive)
                            .put("archived", false)
                            .put(
                                "days",
                                JSONArray().apply {
                                    program.days.forEach { day ->
                                        put(
                                            JSONObject().put("name", day.name).put("order", day.sortOrder).put(
                                                "exercises",
                                                JSONArray().apply {
                                                    day.exercises.forEach { exercise ->
                                                        put(
                                                            JSONObject().put("name", exercise.name).put("order", exercise.sortOrder).put(
                                                                "sets",
                                                                JSONArray().apply {
                                                                    exercise.sets.forEach { set ->
                                                                        put(
                                                                            JSONObject()
                                                                                .put("number", set.setNumber)
                                                                                .put("type", set.targetType.name)
                                                                                .put("value", set.targetValue ?: JSONObject.NULL)
                                                                                .put("minutes", set.targetMinutes ?: JSONObject.NULL)
                                                                                .put("seconds", set.targetSeconds ?: JSONObject.NULL),
                                                                        )
                                                                    }
                                                                },
                                                            ),
                                                        )
                                                    }
                                                },
                                            ),
                                        )
                                    }
                                },
                            ),
                    )
                }
            },
        )
        return root.toString(2)
    }

    fun decode(bytes: ByteArray): PantherBackup {
        require(bytes.size <= PantherBackup.MAX_BACKUP_BYTES) { "Backup file is too large" }
        return decodeValidatedText(String(bytes, Charsets.UTF_8))
    }

    fun decode(text: String): PantherBackup {
        require(utf8ByteLength(text) <= PantherBackup.MAX_BACKUP_BYTES) { "Backup file is too large" }
        return decodeValidatedText(text)
    }

    private fun decodeValidatedText(text: String): PantherBackup {
        val tokener = JSONTokener(text)
        val rootObj = tokener.nextValue()
        if (rootObj !is JSONObject) error("Invalid backup JSON")
        val root = rootObj
        require(tokener.nextClean() == '\u0000') { "Unexpected trailing data" }
        require(root.getString("format") == "panther-backup") { "Not a Panther backup" }
        val version = root.getInt("version")
        require(version == PantherBackup.CURRENT_BACKUP_VERSION) { "Unsupported backup version" }
        val profile = if (root.isNull("profile")) {
            null
        } else {
            root.getJSONObject("profile").let {
                UserProfile(
                    name = it.getString("name"),
                    age = it.nullableInt("age"),
                    heightCm = it.nullableDouble("heightCm"),
                    weightKg = it.nullableDouble("weightKg"),
                )
            }
        }
        val programsJson = root.getJSONArray("programs")
        require(programsJson.length() <= PantherBackup.MAX_PROGRAMS) { "Too many programs" }
        val programs = (0 until programsJson.length()).map { index ->
            val p = programsJson.getJSONObject(index)
            val active = p.getBoolean("active")
            val legacyArchived = p.optBoolean("archived", false)
            require(!(active && legacyArchived)) { "Program cannot be both active and archived" }
            val daysJson = p.getJSONArray("days")
            require(daysJson.length() <= PantherBackup.MAX_DAYS_PER_PROGRAM) { "Too many days" }
            val days = (0 until daysJson.length()).map { dayIndex ->
                val d = daysJson.getJSONObject(dayIndex)
                val exercisesJson = d.getJSONArray("exercises")
                require(exercisesJson.length() <= PantherBackup.MAX_EXERCISES_PER_DAY) { "Too many exercises" }
                DayBackup(
                    d.getString("name"),
                    d.getInt("order"),
                    (0 until exercisesJson.length()).map { exerciseIndex ->
                        val e = exercisesJson.getJSONObject(exerciseIndex)
                        val setsJson = e.getJSONArray("sets")
                        require(setsJson.length() in 1..PantherBackup.MAX_SETS_PER_EXERCISE) { "Invalid number of sets" }
                        ExerciseBackup(
                            e.getString("name"),
                            e.getInt("order"),
                            (0 until setsJson.length()).map { setIndex ->
                                val s = setsJson.getJSONObject(setIndex)
                                SetBackup(
                                    s.getInt("number"),
                                    TargetType.valueOf(s.getString("type")),
                                    s.nullableInt("value"),
                                    s.nullableInt("minutes"),
                                    s.nullableInt("seconds"),
                                )
                            },
                        )
                    },
                )
            }
            ProgramBackup(
                name = p.getString("name"),
                createdAt = p.getLong("createdAt"),
                isActive = active,
                days = days,
                originalId = p.optLong(KEY_ORIGINAL_ID, 0L),
            )
        }
        return PantherBackup(version, profile, programs).also {
            require(WorkoutValidation.backup(it)) { "Backup contains invalid data" }
        }
    }

    /** Computes exact UTF-8 encoded byte length without allocating an intermediate [ByteArray]. */
    internal fun utf8ByteLength(text: String): Long {
        var bytes = 0L
        var i = 0
        val length = text.length
        while (i < length) {
            val ch = text[i]
            when {
                ch.code <= 0x7F -> bytes += 1L
                ch.code <= 0x7FF -> bytes += 2L
                Character.isHighSurrogate(ch) && i + 1 < length && Character.isLowSurrogate(text[i + 1]) -> {
                    bytes += 4L
                    i++
                }
                else -> bytes += 3L
            }
            i++
        }
        return bytes
    }

    private fun JSONObject.nullableInt(key: String): Int? = if (isNull(key)) null else getInt(key)
    private fun JSONObject.nullableDouble(key: String): Double? =
        if (isNull(key)) null else getDouble(key).takeIf { it.isFinite() } ?: error("Invalid number")
}
