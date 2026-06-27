package com.rise.app.ui.overlays

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rise.app.data.RiseDefaults
import com.rise.app.data.RiseUiState
import com.rise.app.ui.common.cardSurface
import com.rise.app.ui.common.pressable
import com.rise.app.ui.theme.Bg
import com.rise.app.ui.theme.Card
import com.rise.app.ui.theme.Fredoka
import com.rise.app.ui.theme.Green
import com.rise.app.ui.theme.Ink
import com.rise.app.ui.theme.InkFaint
import com.rise.app.ui.theme.InkSoft
import com.rise.app.ui.theme.Nunito
import com.rise.app.vm.RiseViewModel

private val Underline = Color(0xFFE6EFE9)
private val SwitchOff = Color(0xFFCFD9D3)
private val RemoveBg = Color(0xFFFBEAE6)
private val RemoveInk = Color(0xFFE0603C)
private val AddBg = Color(0xFFE3F2E9)
private val AddBorder = Color(0xFF9FD3BA)

@Composable
fun SettingsOverlay(state: RiseUiState, vm: RiseViewModel) {
    Column(Modifier.fillMaxSize().background(Bg)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier.size(38.dp).cardSurface(CircleShape, elevation = 6.dp).pressable { vm.closeSettings() },
                contentAlignment = Alignment.Center,
            ) { Text("‹", fontSize = 20.sp, color = Ink) }
            Text("Instellingen", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 22.sp, color = Ink)
        }

        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 4.dp, bottom = 30.dp),
        ) {
            // ---- name ----
            Column(Modifier.fillMaxWidth().cardSurface(RoundedCornerShape(22.dp)).padding(18.dp)) {
                Text("JOUW NAAM", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 12.sp, color = InkFaint)
                BasicTextField(
                    value = state.userName,
                    onValueChange = vm::setName,
                    singleLine = true,
                    textStyle = TextStyle(fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, color = Ink),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
                Box(Modifier.fillMaxWidth().padding(top = 4.dp).height(2.dp).background(Underline))
            }

            // ---- reminders ----
            Text("Herinneringen", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Ink, modifier = Modifier.padding(top = 18.dp))
            Text("Ochtend, avond, en als je 't even kwijt bent.", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = InkSoft)
            Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RiseDefaults.reminders.forEach { def ->
                    val on = state.reminders[def.key] ?: false
                    Row(
                        Modifier.fillMaxWidth().cardSurface(RoundedCornerShape(20.dp), elevation = 4.dp).padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(def.icon, fontSize = 24.sp)
                        Column(Modifier.weight(1f)) {
                            Text(def.label, fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 14.5.sp, color = Ink)
                            Text(def.sub, fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = InkFaint)
                        }
                        ToggleSwitch(on) { vm.toggleReminder(def.key) }
                    }
                }
            }

            // ---- reasons ----
            Text("Waarom ik begon ❤️", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Ink, modifier = Modifier.padding(top = 18.dp))
            Text("Jouw redenen. Deze zie je terug bij de Hulp-knop.", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = InkSoft)
            Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.reasons.forEachIndexed { index, reason ->
                    Row(
                        Modifier.fillMaxWidth().cardSurface(RoundedCornerShape(20.dp), elevation = 4.dp).padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(Modifier.weight(1f)) {
                            if (reason.isEmpty()) {
                                Text("Schrijf je reden...", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = InkFaint)
                            }
                            BasicTextField(
                                value = reason,
                                onValueChange = { vm.editReason(index, it) },
                                textStyle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Ink),
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        Box(
                            Modifier.size(28.dp).clip(CircleShape).background(RemoveBg).pressable { vm.removeReason(index) },
                            contentAlignment = Alignment.Center,
                        ) { Text("✕", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 13.sp, color = RemoveInk) }
                    }
                }
                Text(
                    "+ Reden toevoegen",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Green,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(AddBg)
                        .drawBehind {
                            drawRoundRect(
                                color = AddBorder,
                                cornerRadius = CornerRadius(20.dp.toPx()),
                                style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))),
                            )
                        }
                        .pressable { vm.addReason() }
                        .padding(14.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ToggleSwitch(on: Boolean, onClick: () -> Unit) {
    val knobX by animateDpAsState(if (on) 22.dp else 2.dp, label = "knobX")
    Box(
        Modifier.size(width = 48.dp, height = 28.dp).clip(RoundedCornerShape(16.dp)).background(if (on) Green else SwitchOff).clickable { onClick() },
    ) {
        Box(
            Modifier.padding(top = 2.dp).offset(x = knobX).size(24.dp).clip(CircleShape).background(Card),
        )
    }
}
