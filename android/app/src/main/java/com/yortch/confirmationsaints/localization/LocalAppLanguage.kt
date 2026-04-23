package com.yortch.confirmationsaints.localization

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Current in-app language, exposed to the whole Compose tree.
 *
 * Consumers read this to re-render text on language switch without an
 * Activity restart — mirrors iOS's `@Environment(\.appLanguage)`.
 *
 * Provided once at the root of the tree in MainActivity, bound to
 * [LocalizationService.language].
 */
val LocalAppLanguage = staticCompositionLocalOf { AppLanguage.EN }
