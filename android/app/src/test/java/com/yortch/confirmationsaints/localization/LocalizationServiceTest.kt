package com.yortch.confirmationsaints.localization

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import com.yortch.confirmationsaints.data.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.Locale

/**
 * Tests LocalizationService language state management.
 *
 * Contract under test (mirrors iOS in-app language switch, committed decision):
 *  - LocalizationService exposes a StateFlow<AppLanguage>.
 *  - Selected language persists across process restart via DataStore.
 *  - AppStrings returns the correct translation for the active language.
 *  - Switching language updates the StateFlow synchronously for collectors.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class LocalizationServiceTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var testPrefsFile: File
    private lateinit var testScope: CoroutineScope
    private lateinit var preferencesRepo: PreferencesRepository
    private lateinit var service: LocalizationService

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        testScope = CoroutineScope(testDispatcher + Job())
        val testDir = File("build/test-datastore").apply { mkdirs() }
        testPrefsFile = File(testDir, "localization-${System.nanoTime()}.preferences_pb")
        testDataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
        ) {
            testPrefsFile
        }

        preferencesRepo = PreferencesRepository(testDataStore)
        service = LocalizationService(preferencesRepo, testScope)
    }

    @After
    fun tearDown() {
        testScope.cancel()
        if (::testPrefsFile.isInitialized) {
            testPrefsFile.delete()
        }
        Dispatchers.resetMain()
    }

    @Test
    fun should_default_to_device_locale_when_no_preference_stored() = runTest(testDispatcher) {
        // With an empty DataStore, initial value should match device locale
        val expected = if (Locale.getDefault().language == "es") AppLanguage.ES else AppLanguage.EN
        service.language.test {
            val initial = awaitItem()
            assertEquals("Initial language should match device locale", expected, initial)
        }
    }

    @Test
    fun should_update_stateflow_when_language_is_switched() = runTest(testDispatcher) {
        service.language.test {
            val initial = awaitItem() // consume initial value
            val target = if (initial == AppLanguage.ES) AppLanguage.EN else AppLanguage.ES
            service.setLanguage(target)
            testDispatcher.scheduler.advanceUntilIdle() // process the coroutine
            val updated = awaitItem()
            assertEquals("Language should update to selected language", target, updated)
        }
    }

    @Test
    fun should_persist_language_choice_to_datastore() = runTest(testDispatcher) {
        val target = if (AppLanguage.fromSystemLocale() == AppLanguage.ES) AppLanguage.EN else AppLanguage.ES
        service.setLanguage(target)
        testDispatcher.scheduler.advanceUntilIdle()

        val newService = LocalizationService(preferencesRepo, testScope)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Persisted language should override device locale", target, newService.language.value)
    }

    @Test
    fun should_return_english_string_when_language_is_en() {
        val result = AppStrings.localized("Settings", AppLanguage.EN)
        assertEquals("Settings", result)
    }

    @Test
    fun should_return_spanish_string_when_language_is_es() {
        val result = AppStrings.localized("Settings", AppLanguage.ES)
        assertEquals("Ajustes", result)
        assertNotEquals("Should not return English value", "Settings", result)
    }

    @Test
    fun should_fall_back_to_english_for_missing_translation_key() {
        // Use a key that doesn't exist in Spanish translations
        val missingKey = "This.Key.Does.Not.Exist"
        val result = AppStrings.localized(missingKey, AppLanguage.ES)
        // Should fall back to the key itself (English pattern)
        assertEquals("Should fall back to key when translation missing", missingKey, result)
    }
}
