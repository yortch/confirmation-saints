package com.yortch.confirmationsaints.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yortch.confirmationsaints.data.repository.PreferencesRepository
import com.yortch.confirmationsaints.localization.AppLanguage
import com.yortch.confirmationsaints.localization.LocalizationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Backs the app root: language state + welcome-gating. Kept separate from
 * [SaintListViewModel] so the gating stream doesn't force loading the full
 * saints JSON before the welcome screen renders.
 */
@HiltViewModel
class RootViewModel @Inject constructor(
    localizationService: LocalizationService,
    private val preferences: PreferencesRepository,
) : ViewModel() {

    val language: StateFlow<AppLanguage> = localizationService.language
    val hasSeenWelcome: Flow<Boolean> = preferences.hasSeenWelcome

    fun markWelcomeSeen() {
        viewModelScope.launch { preferences.setHasSeenWelcome(true) }
    }
}
