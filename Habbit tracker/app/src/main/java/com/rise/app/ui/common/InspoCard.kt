package com.rise.app.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rise.app.data.InspoAccent
import com.rise.app.ui.theme.BerryA
import com.rise.app.ui.theme.Card
import com.rise.app.ui.theme.Green
import com.rise.app.ui.theme.Fredoka
import com.rise.app.ui.theme.Orange
import com.rise.app.ui.theme.PurpleSoft
import com.rise.app.ui.theme.Teal
import com.rise.app.ui.theme.YellowB

/** Maps a card's accent to a brand colour token (Color.kt). */
fun InspoAccent.color(): Color = when (this) {
    InspoAccent.GREEN -> Green
    InspoAccent.LAVENDER -> PurpleSoft
    InspoAccent.TEAL -> Teal
    InspoAccent.YELLOW -> YellowB
    InspoAccent.CORAL -> Orange
    InspoAccent.BERRY -> BerryA
}

/**
 * Reusable inspiration card: portrait illustration on top, accent-coloured title footer.
 * While [illustration] is 0 the top shows a tinted placeholder (real art wired in Task 8).
 */
@Composable
fun InspoCard(
    title: String,
    illustration: Int,
    accent: InspoAccent,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String = "",
) {
    Column(
        modifier
            .shadow(6.dp, RoundedCornerShape(24.dp), clip = false)
            .clip(RoundedCornerShape(24.dp))
            .background(Card)
            .pressable { onClick() }
            .semantics { this.contentDescription = title; role = Role.Button },
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.82f)
                .background(accent.color().copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center,
        ) {
            if (illustration != 0) {
                Image(
                    painter = painterResource(illustration),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            } else if (emoji.isNotBlank()) {
                Text(emoji, fontSize = 48.sp)
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .background(accent.color())
                .padding(horizontal = 12.dp, vertical = 12.dp),
        ) {
            Text(
                title,
                fontFamily = Fredoka,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = Card,
            )
        }
    }
}
