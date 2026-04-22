package com.yortch.confirmationsaints.util

import com.yortch.confirmationsaints.data.model.Saint

/**
 * Sort order that strips title prefixes (St., Bl., Our Lady of, Santa, San,
 * Santo, Beato, Beata) before comparing — matches iOS
 * `SaintListViewModel.sortableName`. Case-insensitive.
 */
object SaintNameComparator : Comparator<Saint> {

    private val prefixes = listOf(
        "St. ", "Bl. ", "Our Lady of ",
        "Santa ", "San ", "Santo ", "Beato ", "Beata ",
    )

    fun sortableName(name: String): String {
        for (prefix in prefixes) {
            if (name.startsWith(prefix)) return name.removePrefix(prefix)
        }
        return name
    }

    override fun compare(a: Saint, b: Saint): Int =
        sortableName(a.name).compareTo(sortableName(b.name), ignoreCase = true)
}
