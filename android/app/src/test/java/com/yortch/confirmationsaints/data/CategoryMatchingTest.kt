package com.yortch.confirmationsaints.data

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled

/**
 * STUB — bodies to be filled in once Aragorn lands category matching.
 *
 * Contract under test (cross-language-matching, per the 2026-04-21 decision):
 *  - Matching logic compares against the English-canonical values in
 *    `patronOf`, `tags`, `affinities`, `region`, `lifeState`, `ageCategory`,
 *    `gender` — NOT against the localized display* arrays.
 *  - Result set for a given category value id is therefore identical whether
 *    the active language is EN or ES.
 */
@Disabled("Stub — awaiting category matching implementation (Aragorn, Phase 3)")
class CategoryMatchingTest {

    @Test
    fun should_return_non_empty_saints_for_known_category_value() {
        // TODO: For a well-populated category/value pair (e.g. region=Europe,
        // or affinities contains "students"), assert the matcher returns > 0 saints.
    }

    @Test
    fun should_match_on_english_canonical_values_regardless_of_active_language() {
        // TODO: Switch LocalizationService to ES, run the matcher for the same
        // category value, assert the returned saint id set equals the EN run.
        // This is the committed cross-language-matching guarantee.
    }

    @Test
    fun should_not_match_on_display_localized_fields() {
        // TODO: Construct or pick a saint whose displayPatronOf differs from its
        // canonical patronOf (e.g. "students" → displayPatronOf ["estudiantes"]).
        // Matching on the Spanish display term must NOT return that saint —
        // matching is driven by canonical English values only.
    }

    @Test
    fun should_return_empty_for_unknown_category_value() {
        // TODO: Matcher for a bogus category value id returns an empty list,
        // not null, and does not throw.
    }

    @Test
    fun should_match_young_age_category_saints() {
        // TODO: ageCategory == "young" should return Catherine of Siena,
        // Dominic Savio, Carlo Acutis, Chiara Luce Badano, José Sánchez del Río,
        // etc. Guards against regressions in the "young saints" surface.
    }
}
