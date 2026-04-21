package com.yortch.confirmationsaints.localization

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled

/**
 * STUB — bodies to be filled in once Aragorn lands LocalizationService.
 *
 * Contract under test (mirrors iOS in-app language switch, committed decision):
 *  - LocalizationService exposes a StateFlow<AppLanguage>.
 *  - Selected language persists across process restart via DataStore.
 *  - AppStrings returns the correct translation for the active language.
 *  - Switching language updates the StateFlow synchronously for collectors.
 */
@Disabled("Stub — awaiting LocalizationService implementation (Aragorn, Phase 4)")
class LocalizationServiceTest {

    @Test
    fun should_default_to_device_locale_when_no_preference_stored() {
        // TODO: With an empty DataStore, assert initial StateFlow value matches
        // device locale (EN if device is en-*, ES if device is es-*, EN otherwise).
    }

    @Test
    fun should_update_stateflow_when_language_is_switched() {
        // TODO: Use Turbine. service.setLanguage(ES); expect StateFlow emits ES.
    }

    @Test
    fun should_persist_language_choice_to_datastore() {
        // TODO: setLanguage(ES), recreate service from same DataStore, assert
        // initial value == ES. Use an in-memory or tempFolder DataStore.
    }

    @Test
    fun should_return_english_string_when_language_is_en() {
        // TODO: AppStrings.get("settings.title", EN) returns the English value.
    }

    @Test
    fun should_return_spanish_string_when_language_is_es() {
        // TODO: AppStrings.get("settings.title", ES) returns the Spanish value,
        // which differs from the English value.
    }

    @Test
    fun should_fall_back_to_english_for_missing_translation_key() {
        // TODO: For a key present in EN but missing in ES, ES lookup returns the
        // EN value (do not throw, do not return the raw key).
    }
}
