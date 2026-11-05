package com.panther.app.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.panther.app.data.backup.PantherBackupJson
import com.panther.app.domain.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PantherBackupJsonTest {
    @Test fun roundTripKeepsProfileAndIndependentRepetitionAndDurationTargets() {
        val backup = PantherBackup(
            profile = UserProfile("Test", age = 28, heightCm = 175.5),
            programs = listOf(ProgramBackup("Plan", 1_700_000_000_000, true, false, listOf(
                DayBackup("Day 1", 0, listOf(ExerciseBackup("Squat", 0, listOf(
                    SetBackup(1, TargetType.REPS, 8, null, null),
                    SetBackup(2, TargetType.DURATION, null, 1, 30),
                ))))
            ), originalId = 7)),
            profileImageBase64 = "cGhvdG8=",
        )
        assertEquals(backup, PantherBackupJson.decode(PantherBackupJson.encode(backup)))
    }

    @Test fun rejectsUnsupportedVersionAndTrailingContent() {
        listOf(
            "{\"format\":\"panther-backup\",\"version\":99,\"profile\":null,\"programs\":[]}",
            "{\"format\":\"panther-backup\",\"version\":1,\"profile\":null,\"programs\":[]} trailing",
        ).forEach { json ->
            try { PantherBackupJson.decode(json); fail("Expected invalid backup to be rejected") }
            catch (_: IllegalArgumentException) { }
            catch (_: org.json.JSONException) { }
        }
    }
}
