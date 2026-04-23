package com.yortch.confirmationsaints.data.model

import kotlinx.serialization.Serializable

/**
 * A Catholic saint loaded from per-language JSON (saints-en.json / saints-es.json).
 *
 * Mirrors the iOS `Saint` struct (see ios/CatholicSaints/Models/Saint.swift).
 *
 * Cross-platform contract (see docs/android-architecture.md §Appendix B):
 * - Canonical **English** ids drive matching: [patronOf], [tags], [affinities],
 *   [region], [lifeState], [ageCategory], [gender]. Values are identical in both
 *   language files.
 * - Display equivalents for localized rendering live in [displayPatronOf],
 *   [displayTags], [displayAffinities] (Spanish file only; may be null).
 * - [feastDay] is `"MM-DD"`. Formatting to "October 1" / "1 de octubre" is a UI concern.
 */
@Serializable
data class Saint(
    val id: String,
    val name: String,
    val feastDay: String,
    val birthDate: String? = null,
    val deathDate: String? = null,
    val canonizationDate: String? = null,
    val country: String? = null,
    val region: String? = null,
    val gender: String? = null,
    val lifeState: String? = null,
    val ageCategory: String? = null,
    val patronOf: List<String> = emptyList(),
    val displayPatronOf: List<String>? = null,
    val tags: List<String> = emptyList(),
    val displayTags: List<String>? = null,
    val affinities: List<String> = emptyList(),
    val displayAffinities: List<String>? = null,
    val quote: String? = null,
    val biography: String,
    val whyConfirmationSaint: String? = null,
    val image: SaintImage? = null,
    val sources: List<SourceEntry> = emptyList(),
) {
    val isYoung: Boolean get() = ageCategory == "young"
}

@Serializable
data class SaintImage(
    val filename: String,
    val attribution: String,
)

@Serializable
data class SourceEntry(
    val name: String,
    val url: String,
)
