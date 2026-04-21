package com.yortch.confirmationsaints.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe Navigation Compose routes. Each `@Serializable` class or object
 * becomes a route — Navigation Compose 2.8 handles (de)serialization
 * automatically.
 */
sealed interface Screen {

    // Top-level tabs
    @Serializable data object Welcome : Screen
    @Serializable data object About : Screen
    @Serializable data object Explore : Screen
    @Serializable data object Saints : Screen
    @Serializable data object Settings : Screen

    // Detail destinations
    @Serializable data class SaintDetail(val saintId: String) : Screen
    @Serializable data class CategorySaints(
        val groupId: String,
        val valueId: String,
        val title: String,
    ) : Screen
}
