package com.yortch.confirmationsaints.ui

import org.junit.Ignore
import org.junit.Test

/**
 * STUB — Compose UI instrumentation test for SaintListScreen.
 * Bodies to be filled in once Aragorn lands Phase 3.
 *
 * Contract under test:
 *  - List renders all 70 saints (or the paged/virtualized equivalent).
 *  - Search field filters results using diacritic-insensitive matching
 *    (per 2025-07-17 decision).
 */
@Ignore("Stub — awaiting SaintListScreen (Aragorn, Phase 3)")
class SaintListDisplayTest {

    @Test
    fun should_render_saint_list_with_known_saint_row() {
        // TODO: Launch SaintListScreen. Scroll/assert a known row
        // (e.g. "Thérèse of Lisieux") is displayed.
    }

    @Test
    fun should_filter_results_when_search_text_entered() {
        // TODO: Type "francis" into the search field; assert Francis of Assisi
        // is displayed and a non-matching saint (e.g. "Peter") is not.
    }

    @Test
    fun should_match_diacritic_insensitive_search() {
        // TODO: Type "therese" (no accents). Assert "Thérèse of Lisieux" is
        // displayed. Guards the cross-platform diacritic-insensitive contract.
    }

    @Test
    fun should_show_empty_state_when_no_matches() {
        // TODO: Type "zzzznotasaint". Assert empty-state semantics node shown.
    }
}
