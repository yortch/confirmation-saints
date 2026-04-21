package com.yortch.confirmationsaints.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yortch.confirmationsaints.localization.LocalAppLanguage
import com.yortch.confirmationsaints.localization.LocalizationService
import com.yortch.confirmationsaints.ui.screens.onboarding.WelcomeScreen
import com.yortch.confirmationsaints.viewmodel.RootViewModel

/**
 * App root composable. Provides [LocalAppLanguage] to the whole tree and
 * gates the Welcome screen vs. MainScaffold based on `hasSeenWelcome`.
 *
 * Locale recomposes on every language change because [LocalAppLanguage] is
 * a CompositionLocal keyed off a StateFlow — mirrors iOS
 * `@Environment(\.appLanguage)`.
 */
@Composable
fun AppRoot(rootViewModel: RootViewModel = hiltViewModel()) {
    val language by rootViewModel.language.collectAsStateWithLifecycle()
    val hasSeenWelcome by rootViewModel.hasSeenWelcome.collectAsStateWithLifecycle(initialValue = null)

    // Wait until we know — avoid flashing the welcome screen on top of main
    // on a relaunch.
    val seen = hasSeenWelcome ?: return

    CompositionLocalProvider(LocalAppLanguage provides language) {
        if (!seen) {
            WelcomeScreen(onComplete = { rootViewModel.markWelcomeSeen() })
        } else {
            MainScaffold(onReplayWelcome = { /* state flips via resetOnboarding in SettingsVM */ })
        }
    }
}
