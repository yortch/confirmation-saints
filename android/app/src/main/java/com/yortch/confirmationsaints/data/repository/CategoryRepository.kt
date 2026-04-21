package com.yortch.confirmationsaints.data.repository

import android.content.Context
import android.util.Log
import com.yortch.confirmationsaints.data.json.CategoriesFile
import com.yortch.confirmationsaints.data.json.ConfirmationInfoFile
import com.yortch.confirmationsaints.data.model.CategoryGroup
import com.yortch.confirmationsaints.data.model.ConfirmationSection
import com.yortch.confirmationsaints.localization.AppLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val json: Json,
) {
    fun loadCategories(language: AppLanguage): List<CategoryGroup> =
        decode("categories-${language.code}.json") { raw ->
            json.decodeFromString<CategoriesFile>(raw).categories
        }

    fun loadConfirmationInfo(language: AppLanguage): List<ConfirmationSection> =
        decode("confirmation-info-${language.code}.json") { raw ->
            json.decodeFromString<ConfirmationInfoFile>(raw).sections
        }

    private inline fun <T> decode(asset: String, parser: (String) -> List<T>): List<T> = try {
        val raw = context.assets.open(asset).bufferedReader().use { it.readText() }
        parser(raw)
    } catch (e: Exception) {
        Log.w("CategoryRepository", "Failed to load $asset", e)
        emptyList()
    }
}
