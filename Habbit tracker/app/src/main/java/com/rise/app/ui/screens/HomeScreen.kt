package com.rise.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rise.app.data.RiseUiState
import com.rise.app.data.TaskItem
import com.rise.app.data.HeroStyle
import com.rise.app.ui.common.cardSurface
import com.rise.app.ui.common.pressable
import com.rise.app.ui.plant.PlantWithPot
import com.rise.app.ui.theme.Bg
import com.rise.app.ui.theme.Card
import com.rise.app.ui.theme.CardMint
import com.rise.app.ui.theme.Fredoka
import com.rise.app.ui.theme.Green
import com.rise.app.ui.theme.GreenDark
import com.rise.app.ui.theme.HeroPlantA
import com.rise.app.ui.theme.HeroPlantB
import com.rise.app.ui.theme.Ink
import com.rise.app.ui.theme.InkFaint
import com.rise.app.ui.theme.InkSoft
import com.rise.app.ui.theme.Nunito
import com.rise.app.ui.theme.YellowA
import com.rise.app.ui.theme.YellowB
import com.rise.app.vm.RiseViewModel
import java.time.LocalDate

private val CheckBorder = Color(0xFFCFE0D7)

private val DutchDays = listOf("Maandag", "Dinsdag", "Woensdag", "Donderdag", "Vrijdag", "Zaterdag", "Zondag")
private val DutchMonthsLower = listOf(
    "januari", "februari", "maart", "april", "mei", "juni",
    "juli", "augustus", "september", "oktober", "november", "december",
)

private fun todayLabel(d: LocalDate): String =
    "${DutchDays[d.dayOfWeek.value - 1]} ${d.dayOfMonth} ${DutchMonthsLower[d.monthValue - 1]}"

@Composable
fun HomeScreen(state: RiseUiState, vm: RiseViewModel) {
    val today = remember { LocalDate.now() }
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(6.dp))

        // ---- header ----
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(todayLabel(today), fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = InkSoft)
                Text(
                    "Kom op ${state.displayName}, jij kan dit 💪",
                    fontFamily = Fredoka,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 21.sp,
                    color = Ink,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CircleButton("📊", Card, elevated = true, label = "Cijfers") { vm.goTab(com.rise.app.data.Tab.STATS) }
                CircleButton("⚙️", Card, elevated = true, label = "Instellingen") { vm.openSettings() }
                CircleButton("🙂", CardMint, elevated = false, label = "Stemming bijhouden") { vm.goTab(com.rise.app.data.Tab.MOOD) }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---- hero ----
        if (state.heroStyle == HeroStyle.PLANT) PlantHero(state) else NumberHero(state)

        Spacer(Modifier.height(14.dp))

        // ---- daily check-in ----
        if (!state.checkedInToday) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.horizontalGradient(listOf(Green, GreenDark)), RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .pressable { vm.openCheckin() }
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("🌅", fontSize = 28.sp)
                Column(Modifier.weight(1f)) {
                    Text("Dagelijkse check-in", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Card)
                    Text("Is het je vandaag gelukt? →", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 12.5.sp, color = Card.copy(alpha = 0.9f))
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        // ---- bored CTA ----
        Row(
            Modifier
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(24.dp), clip = false)
                .background(Brush.horizontalGradient(listOf(YellowA, YellowB)), RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .pressable { vm.openBored() }
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
private fun CircleButton(emoji: String, bg: Color, elevated: Boolean, label: String, onClick: () -> Unit) {
    val base = Modifier.size(44.dp)
    val styled = if (elevated) base.cardSurface(CircleShape, bg, elevation = 6.dp) else base.background(bg, CircleShape).clip(CircleShape)
    Box(
        styled
            .pressable { onClick() }
            .semantics { contentDescription = label; role = Role.Button },
        contentAlignment = Alignment.Center,
    ) {
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
private fun TaskRow(task: TaskItem, onToggle: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .cardSurface(RoundedCornerShape(20.dp), elevation = 4.dp)
            .pressable { onToggle() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp),
    ) {
        val fill by animateColorAsState(if (task.done) Green else Color.Transparent, tween(220, easing = EaseOutQuart), label = "fill")
        val ring by animateColorAsState(if (task.done) Green else CheckBorder, tween(220, easing = EaseOutQuart), label = "ring")
        Box(
            Modifier.size(26.dp).border(2.dp, ring, CircleShape).background(fill, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            val checkScale by animateFloatAsState(
                targetValue = if (task.done) 1f else 0f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
                label = "check",
            )
            if (checkScale > 0.01f) {
                Text(
                    "✓",
                    color = Card,
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    modifier = Modifier.scale(checkScale),
                )
            }
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
