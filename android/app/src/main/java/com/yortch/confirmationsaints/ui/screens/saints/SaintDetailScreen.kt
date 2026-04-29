package com.yortch.confirmationsaints.ui.screens.saints

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.yortch.confirmationsaints.data.model.Saint
import com.yortch.confirmationsaints.localization.AppStrings
import com.yortch.confirmationsaints.localization.LocalAppLanguage
import com.yortch.confirmationsaints.ui.components.SaintImage
import com.yortch.confirmationsaints.util.DateFormatting
import com.yortch.confirmationsaints.viewmodel.SaintListViewModel

/**
 * Saint profile. **Takes an id, not a Saint value** — looks the saint up
 * reactively from the shared view model so language changes swap content
 * without a new navigation round-trip (matches iOS `SaintDetailView`
 * "pass ID + Observable" rule).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SaintDetailScreen(
    saintId: String,
    viewModel: SaintListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = LocalAppLanguage.current
    val saint = state.saints.firstOrNull { it.id == saintId }

    if (saint == null) {
        EmptyMessage(title = AppStrings.localized("Content Loading...", language))
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        HeaderSection(saint)
        QuoteSection(saint)
        WhySection(saint)
        BiographySection(saint)
        DetailsSection(saint)
        PatronSection(saint)
        TagsSection(saint)
        SourcesSection(saint)
    }
}

@Composable
private fun HeaderSection(saint: Saint) {
    val language = LocalAppLanguage.current
    val hasLocalImage = !saint.image?.filename.isNullOrBlank()
    var showLargeImage by rememberSaveable(saint.id) { mutableStateOf(false) }
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (hasLocalImage) {
            Box(
                modifier = Modifier.clickable(
                    onClickLabel = AppStrings.localized("View larger image", language),
                    role = Role.Button,
                ) { showLargeImage = true },
                contentAlignment = Alignment.BottomEnd,
            ) {
                SaintImage(saint = saint, size = 128.dp)
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp),
                    tonalElevation = 2.dp,
                ) {
                    Icon(
                        Icons.Default.ZoomIn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(7.dp),
                    )
                }
            }
            Text(
                AppStrings.localized("Tap image to enlarge", language),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
        } else {
            SaintImage(saint = saint, size = 128.dp)
        }

        if (showLargeImage) {
            LargeSaintImageDialog(saint = saint, onDismiss = { showLargeImage = false })
        }

        Text(saint.name, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)

        saint.image?.attribution?.let {
            Text(
                it,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            DateFormatting.formatFeastDay(saint.feastDay, language),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            saint.country?.let { IconText(Icons.Default.Public, it) }
            saint.lifeState?.let { state ->
                val localized = AppStrings.localized(state, language).replaceFirstChar { it.uppercaseChar() }
                IconText(Icons.Default.Person, localized)
            }
        }

        if (saint.isYoung) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFE67E22).copy(alpha = 0.12f),
            ) {
                Row(
                    Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFE67E22),
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        AppStrings.localized("Young Saint", language),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFE67E22),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun LargeSaintImageDialog(saint: Saint, onDismiss: () -> Unit) {
    val filename = saint.image?.filename ?: return
    val language = LocalAppLanguage.current
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        AppStrings.localized("Saint image", language),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = AppStrings.localized("Close image", language),
                        )
                    }
                }

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data("file:///android_asset/images/$filename")
                        .crossfade(true)
                        .build(),
                    contentDescription = saint.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )

                Text(
                    saint.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                saint.image.attribution.takeIf { it.isNotBlank() }?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun QuoteSection(saint: Saint) {
    val quote = saint.quote ?: return
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.06f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                Icons.Default.FormatQuote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f),
            )
            Text(
                quote,
                style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                textAlign = TextAlign.Center,
            )
            Text(
                "— ${saint.name}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WhySection(saint: Saint) {
    val language = LocalAppLanguage.current
    val why = saint.whyConfirmationSaint ?: return
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.error.copy(alpha = 0.04f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            LabelHeader(
                Icons.Default.Favorite,
                AppStrings.localized("Why Choose This Saint?", language),
                tint = MaterialTheme.colorScheme.error,
            )
            Text(why, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun BiographySection(saint: Saint) {
    val language = LocalAppLanguage.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LabelHeader(Icons.Default.Book, AppStrings.localized("Biography", language))
        Text(saint.biography, style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsSection(saint: Saint) {
    val language = LocalAppLanguage.current
    val items = buildList {
        saint.birthDate?.let { add(Triple(AppStrings.localized("Born", language), DateFormatting.formatIsoDate(it, language), Icons.Default.DateRange)) }
        saint.deathDate?.let { add(Triple(AppStrings.localized("Died", language), DateFormatting.formatIsoDate(it, language), Icons.Default.DateRange)) }
        saint.canonizationDate?.let { add(Triple(AppStrings.localized("Canonized", language), DateFormatting.formatIsoDate(it, language), Icons.Default.Star)) }
        saint.region?.let { add(Triple(AppStrings.localized("Region", language), AppStrings.localized(it, language), Icons.Default.Public)) }
    }
    if (items.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LabelHeader(Icons.Default.Info, AppStrings.localized("Details", language))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items.forEach { (label, value, icon) ->
                DetailCard(icon = icon, label = label, value = value)
            }
        }
    }
}

@Composable
private fun DetailCard(icon: ImageVector, label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.width(168.dp),
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PatronSection(saint: Saint) {
    val language = LocalAppLanguage.current
    // Prefer displayPatronOf for localized rendering; fall back to canonical.
    val values = saint.displayPatronOf ?: saint.patronOf
    if (values.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LabelHeader(Icons.Default.Shield, AppStrings.localized("Patron Of", language))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            values.forEach { patron ->
                ChipLabel(
                    text = patron.replaceFirstChar { it.uppercaseChar() },
                    background = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSection(saint: Saint) {
    val language = LocalAppLanguage.current
    val affinities = saint.displayAffinities ?: saint.affinities
    val tags = saint.displayTags ?: saint.tags
    val all = affinities + tags
    if (all.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LabelHeader(Icons.Default.AutoAwesome, AppStrings.localized("Interests & Tags", language))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            all.forEach { tag ->
                ChipLabel(
                    text = tag.replaceFirstChar { it.uppercaseChar() },
                    background = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                )
            }
        }
    }
}

@Composable
private fun ChipLabel(text: String, background: Color) {
    Surface(shape = RoundedCornerShape(50), color = background) {
        Text(
            text,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun SourcesSection(saint: Saint) {
    val language = LocalAppLanguage.current
    if (saint.sources.isEmpty()) return
    val uriHandler = LocalUriHandler.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LabelHeader(Icons.Default.Book, AppStrings.localized("Sources", language))
        saint.sources.forEach { source ->
            val rowModifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { uriHandler.openUri(source.url) }
                .padding(12.dp)
            Row(rowModifier, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    source.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    Icons.Default.OpenInNew,
                    contentDescription = AppStrings.localized("Open link", language),
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun IconText(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LabelHeader(icon: ImageVector, text: String, tint: Color = MaterialTheme.colorScheme.onSurface) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = tint)
    }
}
