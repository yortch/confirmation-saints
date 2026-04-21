package com.yortch.confirmationsaints.ui

import org.junit.Ignore
import org.junit.Test

/**
 * STUB — Compose UI instrumentation test for in-app language switching.
 * Bodies to be filled in once Aragorn lands Settings (Phase 7) and wires
 * LocalizationService into the UI.
 *
 * Contract under test (committed decision — parity with iOS):
 *  - Settings screen exposes an EN / ES toggle.
 *  - Switching to ES updates visible strings *in-app*, without requiring
 *    a process restart or device locale change.
 *  - The toggle persists across process restart (DataStore).
 */
@Ignore("Stub — awaiting SettingsScreen + LocalizationService wiring (Aragorn, Phase 7)")
class LanguageSwitchTest {

    @Test
    fun should_display_english_strings_when_language_is_en() {
        // TODO: Launch app with AppLanguage.EN. Navigate to Settings.
        // Assert the screen title is the English label (e.g. "Settings").
    }

    @Test
    fun should_update_visible_strings_when_switching_to_es() {
        // TODO: In Settings, tap the ES option. Without restarting the process,
        // assert the screen title recomposes to the Spanish label
        // (e.g. "Ajustes" / "Configuración"). This is the in-app switch contract.
    }

    @Test
    fun should_update_saint_list_localized_strings_after_language_switch() {
        // TODO: After switching to ES, navigate back to SaintList. Assert a
        // known localized label (e.g. category header, filter chip) renders in
        // Spanish — confirming the language change propagates across screens.
    }

    @Test
    fun should_persist_language_selection_across_activity_recreation() {
        // TODO: Switch to ES, recreate the Activity (configuration change),
        // assert the app remains in ES (strings stay Spanish).
    }
}
