package com.panther.app.feature

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
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
import com.panther.app.domain.model.ExerciseSet
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
import org.junit.Assume.assumeNotNull
import org.junit.Assume.assumeTrue
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
    private val createdUris = mutableListOf<Uri>()

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
        createdUris.forEach { uri -> runCatching { context.contentResolver.delete(uri, null, null) } }
        createdUris.clear()
    }

    /**
     * A real `content://` document, so the export path runs through `ContentResolver` exactly as it
     * does when the user picks a destination with the system file picker. Skipped (not failed) when
     * the device exposes no external Downloads collection.
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun newDocumentUri(name: String, mimeType: String): Uri {
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, name)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
        }
        val uri = runCatching {
            context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        }.getOrNull()
        assumeNotNull("no external Downloads collection on this device", uri)
        val document = requireNotNull(uri)
        createdUris += document
        return document
    }

    private fun readAll(uri: Uri): ByteArray =
        requireNotNull(context.contentResolver.openInputStream(uri)) { "cannot read back $uri" }
            .use { it.readBytes() }

    private suspend fun awaitOperationMessage(): Int =
        withTimeout(15_000) { viewModel.operationMessage.first { it != null } }!!

    @Test
    fun restoreWritesSafetySnapshotAndReplacesTheData() = runBlocking<Unit> {
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
    fun invalidImportIsRejectedAndLeavesDataUntouched() = runBlocking<Unit> {
        repository.createProgram("Keep me")

        val badFile = File(context.cacheDir, "bad-backup.json").apply { writeText("{ not json") }
        viewModel.validateImport(Uri.fromFile(badFile))

        assertEquals(R.string.backup_invalid, awaitOperationMessage())
        assertNull(viewModel.pendingBackup.value)
        assertEquals("Keep me", repository.observePrograms().first().single().name)
        badFile.delete()
    }

    @Test
    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportBackupWritesADecodableDocumentToThePickedLocation() = runBlocking<Unit> {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        val target = newDocumentUri("panther-export.json", "application/json")
        repository.createProgram("Exported")

        viewModel.exportBackup(target)

        assertEquals(R.string.backup_exported, awaitOperationMessage())
        assertEquals("Exported", PantherBackupJson.decode(readAll(target)).programs.single().name)
    }

    @Test
    @RequiresApi(Build.VERSION_CODES.Q)
    fun exportProgramPdfWritesAPdfToThePickedLocation() = runBlocking<Unit> {
        assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        val target = newDocumentUri("panther-program.pdf", "application/pdf")
        val programId = repository.createProgram("PDF plan")
        val dayId = repository.addDay(programId, "Day A", 0)
        repository.saveExercise(
            dayId,
            null,
            "Squat",
            0,
            listOf(ExerciseSet(exerciseId = 0, setNumber = 1, targetType = TargetType.REPS, targetValue = 8)),
        )

        viewModel.exportProgramPdf(programId, target)

        val message = withTimeout(15_000) { viewModel.pdfMessage.first { it != null } }
        assertEquals(R.string.pdf_exported, message)
        val bytes = readAll(target)
        assertTrue("the exported document must not be empty", bytes.size > 100)
        assertEquals("%PDF-", String(bytes, 0, 5, Charsets.US_ASCII))
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
