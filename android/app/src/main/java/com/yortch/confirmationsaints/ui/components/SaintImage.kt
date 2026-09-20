package com.yortch.confirmationsaints.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.yortch.confirmationsaints.data.model.Saint
import androidx.compose.ui.platform.LocalContext
import kotlin.math.abs

/**
 * Saint id whose portrait is the official FMA logo. Per the legal terms of
 * use for that logo, its shape, colors, and design must not be altered, so
 * it must never be cropped — unlike the circular-cropped photo portraits
 * used for every other saint.
 */
internal const val UNCROPPED_LOGO_SAINT_ID = "maria-troncatti"

/**
 * `ContentScale.Crop` fills (and crops) the circular frame; `Fit` scales the
 * asset down proportionally so the whole image stays visible. Exposed as a
 * pure function so the saint-specific rule is unit-testable without Compose.
 */
internal fun contentScaleForSaint(saintId: String): ContentScale =
    if (saintId == UNCROPPED_LOGO_SAINT_ID) ContentScale.Fit else ContentScale.Crop

/** Whether the portrait should be clipped to a circle. See [contentScaleForSaint]. */
internal fun shouldClipToCircle(saintId: String): Boolean = saintId != UNCROPPED_LOGO_SAINT_ID

/**
 * Displays a saint's portrait as a circular image, falling back to a
 * colored initial bubble when the image asset is missing.
 *
 * Images live at `assets/images/{saint.image.filename}` (copied there by
 * the `syncSharedContent` Gradle task). Coil 3 resolves
 * `file:///android_asset/...` URIs natively — no custom fetcher needed.
 *
 * Mirrors iOS `SaintImageView`. Exception: [UNCROPPED_LOGO_SAINT_ID]'s asset
 * is an official third-party logo whose design must not be altered, so it is
 * shown uncropped (`ContentScale.Fit`, no circular clip) instead.
 */
@Composable
fun SaintImage(
    saint: Saint,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val filename = saint.image?.filename
    val borderColor = MaterialTheme.colorScheme.error
    val borderWidth = if (size > 60.dp) 2.dp else 1.dp

    if (filename.isNullOrBlank()) {
        InitialBubble(saint = saint, size = size, modifier = modifier)
        return
    }

    val context = LocalContext.current
    val clipToCircle = shouldClipToCircle(saint.id)
    var imageModifier = modifier.size(size)
    if (clipToCircle) {
        imageModifier = imageModifier.clip(CircleShape)
    }
    imageModifier = imageModifier
        .border(borderWidth, borderColor.copy(alpha = 0.3f), if (clipToCircle) CircleShape else RoundedCornerShape(0.dp))
        .semantics { contentDescription = saint.name }

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data("file:///android_asset/images/$filename")
            .crossfade(true)
            .build(),
        // Fallback to the initial bubble if decoding fails or file missing.
        error = null,
        contentDescription = saint.name,
        contentScale = contentScaleForSaint(saint.id),
        modifier = imageModifier,
    )
}

@Composable
private fun InitialBubble(saint: Saint, size: Dp, modifier: Modifier) {
    val palette = listOf(
        Color(0xFFC62828), Color(0xFF1565C0), Color(0xFF283593),
        Color(0xFF00796B), Color(0xFFAD1457), Color(0xFFE65100),
        Color(0xFF2E7D32), Color(0xFF00838F),
    )
    val color = palette[abs(saint.id.hashCode()) % palette.size]
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .semantics { contentDescription = saint.name },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = saint.name.take(1).uppercase(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.44f).sp,
        )
    }
}
