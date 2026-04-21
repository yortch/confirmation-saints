/**
 * Language handling: locale auto-detect (es → Spanish, otherwise English),
 * user override persisted via DataStore (`appLanguage`), and the mapping from
 * canonical English category ids to localized display strings. Mirrors
 * `ios/CatholicSaints/Services/LocalizationService.swift`.
 *
 * TODO: implement once the localization scheme is specified in
 * docs/android-architecture.md.
 */
package com.yortch.confirmationsaints.localization
