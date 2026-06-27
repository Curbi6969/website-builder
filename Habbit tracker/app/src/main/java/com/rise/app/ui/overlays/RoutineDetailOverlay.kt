package com.rise.app.ui.overlays

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rise.app.data.RiseUiState
import com.rise.app.data.RoutineCatalog
import com.rise.app.ui.common.cardSurface
import com.rise.app.ui.common.color
import com.rise.app.ui.common.pressable
import com.rise.app.ui.theme.Bg
import com.rise.app.ui.theme.Card
import com.rise.app.ui.theme.Fredoka
import com.rise.app.ui.theme.Green
import com.rise.app.ui.theme.Ink
import com.rise.app.ui.theme.InkSoft
import com.rise.app.ui.theme.Nunito
import com.rise.app.vm.RiseViewModel

/** Bottom-sheet detail for a catalogue routine, with the "voeg toe" action. */
@Composable
fun RoutineDetailOverlay(state: RiseUiState, vm: RiseViewModel) {
    val routine = RoutineCatalog.routines.find { it.id == state.openRoutine } ?: return
    val added = state.addedRoutineIds.contains(routine.id)
    val accent = routine.accent.color()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val sheetMax = maxHeight * 0.9f

        Box(Modifier.fillMaxSize().background(Color(0x66142819)).clickable { vm.closeRoutine() })

        val visible = remember { MutableTransitionState(false).apply { targetState = true } }
        AnimatedVisibility(
            visibleState = visible,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = sheetMax)
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(Bg)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp)
                    .padding(top = 10.dp, bottom = 30.dp),
            ) {
                Box(
                    Modifier.padding(vertical = 6.dp).size(width = 44.dp, height = 5.dp)
                        .clip(RoundedCornerShape(3.dp)).background(Color(0xFFCDDDD4))
                        .align(Alignment.CenterHorizontally),
                )

                // accent header band
                Box(
                    Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(22.dp))
                        .background(accent.copy(alpha = 0.22f)).padding(vertical = 26.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(routine.steps.firstOrNull()?.icon ?: "🌱", fontSize = 46.sp)
                }

                Spacer(Modifier.height(14.dp))
                Text(routine.name, fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Ink)

                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                        .background(accent.copy(alpha = 0.12f)).padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text("💡", fontSize = 18.sp)
                    Text("Waarom dit werkt: ${routine.why}", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = InkSoft)
                }

                Spacer(Modifier.height(16.dp))
                Text("Stappen", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Ink)
                Spacer(Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    routine.steps.forEach { step ->
                        Row(
                            Modifier.fillMaxWidth().cardSurface(RoundedCornerShape(18.dp), elevation = 3.dp)
                                .padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(step.icon, fontSize = 18.sp)
                            Text(
                                if (step.time.isBlank()) step.label else "${step.time} · ${step.label}",
                                fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp,
                                color = Ink, modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                        .background(if (added) Color(0xFFDCE8E1) else Green)
                        .then(
                            if (added) Modifier
                            else Modifier.pressable {
                                vm.addRoutine(routine.id)
                                vm.setActiveRoutine(routine.id)
                                vm.affirm("Toegevoegd ✓")
                                vm.closeRoutine()
                            },
                        )
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (added) "Toegevoegd ✓" else "Voeg toe aan mijn routines",
                        fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 15.sp,
                        color = if (added) InkSoft else Card,
                    )
                }
            }
        }
    }
}
