package com.yortch.confirmationsaints.data.repository

import com.yortch.confirmationsaints.data.model.Saint
import com.yortch.confirmationsaints.util.SaintNameComparator
import com.yortch.confirmationsaints.util.containsIgnoringDiacritics
import com.yortch.confirmationsaints.util.equalsIgnoringDiacritics

/**
 * Port of iOS `SaintListViewModel.saints(forCategoryGroup:valueId:)` +
 * `matchesEra`.
 *
 * **Cross-platform contract:** matches against canonical **English** ids,
 * regardless of the current UI language. Both `saints-en.json` and
 * `saints-es.json` hold identical English values in `patronOf`, `tags`,
 * `affinities`, `region`, `lifeState`, `ageCategory`, `gender`. The UI
 * renders from `display*` arrays; this matcher never looks at those.
 */
object CategoryMatcher {

    fun saintsForCategory(
        groupId: String,
        valueId: String,
        saints: List<Saint>,
    ): List<Saint> {
        val normalizedId = valueId.replace("-", " ")
        return saints.filter { saint ->
            when (groupId) {
                "patronage" -> saint.patronOf.any { it.containsIgnoringDiacritics(normalizedId) }
                "interests" -> saint.affinities.any { it.containsIgnoringDiacritics(normalizedId) } ||
                    saint.tags.any { it.containsIgnoringDiacritics(normalizedId) }
                "age-category" -> saint.ageCategory == valueId
                "region" -> saint.region?.equalsIgnoringDiacritics(normalizedId) == true
                "life-state" -> saint.lifeState == valueId
                "era" -> matchesEra(saint, valueId)
                "gender" -> saint.gender == valueId
                else -> false
            }
        }.sortedWith(SaintNameComparator)
    }

    /**
     * Port of iOS `matchesEra`. Uses 4-digit prefix of `birthDate`
     * (`birthDate.take(4).toIntOrNull()` ↔ `Int(birthDate.prefix(4))`).
     */
    fun matchesEra(saint: Saint, era: String): Boolean {
        val year = saint.birthDate?.take(4)?.toIntOrNull() ?: return false
        return when (era) {
            "early-church" -> year < 500
            "medieval" -> year in 500..1499
            "early-modern" -> year in 1500..1799
            "modern" -> year in 1800..1949
            "contemporary" -> year >= 1950
            else -> false
        }
    }
}
