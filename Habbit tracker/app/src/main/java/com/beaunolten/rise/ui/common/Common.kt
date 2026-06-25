package com.beaunolten.rise.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beaunolten.rise.ui.theme.Card
import com.beaunolten.rise.ui.theme.Ink
import com.beaunolten.rise.ui.theme.Nunito
import com.beaunolten.rise.ui.theme.ShadowInk

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

/** Fake phone status bar (9:41 + glyphs), matching the mockup. */
@Composable
fun StatusBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 26.dp, end = 26.dp, top = 14.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("9:41", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Ink)
        Text("●●●  ⌁  ▮", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Ink)
    }
}
