package com.yortch.confirmationsaints.data.repository

import com.yortch.confirmationsaints.data.model.Saint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CategoryMatcherTest {

    private fun saint(
        id: String,
        birthDate: String? = null,
        region: String? = null,
        ageCategory: String? = null,
        patronOf: List<String> = emptyList(),
    ) = Saint(
        id = id,
        name = id,
        feastDay = "01-01",
        birthDate = birthDate,
        region = region,
        ageCategory = ageCategory,
        patronOf = patronOf,
        biography = "test",
    )

    @Test fun `era matches early-church for pre-500 birth`() {
        assertTrue(CategoryMatcher.matchesEra(saint("a", birthDate = "0347-01-01"), "early-church"))
        assertFalse(CategoryMatcher.matchesEra(saint("a", birthDate = "0600-01-01"), "early-church"))
    }

    @Test fun `era contemporary covers 1950 and later`() {
        assertTrue(CategoryMatcher.matchesEra(saint("a", birthDate = "1950-05-04"), "contemporary"))
        assertTrue(CategoryMatcher.matchesEra(saint("a", birthDate = "1991-03-12"), "contemporary"))
        assertFalse(CategoryMatcher.matchesEra(saint("a", birthDate = "1949-12-31"), "contemporary"))
    }

    @Test fun `era returns false when birthDate is missing`() {
        assertFalse(CategoryMatcher.matchesEra(saint("a", birthDate = null), "medieval"))
    }

    @Test fun `saintsForCategory filters by patronage diacritic-insensitively`() {
        val saints = listOf(
            saint("a", patronOf = listOf("Youth"), birthDate = "1901-01-01"),
            saint("b", patronOf = listOf("Música"), birthDate = "1500-01-01"),
            saint("c", patronOf = listOf("Farmers"), birthDate = "0400-01-01"),
        )
        val result = CategoryMatcher.saintsForCategory("patronage", "musica", saints).map { it.id }
        assertEquals(listOf("b"), result)
    }

    @Test fun `saintsForCategory era filter works end-to-end`() {
        val saints = listOf(
            saint("early", birthDate = "0250-01-01"),
            saint("modern", birthDate = "1850-01-01"),
            saint("contemp", birthDate = "1980-01-01"),
        )
        val result = CategoryMatcher.saintsForCategory("era", "modern", saints).map { it.id }
        assertEquals(listOf("modern"), result)
    }
}
