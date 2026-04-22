package com.yortch.confirmationsaints.localization

import java.util.Locale

/**
 * Supported UI languages. Mirrors iOS `appLanguage` values ("en" / "es").
 * Persisted as a string code in DataStore (see [PreferencesRepository]).
 */
enum class AppLanguage(val code: String, val displayName: String) {
    EN("en", "English"),
    ES("es", "Español");

    companion object {
        fun fromCode(code: String?): AppLanguage? =
            entries.firstOrNull { it.code == code }

        /** Spanish system locale → ES, anything else → EN (matches iOS). */
        fun fromSystemLocale(): AppLanguage =
            if (Locale.getDefault().language == "es") ES else EN
    }
}
