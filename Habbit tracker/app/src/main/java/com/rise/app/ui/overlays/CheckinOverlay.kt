package com.rise.app.ui.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rise.app.data.RiseDefaults
import com.rise.app.data.RiseUiState
import com.rise.app.ui.common.pressable
import com.rise.app.ui.theme.Bg
import com.rise.app.ui.theme.Card
import com.rise.app.ui.theme.Fredoka
import com.rise.app.ui.theme.Green
import com.rise.app.ui.theme.Ink
import com.rise.app.ui.theme.InkSoft
import com.rise.app.ui.theme.Nunito
import com.rise.app.vm.RiseViewModel

/** "Is het je vandaag gelukt?", Ja, or No with an optional trigger. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CheckinOverlay(state: RiseUiState, vm: RiseViewModel) {
    var askTrigger by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(Bg.copy(alpha = 0.97f)), contentAlignment = Alignment.Center) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // close
            Box(
                Modifier.align(Alignment.End).size(34.dp).clip(CircleShape).background(Color(0x141B3A2E))
                    .pressable { vm.closeCheckin() },
                contentAlignment = Alignment.Center,
            ) { Text("✕", fontSize = 18.sp, color = Ink) }

            Text("🌅", fontSize = 44.sp)
            Text(
                if (!askTrigger) "Is het je vandaag gelukt?" else "Wat triggerde het?",
                fontFamily = Fredoka,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                color = Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 12.dp),
            )
            Text(
                if (!askTrigger) "Eerlijk antwoorden helpt je groeien, niemand oordeelt."
                else "Zo leer je je patroon kennen. Of sla over.",
                fontFamily = Nunito,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = InkSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )

            Box(Modifier.height(28.dp))

            if (!askTrigger) {
                Cta("Ja, het is gelukt ✓", Green, Card) { vm.submitCheckin(true, null) }
                Box(Modifier.height(12.dp))
                Cta("Nee, vandaag niet", Color(0xFFF0F2F0), Ink) { askTrigger = true }
            } else {
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    RiseDefaults.triggers.forEach { trig ->
                        Box(
                            Modifier.clip(RoundedCornerShape(14.dp)).background(Color(0xFFF0F2F0))
                                .pressable { vm.submitCheckin(false, trig.label) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                        ) {
                            Text("${trig.icon} ${trig.label}", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Ink)
                        }
                    }
                }
                Box(Modifier.height(16.dp))
                Cta("Overslaan", Color(0xFFF0F2F0), InkSoft) { vm.submitCheckin(false, null) }
            }
        }
    }
}

@Composable
private fun Cta(text: String, bg: Color, fg: Color, onClick: () -> Unit) {
    Text(
        text,
        fontFamily = Nunito,
        fontWeight = FontWeight.Black,
        fontSize = 15.sp,
        color = fg,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(bg).pressable { onClick() }.padding(vertical = 15.dp),
    )
}
