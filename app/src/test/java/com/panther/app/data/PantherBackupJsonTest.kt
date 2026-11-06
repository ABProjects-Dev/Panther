package com.panther.app.data

import com.panther.app.data.backup.PantherBackupJson
import com.panther.app.domain.model.DayBackup
import com.panther.app.domain.model.ExerciseBackup
import com.panther.app.domain.model.PantherBackup
import com.panther.app.domain.model.ProgramBackup
import com.panther.app.domain.model.SetBackup
import com.panther.app.domain.model.TargetType
import com.panther.app.domain.model.UserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * JVM unit tests for the backup codec. These run without an emulator so the codec is
 * verified on every CI run instead of only in instrumented jobs.
 */
class PantherBackupJsonTest {

    private fun sample() = PantherBackup(
        profile = UserProfile("Test", age = 28, heightCm = 175.5),
        programs = listOf(
            ProgramBackup("Plan", 1_700_000_000_000, true, false, listOf(
                DayBackup("Day 1", 0, listOf(
                    ExerciseBackup("Squat", 0, listOf(
                        SetBackup(1, TargetType.REPS, 8, null, null),
                        SetBackup(2, TargetType.DURATION, null, 1, 30),
                    )),
                )),
            ), originalId = 7),
        ),
        profileImageBase64 = "cGhvdG8=",
    )

    @Test
    fun roundTripKeepsProfileAndIndependentTargets() {
        assertEquals(sample(), PantherBackupJson.decode(PantherBackupJson.encode(sample())))
    }

    /** Regression test for the originalId key mismatch that rejected every valid import. */
    @Test
    fun roundTripPreservesOriginalProgramId() {
        val decoded = PantherBackupJson.decode(PantherBackupJson.encode(sample()))
        assertEquals(7L, decoded.programs.single().originalId)
    }

    @Test
    fun encodedProgramUsesIdKey() {
        val json = PantherBackupJson.encode(sample())
        assertTrue("encoded backup must expose the program id key", json.contains("\"id\": 7"))
    }

    @Test
    fun programWithoutIdDecodesWithZeroOriginalId() {
        val json = """
            {"format":"panther-backup","version":1,"profile":null,
             "programs":[{"name":"P","createdAt":1700000000000,"active":false,"archived":false,
               "days":[{"name":"D","order":0,"exercises":[{"name":"E","order":0,
                 "sets":[{"number":1,"type":"REPS","value":5,"minutes":null,"seconds":null}]}]}]}]}
        """.trimIndent()
        val decoded = PantherBackupJson.decode(json)
        assertEquals(0L, decoded.programs.single().originalId)
        assertNull(decoded.profile)
    }

    @Test
    fun rejectsUnsupportedVersionAndTrailingContent() {
        listOf(
            "{\"format\":\"panther-backup\",\"version\":99,\"profile\":null,\"programs\":[]}",
            "{\"format\":\"panther-backup\",\"version\":1,\"profile\":null,\"programs\":[]} trailing",
        ).forEach { json ->
            try {
                PantherBackupJson.decode(json)
                fail("Expected invalid backup to be rejected")
            } catch (_: IllegalArgumentException) {
            } catch (_: org.json.JSONException) {
            }
        }
    }

    @Test
    fun rejectsNonPantherFormat() {
        try {
            PantherBackupJson.decode("{\"format\":\"other\",\"version\":1,\"programs\":[]}")
            fail("Expected non-Panther payload to be rejected")
        } catch (_: IllegalArgumentException) {
        }
    }
}
