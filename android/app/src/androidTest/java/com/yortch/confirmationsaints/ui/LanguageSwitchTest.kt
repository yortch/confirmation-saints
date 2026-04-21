package com.yortch.confirmationsaints.ui

import org.junit.Ignore
import org.junit.Test

/**
 * Compose UI instrumentation test for in-app language switching.
 *
 * Contract under test (committed decision — parity with iOS):
 *  - Settings screen exposes an EN / ES toggle (radio).
 *  - Switching to ES updates visible strings *in-app*, without requiring
 *    a process restart or device locale change.
 *  - The toggle persists across process restart (DataStore).
 *
 * Scope note: [com.yortch.confirmationsaints.ui.screens.settings.SettingsScreen]
 * injects two `@HiltViewModel`s (SettingsViewModel, SaintListViewModel) via
 * `hiltViewModel()`. Driving the language toggle through the UI therefore
 * requires the Hilt test harness:
 *   1. A HiltTestRunner declared as `testInstrumentationRunner` in
 *      app/build.gradle.kts.
 *   2. `@HiltAndroidTest` + `HiltAndroidRule` on this class.
 *   3. `createAndroidComposeRule<MainActivity>()` so `LocalAppLanguage` is
 *      bound to the real `LocalizationService` StateFlow.
 *
 * None of that exists yet. Per the testing lane contract, this file does NOT
 * add the HiltTestRunner — see
 * `.squad/decisions/inbox/legolas-android-instrumentation-tests.md`. Once the
 * harness lands, remove each @Ignore and implement the TODOs below. Stable
 * EN/ES string pairs to assert on (both sourced from
 * [com.yortch.confirmationsaints.localization.AppStrings]):
 *   - "Settings" ↔ "Ajustes"
 *   - "Language" ↔ "Idioma"
 *   - "Saints"   ↔ "Santos"
 */
class LanguageSwitchTest {

    @Test
    @Ignore("Needs HiltTestRunner to launch MainActivity + SettingsScreen. Blocking on Aragorn (see decisions inbox).")
    fun should_display_english_strings_when_language_is_en() {
        // TODO(after Hilt test runner lands): launch MainActivity with
        // AppLanguage.EN seeded, navigate to the Settings tab (bottom-nav
        // label "Settings"), assert the top-bar title "Settings" and the
        // section header "Language" are displayed.
    }

    @Test
    @Ignore("Needs HiltTestRunner — the ES radio must be wired to the real SettingsViewModel. Blocking on Aragorn (see decisions inbox).")
    fun should_update_visible_strings_when_switching_to_es() {
        // TODO(after Hilt test runner lands): on the Settings tab, tap the
        // "Español" radio (displayName for AppLanguage.ES). Without an
        // Activity recreate, assert the top-bar title recomposes to
        // "Ajustes" and the section header reads "Idioma". This verifies
        // the iOS-parity in-app switch contract.
    }

    @Test
    @Ignore("Needs HiltTestRunner — cross-screen recomposition via LocalAppLanguage requires the Hilt-wired root. Blocking on Aragorn (see decisions inbox).")
    fun should_update_saint_list_localized_strings_after_language_switch() {
        // TODO(after Hilt test runner lands): after switching to ES,
        // navigate to the Saints tab. Assert the bottom-nav label reads
        // "Santos" and the list renders the Spanish saint name
        // "Santa Teresa de Lisieux" (from saints-es.json).
    }

    @Test
    @Ignore("Needs HiltTestRunner + DataStore survival across Activity recreation. Blocking on Aragorn (see decisions inbox).")
    fun should_persist_language_selection_across_activity_recreation() {
        // TODO(after Hilt test runner lands): switch to ES, call
        // composeRule.activity.recreate(), then assert "Ajustes" still
        // renders (DataStore persisted the choice and LocalizationService
        // re-emits ES on next composition).
    }
}
