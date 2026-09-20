package com.yortch.confirmationsaints.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
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
 * Instrumentation coverage for the Rate & Review Settings action, per
 * Gandalf's Rate & Review Settings contract.
 *
 * Contract under test:
 *  - The "Rate & Review" section renders between "App Info" and
 *    "Onboarding" (ordering, not just presence — visible without scrolling).
 *  - The "Rate Confirmation Saints" row exposes a destination-announcing
 *    content description mentioning "Google Play" and behaves as a
 *    clickable button (not a bare link-out row).
 *
 * This intentionally does not attempt to assert what happens after the row
 * is tapped: `ReviewManagerFactory`/the market:// and https Play Store
 * fallback intents are not reliably observable from a bare instrumentation
 * test (see [com.yortch.confirmationsaints.viewmodel.SettingsViewModelReviewTest]
 * for the fallback URI-building unit coverage instead).
 */
@HiltAndroidTest
class RateReviewSettingsTest {

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
    fun should_render_rate_and_review_section_between_app_info_and_onboarding() {
        ActivityScenario.launch(MainActivity::class.java).use {
            awaitMainScaffold()
            navigateToSettings()

            composeRule.onNodeWithText("App Info").assertIsDisplayed()
            composeRule.onNodeWithText("Rate & Review").assertIsDisplayed()
            composeRule.onNodeWithText("Onboarding").assertIsDisplayed()

            // Ordering assertion (not just presence): App Info must render
            // above Rate & Review, which must render above Onboarding.
            val appInfoTop = composeRule.onNodeWithText("App Info")
                .fetchSemanticsNode().boundsInRoot.top
            val rateReviewTop = composeRule.onNodeWithText("Rate & Review")
                .fetchSemanticsNode().boundsInRoot.top
            val onboardingTop = composeRule.onNodeWithText("Onboarding")
                .fetchSemanticsNode().boundsInRoot.top

            assert(appInfoTop < rateReviewTop) {
                "Expected 'App Info' ($appInfoTop) to render above 'Rate & Review' ($rateReviewTop)"
            }
            assert(rateReviewTop < onboardingTop) {
                "Expected 'Rate & Review' ($rateReviewTop) to render above 'Onboarding' ($onboardingTop)"
            }
        }
    }

    @Test
    fun rate_row_content_description_mentions_google_play_and_is_a_button() {
        ActivityScenario.launch(MainActivity::class.java).use {
            awaitMainScaffold()
            navigateToSettings()

            composeRule.onNodeWithText("Rate Confirmation Saints").assertIsDisplayed()

            composeRule.onNode(hasContentDescription("Google Play", substring = true))
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    private fun awaitMainScaffold() {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Name, interest, country...")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun navigateToSettings() {
        composeRule.onNodeWithText("Settings").performClick()
        composeRule.waitForIdle()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Language").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
