package com.yortch.confirmationsaints.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import com.yortch.confirmationsaints.MainActivity
import com.yortch.confirmationsaints.data.repository.PreferencesRepository
import com.yortch.confirmationsaints.localization.AppLanguage
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * Instrumentation coverage for the in-app EN ↔ ES language toggle.
 *
 * Contract under test (parity with iOS — committed decision):
 *  - Settings exposes an EN/ES radio ("English" / "Español"); tapping ES
 *    recomposes live UI strings across the whole app without an Activity
 *    restart or device-locale change.
 *  - Persists across `Activity.recreate()` via DataStore.
 *
 * Stable EN ↔ ES string pairs asserted here (all from [com.yortch.confirmationsaints.localization.AppStrings]):
 *  - "Settings" ↔ "Ajustes"
 *  - "Language" ↔ "Idioma"
 *  - "Saints"   ↔ "Santos"
 *  - "St. Thérèse of Lisieux" ↔ "Santa Teresa de Lisieux"
 *
 * Seeding: starts each test with `hasSeenWelcome=true, language=EN` so
 * MainActivity lands on the Saints tab in English.
 */
@HiltAndroidTest
class LanguageSwitchTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var prefs: PreferencesRepository

    @Before
    fun setUp() {
        hiltRule.inject()
        runBlocking {
            prefs.setHasSeenWelcome(true)
            prefs.setLanguage(AppLanguage.EN)
        }
    }

    @Test
    fun should_display_english_strings_when_language_is_en() {
        ActivityScenario.launch(MainActivity::class.java).use {
            awaitMainScaffold()
            navigateToSettings()

            // Top-bar title + section header both localize through
            // AppStrings.localized() with AppLanguage.EN — in English they
            // render as the literal keys.
            // "Settings" is also the bottom-nav label, so assert via
            // onAllNodesWithText (expect at least 2: nav label + top bar).
            val settingsNodes = composeRule
                .onAllNodesWithText("Settings")
                .fetchSemanticsNodes()
            assert(settingsNodes.size >= 2) {
                "Expected 'Settings' to render in both top-bar and bottom-nav; found ${settingsNodes.size}"
            }
            composeRule.onNodeWithText("Language").assertIsDisplayed()
        }
    }

    @Test
    fun should_update_visible_strings_when_switching_to_es() {
        ActivityScenario.launch(MainActivity::class.java).use {
            awaitMainScaffold()
            navigateToSettings()

            // Tap the Spanish radio — displayName of AppLanguage.ES.
            composeRule.onNodeWithText("Español").performClick()
            composeRule.waitForIdle()

            // After the DataStore write propagates through
            // LocalizationService, all AppStrings.localized() call sites
            // recompose. No Activity recreate required.
            composeRule.waitUntil(5_000) {
                composeRule.onAllNodesWithText("Ajustes").fetchSemanticsNodes().isNotEmpty()
            }
            // Both top-bar title and the newly-translated bottom-nav label
            // must read "Ajustes".
            val ajustesNodes = composeRule
                .onAllNodesWithText("Ajustes")
                .fetchSemanticsNodes()
            assert(ajustesNodes.size >= 2) {
                "Expected 'Ajustes' in both top-bar and bottom-nav after ES switch; found ${ajustesNodes.size}"
            }
            composeRule.onNodeWithText("Idioma").assertIsDisplayed()
            // English must no longer render anywhere.
            composeRule.onNodeWithText("Language").assertDoesNotExist()
        }
    }

    @Test
    fun should_update_saint_list_localized_strings_after_language_switch() {
        ActivityScenario.launch(MainActivity::class.java).use {
            awaitMainScaffold()
            navigateToSettings()

            composeRule.onNodeWithText("Español").performClick()
            composeRule.waitForIdle()
            composeRule.waitUntil(5_000) {
                composeRule.onAllNodesWithText("Ajustes").fetchSemanticsNodes().isNotEmpty()
            }

            // Back to the Saints tab — bottom-nav label is now "Santos".
            composeRule.onNodeWithText("Santos").performClick()
            composeRule.waitForIdle()

            // SaintListViewModel re-reads `saints-es.json` on language
            // change, so the row must render the Spanish name.
            composeRule.waitUntil(10_000) {
                composeRule.onAllNodesWithText("Santa Teresa de Lisieux", substring = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Santa Teresa de Lisieux", substring = true)
                .assertIsDisplayed()
        }
    }

    @Test
    fun should_persist_language_selection_across_activity_recreation() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            awaitMainScaffold()
            navigateToSettings()

            composeRule.onNodeWithText("Español").performClick()
            composeRule.waitForIdle()
            composeRule.waitUntil(5_000) {
                composeRule.onAllNodesWithText("Ajustes").fetchSemanticsNodes().isNotEmpty()
            }

            // Force the Activity to be destroyed and recreated — the
            // persisted `appLanguage` code in DataStore must drive
            // LocalizationService to re-emit ES on next composition, so
            // "Ajustes" must still render.
            scenario.recreate()
            composeRule.waitForIdle()
            composeRule.waitUntil(10_000) {
                composeRule.onAllNodesWithText("Ajustes").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Ajustes").assertIsDisplayed()
            composeRule.onNodeWithText("Settings").assertDoesNotExist()
        } finally {
            scenario.close()
        }
    }

    private fun awaitMainScaffold() {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Name, interest, country...")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun navigateToSettings() {
        // Bottom-nav "Settings" label (in EN). Unambiguous on SaintsHome
        // because the top-bar there reads "Saints", not "Settings".
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.waitForIdle()
        composeRule.waitUntil(5_000) {
            // The Language section header is a Settings-only landmark.
            composeRule.onAllNodesWithText("Language").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
