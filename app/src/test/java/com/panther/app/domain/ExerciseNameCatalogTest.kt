package com.panther.app.domain

import com.panther.app.domain.model.ExerciseNameCatalog
import com.panther.app.domain.model.WorkoutDefaults
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The exercise-name catalogue is a device-only convenience list, so its matching rules decide what
 * the name field suggests and when the "add to catalogue" action appears.
 */
class ExerciseNameCatalogTest {
    private val catalog = listOf("اسکوات", "پرس سینه", "اسکوات از جلو", "  ددلیفت  ", "")

    @Test
    fun normalizeTrimsAndCollapsesWhitespace() {
        assertEquals("پرس سینه", ExerciseNameCatalog.normalize("  پرس   سینه "))
        assertEquals("", ExerciseNameCatalog.normalize("   "))
    }

    @Test
    fun normalizeFoldsPersianDigitsSoTypedVariantsMatch() {
        assertEquals("دمبل 12", ExerciseNameCatalog.normalize("دمبل ۱۲"))
    }

    @Test
    fun distinctDropsBlanksAndDuplicateSpellings() {
        val names = ExerciseNameCatalog.distinct(listOf("اسکوات", " اسکوات ", "Squat", "squat", ""))

        assertEquals(listOf("اسکوات", "Squat"), names)
    }

    @Test
    fun containsIgnoresCaseAndSurroundingSpaces() {
        assertTrue(ExerciseNameCatalog.contains(catalog, "  اسکوات  "))
        assertTrue(ExerciseNameCatalog.contains(catalog, "پرس سینه"))
        assertFalse(ExerciseNameCatalog.contains(catalog, "پرس سرشانه"))
        assertFalse(ExerciseNameCatalog.contains(catalog, "  "))
    }

    @Test
    fun suggestionsPreferPrefixMatchesThenShorterNames() {
        val suggestions = ExerciseNameCatalog.suggestions(catalog, "اسک")

        assertEquals(listOf("اسکوات", "اسکوات از جلو"), suggestions)
    }

    @Test
    fun suggestionsFallBackToSubstringMatches() {
        val suggestions = ExerciseNameCatalog.suggestions(catalog, "جلو")

        assertEquals(listOf("اسکوات از جلو"), suggestions)
    }

    @Test
    fun anEmptyQueryListsTheCatalogueSoTheFieldAlsoWorksAsPicker() {
        val suggestions = ExerciseNameCatalog.suggestions(catalog, "", limit = 2)

        assertEquals(2, suggestions.size)
    }

    @Test
    fun suggestionsAreLimitedAndBlankWhenNothingMatches() {
        assertEquals(1, ExerciseNameCatalog.suggestions(catalog, "ا", limit = 1).size)
        assertTrue(ExerciseNameCatalog.suggestions(catalog, "zzz").isEmpty())
    }

    @Test
    fun defaultsMatchTheOwnerDecisionOf2026_11_19() {
        assertEquals(12, WorkoutDefaults.DEFAULT_REPS)
        assertEquals(3, WorkoutDefaults.DEFAULT_SET_COUNT)
        assertEquals("0:30", WorkoutDefaults.DEFAULT_DURATION_RAW)
    }
}
