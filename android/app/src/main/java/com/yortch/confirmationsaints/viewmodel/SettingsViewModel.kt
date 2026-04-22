package com.yortch.confirmationsaints.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
}
