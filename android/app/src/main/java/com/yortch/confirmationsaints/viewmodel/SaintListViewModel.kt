package com.yortch.confirmationsaints.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yortch.confirmationsaints.data.model.CategoryGroup
import com.yortch.confirmationsaints.data.model.ConfirmationSection
import com.yortch.confirmationsaints.data.model.Saint
import com.yortch.confirmationsaints.data.repository.CategoryMatcher
import com.yortch.confirmationsaints.data.repository.CategoryRepository
import com.yortch.confirmationsaints.data.repository.SaintFilterEngine
import com.yortch.confirmationsaints.data.repository.SaintFilters
import com.yortch.confirmationsaints.data.repository.SaintRepository
import com.yortch.confirmationsaints.localization.AppLanguage
import com.yortch.confirmationsaints.localization.LocalizationService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Top-level state for the saints / categories / about tabs.
 *
 * Mirrors the iOS single-`@Observable` pattern (`SaintListViewModel`): one
 * source of truth shared across tabs so language switches re-load everything
 * in lock-step.
 */
data class SaintListUiState(
    val language: AppLanguage = AppLanguage.EN,
    val saints: List<Saint> = emptyList(),
    val categories: List<CategoryGroup> = emptyList(),
    val confirmationSections: List<ConfirmationSection> = emptyList(),
    val filters: SaintFilters = SaintFilters(),
    val isLoading: Boolean = true,
) {
    val filteredSaints: List<Saint> get() = SaintFilterEngine.apply(saints, filters)
}

@HiltViewModel
class SaintListViewModel @Inject constructor(
    private val saintRepository: SaintRepository,
    private val categoryRepository: CategoryRepository,
    private val localizationService: LocalizationService,
) : ViewModel() {

    private val _state = MutableStateFlow(SaintListUiState(language = localizationService.language.value))
    val state: StateFlow<SaintListUiState> = _state.asStateFlow()

    init {
        // Re-load content whenever the language flips. This is the on-ramp
        // for live-switch without Activity recreate (mirrors iOS onChange).
        viewModelScope.launch {
            localizationService.language.collect { lang -> load(lang) }
        }
    }

    private suspend fun load(language: AppLanguage) {
        _state.update { it.copy(isLoading = true, language = language) }
        val (saints, categories, sections) = withContext(Dispatchers.IO) {
            Triple(
                saintRepository.loadSaints(language),
                categoryRepository.loadCategories(language),
                categoryRepository.loadConfirmationInfo(language),
            )
        }
        _state.update {
            it.copy(
                language = language,
                saints = saints,
                categories = categories,
                confirmationSections = sections,
                isLoading = false,
            )
        }
    }

    // ---- Filter mutations (mirror the iOS `@Bindable` setters) ----

    fun setSearch(query: String) = updateFilters { it.copy(searchText = query) }
    fun setRegion(region: String?) = updateFilters { it.copy(selectedRegion = region) }
    fun setLifeState(state: String?) = updateFilters { it.copy(selectedLifeState = state) }
    fun setAgeCategory(age: String?) = updateFilters { it.copy(selectedAgeCategory = age) }
    fun setGender(gender: String?) = updateFilters { it.copy(selectedGender = gender) }
    fun setAffinity(affinity: String?) = updateFilters { it.copy(selectedAffinity = affinity) }
    fun setEra(era: String?) = updateFilters { it.copy(selectedEra = era) }

    fun clearFilters() {
        _state.update { it.copy(filters = SaintFilters()) }
    }

    /** Lookup single saint by id — reactive to current saint list. */
    fun findSaint(id: String): Saint? = _state.value.saints.firstOrNull { it.id == id }

    /** For category browse: matching saints given a group + value id. */
    fun saintsForCategory(groupId: String, valueId: String): List<Saint> =
        CategoryMatcher.saintsForCategory(groupId, valueId, _state.value.saints)

    private inline fun updateFilters(transform: (SaintFilters) -> SaintFilters) {
        _state.update { it.copy(filters = transform(it.filters)) }
    }
}
