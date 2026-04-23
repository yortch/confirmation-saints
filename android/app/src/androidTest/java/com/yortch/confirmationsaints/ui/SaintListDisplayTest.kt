package com.yortch.confirmationsaints.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
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
 * Instrumentation coverage for [com.yortch.confirmationsaints.ui.screens.saints.SaintListScreen]
 * rendering + search, reached via the full MainActivity → MainScaffold path
 * (Saint List is the `startDestination` of the Saints tab, which is the
 * default tab after welcome).
 *
 * Stable saint-name pairs used below (present in
 * `SharedContent/saints/saints-{en,es}.json`; guaranteed by the cross-platform
 * parity guardrail):
 *  - `therese-of-lisieux`    EN="St. Thérèse of Lisieux"  ES="Santa Teresa de Lisieux"
 *  - `francis-of-assisi`     EN="St. Francis of Assisi"   ES="San Francisco de Asís"
 *  - `joan-of-arc`           EN="St. Joan of Arc"         ES="Santa Juana de Arco"
 *
 * Diacritic-insensitive search is a committed contract (decisions.md
 * 2025-07-17). Typing "therese" (no accent) MUST return St. Thérèse.
 *
 * Seeding: `hasSeenWelcome=true` so MainActivity lands directly on the Saint
 * List without an intermediate pager step.
 */
@HiltAndroidTest
class SaintListDisplayTest {

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
    fun should_render_saint_list_with_known_saint_row() {
        ActivityScenario.launch(MainActivity::class.java).use {
            awaitSaintList()

            // Typing a query that matches exactly one saint collapses the
            // LazyColumn to a single row, which is guaranteed to be in the
            // viewport regardless of the underlying sort order.
            composeRule.onNodeWithText("Name, interest, country...")
                .performTextInput("Thérèse")
            composeRule.waitForIdle()

            composeRule.waitUntil(5_000) {
                composeRule.onAllNodesWithText("St. Thérèse of Lisieux", substring = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("St. Thérèse of Lisieux", substring = true)
                .assertIsDisplayed()
        }
    }

    @Test
    fun should_filter_results_when_search_text_entered() {
        ActivityScenario.launch(MainActivity::class.java).use {
            awaitSaintList()

            composeRule.onNodeWithText("Name, interest, country...")
                .performTextInput("Francis of Assisi")
            composeRule.waitForIdle()

            composeRule.waitUntil(5_000) {
                composeRule.onAllNodesWithText("St. Francis of Assisi", substring = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("St. Francis of Assisi", substring = true)
                .assertIsDisplayed()
            // Saints not matching the query must be filtered out.
            composeRule.onNodeWithText("St. Thérèse of Lisieux", substring = true)
                .assertDoesNotExist()
            composeRule.onNodeWithText("St. Joan of Arc", substring = true)
                .assertDoesNotExist()
        }
    }

    @Test
    fun should_match_diacritic_insensitive_search() {
        ActivityScenario.launch(MainActivity::class.java).use {
            awaitSaintList()

            // Type the ASCII spelling "therese" — WITHOUT the é accent. Guards
            // decisions.md 2025-07-17 (String+Diacritics contract).
            composeRule.onNodeWithText("Name, interest, country...")
                .performTextInput("therese")
            composeRule.waitForIdle()

            composeRule.waitUntil(5_000) {
                composeRule.onAllNodesWithText("St. Thérèse of Lisieux", substring = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("St. Thérèse of Lisieux", substring = true)
                .assertIsDisplayed()
        }
    }

    @Test
    fun should_show_empty_state_when_no_matches() {
        ActivityScenario.launch(MainActivity::class.java).use {
            awaitSaintList()

            composeRule.onNodeWithText("Name, interest, country...")
                .performTextInput("zzzznotasaint")
            composeRule.waitForIdle()

            composeRule.waitUntil(5_000) {
                composeRule.onAllNodesWithText("No Saints Found")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("No Saints Found").assertIsDisplayed()
            // None of the known saint rows should be present.
            composeRule.onNodeWithText("St. Thérèse of Lisieux", substring = true)
                .assertDoesNotExist()
        }
    }

    /**
     * Waits until the Saint List has finished its initial load —
     * `isLoading = false` and at least one saint row has been composed.
     * Uses the search-field placeholder as a stable landmark for the screen
     * itself and the "St. " prefix (shared by every saint name) for content.
     */
    private fun awaitSaintList() {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Name, interest, country...")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("St. ", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
