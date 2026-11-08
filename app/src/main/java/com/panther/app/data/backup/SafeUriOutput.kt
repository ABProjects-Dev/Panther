package com.panther.app.data.backup

import android.content.ContentResolver
import android.net.Uri
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream

object SafeUriOutput {
    /**
     * Opens a truncated [OutputStream] for [uri], falling back to `"w"` mode if the target
     * [android.provider.DocumentsProvider] rejects explicit `"wt"` mode on Android 10+.
     */
    fun openTruncated(contentResolver: ContentResolver, uri: Uri): OutputStream {
        val stream = try {
            contentResolver.openOutputStream(uri, "wt")
        } catch (_: IllegalArgumentException) {
            contentResolver.openOutputStream(uri, "w")
        } catch (_: UnsupportedOperationException) {
            contentResolver.openOutputStream(uri, "w")
        } catch (_: FileNotFoundException) {
            contentResolver.openOutputStream(uri, "w")
        } ?: throw IllegalArgumentException("Invalid export URI")

        if (stream is FileOutputStream) {
            runCatching { stream.channel.truncate(0L) }
        }
        return stream
    }
}
