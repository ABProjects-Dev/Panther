package com.panther.app.core.format

import java.time.LocalDate
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class PantherDateFormatTest {

    @Test
    fun convertsGregorianDatesToExactJalaliCalendar() {
        assertEquals(JalaliDate(1405, 8, 16), PantherDateFormat.gregorianToJalali(2026, 11, 7))
        assertEquals(JalaliDate(1404, 12, 29), PantherDateFormat.gregorianToJalali(2026, 3, 20))
        assertEquals(JalaliDate(1405, 1, 1), PantherDateFormat.gregorianToJalali(2026, 3, 21))
        // 1403 Solar Hijri leap year (30 Esfand)
        assertEquals(JalaliDate(1403, 12, 30), PantherDateFormat.gregorianToJalali(2025, 3, 20))
        assertEquals(JalaliDate(1404, 1, 1), PantherDateFormat.gregorianToJalali(2025, 3, 21))
    }

    @Test
    fun formatsUniformPersianJalaliString() {
        val epochMs = LocalDate.of(2026, 11, 7).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals("۱۴۰۵/۰۸/۱۶", PantherDateFormat.format(epochMs, ZoneOffset.UTC))
    }

    @Test
    fun normalizesDigitsInBothDirections() {
        assertEquals("۱۲۳۴۵", PantherDateFormat.persianDigits("12345"))
        assertEquals("123.45:00", PantherDateFormat.asciiDigits("۱۲۳٫۴۵：۰۰"))
    }
}
