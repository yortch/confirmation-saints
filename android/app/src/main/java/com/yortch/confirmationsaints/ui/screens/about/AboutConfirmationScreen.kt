package com.yortch.confirmationsaints.ui.screens.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yortch.confirmationsaints.data.model.ConfirmationSection
import com.yortch.confirmationsaints.localization.AppStrings
import com.yortch.confirmationsaints.localization.LocalAppLanguage
import com.yortch.confirmationsaints.viewmodel.SaintListViewModel

/** About Confirmation screen — markdown-ish sections. Port of iOS AboutConfirmationView. */
@Composable
fun AboutConfirmationScreen(viewModel: SaintListViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = LocalAppLanguage.current

    if (state.confirmationSections.isEmpty()) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(AppStrings.localized("Content Loading...", language))
        }
        return
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        items(state.confirmationSections, key = { it.id }) { section ->
            SectionView(section)
            HorizontalDivider()
        }
    }
}

@Composable
private fun SectionView(section: ConfirmationSection) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                iconForSection(section.id),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                section.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        section.content.forEach { block ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(block.heading, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                // Body text: render paragraphs separated by blank lines.
                block.body.split("\n\n").forEach { paragraph ->
                    Text(
                        parseInlineMarkdown(paragraph),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
        if (section.sources.isNotEmpty()) {
            Text(
                section.sources.joinToString(", "),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun iconForSection(id: String): ImageVector = when (id) {
    "what-is-confirmation" -> Icons.Default.LocalFireDepartment
    "choosing-your-saint" -> Icons.Default.PersonPin
    "tips-for-finding-your-match" -> Icons.Default.Lightbulb
    else -> Icons.Default.Book
}

/**
 * Render a subset of inline markdown: `**bold**`. Keeps it trivial — no
 * emphasis, links, or code spans needed by current About content.
 */
private fun parseInlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    val regex = Regex("""\*\*(.+?)\*\*""")
    var cursor = 0
    for (match in regex.findAll(text)) {
        if (match.range.first > cursor) {
            append(text.substring(cursor, match.range.first))
        }
        withStyleBold { append(match.groupValues[1]) }
        cursor = match.range.last + 1
    }
    if (cursor < text.length) append(text.substring(cursor))
}

private inline fun androidx.compose.ui.text.AnnotatedString.Builder.withStyleBold(block: () -> Unit) {
    val idx = pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
    try { block() } finally { pop(idx) }
}
