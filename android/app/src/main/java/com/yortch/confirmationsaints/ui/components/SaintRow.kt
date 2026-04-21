package com.yortch.confirmationsaints.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.yortch.confirmationsaints.data.model.Saint
import com.yortch.confirmationsaints.localization.AppLanguage
import com.yortch.confirmationsaints.localization.AppStrings
import com.yortch.confirmationsaints.localization.LocalAppLanguage
import com.yortch.confirmationsaints.util.DateFormatting

/** Saint list row. Port of iOS `SaintRowView`. */
@Composable
fun SaintRow(saint: Saint, modifier: Modifier = Modifier) {
    val language = LocalAppLanguage.current
    val feastDay = DateFormatting.formatFeastDay(saint.feastDay, language)
    val country = saint.country

    val accessibilityLabel = buildString {
        append(saint.name)
        append(", ").append(feastDay)
        if (country != null) append(", ").append(country)
        if (saint.isYoung) append(", ").append(AppStrings.localized("Young Saint", language))
    }

    Row(
        modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clearAndSetSemantics { contentDescription = accessibilityLabel },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SaintImage(saint = saint, size = 50.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.fillMaxWidth().weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(saint.name, style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DateRange,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    feastDay,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (country != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Public,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        country,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        if (saint.isYoung) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFE67E22),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// Small helper to use "current" language without boilerplate — not exported.
@Composable
private fun withLanguage(lang: AppLanguage, content: @Composable () -> Unit) =
    CompositionLocalProvider(LocalAppLanguage provides lang, content = content)
