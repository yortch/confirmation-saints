package com.yortch.confirmationsaints.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmationSection(
    val id: String,
    val title: String,
    val content: List<ConfirmationContent>,
    val sources: List<String> = emptyList(),
)

@Serializable
data class ConfirmationContent(
    val heading: String,
    val body: String,
)
