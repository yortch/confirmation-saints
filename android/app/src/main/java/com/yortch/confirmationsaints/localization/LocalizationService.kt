package com.yortch.confirmationsaints.localization

import com.yortch.confirmationsaints.data.repository.PreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-wide language state. Resolves the persisted DataStore value, falling
 * back to the system locale (`es` → ES, otherwise EN) when nothing is
 * stored yet — matches iOS first-launch behaviour.
 *
 * Exposed as a [StateFlow] so any Compose consumer can
 * `collectAsStateWithLifecycle()` and recompose on switch without an
 * Activity restart.
 */
@Singleton
class LocalizationService @Inject constructor(
    private val preferences: PreferencesRepository,
    private val appScope: CoroutineScope,
) {
    val language: StateFlow<AppLanguage> = preferences.languageCode
        .map { code -> AppLanguage.fromCode(code) ?: AppLanguage.fromSystemLocale() }
        .stateIn(
            scope = appScope,
            started = SharingStarted.Eagerly,
            initialValue = AppLanguage.fromSystemLocale(),
        )

    fun setLanguage(lang: AppLanguage) {
        appScope.launch { preferences.setLanguage(lang) }
    }
}
