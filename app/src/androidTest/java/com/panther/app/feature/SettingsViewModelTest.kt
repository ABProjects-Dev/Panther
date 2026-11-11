package com.panther.app.feature

import android.content.Context
import android.net.Uri
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.panther.app.R
import com.panther.app.core.settings.ThemePreferences
import com.panther.app.data.backup.PantherBackupJson
import com.panther.app.data.local.PantherDatabase
import com.panther.app.data.repository.RoomPantherRepository
import com.panther.app.domain.model.DayBackup
import com.panther.app.domain.model.ExerciseBackup
import com.panther.app.domain.model.PantherBackup
import com.panther.app.domain.model.ProgramBackup
import com.panther.app.domain.model.SetBackup
import com.panther.app.domain.model.TargetType
import com.panther.app.domain.model.UserProfile
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Covers the restore orchestration that lives in the ViewModel: import validation, the pre-restore
 * safety snapshot, and the fact that a rejected file leaves the existing data untouched. Room-level
 * invariants are covered by `PantherDatabaseTest`.
 */
@RunWith(AndroidJUnit4::class)
class SettingsViewModelTest {
    private lateinit var context: Context
    private lateinit var db: PantherDatabase
    private lateinit var repository: RoomPantherRepository
    private lateinit var viewModel: SettingsViewModel
    private lateinit var snapshotFile: File

    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, PantherDatabase::class.java).build()
        repository = RoomPantherRepository(db)
        viewModel = SettingsViewModel(ThemePreferences(context), repository, context)
        snapshotFile = File(context.filesDir, "pre-restore/backup-before-restore.json")
        snapshotFile.delete()
    }

    @After
    fun tearDown() {
        db.close()
        snapshotFile.delete()
    }

    private suspend fun awaitOperationMessage(): Int =
        withTimeout(15_000) { viewModel.operationMessage.first { it != null } }!!

    @Test
    fun restoreWritesSafetySnapshotAndReplacesTheData() = runBlocking {
        repository.createProgram("Original")

        val importFile = File(context.cacheDir, "import-backup.json").apply {
            writeText(PantherBackupJson.encode(importedBackup()))
        }
        viewModel.validateImport(Uri.fromFile(importFile))
        withTimeout(15_000) { viewModel.pendingBackup.first { it != null } }

        viewModel.restorePendingBackup()

        assertEquals(R.string.backup_imported, awaitOperationMessage())
        assertNull("the pending import must be consumed", viewModel.pendingBackup.value)
        assertEquals("Imported plan", repository.observePrograms().first().single().name)
        assertTrue("the pre-restore snapshot must exist", snapshotFile.exists())
        val snapshot = PantherBackupJson.decode(snapshotFile.readBytes())
        assertEquals("Original", snapshot.programs.single().name)
        importFile.delete()
    }

    @Test
    fun invalidImportIsRejectedAndLeavesDataUntouched() = runBlocking {
        repository.createProgram("Keep me")

        val badFile = File(context.cacheDir, "bad-backup.json").apply { writeText("{ not json") }
        viewModel.validateImport(Uri.fromFile(badFile))

        assertEquals(R.string.backup_invalid, awaitOperationMessage())
        assertNull(viewModel.pendingBackup.value)
        assertEquals("Keep me", repository.observePrograms().first().single().name)
        badFile.delete()
    }

    private fun importedBackup() = PantherBackup(
        profile = UserProfile("Imported"),
        programs = listOf(
            ProgramBackup(
                name = "Imported plan",
                createdAt = System.currentTimeMillis(),
                isActive = true,
                days = listOf(
                    DayBackup(
                        name = "Day A",
                        sortOrder = 0,
                        exercises = listOf(
                            ExerciseBackup(
                                name = "Squat",
                                sortOrder = 0,
                                sets = listOf(SetBackup(1, TargetType.REPS, 8, null, null)),
                            ),
                        ),
                    ),
                ),
            ),
        ),
    )
}
