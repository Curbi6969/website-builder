package com.beaunolten.rise.ui.overlays

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.beaunolten.rise.data.ActivityDef
import com.beaunolten.rise.data.RiseDefaults
import com.beaunolten.rise.data.RiseUiState
import com.beaunolten.rise.ui.common.cardSurface
import com.beaunolten.rise.ui.theme.Bg
import com.beaunolten.rise.ui.theme.Card
import com.beaunolten.rise.ui.theme.Fredoka
import com.beaunolten.rise.ui.theme.Green
import com.beaunolten.rise.ui.theme.GreenDark
import com.beaunolten.rise.ui.theme.GreenDeep
import com.beaunolten.rise.ui.theme.Ink
import com.beaunolten.rise.ui.theme.InkSoft
import com.beaunolten.rise.ui.theme.Nunito
import com.beaunolten.rise.ui.theme.Purple
import com.beaunolten.rise.ui.theme.PurpleSoft
import com.beaunolten.rise.vm.RiseViewModel

@Composable
fun BoredSheet(state: RiseUiState, vm: RiseViewModel) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val sheetMax = maxHeight * 0.88f

        Box(Modifier.fillMaxSize().background(Color(0x66142819)).clickable { vm.closeBored() })

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
                // grabber
                Box(
                    Modifier.padding(vertical = 6.dp).size(width = 44.dp, height = 5.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFFCDDDD4)).align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.height(8.dp))
                Text("Vul de leegte 🎯", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 23.sp, color = Ink)
                Text("Verveling is de #1 trigger. Doe gewoon één ding — klein telt.", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = InkSoft, modifier = Modifier.padding(top = 2.dp))

                // mini-goal
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.linearGradient(listOf(Green, GreenDark)))
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text("⭐", fontSize = 30.sp)
                    Column(Modifier.weight(1f)) {
                        Text("MINI-DOEL VAN NU", fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Card.copy(alpha = 0.9f))
                        Text("Drink een groot glas water", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 16.sp, color = Card)
                    }
                    Text(
                        "Doe 't!",
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = GreenDeep,
                        modifier = Modifier.clip(RoundedCornerShape(14.dp)).background(Card).clickable { vm.doGoal() }.padding(horizontal = 14.dp, vertical = 10.dp),
                    )
                }

                Text("Snelle acties", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Ink, modifier = Modifier.padding(top = 16.dp))

                Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    RiseDefaults.activities.chunked(2).forEach { rowItems ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowItems.forEach { activity ->
                                ActivityCard(activity, Modifier.weight(1f)) { vm.doActivity() }
                            }
                            if (rowItems.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }

                // journal CTA
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(listOf(Purple, PurpleSoft)))
                        .clickable { vm.goJournalFromBored() }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("🌳", fontSize = 26.sp)
                    Column(Modifier.weight(1f)) {
                        Text("Schrijf 3 dingen waar je dankbaar voor bent", fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Card)
                        Text("Naar je boomhut →", fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Card.copy(alpha = 0.9f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityCard(activity: ActivityDef, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier
            .cardSurface(RoundedCornerShape(20.dp), elevation = 5.dp)
            .clickable { onClick() }
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(activity.icon, fontSize = 28.sp)
        Text(activity.label, fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 14.sp, color = Ink)
    }
}
