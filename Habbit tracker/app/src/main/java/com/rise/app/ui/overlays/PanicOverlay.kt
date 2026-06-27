package com.rise.app.ui.overlays

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rise.app.data.Panic
import com.rise.app.data.RiseUiState
import com.rise.app.ui.common.pressable
import com.rise.app.ui.theme.Card
import com.rise.app.ui.theme.Fredoka
import com.rise.app.ui.theme.GreenDark
import com.rise.app.ui.theme.GreenDeep
import com.rise.app.ui.theme.Nunito
import com.rise.app.vm.RiseViewModel

@Composable
fun PanicOverlay(state: RiseUiState, vm: RiseViewModel) {
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(GreenDark, GreenDeep)))) {
        // header
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Je bent veilig. Adem.", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Card)
            Box(
                Modifier.size(34.dp).clip(CircleShape).background(Card.copy(alpha = 0.18f)).pressable {vm.closePanic() },
                contentAlignment = Alignment.Center,
            ) { Text("✕", fontSize = 18.sp, color = Card) }
        }

        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 22.dp).padding(bottom = 26.dp),
        ) {
            when (state.panic) {
                Panic.MENU -> PanicMenu(vm)
                Panic.BREATHING -> Breathing(vm)
                Panic.WATER -> ColdWater(vm)
                Panic.GAME -> PanicGames(state, vm)
                Panic.REASONS -> Reasons(state, vm)
                Panic.DONE -> Done(vm)
                Panic.NONE -> {}
            }
        }
    }
}

@Composable
private fun PanicMenu(vm: RiseViewModel) {
    Column {
        Text(
            "Deze drang is een golf. Hij piekt en zakt, altijd. Kies iets en rij 'm uit, man. 💪",
            fontFamily = Nunito,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            color = Card.copy(alpha = 0.92f),
            lineHeight = 22.sp,
        )
        Spacer(Modifier.height(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ActionRow("🌬️", "Ademhaling", "Kalmeer je lijf in 1 minuut") { vm.panicGo(Panic.BREATHING) }
            ActionRow("💧", "Koud water reset", "Breek het patroon nú") { vm.panicGo(Panic.WATER) }
            ActionRow("🫧", "Snelle afleiding", "Klein spelletje, hoofd leeg") { vm.panicOpenGames() }
            ActionRow("❤️", "Waarom ik begon", "Lees je eigen woorden terug") { vm.panicGo(Panic.REASONS) }
        }
        Spacer(Modifier.height(18.dp))
        WhiteCta("Ik heb 'm verslagen ✓") { vm.panicBeat() }
    }
}

@Composable
private fun Breathing(vm: RiseViewModel) {
    val transition = rememberInfiniteTransition(label = "breathe")
    val scale by transition.animateFloat(
        initialValue = 0.62f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4000), RepeatMode.Reverse),
        label = "breatheScale",
    )
    Column(Modifier.fillMaxWidth().padding(top = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(230.dp), contentAlignment = Alignment.Center) {
            Box(Modifier.size(200.dp).scale(scale).clip(CircleShape).background(Card.copy(alpha = 0.18f)))
            Box(Modifier.size(140.dp).scale(scale).clip(CircleShape).background(Card.copy(alpha = 0.28f)))
            Text("Adem\nmee", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Card, textAlign = TextAlign.Center)
        }
        Text("In · 4  ·  Vast · 4  ·  Uit · 4", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Card.copy(alpha = 0.92f), modifier = Modifier.padding(top = 24.dp))
        Text("Volg de cirkel. Nog even, je doet 't goed.", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Card.copy(alpha = 0.8f), modifier = Modifier.padding(top = 6.dp))
        Spacer(Modifier.height(26.dp))
        BackPill { vm.panicBack() }
        Spacer(Modifier.height(12.dp))
        WhiteCta("Ik voel me rustiger ✓") { vm.panicBeat() }
    }
}

@Composable
private fun ColdWater(vm: RiseViewModel) {
    Column(Modifier.fillMaxWidth().padding(top = 10.dp)) {
        Text("💧", fontSize = 64.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Text("Koud water reset", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Card, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 6.dp))
        Text(
            "Sta op. Beweeg je lijf. Dit breekt het patroon direct, het werkt echt.",
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Card.copy(alpha = 0.88f),
            textAlign = TextAlign.Center,
            lineHeight = 21.sp,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        )
        Spacer(Modifier.height(18.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf(
                "1 · Loop naar de kraan of douche",
                "2 · Koud water op je gezicht / nek",
                "3 · Adem diep, 3 keer",
                "4 · Kom terug. Je bent er doorheen.",
            ).forEach { step -> StepRow(step) }
        }
        Spacer(Modifier.height(18.dp))
        BackPill { vm.panicBack() }
        Spacer(Modifier.height(12.dp))
        WhiteCta("Gedaan, ik voel me beter ✓") { vm.panicBeat() }
    }
}

@Composable
private fun Reasons(state: RiseUiState, vm: RiseViewModel) {
    Column(Modifier.fillMaxWidth().padding(top = 6.dp)) {
        Text("Waarom je begon", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Card, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text("Jouw eigen woorden. Lees ze langzaam.", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Card.copy(alpha = 0.85f), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
        Spacer(Modifier.height(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.reasons.filter { it.isNotBlank() }.forEach { reason ->
                Text(
                    "\"$reason\"",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Card,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Card.copy(alpha = 0.14f)).padding(18.dp),
                )
            }
        }
        Spacer(Modifier.height(18.dp))
        BackPill { vm.panicBack() }
        Spacer(Modifier.height(12.dp))
        WhiteCta("Hierom blijf ik sterk ✓") { vm.panicBeat() }
    }
}

@Composable
private fun Done(vm: RiseViewModel) {
    Column(Modifier.fillMaxWidth().padding(top = 50.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("🎉", fontSize = 84.sp)
        Text("Je hebt 'm verslagen!", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 28.sp, color = Card, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
        Text(
            "Dít is kracht, man. De golf kwam en jij bleef staan. Je streak leeft nog. Trots op je.",
            fontFamily = Nunito,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp,
            color = Card.copy(alpha = 0.92f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(top = 8.dp),
        )
        Spacer(Modifier.height(28.dp))
        WhiteCta("Terug naar mijn dag →") { vm.closePanic() }
    }
}

// ---- shared panic bits ----

@Composable
private fun ActionRow(icon: String, title: String, sub: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Card.copy(alpha = 0.14f)).pressable {onClick() }.padding(18.dp),
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

@Composable
private fun StepRow(text: String) {
    Text(
        text,
        fontFamily = Nunito,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.5.sp,
        color = Card,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Card.copy(alpha = 0.14f)).padding(horizontal = 18.dp, vertical = 15.dp),
    )
}

@Composable
internal fun WhiteCta(text: String, onClick: () -> Unit) {
    Text(
        text,
        fontFamily = Nunito,
        fontWeight = FontWeight.Black,
        fontSize = 15.sp,
        color = GreenDeep,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Card).pressable {onClick() }.padding(vertical = 15.dp),
    )
}

@Composable
internal fun BackPill(onClick: () -> Unit) {
    Text(
        "‹ Terug",
        fontFamily = Nunito,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 14.sp,
        color = Card,
        modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(Card.copy(alpha = 0.16f)).pressable {onClick() }.padding(horizontal = 24.dp, vertical = 12.dp),
    )
}
