package com.yortch.confirmationsaints.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CategoryGroup(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val values: List<CategoryValue>,
)

@Serializable
data class CategoryValue(
    val id: String,
    val label: String,
)
