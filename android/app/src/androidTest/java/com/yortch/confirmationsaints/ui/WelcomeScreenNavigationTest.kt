package com.yortch.confirmationsaints.ui

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.yortch.confirmationsaints.localization.AppLanguage
import com.yortch.confirmationsaints.localization.LocalAppLanguage
import com.yortch.confirmationsaints.ui.screens.onboarding.WelcomeScreen
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertTrue

/**
 * Compose UI instrumentation test for the Welcome onboarding pager.
 *
 * Contract under test (mirrors iOS committed behavior):
 *  - On first launch (hasSeenWelcome == false), the Welcome pager is shown.
 *  - Tapping "Get started" on the last page invokes the completion callback,
 *    which at the app level marks hasSeenWelcome = true and surfaces the
 *    Saint List.
 *  - On subsequent launches, the Saint List is the initial destination.
 *
 * Scope note: This class exercises the [WelcomeScreen] composable in
 * isolation via [createComposeRule]. The two whole-app relaunch/persistence
 * cases need a MainActivity + Hilt launch and therefore remain @Ignore
 * pending a HiltTestRunner (see
 * `.squad/decisions/inbox/legolas-android-instrumentation-tests.md`).
 */
class WelcomeScreenNavigationTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun should_show_welcome_screen_when_hasSeenWelcome_is_false() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.EN) {
                WelcomeScreen(onComplete = {})
            }
        }

        // Page 1 title (English) renders when the pager first composes —
        // which is exactly what AppRoot shows when hasSeenWelcome == false.
        composeRule.onNodeWithText("Find Your Confirmation Saint").assertIsDisplayed()
        // Skip + Next controls are visible on the first three pages.
        composeRule.onNodeWithText("Skip").assertIsDisplayed()
        composeRule.onNodeWithText("Next").assertIsDisplayed()
    }

    @Test
    fun should_navigate_to_saint_list_when_get_started_is_tapped() {
        var completed = false
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLanguage provides AppLanguage.EN) {
                WelcomeScreen(onComplete = { completed = true })
            }
        }

        // Advance through the 4-page pager by tapping Next three times —
        // exactly how a user reaches the final "Let's Go!" CTA.
        repeat(3) {
            composeRule.onNodeWithText("Next").performClick()
            composeRule.waitForIdle()
        }

        // Final page: the "Let's Go!" button is the "Get started" CTA.
        composeRule.onNodeWithText("Ready to Find Your Saint?").assertIsDisplayed()
        composeRule.onNodeWithText("Let's Go!").assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        // Invocation of onComplete is what drives the app-level transition to
        // the Saint List (AppRoot flips hasSeenWelcome and routes to MainScaffold).
        assertTrue("Tapping Let's Go! must invoke onComplete", completed)
    }

    @Test
    @Ignore(
        "Needs HiltTestRunner + DataStore seeding to assert persistence " +
            "across the whole-app boundary. Blocking on Aragorn to add a " +
            "HiltTestRunner (see decisions inbox)."
    )
    fun should_persist_hasSeenWelcome_true_after_completing_onboarding() {
        // TODO(after Hilt test runner lands): launch MainActivity via
        // createAndroidComposeRule<MainActivity>(), advance pager, tap
        // "Let's Go!", then read PreferencesRepository.hasSeenWelcome and
        // assert == true.
    }

    @Test
    @Ignore(
        "Needs HiltTestRunner + DataStore seeding (hasSeenWelcome=true pre-launch) " +
            "to assert the initial destination is Saint List. Blocking on Aragorn " +
            "to add a HiltTestRunner (see decisions inbox)."
    )
    fun should_skip_welcome_on_relaunch_when_hasSeenWelcome_is_true() {
        // TODO(after Hilt test runner lands): seed DataStore with
        // hasSeenWelcome=true before MainActivity launches, then assert the
        // Saint List is the initial composition and Welcome never appears.
    }
}
