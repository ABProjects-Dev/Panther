package com.panther.app.domain

import com.panther.app.domain.model.DurationCatalog
import com.panther.app.domain.model.DurationOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DurationCatalogTest {

    @Test
    fun parseAcceptsMinutesAndSeconds() {
        val parsed = DurationCatalog.parse("1:30")
        assertNotNull(parsed)
        assertEquals(1, parsed!!.minutes)
        assertEquals(30, parsed.seconds)
        assertEquals(90, parsed.totalSeconds)
    }

    @Test
    fun parseAcceptsPersianDigits() {
        val parsed = DurationCatalog.parse("۲:۱۵")
        assertNotNull(parsed)
        assertEquals(2, parsed!!.minutes)
        assertEquals(15, parsed.seconds)
    }

    @Test
    fun parseTrimsSurroundingWhitespace() {
        assertEquals(75, DurationCatalog.parse("  1:15  ")?.totalSeconds)
    }

    @Test
    fun parseRejectsZeroZero() {
        assertNull(DurationCatalog.parse("0:00"))
    }

    @Test
    fun parseRejectsOutOfRangeSeconds() {
        assertNull(DurationCatalog.parse("1:60"))
    }

    @Test
    fun parseRejectsNegativeMinutes() {
        assertNull(DurationCatalog.parse("-1:30"))
    }

    @Test
    fun parseRejectsMalformedInput() {
        assertNull(DurationCatalog.parse(""))
        assertNull(DurationCatalog.parse("abc"))
        assertNull(DurationCatalog.parse("1"))
        assertNull(DurationCatalog.parse("1:2:3"))
    }

    @Test
    fun presetsAreSortedAndUnique() {
        val presets = DurationCatalog.presets
        assertTrue(presets.isNotEmpty())
        assertEquals(presets.size, presets.map { it.totalSeconds }.toSet().size)
        assertEquals(presets.map { it.totalSeconds }.sorted(), presets.map { it.totalSeconds })
    }

    @Test
    fun presetsCoverSecondsMinutesAndHours() {
        val totals = DurationCatalog.presets.map { it.totalSeconds }.toSet()
        assertTrue(5 in totals)
        assertTrue(60 in totals)
        assertTrue(3600 in totals)
    }

    @Test
    fun optionsIncludingAddsUnknownCustomValueInSortedPosition() {
        val options = DurationCatalog.optionsIncluding("0:07")
        val totals = options.map { it.totalSeconds }
        assertTrue(7 in totals)
        assertEquals(totals.sorted(), totals)
    }

    @Test
    fun optionsIncludingIgnoresValueAlreadyInPresets() {
        val baseline = DurationCatalog.presets
        val options = DurationCatalog.optionsIncluding("0:30")
        assertEquals(baseline.size, options.size)
    }

    @Test
    fun optionsIncludingIgnoresUnparsableValue() {
        assertEquals(DurationCatalog.presets.size, DurationCatalog.optionsIncluding("nonsense").size)
    }

    @Test
    fun searchReturnsEverythingForBlankQuery() {
        assertEquals(DurationCatalog.presets.size, DurationCatalog.search("").size)
    }

    @Test
    fun searchMatchesByExactTotalSeconds() {
        val results = DurationCatalog.search("150")
        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.totalSeconds == 150 })
    }

    @Test
    fun searchMatchesPersianQueryAgainstTheLabel() {
        val results = DurationCatalog.search("۲ دقیقه")
        assertFalse(results.isEmpty())
        assertTrue(results.any { it.minutes == 2 })
    }

    @Test
    fun searchIsCaseAndSpaceInsensitiveForAsciiDigits() {
        assertEquals(
            DurationCatalog.search("1:30").map { it.totalSeconds },
            DurationCatalog.search("  ۱:۳۰ ").map { it.totalSeconds },
        )
    }

    @Test
    fun searchFindsNothingForAnImpossibleValue() {
        assertTrue(DurationCatalog.search("99999").isEmpty())
    }

    @Test
    fun optionKeyUsesPaddedSecondsAndCompactLabelUsesPersianDigits() {
        val option = DurationOption(minutes = 3, seconds = 5)
        assertEquals("3:05", option.key)
        assertEquals("۰۳:۰۵", option.compactLabel)
    }
}
