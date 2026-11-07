package com.panther.app.core.format

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

data class JalaliDate(val year: Int, val month: Int, val day: Int)

/**
 * Single source of truth for Persian (Jalali / Solar Hijri) date formatting and
 * Persian digit conversion across all screens and PDF exports.
 */
object PantherDateFormat {
    /** Locale used by the application interface. */
    val locale: Locale = Locale.forLanguageTag("fa-IR")

    private val gregorianDaysBeforeMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)

    /** Converts a Gregorian [LocalDate] to an exact Solar Hijri [JalaliDate]. */
    fun toJalali(date: LocalDate): JalaliDate =
        gregorianToJalali(date.year, date.monthValue, date.dayOfMonth)

    /** Converts Gregorian year, month (1..12) and day (1..31) to [JalaliDate]. */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gyAdjusted = if (gm > 2) gy + 1 else gy
        var days = 355666 +
            (365 * gy) +
            ((gyAdjusted + 3) / 4) -
            ((gyAdjusted + 99) / 100) +
            ((gyAdjusted + 399) / 400) +
            gd +
            gregorianDaysBeforeMonth[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return JalaliDate(year = jy, month = jm, day = jd)
    }

    /** Formats a [JalaliDate] uniformly as `YYYY/MM/DD` with Persian digits. */
    fun formatJalali(date: JalaliDate): String {
        val y = date.year.toString().padStart(4, '0')
        val m = date.month.toString().padStart(2, '0')
        val d = date.day.toString().padStart(2, '0')
        return persianDigits("$y/$m/$d")
    }

    /** Formats epoch milliseconds in [zoneId] as a uniform Persian Jalali date (`YYYY/MM/DD`). */
    fun format(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault()): String {
        val localDate = Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
        return formatJalali(toJalali(localDate))
    }

    /** Primary display formatter used uniformly across UI screens and PDF export. */
    fun medium(timestamp: Long): String = format(timestamp)

    /** Converts ASCII and Arabic-Indic digits to Persian digits (`۰`..`۹`). */
    fun persianDigits(value: String): String = buildString(value.length) {
        for (ch in value) {
            append(
                when (ch) {
                    in '0'..'9' -> '۰' + (ch - '0')
                    in '٠'..'٩' -> '۰' + (ch - '٠')
                    else -> ch
                },
            )
        }
    }

    fun persianNumber(value: Int): String = persianDigits(value.toString())

    /** Converts Persian and Arabic-Indic digits (and fullwidth separators) to ASCII for parsing/search. */
    fun asciiDigits(value: String): String = buildString(value.length) {
        for (ch in value) {
            append(
                when (ch) {
                    in '۰'..'۹' -> '0' + (ch - '۰')
                    in '٠'..'٩' -> '0' + (ch - '٠')
                    '٫', ',' -> '.'
                    '：' -> ':'
                    else -> ch
                },
            )
        }
    }
}
