package com.rise.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rise.app.data.RiseDefaults
import com.rise.app.data.RiseRepository
import com.rise.app.notif.Reminders
import com.rise.app.ui.common.pressable
import com.rise.app.ui.theme.Bg
import com.rise.app.ui.theme.Card
import com.rise.app.ui.theme.Fredoka
import com.rise.app.ui.theme.Green
import com.rise.app.ui.theme.Ink
import com.rise.app.ui.theme.InkSoft
import com.rise.app.ui.theme.Nunito
import kotlinx.coroutines.launch
import java.time.LocalDate

private val focusOptions = listOf("Alcohol", "Roken", "Gokken", "Drugs", "Scrollen", "Porno", "Anders")
private val soberOptions = listOf("Vandaag" to 0L, "1 week" to 7L, "2 weken" to 14L, "1 maand" to 30L, "3 maanden" to 90L, "1 jaar" to 365L)
private val timeOptions = listOf("08:00", "12:00", "18:00", "20:00", "21:00", "22:00")
private const val LAST_STEP = 5

/** One-time personalisation flow shown after login until the profile is onboarded. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WizardScreen(onDone: () -> Unit) {
    val repo = remember { RiseRepository() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var step by remember { mutableStateOf(0) }
    var saving by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var focus by remember { mutableStateOf<String?>(null) }
    var soberDays by remember { mutableStateOf(0L) }
    val reasons = remember { mutableStateListOf(*RiseDefaults.reasons.toTypedArray()) }
    val triggers = remember { mutableStateListOf<String>() }
    var time by remember { mutableStateOf("21:00") }

    val canNext = when (step) {
        0 -> name.isNotBlank()
        1 -> focus != null
        4 -> triggers.isNotEmpty()
        else -> true
    }

    Column(
        Modifier.fillMaxSize().background(Bg).padding(horizontal = 26.dp),
    ) {
        Spacer(Modifier.height(40.dp))
        // progress dots
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            for (i in 0..LAST_STEP) {
                Box(
                    Modifier.height(6.dp).weight(1f).clip(RoundedCornerShape(3.dp))
                        .background(if (i <= step) Green else Color(0xFFE2E9E4)),
                )
            }
        }
        Spacer(Modifier.height(28.dp))

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            when (step) {
                0 -> {
                    Heading("Welkom bij Rise 🌱", "Laten we 'm van jou maken. Hoe heet je?")
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        singleLine = true,
                        label = { Text("Je voornaam", fontFamily = Nunito) },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }
                1 -> {
                    Heading("Waar wil je vanaf?", "Dit blijft tussen ons. Kies wat past.")
                    Chips(focusOptions, selected = { it == focus }) { focus = it }
                }
                2 -> {
                    Heading("Sinds wanneer ben je clean?", "Zo telt je streak vanaf de echte dag.")
                    Chips(soberOptions.map { it.first }, selected = { soberOptions.first { o -> o.first == it }.second == soberDays }) { label ->
                        soberDays = soberOptions.first { it.first == label }.second
                    }
                }
                3 -> {
                    Heading("Waarom doe je dit?", "Je eigen woorden, je leest ze terug bij drang.")
                    reasons.forEachIndexed { i, r ->
                        OutlinedTextField(
                            value = r,
                            onValueChange = { reasons[i] = it },
                            label = { Text("Reden ${i + 1}", fontFamily = Nunito) },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )
                    }
                }
                4 -> {
                    Heading("Wat zijn je triggers?", "Zo weet je waar je sterk moet staan. Meerdere mag.")
                    Chips(RiseDefaults.triggers.map { "${it.icon} ${it.label}" }, selected = { label ->
                        triggers.contains(label.substringAfter(" "))
                    }) { label ->
                        val key = label.substringAfter(" ")
                        if (triggers.contains(key)) triggers.remove(key) else triggers.add(key)
                    }
                }
                else -> {
                    Heading("Dagelijkse check-in", "Een seintje per dag: 'is het je gelukt?' Houdt je scherp.")
                    Chips(timeOptions, selected = { it == time }) { time = it }
                    Text(
                        "Je kunt dit later aanpassen. We vragen zo om toestemming voor notificaties.",
                        fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.5.sp,
                        color = InkSoft, modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }
        }

        // nav
        Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (step > 0) {
                Pill("Terug", bg = Color(0xFFEFF2F0), fg = Ink, modifier = Modifier.weight(1f)) { step-- }
            }
            Pill(
                if (step == LAST_STEP) (if (saving) "Bezig..." else "Beginnen 🚀") else "Volgende",
                bg = if (canNext && !saving) Green else Green.copy(alpha = 0.4f),
                fg = Card,
                modifier = Modifier.weight(2f),
            ) {
                if (!canNext || saving) return@Pill
                if (step < LAST_STEP) step++ else {
                    saving = true
                    scope.launch {
                        runCatching {
                            repo.saveOnboarding(
                                name = name.trim(),
                                focus = focus,
                                soberSince = LocalDate.now().minusDays(soberDays),
                                reasons = reasons.map { it.trim() }.filter { it.isNotEmpty() },
                                triggers = triggers.toList(),
                                reminderTime = time,
                            )
                        }
                        Reminders.schedule(context, time)
                        onDone()
                    }
                }
            }
        }
    }
}

@Composable
private fun Heading(title: String, sub: String) {
    Text(title, fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, color = Ink)
    Text(sub, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = InkSoft, modifier = Modifier.padding(top = 6.dp))
    Spacer(Modifier.height(8.dp))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Chips(options: List<String>, selected: (String) -> Boolean, onPick: (String) -> Unit) {
    FlowRow(
        Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        options.forEach { opt ->
            val on = selected(opt)
            Box(
                Modifier.clip(RoundedCornerShape(14.dp))
                    .background(if (on) Green else Card)
                    .border(1.5.dp, if (on) Green else Color(0xFFD8E2DC), RoundedCornerShape(14.dp))
                    .pressable { onPick(opt) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
            ) {
                Text(opt, fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 13.5.sp, color = if (on) Card else Ink)
            }
        }
    }
}

@Composable
private fun Pill(text: String, bg: Color, fg: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier.clip(RoundedCornerShape(16.dp)).background(bg).pressable { onClick() }.padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 15.sp, color = fg, textAlign = TextAlign.Center)
    }
}
