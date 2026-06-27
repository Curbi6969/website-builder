package com.rise.app.ui.screens

import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rise.app.data.RiseDefaults
import com.rise.app.data.RiseUiState
import com.rise.app.ui.common.cardSurface
import com.rise.app.ui.theme.Card
import com.rise.app.ui.theme.Fredoka
import com.rise.app.ui.theme.Green
import com.rise.app.ui.theme.GreenDark
import com.rise.app.ui.theme.Ink
import com.rise.app.ui.theme.InkFaint
import com.rise.app.ui.theme.InkGhost
import com.rise.app.ui.theme.InkSoft
import com.rise.app.ui.theme.Nunito
import com.rise.app.ui.theme.Orange
import com.rise.app.ui.theme.Teal
import com.rise.app.ui.theme.TealDark
import com.rise.app.vm.RiseViewModel
import kotlin.math.roundToInt

private val TrackBg = Color(0xFFEDF4F0)
private val WeekBarA = Color(0xFFFF8A65)
private val WeekBarB = Color(0xFFFF6B43)

private val DutchShort = listOf(
    "jan", "feb", "mrt", "apr", "mei", "jun", "jul", "aug", "sep", "okt", "nov", "dec",
)

private fun startLabel(date: java.time.LocalDate?): String =
    date?.let { "${it.dayOfMonth} ${DutchShort[it.monthValue - 1]}" } ?: "start"

@Composable
fun StatsScreen(state: RiseUiState, vm: RiseViewModel) {
    Column(Modifier.fillMaxWidth()) {
        var started by remember { mutableStateOf(false) }
        val grow by animateFloatAsState(if (started) 1f else 0f, tween(800, easing = EaseOutQuart), label = "grow")
        LaunchedEffect(Unit) { started = true }
        Spacer(Modifier.height(8.dp))
        Text("Jouw cijfers", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, color = Ink)
        Text("Bewijs zwart op wit: je groeit.", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = InkSoft)

        Spacer(Modifier.height(16.dp))

        // ---- reclaimed ----
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ReclaimedCard("⏳", "36u", "tijd teruggewonnen", listOf(Green, GreenDark))
            ReclaimedCard("🧠", "+18%", "focus & energie", listOf(Teal, TealDark))
        }

        Spacer(Modifier.height(14.dp))

        // ---- streak chart ----
        Column(Modifier.fillMaxWidth().cardSurface(RoundedCornerShape(26.dp)).padding(18.dp)) {
            val cleanDays = state.streak
            Text("Schone dagen · $cleanDays dagen", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Ink)
            Row(
                Modifier.fillMaxWidth().height(96.dp).padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                val filled = cleanDays.coerceIn(0, 14)
                for (i in 0 until 14) {
                    val h = 40 + (i / 13f * 58f).roundToInt()
                    val color = when {
                        i == filled - 1 -> Orange
                        i < filled -> Green
                        else -> TrackBg
                    }
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight((h / 100f) * grow)
                            .clip(RoundedCornerShape(6.dp))
                            .background(color),
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(startLabel(state.soberSince), fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = InkGhost)
                Text("vandaag", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = InkGhost)
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---- triggers ----
        Column(Modifier.fillMaxWidth().cardSurface(RoundedCornerShape(26.dp)).padding(18.dp)) {
            Text("Wanneer komt de drang op?", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Ink)
            Text("Zo weet je waar je sterk moet staan.", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = InkSoft, modifier = Modifier.padding(top = 2.dp))
            Column(Modifier.padding(top = 14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RiseDefaults.triggers.forEach { trig ->
                    Column {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${trig.icon} ${trig.label}", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Ink)
                            Text("${trig.pct}%", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = InkFaint)
                        }
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 5.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(TrackBg),
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth((trig.pct / 100f) * grow)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(trig.color),
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---- urges beaten weekly ----
        Column(Modifier.fillMaxWidth().cardSurface(RoundedCornerShape(26.dp)).padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Drang verslagen", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Ink)
                Text("${state.urgeThisWeek}× deze week 🔥", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 12.sp, color = Green)
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                val weekly = state.urgeWeekly.ifEmpty { listOf(0, 0, 0, 0) }
                val maxC = (weekly.maxOrNull() ?: 0).coerceAtLeast(1)
                val labels = listOf("3 wk", "2 wk", "1 wk", "nu")
                weekly.forEachIndexed { i, count ->
                    val frac = if (count == 0) 0f else (count.toFloat() / maxC).coerceAtLeast(0.18f)
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.BottomCenter) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(frac * grow)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.verticalGradient(listOf(WeekBarA, WeekBarB))),
                            )
                        }
                        Text(labels.getOrElse(i) { "" }, fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = InkFaint, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.ReclaimedCard(icon: String, value: String, label: String, gradient: List<Color>) {
    Column(
        Modifier
            .weight(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(gradient))
            .padding(18.dp),
    ) {
        Text(icon, fontSize = 24.sp)
        Text(value, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = Card, modifier = Modifier.padding(top = 4.dp))
        Text(label, fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Card.copy(alpha = 0.92f))
    }
}
