package com.yortch.confirmationsaints.ui

import org.junit.Ignore
import org.junit.Test

/**
 * Compose UI instrumentation test for SaintListScreen.
 *
 * Contract under test:
 *  - List renders all 70 saints (or the paged/virtualized equivalent) from the
 *    bundled JSON (SharedContent/saints/saints-en.json).
 *  - Search field filters results using diacritic-insensitive matching
 *    (per 2025-07-17 decision).
 *
 * Scope note: [SaintListScreen] uses `hiltViewModel()` to inject
 * [com.yortch.confirmationsaints.viewmodel.SaintListViewModel] (which depends on
 * [com.yortch.confirmationsaints.data.repository.SaintRepository]). Launching
 * the screen in an instrumentation test therefore requires:
 *   1. A HiltTestRunner declared as `testInstrumentationRunner` in
 *      app/build.gradle.kts.
 *   2. `@HiltAndroidTest` + `HiltAndroidRule` on this class.
 *   3. `createAndroidComposeRule<MainActivity>()` (or a Hilt test activity).
 *
 * None of those exist yet. Per the testing lane contract, this file does NOT
 * add the HiltTestRunner — see
 * `.squad/decisions/inbox/legolas-android-instrumentation-tests.md` for the
 * gap filed to Aragorn. Once that lands, remove each @Ignore and implement the
 * TODOs below. Stable saint-id/name pairs to use are:
 *   - therese-of-lisieux:   EN="St. Thérèse of Lisieux",  ES="Santa Teresa de Lisieux"
 *   - joan-of-arc:          EN="St. Joan of Arc",         ES="Santa Juana de Arco"
 *   - patrick:              EN="St. Patrick",             ES="San Patricio"
 */
class SaintListDisplayTest {

    @Test
    @Ignore("Needs HiltTestRunner — SaintListScreen depends on @HiltViewModel. Blocking on Aragorn (see decisions inbox).")
    fun should_render_saint_list_with_known_saint_row() {
        // TODO(after Hilt test runner lands):
        //   composeRule.setContent {
        //     CompositionLocalProvider(LocalAppLanguage provides AppLanguage.EN) {
        //       SaintListScreen(onSaintClick = {})
        //     }
        //   }
        //   composeRule.onNodeWithText("St. Thérèse of Lisieux", substring = true)
        //     .assertIsDisplayed()
    }

    @Test
    @Ignore("Needs HiltTestRunner — requires SaintListViewModel wired via Hilt. Blocking on Aragorn (see decisions inbox).")
    fun should_filter_results_when_search_text_entered() {
        // TODO(after Hilt test runner lands): type "francis" into the search
        // field, assert "St. Francis of Assisi" is displayed (use substring
        // match) and "St. Peter" is absent. Field placeholder key:
        // "Name, interest, country...".
    }

    @Test
    @Ignore("Needs HiltTestRunner — diacritic-insensitive search runs through the real filter pipeline. Blocking on Aragorn (see decisions inbox).")
    fun should_match_diacritic_insensitive_search() {
        // TODO(after Hilt test runner lands): type "therese" (no accents),
        // assert "St. Thérèse of Lisieux" is displayed. Guards the
        // cross-platform diacritic-insensitive contract (decisions.md
        // 2025-07-17).
    }

    @Test
    @Ignore("Needs HiltTestRunner — empty-state text belongs to SaintListScreen hosted by Hilt. Blocking on Aragorn (see decisions inbox).")
    fun should_show_empty_state_when_no_matches() {
        // TODO(after Hilt test runner lands): type "zzzznotasaint", assert
        // the English empty-state title "No Saints Found" is displayed.
    }
}
