package com.yortch.confirmationsaints.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yortch.confirmationsaints.BuildConfig
import com.yortch.confirmationsaints.data.repository.PreferencesRepository
import com.yortch.confirmationsaints.localization.AppLanguage
import com.yortch.confirmationsaints.localization.LocalizationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Drives [SettingsScreen]. */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val localizationService: LocalizationService,
    private val preferences: PreferencesRepository,
) : ViewModel() {

    fun setLanguage(lang: AppLanguage) {
        localizationService.setLanguage(lang)
    }

    fun resetOnboarding() {
        viewModelScope.launch { preferences.setHasSeenWelcome(false) }
    }

    /**
     * Play Store fallback URIs for the "Rate & Review" action. Kept as plain,
     * testable string-building functions — no [android.app.Activity] reference
     * belongs on a ViewModel, so the actual review-flow/intent launching happens
     * in the Composable, which calls these to build the URIs it opens.
     *
     * Derived from [BuildConfig.APPLICATION_ID] (not a hardcoded package string)
     * so the fallback never drifts if the application ID changes.
     */
    fun playStoreMarketUri(applicationId: String = BuildConfig.APPLICATION_ID): String =
        "market://details?id=$applicationId"

    fun playStoreHttpsUri(applicationId: String = BuildConfig.APPLICATION_ID): String =
        "https://play.google.com/store/apps/details?id=$applicationId"

    /**
     * Whether the in-app [com.google.android.play.core.review.ReviewManager] flow
     * should even be attempted. Debug builds skip it entirely — In-App Review
     * requires a Play-signed install channel (internal test track / Play Store)
     * and silently never shows on bare debug/emulator builds, so there is no
     * reliable "was it shown" signal to gate on there.
     */
    fun shouldAttemptInAppReview(isDebugBuild: Boolean = BuildConfig.DEBUG): Boolean = !isDebugBuild
}
