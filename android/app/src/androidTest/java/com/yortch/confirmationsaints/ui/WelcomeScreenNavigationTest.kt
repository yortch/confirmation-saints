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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

/**
 * Full-app Compose UI instrumentation tests for the Welcome → Saint List
 * onboarding gate.
 *
 * Contract under test (mirrors iOS committed behavior):
 *  - On first launch (DataStore `hasSeenWelcome == false`) MainActivity shows
 *    the Welcome pager.
 *  - Tapping "Let's Go!" on the last pager page flips `hasSeenWelcome = true`
 *    and swaps the root to the Saint List (first destination of MainScaffold).
 *  - On subsequent launches (DataStore already `hasSeenWelcome == true`) the
 *    Welcome pager is never shown; Saint List is the initial destination.
 *
 * Seeding strategy (see SKILL.md — Pattern B + DataStore seeding):
 *  - Uses [createEmptyComposeRule] plus a manual [ActivityScenario.launch] so
 *    we can call `prefs.setHasSeenWelcome(...)` *before* MainActivity exists.
 *    `createAndroidComposeRule<MainActivity>()` launches the Activity in its
 *    own `before()` — too early for the "skip welcome on relaunch" test.
 *  - `@Before` resets the DataStore to a known baseline so tests don't
 *    leak state into one another (tests run in the same HiltTestApplication
 *    process; DataStore writes persist).
 */
@HiltAndroidTest
class WelcomeScreenNavigationTest {

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
            prefs.setHasSeenWelcome(false)
            prefs.setLanguage(AppLanguage.EN)
        }
    }

    @Test
    fun should_show_welcome_screen_when_hasSeenWelcome_is_false() {
        runBlocking { prefs.setHasSeenWelcome(false) }

        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.waitForIdle()
            composeRule.waitUntil(5_000) {
                composeRule.onAllNodesWithText("Find Your Confirmation Saint")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Find Your Confirmation Saint").assertIsDisplayed()
            composeRule.onNodeWithText("Skip").assertIsDisplayed()
            composeRule.onNodeWithText("Next").assertIsDisplayed()
        }
    }

    @Test
    fun should_navigate_to_saint_list_when_get_started_is_tapped() {
        runBlocking { prefs.setHasSeenWelcome(false) }

        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.waitUntil(5_000) {
                composeRule.onAllNodesWithText("Find Your Confirmation Saint")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            // Advance the 4-page pager via the user-visible Next button.
            repeat(3) {
                composeRule.onNodeWithText("Next").performClick()
                composeRule.waitForIdle()
            }

            composeRule.onNodeWithText("Ready to Find Your Saint?").assertIsDisplayed()
            composeRule.onNodeWithText("Let's Go!").performClick()
            composeRule.waitForIdle()

            // After Let's Go! AppRoot flips to MainScaffold → SaintsHome.
            // The search placeholder is a uniquely identifying Saint List
            // element (top-bar "Saints" would collide with the nav tab).
            composeRule.waitUntil(5_000) {
                composeRule.onAllNodesWithText("Name, interest, country...")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Name, interest, country...").assertIsDisplayed()
        }
    }

    @Test
    fun should_persist_hasSeenWelcome_true_after_completing_onboarding() {
        runBlocking { prefs.setHasSeenWelcome(false) }

        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.waitUntil(5_000) {
                composeRule.onAllNodesWithText("Find Your Confirmation Saint")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            repeat(3) {
                composeRule.onNodeWithText("Next").performClick()
                composeRule.waitForIdle()
            }
            composeRule.onNodeWithText("Let's Go!").performClick()

            // Wait for AppRoot to recompose past the Welcome screen — proves
            // the onComplete → markWelcomeSeen → DataStore write round-trip
            // completed.
            composeRule.waitUntil(5_000) {
                composeRule.onAllNodesWithText("Name, interest, country...")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            val persisted = runBlocking { prefs.hasSeenWelcome.first() }
            assertEquals(
                "Completing the onboarding pager must persist hasSeenWelcome=true",
                true,
                persisted,
            )
        }
    }

    @Test
    fun should_skip_welcome_on_relaunch_when_hasSeenWelcome_is_true() {
        // Seed BEFORE the Activity exists — this is the whole reason we use
        // createEmptyComposeRule + manual ActivityScenario rather than
        // createAndroidComposeRule<MainActivity>().
        runBlocking { prefs.setHasSeenWelcome(true) }

        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.waitUntil(5_000) {
                composeRule.onAllNodesWithText("Name, interest, country...")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            // Saint List is the initial destination — no Welcome content ever
            // becomes visible.
            composeRule.onNodeWithText("Name, interest, country...").assertIsDisplayed()
            composeRule.onNodeWithText("Find Your Confirmation Saint").assertDoesNotExist()
            composeRule.onNodeWithText("Let's Go!").assertDoesNotExist()
        }
    }
}
