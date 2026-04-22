package com.yortch.confirmationsaints.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe Navigation Compose routes. Each `@Serializable` class or object
 * becomes a route — Navigation Compose 2.8 handles (de)serialization
 * automatically.
 */
sealed interface Screen {

    // Welcome (pre-main) destination
    @Serializable data object Welcome : Screen

    // Top-level tab graphs — each is a nested navigation graph that owns
    // its own back stack. `navigateTopLevel` targets these.
    @Serializable data object About : Screen
    @Serializable data object Explore : Screen
    @Serializable data object Saints : Screen
    @Serializable data object Settings : Screen

    // Start destinations inside each tab graph — the actual leaf screens.
    @Serializable data object AboutHome : Screen
    @Serializable data object ExploreHome : Screen
    @Serializable data object SaintsHome : Screen
    @Serializable data object SettingsHome : Screen

    // Detail destinations — pushed onto the CURRENT tab's back stack.
    @Serializable data class SaintDetail(val saintId: String) : Screen
    @Serializable data class CategorySaints(
        val groupId: String,
        val valueId: String,
        val title: String,
    ) : Screen
}
