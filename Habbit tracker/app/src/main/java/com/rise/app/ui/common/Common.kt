package com.rise.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rise.app.ui.theme.Card
import com.rise.app.ui.theme.ShadowInk

/** Soft elevated card surface matching the design's low-alpha shadows. */
fun Modifier.cardSurface(
    shape: Shape = RoundedCornerShape(22.dp),
    bg: Color = Card,
    elevation: Dp = 8.dp,
): Modifier = this
    .shadow(
        elevation = elevation,
        shape = shape,
        clip = false,
        ambientColor = ShadowInk.copy(alpha = 0.5f),
        spotColor = ShadowInk.copy(alpha = 0.5f),
    )
    .background(bg, shape)
    .clip(shape)

/**
 * Clickable for cards/buttons. The default Material ripple fills the surface — because
 * callers clip to their shape (via [cardSurface] or `.clip`) before this, the ripple
 * covers the whole rounded card. No press-scale (that detached the highlight from the edges).
 */
@Composable
fun Modifier.pressable(onClick: () -> Unit): Modifier = this.clickable(onClick = onClick)
