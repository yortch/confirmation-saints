package com.yortch.confirmationsaints.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Tests for birth year extraction from ISO date strings.
 *
 * Contract under test (iOS-committed edge case):
 *  - Birth year extraction must handle 4-digit zero-padded years such as
 *    "0256-MM-DD" (e.g. very early saints) and return the integer 256, not
 *    0, not throw, not silently drop the record.
 *  - birthDate may be null; parser returns null, not 0.
 *  - Both "YYYY-MM-DD" and "YYYY" shapes are accepted.
 */
class BirthDateParsingTest {

    @Test
    fun should_extract_year_256_from_zero_padded_string_0256() {
        assertEquals(256, DateFormatting.parseBirthYear("0256-12-26"))
    }

    @Test
    fun should_extract_year_from_standard_four_digit_date() {
        assertEquals(1873, DateFormatting.parseBirthYear("1873-01-02"))
    }

    @Test
    fun should_extract_year_when_only_year_provided() {
        assertEquals(1245, DateFormatting.parseBirthYear("1245"))
    }

    @Test
    fun should_return_null_for_null_birth_date() {
        assertNull(DateFormatting.parseBirthYear(null))
    }

    @Test
    fun should_return_null_for_malformed_birth_date() {
        assertNull(DateFormatting.parseBirthYear("unknown"))
    }

    @Test
    fun should_not_confuse_leading_zero_year_with_octal() {
        assertEquals(88, DateFormatting.parseBirthYear("0088"))
    }
}
