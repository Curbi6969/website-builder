package com.rise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rise.app.data.InspoAccent
import com.rise.app.data.PlayerContent
import com.rise.app.data.RiseDefaults
import com.rise.app.data.RiseUiState
import com.rise.app.data.RoutineCatalog
import com.rise.app.data.RoutineCategory
import com.rise.app.ui.common.InspoCard
import com.rise.app.ui.common.pressable
import com.rise.app.ui.theme.Card
import com.rise.app.ui.theme.Fredoka
import com.rise.app.ui.theme.Ink
import com.rise.app.ui.theme.InkSoft
import com.rise.app.ui.theme.Nunito
import com.rise.app.ui.theme.Teal
import com.rise.app.ui.theme.TealDark
import com.rise.app.voice.VoiceLab
import com.rise.app.vm.RiseViewModel

private val CardWidth = 158.dp

/** Accents cycled across the meditation cards (courses carry no accent of their own). */
private val courseAccents = listOf(
    InspoAccent.TEAL, InspoAccent.LAVENDER, InspoAccent.YELLOW, InspoAccent.GREEN, InspoAccent.CORAL,
)

/**
 * Inspiratie tab — the app's "inspire" hub. A featured card on top, then grouped
 * horizontal rows: routines per theme, meditations (open the player), life hacks and
 * self-checks. Routine/life-hack cards open [RoutineDetailOverlay] with "Voeg toe aan
 * mijn routines"; meditation cards open the timer player (merged here from the old Rust tab).
 */
@Composable
fun InspiratieScreen(state: RiseUiState, vm: RiseViewModel) {
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(6.dp))
        Text("Inspiratie", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = Ink)
        Text(
            "Technieken, routines en check-ins die werken",
            fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = InkSoft,
        )

        Spacer(Modifier.height(16.dp))

        // ---- featured: urge surfing (carried over from Rust) ----
        FeaturedCard(
            tag = "AANBEVOLEN BIJ DRANG",
            title = "Urge surfen",
            body = "Rij de golf uit. Hij komt op, piekt, en zakt — altijd.",
            cta = "▶ Start · 8 min",
            emoji = "🌊",
        ) { vm.openUrgeSurf() }

        Spacer(Modifier.height(22.dp))

        // ---- grouped routine sections ----
        RoutineSection("Bij drang", "Als de golf opkomt", RoutineCategory.DRANG, vm)
        RoutineSection("Life hacks", "Kleine slimme zetten", RoutineCategory.LIFEHACK, vm)
        RoutineSection("Zacht voor jezelf", "Zelfzorg & zelfwaardering", RoutineCategory.ZELFZORG, vm)
        RoutineSection("Sterke ochtend", "Begin met intentie", RoutineCategory.OCHTEND, vm)
        RoutineSection("Beter slapen", "Zacht afsluiten", RoutineCategory.SLAAP, vm)

        // ---- meditations (open the timer player) ----
        SectionHeader("Meditaties", "Even tot rust komen")
        CardRow {
            RiseDefaults.courses.forEachIndexed { i, course ->
                InspoCard(
                    title = course.title,
                    illustration = 0,
                    accent = courseAccents[i % courseAccents.size],
                    onClick = { vm.openPlayer(PlayerContent(course.title, course.sub, course.icon, course.min)) },
                    modifier = Modifier.width(CardWidth),
                    emoji = course.icon,
                )
            }
        }

        // ---- non-diagnostic self-checks ----
        SectionHeader("Check even in", "Hoe gaat het nu echt?")
        CardRow {
            RoutineCatalog.selfChecks.forEach { check ->
                InspoCard(
                    title = check.name,
                    illustration = check.illustration,
                    accent = check.accent,
                    onClick = { vm.openSelfCheck(check.id) },
                    modifier = Modifier.width(CardWidth),
                )
            }
        }

        // TEMPORARY on-device voice smoke test — remove once the real journal UI lands.
        VoiceLab()

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RoutineSection(title: String, subtitle: String, category: RoutineCategory, vm: RiseViewModel) {
    val routines = RoutineCatalog.routines.filter { it.category == category }
    if (routines.isEmpty()) return
    SectionHeader(title, subtitle)
    CardRow {
        routines.forEach { r ->
            InspoCard(
                title = r.name,
                illustration = r.illustration,
                accent = r.accent,
                onClick = { vm.openRoutine(r.id) },
                modifier = Modifier.width(CardWidth),
                emoji = r.steps.firstOrNull()?.icon ?: "",
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(Modifier.padding(top = 4.dp, bottom = 12.dp)) {
        Text(title, fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = Ink)
        Text(subtitle, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = InkSoft)
    }
}

/** A horizontally-scrolling row of cards. The parent screen scrolls vertically. */
@Composable
private fun CardRow(content: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        content()
    }
}

@Composable
private fun FeaturedCard(
    tag: String,
    title: String,
    body: String,
    cta: String,
    emoji: String,
    onClick: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .shadow(14.dp, RoundedCornerShape(28.dp), clip = false)
            .background(Brush.linearGradient(listOf(Teal, TealDark)), RoundedCornerShape(28.dp))
            .clip(RoundedCornerShape(28.dp))
            .pressable { onClick() }
            .padding(22.dp),
    ) {
        Text(emoji, fontSize = 90.sp, color = Card.copy(alpha = 0.2f), modifier = Modifier.align(Alignment.TopEnd))
        Column {
            Text(tag, fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Card.copy(alpha = 0.9f))
            Text(title, fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, color = Card, modifier = Modifier.padding(top = 4.dp))
            Text(
                body,
                fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Card.copy(alpha = 0.95f),
                modifier = Modifier.widthIn(max = 230.dp).padding(top = 2.dp),
            )
            Text(
                cta,
                fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 13.sp, color = Card,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .background(Card.copy(alpha = 0.25f), RoundedCornerShape(30.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
    }
}
