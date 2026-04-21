package com.yortch.confirmationsaints.data.repository

import android.content.Context
import android.util.Log
import com.yortch.confirmationsaints.data.json.SaintsFile
import com.yortch.confirmationsaints.data.model.Saint
import com.yortch.confirmationsaints.localization.AppLanguage
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads saints from the language-specific JSON bundled under `assets/`.
 *
 * The `syncSharedContent` Gradle task copies
 * `SharedContent/saints/saints-{en,es}.json` → `app/src/main/assets/` at
 * build time; we never embed JSON in source.
 *
 * Failures are logged and return an empty list — matches the iOS
 * `SaintDataService` behaviour (see ios/.../SaintDataService.swift).
 */
@Singleton
class SaintRepository @Inject constructor(
    private val context: Context,
    private val json: Json,
) {
    fun loadSaints(language: AppLanguage): List<Saint> {
        val asset = "saints-${language.code}.json"
        return try {
            val raw = context.assets.open(asset).bufferedReader().use { it.readText() }
            json.decodeFromString<SaintsFile>(raw).saints
        } catch (e: IOException) {
            Log.w(TAG, "Missing asset: $asset", e)
            emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to decode $asset", e)
            emptyList()
        }
    }

    private companion object {
        const val TAG = "SaintRepository"
    }
}
