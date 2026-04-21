package com.yortch.confirmationsaints.ui

import org.junit.Ignore
import org.junit.Test

/**
 * STUB — Compose UI instrumentation test. Bodies to be filled in once
 * Aragorn lands the Welcome onboarding screen (Phase 5) and the NavHost.
 *
 * Contract under test (mirrors iOS committed behavior):
 *  - On first launch (hasSeenWelcome == false), the Welcome pager is shown.
 *  - Tapping "Get started" on the last page marks hasSeenWelcome = true and
 *    navigates to the Saint List screen.
 *  - On subsequent launches, the Saint List is the initial destination.
 */
@Ignore("Stub — awaiting WelcomeScreen + NavHost (Aragorn, Phase 5)")
class WelcomeScreenNavigationTest {

    @Test
    fun should_show_welcome_screen_when_hasSeenWelcome_is_false() {
        // TODO: Launch AppNavHost with a DataStore seeded hasSeenWelcome=false.
        // Assert nodes with welcome page titles/tags are displayed.
    }

    @Test
    fun should_navigate_to_saint_list_when_get_started_is_tapped() {
        // TODO: composeTestRule.onNodeWithText("Get started").performClick()
        // Assert saint-list semantics node ("SaintList" tag) is displayed and
        // Welcome is gone.
    }

    @Test
    fun should_persist_hasSeenWelcome_true_after_completing_onboarding() {
        // TODO: After tapping Get started, read DataStore and assert
        // hasSeenWelcome == true.
    }

    @Test
    fun should_skip_welcome_on_relaunch_when_hasSeenWelcome_is_true() {
        // TODO: Launch with hasSeenWelcome=true. Assert Saint List is the
        // initial composition; Welcome is never composed.
    }
}
