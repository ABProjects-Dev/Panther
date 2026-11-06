package com.panther.app.core.format

import java.text.DateFormat
import java.util.Date
import java.util.Locale

/**
 * Single source of truth for date rendering so every screen and the PDF export
 * use the same Persian calendar and locale.
 */
object PantherDateFormat {
    /** Locale used by the application interface. */
    val locale: Locale = Locale.forLanguageTag("fa-IR")

    fun medium(timestamp: Long): String =
        DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(Date(timestamp))
}
