package com.yortch.confirmationsaints.data.json

import com.yortch.confirmationsaints.data.model.CategoryGroup
import com.yortch.confirmationsaints.data.model.ConfirmationSection
import com.yortch.confirmationsaints.data.model.Saint
import kotlinx.serialization.Serializable

/** Wrapper for saints-{lang}.json (mirrors iOS SaintsFile). */
@Serializable
data class SaintsFile(
    val version: String,
    val language: String,
    val lastUpdated: String,
    val saints: List<Saint>,
)

/** Wrapper for categories-{lang}.json. */
@Serializable
data class CategoriesFile(
    val version: String,
    val language: String,
    val lastUpdated: String,
    val categories: List<CategoryGroup>,
)

/** Wrapper for confirmation-info-{lang}.json. */
@Serializable
data class ConfirmationInfoFile(
    val version: String,
    val language: String,
    val lastUpdated: String,
    val sections: List<ConfirmationSection>,
)
