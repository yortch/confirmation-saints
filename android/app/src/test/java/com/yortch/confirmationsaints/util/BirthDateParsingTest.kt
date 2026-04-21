package com.yortch.confirmationsaints.util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled

/**
 * STUB — bodies to be filled in once Aragorn lands DateFormatUtils / birth-date parsing.
 *
 * Contract under test (iOS-committed edge case):
 *  - Birth year extraction must handle 4-digit zero-padded years such as
 *    "0256-MM-DD" (e.g. very early saints) and return the integer 256, not
 *    0, not throw, not silently drop the record.
 *  - birthDate may be null; parser returns null, not 0.
 *  - Both "YYYY-MM-DD" and "YYYY" shapes are accepted.
 */
@Disabled("Stub — awaiting DateFormatUtils / birth-date parser (Aragorn, Phase 2)")
class BirthDateParsingTest {

    @Test
    fun should_extract_year_256_from_zero_padded_string_0256() {
        // TODO: assertEquals(256, parseBirthYear("0256-12-26"))
        // Regression guard for the iOS "0256" edge case.
    }

    @Test
    fun should_extract_year_from_standard_four_digit_date() {
        // TODO: assertEquals(1873, parseBirthYear("1873-01-02"))
    }

    @Test
    fun should_extract_year_when_only_year_provided() {
        // TODO: assertEquals(1245, parseBirthYear("1245"))
    }

    @Test
    fun should_return_null_for_null_birth_date() {
        // TODO: assertNull(parseBirthYear(null))
    }

    @Test
    fun should_return_null_for_malformed_birth_date() {
        // TODO: assertNull(parseBirthYear("unknown")); must not throw.
    }

    @Test
    fun should_not_confuse_leading_zero_year_with_octal() {
        // TODO: parseBirthYear("0088") == 88, not 0, not 72 (octal).
    }
}
