package com.beaunolten.rise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beaunolten.rise.data.RiseDefaults
import com.beaunolten.rise.data.RiseUiState
import com.beaunolten.rise.ui.common.cardSurface
import com.beaunolten.rise.ui.theme.Card
import com.beaunolten.rise.ui.theme.Fredoka
import com.beaunolten.rise.ui.theme.Ink
import com.beaunolten.rise.ui.theme.InkFaint
import com.beaunolten.rise.ui.theme.InkGhost
import com.beaunolten.rise.ui.theme.InkSoft
import com.beaunolten.rise.ui.theme.JournalA
import com.beaunolten.rise.ui.theme.JournalB
import com.beaunolten.rise.ui.theme.Nunito
import com.beaunolten.rise.ui.theme.Purple
import com.beaunolten.rise.ui.theme.PurpleInk
import com.beaunolten.rise.ui.theme.PurpleInk2
import com.beaunolten.rise.vm.RiseViewModel

@Composable
fun MoodScreen(state: RiseUiState, vm: RiseViewModel) {
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        Text("Stemming", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, color = Ink)
        Text("Leer jezelf kennen — zonder oordeel.", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = InkSoft)

        Spacer(Modifier.height(16.dp))

        // ---- log now ----
        Column(
            Modifier.fillMaxWidth().cardSurface(RoundedCornerShape(26.dp)).padding(horizontal = 18.dp, vertical = 20.dp),
        ) {
            Text("Hoe voel je je nu?", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Ink, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                RiseDefaults.moods.forEach { mood ->
                    Column(
                        Modifier.weight(1f).clickable { vm.pickMood(mood) },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val picked = state.moodPicked == mood.key
                        Box(
                            Modifier.size(48.dp).scale(if (picked) 1.18f else 1f).clip(CircleShape).background(mood.bg),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(mood.face, fontSize = 26.sp)
                        }
                        Text(mood.label, fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 10.5.sp, color = InkSoft)
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ---- journaling ----
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(26.dp))
                .background(Brush.linearGradient(listOf(JournalA, JournalB)))
                .padding(horizontal = 18.dp, vertical = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🌳", fontSize = 18.sp)
                Text("Jouw boomhut", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 15.sp, color = PurpleInk)
            }
            Text(
                "Wat zijn 3 dingen die je vandaag goed deed?",
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = PurpleInk2,
                modifier = Modifier.padding(top = 8.dp),
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(74.dp)
                    .background(Card.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                if (state.journalText.isEmpty()) {
                    Text("Typ hier… niemand leest mee.", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PurpleInk2.copy(alpha = 0.55f))
                }
                BasicTextField(
                    value = state.journalText,
                    onValueChange = vm::setJournal,
                    textStyle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PurpleInk2),
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Text(
                "Bewaren ✨",
                fontFamily = Nunito,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = Card,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Purple)
                    .clickable { vm.saveJournal() }
                    .padding(vertical = 12.dp),
            )
        }

        Spacer(Modifier.height(14.dp))

        // ---- calendar ----
        Column(
            Modifier.fillMaxWidth().cardSurface(RoundedCornerShape(26.dp)).padding(18.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Juni 2026", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Ink)
                Text("Stemmingen", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = InkFaint)
            }
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    RiseDefaults.weekdayLabels.forEach { w ->
                        Text(w, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 10.sp, color = InkGhost)
                    }
                }
                buildCalendar().chunked(7).forEach { week ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        week.forEach { cell -> CalendarCell(cell, Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

private data class CalCellData(val content: String, val bg: Color, val color: Color)

private fun buildCalendar(): List<CalCellData> {
    val seed = RiseDefaults.moodCalSeed
    val faces = RiseDefaults.moodCalFaces
    val colors = RiseDefaults.moodCalColors
    val out = ArrayList<CalCellData>(35)
    for (i in 0 until 35) {
        val day = i - 1 // 2 leading blanks
        when {
            day < 1 || day > 30 -> out.add(CalCellData("", Color.Transparent, Color.Transparent))
            day <= 25 -> {
                val mi = seed[(day - 1) % seed.size]
                out.add(CalCellData(faces[mi], colors[mi], Ink))
            }
            else -> out.add(CalCellData("$day", Color(0xFFF0F6F2), InkGhost))
        }
    }
    return out
}

@Composable
private fun CalendarCell(cell: CalCellData, modifier: Modifier) {
    Box(
        modifier.aspectRatio(1f).clip(CircleShape).background(cell.bg),
        contentAlignment = Alignment.Center,
    ) {
        if (cell.content.isNotEmpty()) {
            Text(cell.content, fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = cell.color)
        }
    }
}
