package com.beaunolten.rise.ui.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beaunolten.rise.data.Game
import com.beaunolten.rise.data.RiseDefaults
import com.beaunolten.rise.data.RiseUiState
import com.beaunolten.rise.ui.theme.Card
import com.beaunolten.rise.ui.theme.Fredoka
import com.beaunolten.rise.ui.theme.MoodGreat
import com.beaunolten.rise.ui.theme.Nunito
import com.beaunolten.rise.vm.RiseViewModel

@Composable
fun PanicGames(state: RiseUiState, vm: RiseViewModel) {
    when (state.game) {
        Game.NONE -> GameMenu(vm)
        Game.BUBBLES -> Bubbles(state, vm)
        Game.GROUND -> Grounding(state, vm)
        Game.TAP -> TapGreen(state, vm)
    }
}

@Composable
private fun GameMenu(vm: RiseViewModel) {
    Column(Modifier.fillMaxWidth()) {
        Text("Kies je afleiding", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Card, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text("Even je hoofd ergens anders. Pak er één.", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Card.copy(alpha = 0.85f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
        Spacer(Modifier.height(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GameChoice("🫧", "Bubbels knappen", "Tik en leeg je hoofd") { vm.pickBubbles() }
            GameChoice("🧭", "5-4-3-2-1 grounding", "Terug in het hier en nu") { vm.pickGround() }
            GameChoice("⚡", "Tik de groene", "Snelheid & focus") { vm.pickTap() }
        }
        Spacer(Modifier.height(18.dp))
        BackPill { vm.panicBack() }
    }
}

@Composable
private fun Bubbles(state: RiseUiState, vm: RiseViewModel) {
    Column(Modifier.fillMaxWidth()) {
        Text("Tik de bubbels", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Card, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text("Geknapt: ${state.gameScore}", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Card.copy(alpha = 0.85f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
        Box(
            Modifier.fillMaxWidth().padding(top = 14.dp).height(320.dp).clip(RoundedCornerShape(24.dp)).background(Card.copy(alpha = 0.1f)),
        ) {
            state.bubbles.forEach { bubble ->
                Box(
                    Modifier
                        .offset(x = bubble.x.dp, y = bubble.y.dp)
                        .size(bubble.size.dp)
                        .clip(CircleShape)
                        .background(bubble.color)
                        .clickable { vm.popBubble(bubble.id) },
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        BackPill { vm.gameBack() }
        Spacer(Modifier.height(12.dp))
        WhiteCta("De drang is weg ✓") { vm.panicBeat() }
    }
}

@Composable
private fun Grounding(state: RiseUiState, vm: RiseViewModel) {
    val steps = RiseDefaults.groundSteps
    if (state.groundDone) {
        Column(Modifier.fillMaxWidth().padding(top = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🌿", fontSize = 70.sp)
            Text("Je bent terug in het nu", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = Card, modifier = Modifier.padding(top = 8.dp))
            Text("Voel je de grond onder je voeten? Goed gedaan.", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Card.copy(alpha = 0.9f), textAlign = TextAlign.Center, modifier = Modifier.padding(top = 6.dp))
            Spacer(Modifier.height(24.dp))
            WhiteCta("Klaar ✓") { vm.panicBeat() }
        }
        return
    }

    val idx = state.groundStep.coerceAtMost(steps.size - 1)
    val gStep = steps[idx]
    val canNext = state.groundFilled >= gStep.n

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("STAP ${state.groundStep + 1} / ${steps.size}", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 12.sp, color = Card.copy(alpha = 0.7f), modifier = Modifier.padding(top = 6.dp))
        Text(gStep.icon, fontSize = 60.sp, modifier = Modifier.padding(top = 10.dp))
        Text("Vind ${gStep.n}", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = Card, modifier = Modifier.padding(top = 6.dp))
        Text(gStep.label, fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Card.copy(alpha = 0.92f), textAlign = TextAlign.Center)
        Row(Modifier.padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(gStep.n) { i ->
                val filled = i < state.groundFilled
                Box(
                    Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .then(if (filled) Modifier.background(MoodGreat) else Modifier.border(2.dp, Card.copy(alpha = 0.5f), CircleShape)),
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Card.copy(alpha = 0.18f)).clickable { vm.tapGround(gStep.n) }.padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text("Ik heb er één gevonden ✓", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Card)
        }
        if (canNext) {
            Spacer(Modifier.height(12.dp))
            WhiteCta("Volgende →") { vm.nextGround(steps.size) }
        }
        Spacer(Modifier.height(16.dp))
        BackPill { vm.gameBack() }
    }
}

@Composable
private fun TapGreen(state: RiseUiState, vm: RiseViewModel) {
    Column(Modifier.fillMaxWidth()) {
        Text("Tik de groene", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Card, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        val caption = if (state.tapOver) "Tijd! Score: ${state.tapScore} · record ${state.tapRecord}"
        else "Score: ${state.tapScore}  ·  nog ${state.tapTime}s"
        Text(caption, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Card.copy(alpha = 0.85f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))

        Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (row in 0 until 3) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (col in 0 until 3) {
                        val i = row * 3 + col
                        val isTarget = i == state.tapTarget && state.tapRunning
                        Box(
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isTarget) MoodGreat else Card.copy(alpha = 0.14f))
                                .clickable { vm.hitTap(i) },
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        if (state.tapOver) {
            WhiteCta("Opnieuw spelen") { vm.startTap() }
            Spacer(Modifier.height(12.dp))
        }
        BackPill { vm.gameBack() }
        Spacer(Modifier.height(12.dp))
        WhiteCta("De drang is weg ✓") { vm.panicBeat() }
    }
}

@Composable
private fun GameChoice(icon: String, title: String, sub: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Card.copy(alpha = 0.14f)).clickable { onClick() }.padding(18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(icon, fontSize = 30.sp)
        Column(Modifier.weight(1f)) {
            Text(title, fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Card)
            Text(sub, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Card.copy(alpha = 0.85f))
        }
        Text("›", fontSize = 20.sp, color = Card.copy(alpha = 0.6f))
    }
}
