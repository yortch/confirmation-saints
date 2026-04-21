package com.yortch.confirmationsaints.data.repository

import com.yortch.confirmationsaints.data.model.Saint
import com.yortch.confirmationsaints.util.SaintNameComparator
import com.yortch.confirmationsaints.util.containsIgnoringDiacritics
import com.yortch.confirmationsaints.util.equalsIgnoringDiacritics

/**
 * Port of iOS `SaintListViewModel.filteredSaints`. Pure function — takes the
 * raw list plus filter state, returns the filtered + sorted list.
 *
 * Search rules (match iOS):
 * - Name is always searched.
 * - Queries of length ≥ 3 also search patronOf / affinities / tags /
 *   displayAffinities / displayTags / country — diacritic-insensitive.
 */
data class SaintFilters(
    val searchText: String = "",
    val selectedRegion: String? = null,
    val selectedLifeState: String? = null,
    val selectedAgeCategory: String? = null,
    val selectedGender: String? = null,
    val selectedAffinity: String? = null,
) {
    val hasActiveFilters: Boolean
        get() = selectedRegion != null || selectedLifeState != null ||
            selectedAgeCategory != null || selectedGender != null ||
            selectedAffinity != null
}

object SaintFilterEngine {

    fun apply(saints: List<Saint>, filters: SaintFilters): List<Saint> {
        var result = saints

        val query = filters.searchText
        if (query.isNotEmpty()) {
            val deep = query.length >= 3
            result = result.filter { s ->
                if (s.name.containsIgnoringDiacritics(query)) return@filter true
                if (!deep) return@filter false
                s.patronOf.any { it.containsIgnoringDiacritics(query) } ||
                    s.affinities.any { it.containsIgnoringDiacritics(query) } ||
                    s.tags.any { it.containsIgnoringDiacritics(query) } ||
                    (s.displayAffinities?.any { it.containsIgnoringDiacritics(query) } == true) ||
                    (s.displayTags?.any { it.containsIgnoringDiacritics(query) } == true) ||
                    (s.country?.containsIgnoringDiacritics(query) == true)
            }
        }

        filters.selectedRegion?.let { region ->
            result = result.filter { it.region?.equalsIgnoringDiacritics(region) == true }
        }
        filters.selectedLifeState?.let { state ->
            result = result.filter { it.lifeState == state }
        }
        filters.selectedAgeCategory?.let { age ->
            result = result.filter { it.ageCategory == age }
        }
        filters.selectedGender?.let { gender ->
            result = result.filter { it.gender == gender }
        }
        filters.selectedAffinity?.let { affinity ->
            result = result.filter { s ->
                s.affinities.any { it.containsIgnoringDiacritics(affinity) }
            }
        }

        return result.sortedWith(SaintNameComparator)
    }
}
