package com.yortch.confirmationsaints.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiacriticsTest {
    @Test fun `strips common Latin diacritics`() {
        assertEquals("Therese", Diacritics.stripDiacritics("Thérèse"))
        assertEquals("Jose", Diacritics.stripDiacritics("José"))
        assertEquals("Nicolas", Diacritics.stripDiacritics("Nicolás"))
    }

    @Test fun `contains is diacritic-insensitive and case-insensitive`() {
        assertTrue("Santa Thérèse de Lisieux".containsIgnoringDiacritics("therese"))
        assertTrue("José María".containsIgnoringDiacritics("jose maria"))
    }

    @Test fun `equals is diacritic-insensitive`() {
        assertTrue("Ángel".equalsIgnoringDiacritics("angel"))
    }
}

