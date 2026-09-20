package com.yortch.confirmationsaints.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.yortch.confirmationsaints.BuildConfig
import com.yortch.confirmationsaints.data.repository.PreferencesRepository
import com.yortch.confirmationsaints.localization.LocalizationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Unit coverage for the Rate & Review Play Store fallback helpers on
 * [SettingsViewModel], per Gandalf's Rate & Review Settings contract.
 *
 * Contract under test:
 *  - `playStoreMarketUri` / `playStoreHttpsUri` are built from the supplied
 *    application id (defaulting to [BuildConfig.APPLICATION_ID]) rather than
 *    a hardcoded package string.
 *  - `shouldAttemptInAppReview` returns `false` for debug builds — the
 *    in-app Play Review flow is skipped entirely in favor of the fallback
 *    when `BuildConfig.DEBUG` is true — and `true` otherwise.
 *
 * These are plain, DI-free functions on the ViewModel (no [android.app.Activity]
 * reference), so the actual review-flow/intent launching lives in the
 * Composable; this test only exercises the pure string-building/gating logic.
 */
class SettingsViewModelReviewTest {

    private lateinit var testPrefsFile: File
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var testScope: CoroutineScope
    private lateinit var viewModel: SettingsViewModel

    @Before
    fun setUp() {
        testPrefsFile = File.createTempFile("settings_review_test", ".preferences_pb")
        testDataStore = PreferenceDataStoreFactory.create { testPrefsFile }
        testScope = CoroutineScope(Dispatchers.Unconfined + Job())
        val preferences = PreferencesRepository(testDataStore)
        val localizationService = LocalizationService(preferences, testScope)
        viewModel = SettingsViewModel(localizationService, preferences)
    }

    @After
    fun tearDown() {
        testScope.cancel()
        testPrefsFile.delete()
    }

    @Test
    fun `playStoreMarketUri builds market deep link from supplied application id`() {
        assertEquals(
            "market://details?id=com.example.testapp",
            viewModel.playStoreMarketUri(applicationId = "com.example.testapp"),
        )
    }

    @Test
    fun `playStoreHttpsUri builds https Play Store listing from supplied application id`() {
        assertEquals(
            "https://play.google.com/store/apps/details?id=com.example.testapp",
            viewModel.playStoreHttpsUri(applicationId = "com.example.testapp"),
        )
    }

    @Test
    fun `playStoreMarketUri and playStoreHttpsUri default to BuildConfig APPLICATION_ID`() {
        // Not a hardcoded package string: must track BuildConfig.APPLICATION_ID
        // so the fallback never drifts if the application id ever changes.
        assertTrue(viewModel.playStoreMarketUri().endsWith("id=${BuildConfig.APPLICATION_ID}"))
        assertTrue(viewModel.playStoreHttpsUri().endsWith("id=${BuildConfig.APPLICATION_ID}"))
    }

    @Test
    fun `shouldAttemptInAppReview is false for debug builds`() {
        assertFalse(viewModel.shouldAttemptInAppReview(isDebugBuild = true))
    }

    @Test
    fun `shouldAttemptInAppReview is true for non-debug builds`() {
        assertTrue(viewModel.shouldAttemptInAppReview(isDebugBuild = false))
    }

    @Test
    fun `shouldAttemptInAppReview defaults to BuildConfig DEBUG`() {
        // Unit tests run against the debug BuildConfig, so ReviewManager must
        // be skipped by default and the fallback path taken instead.
        assertEquals(!BuildConfig.DEBUG, viewModel.shouldAttemptInAppReview())
    }
}
