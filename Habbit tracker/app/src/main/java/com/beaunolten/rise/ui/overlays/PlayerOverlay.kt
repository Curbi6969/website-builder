package com.beaunolten.rise.ui.overlays

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beaunolten.rise.data.RiseUiState
import com.beaunolten.rise.ui.theme.Card
import com.beaunolten.rise.ui.theme.TealDark
import com.beaunolten.rise.ui.theme.TealDeep
import com.beaunolten.rise.vm.RiseViewModel

@Composable
fun PlayerOverlay(state: RiseUiState, vm: RiseViewModel) {
    val player = state.player ?: return
    val total = state.playerTotal.coerceAtLeast(1)
    val pct = (total - state.playerLeft).toFloat() / total
    val sweep = pct * 360f
    val mm = state.playerLeft / 60
    val ss = state.playerLeft % 60
    val timeFmt = "$mm:${ss.toString().padStart(2, '0')}"

    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(TealDark, TealDeep))),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CloseCircle { vm.closePlayer() }
            Text("Meditatie", fontFamily = com.beaunolten.rise.ui.theme.Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Card)
            Spacer(Modifier.width(34.dp))
        }

        Column(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(player.icon, fontSize = 50.sp)
            Text(player.title, fontFamily = com.beaunolten.rise.ui.theme.Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, color = Card, modifier = Modifier.padding(top = 10.dp))
            Text(
                player.sub,
                fontFamily = com.beaunolten.rise.ui.theme.Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Card.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 260.dp).padding(top = 4.dp),
            )

            Box(Modifier.padding(top = 28.dp).size(226.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(226.dp)) {
                    drawArc(color = Card, startAngle = -90f, sweepAngle = sweep, useCenter = true)
                    drawArc(color = Card.copy(alpha = 0.22f), startAngle = -90f + sweep, sweepAngle = 360f - sweep, useCenter = true)
                }
                Box(Modifier.size(194.dp).clip(CircleShape).background(TealDeep), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(timeFmt, fontFamily = com.beaunolten.rise.ui.theme.Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 44.sp, color = Card)
                        Text("resterend", fontFamily = com.beaunolten.rise.ui.theme.Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Card.copy(alpha = 0.7f))
                    }
                }
            }

            if (!state.playerDone) {
                Box(
                    Modifier
                        .padding(top = 26.dp)
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Card)
                        .clickable { vm.togglePlay() },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(if (state.playerPlaying) "⏸" else "▶", fontSize = 24.sp, color = TealDeep, fontWeight = FontWeight.Black)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 24.dp)) {
                    Text("Mooi. Je nam tijd voor jezelf. 🌱", fontFamily = com.beaunolten.rise.ui.theme.Nunito, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Card)
                    Text(
                        "Klaar ✓",
                        fontFamily = com.beaunolten.rise.ui.theme.Nunito,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        color = TealDeep,
                        modifier = Modifier
                            .padding(top = 14.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Card)
                            .clickable { vm.closePlayer() }
                            .padding(horizontal = 28.dp, vertical = 14.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CloseCircle(onClick: () -> Unit) {
    Box(
        Modifier.size(34.dp).clip(CircleShape).background(Card.copy(alpha = 0.18f)).clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Text("✕", fontSize = 18.sp, color = Card)
    }
}
