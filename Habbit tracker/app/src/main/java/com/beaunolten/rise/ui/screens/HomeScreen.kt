package com.beaunolten.rise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beaunolten.rise.data.RiseUiState
import com.beaunolten.rise.data.TaskItem
import com.beaunolten.rise.data.HeroStyle
import com.beaunolten.rise.ui.common.cardSurface
import com.beaunolten.rise.ui.plant.PlantWithPot
import com.beaunolten.rise.ui.theme.Bg
import com.beaunolten.rise.ui.theme.Card
import com.beaunolten.rise.ui.theme.CardMint
import com.beaunolten.rise.ui.theme.Fredoka
import com.beaunolten.rise.ui.theme.Green
import com.beaunolten.rise.ui.theme.GreenDark
import com.beaunolten.rise.ui.theme.HeroPlantA
import com.beaunolten.rise.ui.theme.HeroPlantB
import com.beaunolten.rise.ui.theme.Ink
import com.beaunolten.rise.ui.theme.InkFaint
import com.beaunolten.rise.ui.theme.InkSoft
import com.beaunolten.rise.ui.theme.Nunito
import com.beaunolten.rise.ui.theme.Orange
import com.beaunolten.rise.ui.theme.Teal
import com.beaunolten.rise.ui.theme.YellowA
import com.beaunolten.rise.ui.theme.YellowB
import com.beaunolten.rise.vm.RiseViewModel

private val CheckBorder = Color(0xFFCFE0D7)

@Composable
fun HomeScreen(state: RiseUiState, vm: RiseViewModel) {
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(6.dp))

        // ---- header ----
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Dinsdag 25 juni", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = InkSoft)
                Text(
                    "Kom op ${state.displayName}, jij kan dit 💪",
                    fontFamily = Fredoka,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 21.sp,
                    color = Ink,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircleButton("⚙️", Card, elevated = true) { vm.openSettings() }
                CircleButton("🙂", CardMint, elevated = false) { vm.goTab(com.beaunolten.rise.data.Tab.MOOD) }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---- hero ----
        if (state.heroStyle == HeroStyle.PLANT) PlantHero(state) else NumberHero(state)

        Spacer(Modifier.height(14.dp))

        // ---- mini stats ----
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MiniStat("36u", "tijd terug", Teal)
            MiniStat("${state.urgePct}%", "drang verslagen", Orange)
            MiniStat("${state.doneCount}", "wins vandaag", Green)
        }

        Spacer(Modifier.height(14.dp))

        // ---- bored CTA ----
        Row(
            Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(24.dp), clip = false)
                .background(Brush.horizontalGradient(listOf(YellowA, YellowB)), RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .clickable { vm.openBored() }
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text("🎯", fontSize = 30.sp)
            Column(Modifier.weight(1f)) {
                Text("Voel je je leeg of verveeld?", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color(0xFF7A5A14))
                Text("Pak een snelle actie — vul de leegte →", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp, color = Color(0xFF9A7826))
            }
        }

        Spacer(Modifier.height(18.dp))

        // ---- today ----
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Plan van vandaag", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Ink)
            Text("${state.doneCount} / ${state.taskTotal} gedaan", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Green)
        }

        Spacer(Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            state.tasks.forEach { task ->
                TaskRow(task) { vm.toggleTask(task.id) }
            }
        }
    }
}

@Composable
private fun CircleButton(emoji: String, bg: Color, elevated: Boolean, onClick: () -> Unit) {
    val base = Modifier.size(44.dp)
    val styled = if (elevated) base.cardSurface(CircleShape, bg, elevation = 6.dp) else base.background(bg, CircleShape).clip(CircleShape)
    Box(styled.clickable { onClick() }, contentAlignment = Alignment.Center) {
        Text(emoji, fontSize = 20.sp)
    }
}

@Composable
private fun PlantHero(state: RiseUiState) {
    Column(
        Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(30.dp), clip = false)
            .background(Brush.verticalGradient(listOf(HeroPlantA, HeroPlantB)), RoundedCornerShape(30.dp))
            .clip(RoundedCornerShape(30.dp))
            .padding(top = 18.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            "JOUW GROEI · ${state.streak} DAGEN",
            fontFamily = Nunito,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            color = Green,
        )
        PlantWithPot(generations = state.plantGenerations, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun NumberHero(state: RiseUiState) {
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(30.dp), clip = false)
            .background(Brush.linearGradient(listOf(Green, GreenDark)), RoundedCornerShape(30.dp))
            .clip(RoundedCornerShape(30.dp))
            .padding(24.dp),
    ) {
        Text("🌿", fontSize = 120.sp, color = Card.copy(alpha = 0.18f), modifier = Modifier.align(Alignment.BottomEnd))
        Column {
            Text("JE STREAK", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Card.copy(alpha = 0.9f))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${state.streak}", fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 72.sp, color = Card)
                Text(" dagen", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = Card, modifier = Modifier.padding(bottom = 12.dp))
            }
            Text("Sterk & vrij. Record: 21 dagen.", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Card.copy(alpha = 0.95f))
        }
    }
}

@Composable
private fun RowScope.MiniStat(value: String, label: String, color: Color) {
    Column(
        Modifier
            .weight(1f)
            .cardSurface(RoundedCornerShape(22.dp), elevation = 6.dp)
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(value, fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 23.sp, color = color)
        Text(label, fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = InkSoft)
    }
}

@Composable
private fun TaskRow(task: TaskItem, onToggle: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .cardSurface(RoundedCornerShape(20.dp), elevation = 4.dp)
            .clickable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        val checkMod = if (task.done) {
            Modifier.size(26.dp).background(Green, CircleShape)
        } else {
            Modifier.size(26.dp).border(2.dp, CheckBorder, CircleShape)
        }
        Box(checkMod, contentAlignment = Alignment.Center) {
            if (task.done) Text("✓", color = Card, fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 14.sp)
        }
        Text(
            "${task.time} · ${task.label}",
            modifier = Modifier.weight(1f),
            fontFamily = Nunito,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            color = if (task.done) InkFaint else Ink,
            textDecoration = if (task.done) TextDecoration.LineThrough else TextDecoration.None,
        )
        Text(task.icon, fontSize = 18.sp)
    }
}
