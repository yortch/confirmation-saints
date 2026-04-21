package com.yortch.confirmationsaints.util

import java.text.Normalizer

/**
 * Diacritic-insensitive string utilities — direct port of iOS
 * `String+Diacritics.swift`.
 *
 * **Non-negotiable** for this codebase: all search / filter / category matching
 * against saint text must use these helpers. Direct `contains()` / `equals()`
 * on names, patronage, tags, etc. is a bug — Spanish accented characters
 * ("Thérèse", "José", "María") would fail to match English-keyboard input.
 */
object Diacritics {
    private val combiningMarks = Regex("\\p{InCombiningDiacriticalMarks}+")

    fun stripDiacritics(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD).replace(combiningMarks, "")
}

fun String.stripDiacritics(): String = Diacritics.stripDiacritics(this)

fun String.containsIgnoringDiacritics(other: String): Boolean =
    this.stripDiacritics().contains(other.stripDiacritics(), ignoreCase = true)

fun String.equalsIgnoringDiacritics(other: String): Boolean =
    this.stripDiacritics().equals(other.stripDiacritics(), ignoreCase = true)

/**
 * Token-prefix match for deep search fields (tags / patronOf / affinities).
 * A query matches if it's the prefix of any whitespace/hyphen/slash/comma-
 * delimited word. Avoids the "ter" -> "writers"/"interpreters" false
 * positives that plain substring `contains` produces.
 */
fun String.matchesTokenPrefixIgnoringDiacritics(query: String): Boolean {
    val q = query.stripDiacritics().lowercase()
    if (q.isEmpty()) return false
    return this.stripDiacritics().lowercase()
        .split(' ', '-', '/', ',', '\t')
        .any { it.startsWith(q) }
}
