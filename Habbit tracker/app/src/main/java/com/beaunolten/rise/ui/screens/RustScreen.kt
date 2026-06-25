package com.beaunolten.rise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import com.beaunolten.rise.data.Course
import com.beaunolten.rise.data.PlayerContent
import com.beaunolten.rise.data.RiseDefaults
import com.beaunolten.rise.data.RiseUiState
import com.beaunolten.rise.ui.common.cardSurface
import com.beaunolten.rise.ui.theme.Card
import com.beaunolten.rise.ui.theme.Fredoka
import com.beaunolten.rise.ui.theme.Ink
import com.beaunolten.rise.ui.theme.InkFaint
import com.beaunolten.rise.ui.theme.InkSoft
import com.beaunolten.rise.ui.theme.Nunito
import com.beaunolten.rise.ui.theme.Teal
import com.beaunolten.rise.ui.theme.TealDark
import com.beaunolten.rise.vm.RiseViewModel

@Composable
fun RustScreen(state: RiseUiState, vm: RiseViewModel) {
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(8.dp))
        Text("Rust & meditatie", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, color = Ink)
        Text("Adem. Reset. Jij hebt de leiding.", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = InkSoft)

        Spacer(Modifier.height(16.dp))

        // featured: urge surfing
        Box(
            Modifier
                .fillMaxWidth()
                .shadow(14.dp, RoundedCornerShape(28.dp), clip = false)
                .background(Brush.linearGradient(listOf(Teal, TealDark)), RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .clickable { vm.openUrgeSurf() }
                .padding(22.dp),
        ) {
            Text("🌊", fontSize = 90.sp, color = Card.copy(alpha = 0.2f), modifier = Modifier.align(Alignment.TopEnd))
            Column {
                Text("AANBEVOLEN BIJ DRANG", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Card.copy(alpha = 0.9f))
                Text("Urge surfen", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, color = Card, modifier = Modifier.padding(top = 4.dp))
                Text(
                    "Rij de golf uit. Hij komt op, piekt, en zakt — altijd.",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Card.copy(alpha = 0.95f),
                    modifier = Modifier.widthIn(max = 230.dp).padding(top = 2.dp),
                )
                Text(
                    "▶ Start · 8 min",
                    fontFamily = Nunito,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = Card,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .background(Card.copy(alpha = 0.25f), RoundedCornerShape(30.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }

        Spacer(Modifier.height(20.dp))
        Text("Routines", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = Ink)
        Spacer(Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            RiseDefaults.courses.forEach { course ->
                CourseRow(course) {
                    vm.openPlayer(PlayerContent(course.title, course.sub, course.icon, course.min))
                }
            }
        }
    }
}

@Composable
private fun CourseRow(course: Course, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .cardSurface(RoundedCornerShape(22.dp), elevation = 5.dp)
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(52.dp).background(course.bg, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(course.icon, fontSize = 24.sp)
        }
        Column(Modifier.weight(1f)) {
            Text(course.title, fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Ink)
            Text(course.sub, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = InkSoft)
        }
        Text(course.len, fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = InkFaint)
    }
}
