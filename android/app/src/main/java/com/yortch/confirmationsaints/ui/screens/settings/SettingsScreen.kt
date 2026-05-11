package com.yortch.confirmationsaints.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.ContactMail
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yortch.confirmationsaints.BuildConfig
import com.yortch.confirmationsaints.localization.AppLanguage
import com.yortch.confirmationsaints.localization.AppStrings
import com.yortch.confirmationsaints.localization.LocalAppLanguage
import com.yortch.confirmationsaints.viewmodel.SaintListViewModel
import com.yortch.confirmationsaints.viewmodel.SettingsViewModel

/**
 * App settings. Port of iOS `SettingsView`:
 *
 * - Language (EN/ES radio) persisted to DataStore — switches UI/content live.
 * - Replay welcome (sets `hasSeenWelcome = false`; NavHost routes back to Welcome).
 * - App metadata (version from [BuildConfig.VERSION_NAME], not a hardcoded
 *   constant — this is what the iOS 0.1.0-hardcode bug taught us).
 * - Content source attributions + legal links.
 */
@Composable
fun SettingsScreen(
    onReplayWelcome: () -> Unit,
    listViewModel: SaintListViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val language = LocalAppLanguage.current
    val listState by listViewModel.state.collectAsStateWithLifecycle()
    val uri = LocalUriHandler.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item {
            Section(icon = Icons.Default.Language, title = AppStrings.localized("Language", language)) {
                AppLanguage.entries.forEach { lang ->
                    LanguageRow(
                        lang = lang,
                        selected = language == lang,
                        onSelect = { settingsViewModel.setLanguage(lang) },
                    )
                }
                Text(
                    AppStrings.localized(
                        "Changing the language updates all saint content and app text.", language,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }

        item {
            Section(icon = Icons.Default.Info, title = AppStrings.localized("App Info", language)) {
                InfoRow(
                    label = AppStrings.localized("Version", language),
                    // Use BuildConfig.VERSION_NAME — NOT a hardcoded string.
                    // iOS previously hardcoded "0.1.0" and drifted; don't repeat that.
                    value = BuildConfig.VERSION_NAME,
                )
                InfoRow(
                    label = AppStrings.localized("Saints Included", language),
                    value = listState.saints.size.toString(),
                )
                InfoRow(
                    label = AppStrings.localized("Languages", language),
                    value = AppStrings.localized("English, Spanish", language),
                )
            }
        }

        item {
            Section(icon = Icons.Default.EmojiEmotions, title = AppStrings.localized("Onboarding", language)) {
                ActionRow(
                    icon = Icons.Default.Refresh,
                    label = AppStrings.localized("Show Welcome Screen", language),
                    onClick = {
                        settingsViewModel.resetOnboarding()
                        onReplayWelcome()
                    },
                )
                Text(
                    AppStrings.localized("Replay the welcome screen to revisit how the app works.", language),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        item {
            Section(icon = Icons.Default.Shield, title = AppStrings.localized("Support & Legal", language)) {
                LinkRow(
                    icon = Icons.Default.Shield,
                    label = AppStrings.localized("Privacy Policy", language),
                    onClick = { uri.openUri("https://yortch.github.io/confirmation-saints/privacy-policy.html") },
                )
                LinkRow(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    label = AppStrings.localized("Support", language),
                    onClick = { uri.openUri("https://yortch.github.io/confirmation-saints/support.html") },
                )
                LinkRow(
                    icon = Icons.Default.ContactMail,
                    label = AppStrings.localized("Contact Us", language),
                    onClick = { uri.openUri("https://yortch.github.io/confirmation-saints/support.html") },
                )
            }
        }

        item {
            Section(icon = Icons.Default.Book, title = AppStrings.localized("Content Sources", language)) {
                val sources = listOf(
                    ContentSource("Loyola Press", "https://www.loyolapress.com/", "Biographical information"),
                    ContentSource("Focus", "https://www.focus.org/", "Biographical information"),
                    ContentSource("Lifeteen", "https://lifeteen.com/", "Biographical information"),
                    ContentSource("Ascension Press", "https://ascensionpress.com/", "Biographical information"),
                    ContentSource("Hallow", "https://hallow.com/", "Biographical information"),
                    ContentSource("Catholic Encyclopedia", "https://www.newadvent.org/cathen/", "Biographical information"),
                    ContentSource("Franciscan Media", "https://www.franciscanmedia.org/", "Biographical information"),
                    ContentSource("Wikipedia", "https://en.wikipedia.org/", "Biographical information"),
                    ContentSource("Wikimedia Commons", "https://commons.wikimedia.org/", "Public domain images"),
                )
                sources.forEach { source ->
                    SourceRow(
                        name = source.name,
                        description = AppStrings.localized(source.description, language),
                        url = source.url,
                        onClick = { uri.openUri(source.url) },
                    )
                }
                Text(
                    AppStrings.localized(
                        "Saint information is sourced from trusted Catholic resources. Each saint entry includes specific attribution.",
                        language,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun Section(icon: ImageVector, title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.size(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider()
        content()
    }
}

@Composable
private fun LanguageRow(lang: AppLanguage, selected: Boolean, onSelect: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.size(8.dp))
        Text(lang.displayName, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ActionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun LinkRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.size(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SourceRow(name: String, description: String, url: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyMedium)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.OpenInNew,
            contentDescription = "Open $name in browser",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private data class ContentSource(
    val name: String,
    val url: String,
    val description: String,
)
