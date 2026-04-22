package com.yortch.confirmationsaints.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
 * Displays a saint's portrait as a circular image, falling back to a
 * colored initial bubble when the image asset is missing.
 *
 * Images live at `assets/images/{saint.image.filename}` (copied there by
 * the `syncSharedContent` Gradle task). Coil 3 resolves
 * `file:///android_asset/...` URIs natively — no custom fetcher needed.
 *
 * Mirrors iOS `SaintImageView`.
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
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data("file:///android_asset/images/$filename")
            .crossfade(true)
            .build(),
        // Fallback to the initial bubble if decoding fails or file missing.
        error = null,
        contentDescription = saint.name,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(borderWidth, borderColor.copy(alpha = 0.3f), CircleShape)
            .semantics { contentDescription = saint.name },
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
