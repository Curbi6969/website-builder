package com.rise.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rise.app.data.InspoAccent
import com.rise.app.data.RiseUiState
import com.rise.app.data.RoutineCatalog
import com.rise.app.data.RoutineCategory
import com.rise.app.ui.common.InspoCard
import com.rise.app.ui.common.pressable
import com.rise.app.ui.theme.Card
import com.rise.app.ui.theme.Fredoka
import com.rise.app.ui.theme.Green
import com.rise.app.ui.theme.GreenDark
import com.rise.app.ui.theme.Ink
import com.rise.app.ui.theme.InkSoft
import com.rise.app.ui.theme.Nunito
import com.rise.app.vm.RiseViewModel

private val ChipBorder = Color(0xFFCFE0D7)

private data class CardItem(
    val title: String,
    val illustration: Int,
    val accent: InspoAccent,
    val category: RoutineCategory,
    val onClick: () -> Unit,
)

/** Inspiratie tab — routine cards + non-diagnostic self-checks, filterable by category. */
@Composable
fun InspiratieScreen(state: RiseUiState, vm: RiseViewModel) {
    val items: List<CardItem> =
        (RoutineCatalog.routines.map { r ->
            CardItem(r.name, r.illustration, r.accent, r.category) { vm.openRoutine(r.id) }
        } + RoutineCatalog.selfChecks.map { c ->
            CardItem(c.name, c.illustration, c.accent, c.category) { vm.openSelfCheck(c.id) }
        }).let { all ->
            state.inspoCategory?.let { cat -> all.filter { it.category == cat } } ?: all
        }

    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(6.dp))
        Text("Inspiratie", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 24.sp, color = Ink)
        Text(
            "Routines en check-ins die werken",
            fontFamily = Nunito,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = InkSoft,
        )

        Spacer(Modifier.height(14.dp))

        // ---- category chips ----
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CategoryChip("Alles", state.inspoCategory == null) { vm.setInspoCategory(null) }
            RoutineCategory.values().forEach { cat ->
                CategoryChip(cat.label, state.inspoCategory == cat) { vm.setInspoCategory(cat) }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ---- card grid (2 columns; parent already scrolls) ----
        if (items.isEmpty()) {
            Text(
                "Nog niks in deze categorie.",
                fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = InkSoft,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items.chunked(2).forEach { rowItems ->
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { item ->
                            InspoCard(item.title, item.illustration, item.accent, item.onClick, Modifier.weight(1f))
                        }
                        if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, active: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (active) Green.copy(alpha = 0.15f) else Card)
            .then(if (active) Modifier else Modifier.border(1.dp, ChipBorder, RoundedCornerShape(16.dp)))
            .pressable { onClick() }
            .padding(horizontal = 14.dp, vertical = 9.dp),
    ) {
        Text(
            label,
            fontFamily = Nunito,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.5.sp,
            color = if (active) GreenDark else InkSoft,
            maxLines = 1,
        )
    }
}
