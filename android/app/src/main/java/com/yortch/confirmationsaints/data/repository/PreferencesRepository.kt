package com.yortch.confirmationsaints.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.yortch.confirmationsaints.localization.AppLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thin DataStore wrapper for the two app-level preferences.
 *
 * Mirrors iOS `@AppStorage` keys 1:1 so persisted values round-trip across
 * platforms conceptually:
 *
 * | Key              | Type    | iOS equivalent                  |
 * |------------------|---------|---------------------------------|
 * | `appLanguage`    | String  | `@AppStorage("appLanguage")`    |
 * | `hasSeenWelcome` | Boolean | `@AppStorage("hasSeenWelcome")` |
 */
@Singleton
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    /**
     * Persisted language code, or `null` if the user hasn't picked one —
     * in which case callers should fall back to [AppLanguage.fromSystemLocale].
     */
    val languageCode: Flow<String?> = dataStore.data.map { it[KEY_LANGUAGE] }

    val hasSeenWelcome: Flow<Boolean> = dataStore.data.map { it[KEY_HAS_SEEN_WELCOME] ?: false }

    suspend fun setLanguage(lang: AppLanguage) {
        dataStore.edit { it[KEY_LANGUAGE] = lang.code }
    }

    suspend fun setHasSeenWelcome(value: Boolean) {
        dataStore.edit { it[KEY_HAS_SEEN_WELCOME] = value }
    }

    private companion object {
        val KEY_LANGUAGE = stringPreferencesKey("appLanguage")
        val KEY_HAS_SEEN_WELCOME = booleanPreferencesKey("hasSeenWelcome")
    }
}
