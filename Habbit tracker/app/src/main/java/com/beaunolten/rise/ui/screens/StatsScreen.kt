package com.beaunolten.rise.ui.screens

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beaunolten.rise.data.RiseDefaults
import com.beaunolten.rise.data.RiseUiState
import com.beaunolten.rise.ui.common.cardSurface
import com.beaunolten.rise.ui.theme.Card
import com.beaunolten.rise.ui.theme.Fredoka
import com.beaunolten.rise.ui.theme.Green
import com.beaunolten.rise.ui.theme.GreenDark
import com.beaunolten.rise.ui.theme.Ink
import com.beaunolten.rise.ui.theme.InkFaint
import com.beaunolten.rise.ui.theme.InkGhost
import com.beaunolten.rise.ui.theme.InkSoft
import com.beaunolten.rise.ui.theme.Nunito
import com.beaunolten.rise.ui.theme.Orange
import com.beaunolten.rise.ui.theme.Teal
import com.beaunolten.rise.ui.theme.TealDark
import com.beaunolten.rise.vm.RiseViewModel
import kotlin.math.roundToInt

private val TrackBg = Color(0xFFEDF4F0)
private val WeekBarA = Color(0xFFFF8A65)
private val WeekBarB = Color(0xFFFF6B43)

@Composable
fun StatsScreen(state: RiseUiState, vm: RiseViewModel) {
    Column(Modifier.fillMaxWidth()) {
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
            Text("Schone dagen · 14 dagen", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Ink)
            Row(
                Modifier.fillMaxWidth().height(96.dp).padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                for (i in 0 until 14) {
                    val h = 40 + (i / 13f * 58f).roundToInt()
                    val color = if (i == 13) Orange else Green
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight(h / 100f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(color),
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("12 jun", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = InkGhost)
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
                                    .fillMaxWidth(trig.pct / 100f)
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
                Text("92% deze week 🔥", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 12.sp, color = Green)
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Bottom,
            ) {
                RiseDefaults.weeks.forEach { (label, h) ->
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.BottomCenter) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .fillMaxHeight(h / 100f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.verticalGradient(listOf(WeekBarA, WeekBarB))),
                            )
                        }
                        Text(label, fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp, color = InkFaint, modifier = Modifier.padding(top = 6.dp))
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
