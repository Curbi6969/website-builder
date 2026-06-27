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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rise.app.data.CheckKind
import com.rise.app.data.RiseUiState
import com.rise.app.data.RoutineCatalog
import com.rise.app.data.SelfCheck
import com.rise.app.ui.common.cardSurface
import com.rise.app.ui.common.color
import com.rise.app.ui.common.pressable
import com.rise.app.ui.theme.Bg
import com.rise.app.ui.theme.Card
import com.rise.app.ui.theme.Fredoka
import com.rise.app.ui.theme.Green
import com.rise.app.ui.theme.Ink
import com.rise.app.ui.theme.InkFaint
import com.rise.app.ui.theme.InkSoft
import com.rise.app.ui.theme.Nunito
import com.rise.app.ui.theme.Orange
import com.rise.app.vm.RiseViewModel

private data class Suggestion(val label: String, val action: () -> Unit)

private fun suggestionFor(id: String, vm: RiseViewModel): Suggestion? = when (id) {
    "halt" -> Suggestion("Start Drang-routine →") { vm.closeSelfCheck(); vm.openRoutine("drang") }
    "drang_check" -> Suggestion("Start urge surfen 🌊") { vm.closeSelfCheck(); vm.openUrgeSurf() }
    "zelfcompassie_check" -> Suggestion("Probeer Zelfcompassie-routine →") { vm.closeSelfCheck(); vm.openRoutine("zelfcompassie") }
    else -> null
}

/** Bottom-sheet self-check: short questions → supportive, non-diagnostic feedback (+ 113 on heavy). */
@Composable
fun SelfCheckOverlay(state: RiseUiState, vm: RiseViewModel) {
    val check = RoutineCatalog.selfChecks.find { it.id == state.openSelfCheck } ?: return
    val accent = check.accent.color()

    val answers: SnapshotStateList<Int> = remember(check.id) {
        check.questions.map { if (it.kind == CheckKind.SCALE_0_10) 0 else -1 }.toMutableStateList()
    }
    var showFeedback by remember(check.id) { mutableStateOf(false) }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val sheetMax = maxHeight * 0.9f
        Box(Modifier.fillMaxSize().background(Color(0x66142819)).clickable { vm.closeSelfCheck() })

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
                Spacer(Modifier.height(8.dp))
                Text(check.name, fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Ink)

                if (!showFeedback) {
                    Text(
                        check.intro,
                        fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = InkSoft,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Spacer(Modifier.height(16.dp))
                    check.questions.forEachIndexed { i, q ->
                        Text(q.text, fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 14.5.sp, color = Ink)
                        Spacer(Modifier.height(8.dp))
                        when (q.kind) {
                            CheckKind.YES_NO -> YesNoRow(answers[i], accent) { answers[i] = it }
                            CheckKind.LIKERT_1_5 -> LikertRow(answers[i], accent) { answers[i] = it }
                            CheckKind.SCALE_0_10 -> ScaleRow(answers[i], accent) { answers[i] = it }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    val ready = answers.none { it < 0 }
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                            .background(if (ready) Green else Color(0xFFDCE8E1))
                            .then(if (ready) Modifier.pressable { showFeedback = true } else Modifier)
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "Klaar",
                            fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 15.sp,
                            color = if (ready) Card else InkFaint,
                        )
                    }
                } else {
                    val heavy = check.heavy(answers.toList())
                    Spacer(Modifier.height(12.dp))
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                            .background(accent.copy(alpha = 0.15f)).padding(18.dp),
                    ) {
                        Text(
                            check.feedback(answers.toList(), state.moodPicked),
                            fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = Ink,
                        )
                    }

                    suggestionFor(check.id, vm)?.let { sug ->
                        Spacer(Modifier.height(12.dp))
                        Box(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Green)
                                .pressable { sug.action() }.padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(sug.label, fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Card)
                        }
                    }

                    if (heavy) {
                        Spacer(Modifier.height(12.dp))
                        Column(
                            Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                                .background(Orange.copy(alpha = 0.14f)).padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                "Heb je nu iemand nodig? Bel 113 (gratis, 24/7) of gebruik de Hulp-knop.",
                                fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = Ink,
                            )
                            Box(
                                Modifier.clip(RoundedCornerShape(14.dp)).background(Orange)
                                    .pressable { vm.closeSelfCheck(); vm.openPanic() }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                            ) {
                                Text("Open Hulp", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 13.sp, color = Card)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Text(
                        "Dit is geen medisch advies of diagnose.",
                        fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 11.5.sp, color = InkFaint,
                    )
                    Spacer(Modifier.height(12.dp))
                    Box(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFDCE8E1)).pressable { vm.closeSelfCheck() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("Sluiten", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 14.sp, color = InkSoft)
                    }
                }
            }
        }
    }
}

@Composable
private fun YesNoRow(value: Int, accent: Color, onPick: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        PillOption("Nee", value == 0, accent, Modifier.weight(1f)) { onPick(0) }
        PillOption("Ja", value == 1, accent, Modifier.weight(1f)) { onPick(1) }
    }
}

@Composable
private fun LikertRow(value: Int, accent: Color, onPick: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        (1..5).forEach { n ->
            val selected = value == n
            Box(
                Modifier.weight(1f).clip(CircleShape)
                    .background(if (selected) accent else Card)
                    .pressable { onPick(n) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("$n", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 15.sp, color = if (selected) Card else InkSoft)
            }
        }
    }
}

@Composable
private fun ScaleRow(value: Int, accent: Color, onPick: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Text("$value", fontFamily = Fredoka, fontWeight = FontWeight.Bold, fontSize = 30.sp, color = Ink, modifier = Modifier.align(Alignment.CenterHorizontally))
        Slider(
            value = value.toFloat(),
            onValueChange = { onPick(it.toInt()) },
            valueRange = 0f..10f,
            steps = 9,
            colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent),
        )
    }
}

@Composable
private fun PillOption(label: String, selected: Boolean, accent: Color, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(16.dp))
            .background(if (selected) accent else Card)
            .pressable { onClick() }
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 14.sp, color = if (selected) Card else InkSoft)
    }
}
