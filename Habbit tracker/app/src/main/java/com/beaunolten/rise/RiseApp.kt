package com.beaunolten.rise

import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.beaunolten.rise.data.Panic
import com.beaunolten.rise.data.Tab
import com.beaunolten.rise.ui.common.RiseToast
import com.beaunolten.rise.ui.common.StatusBar
import com.beaunolten.rise.ui.overlays.BoredSheet
import com.beaunolten.rise.ui.overlays.PanicOverlay
import com.beaunolten.rise.ui.overlays.PlayerOverlay
import com.beaunolten.rise.ui.overlays.SettingsOverlay
import com.beaunolten.rise.ui.screens.HomeScreen
import com.beaunolten.rise.ui.screens.MoodScreen
import com.beaunolten.rise.ui.screens.RustScreen
import com.beaunolten.rise.ui.screens.StatsScreen
import com.beaunolten.rise.ui.theme.Bg
import com.beaunolten.rise.ui.theme.Card
import com.beaunolten.rise.ui.theme.Fredoka
import com.beaunolten.rise.ui.theme.Green
import com.beaunolten.rise.ui.theme.NavIdle
import com.beaunolten.rise.ui.theme.Nunito
import com.beaunolten.rise.ui.theme.OrangeA
import com.beaunolten.rise.ui.theme.OrangeB
import com.beaunolten.rise.vm.RiseViewModel

@Composable
fun RiseApp(vm: RiseViewModel = viewModel()) {
    val state by vm.state.collectAsState()

    Box(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(Modifier.fillMaxSize()) {
            StatusBar()
            Box(Modifier.weight(1f).fillMaxWidth()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 110.dp),
                ) {
                    when (state.tab) {
                        Tab.HOME -> HomeScreen(state, vm)
                        Tab.RUST -> RustScreen(state, vm)
                        Tab.MOOD -> MoodScreen(state, vm)
                        Tab.STATS -> StatsScreen(state, vm)
                    }
                }
            }
        }

        BottomNav(active = state.tab, onTab = vm::goTab, modifier = Modifier.align(Alignment.BottomCenter))
        PanicFab(
            onClick = vm::openPanic,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 46.dp),
        )

        // Overlays — drawn over nav/FAB, lowest z first.
        if (state.bored) BoredSheet(state, vm)
        if (state.showSettings) SettingsOverlay(state, vm)
        if (state.panic != Panic.NONE) PanicOverlay(state, vm)
        if (state.player != null) PlayerOverlay(state, vm)

        RiseToast(state.toast, Modifier.align(Alignment.BottomCenter).padding(bottom = 130.dp))
    }
}

@Composable
private fun BottomNav(active: Tab, onTab: (Tab) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .height(86.dp)
            .background(Card)
            .drawBehind {
                drawRect(
                    color = Bg,
                    topLeft = androidx.compose.ui.geometry.Offset(0f, 0f),
                    size = androidx.compose.ui.geometry.Size(size.width, 1.dp.toPx()),
                )
            }
            .padding(top = 12.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.Top,
    ) {
        NavItem("⌂", "Start", active == Tab.HOME) { onTab(Tab.HOME) }
        NavItem("🧘", "Rust", active == Tab.RUST) { onTab(Tab.RUST) }
        Spacer(Modifier.width(64.dp))
        NavItem("☺", "Stemming", active == Tab.MOOD) { onTab(Tab.MOOD) }
        NavItem("▥", "Cijfers", active == Tab.STATS) { onTab(Tab.STATS) }
    }
}

@Composable
private fun NavItem(icon: String, label: String, active: Boolean, onClick: () -> Unit) {
    val color = if (active) Green else NavIdle
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }.padding(horizontal = 4.dp),
    ) {
        Text(icon, fontSize = 20.sp, color = color)
        Text(
            label,
            fontFamily = Nunito,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            color = color,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun PanicFab(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "fab")
    val ringScale by infinite.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseOut), RepeatMode.Restart),
        label = "ringScale",
    )
    val ringAlpha by infinite.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2200, easing = EaseOut), RepeatMode.Restart),
        label = "ringAlpha",
    )

    Box(
        modifier.size(70.dp).clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        // Pulsing ring
        Box(
            Modifier
                .size(70.dp)
                .graphicsLayer { scaleX = ringScale; scaleY = ringScale; alpha = ringAlpha }
                .background(OrangeB, CircleShape),
        )
        // Main button
        Box(
            Modifier
                .size(70.dp)
                .background(Brush.linearGradient(listOf(OrangeA, OrangeB)), CircleShape)
                .border(4.dp, Card, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.width(5.dp).height(17.dp).background(Card, RoundedCornerShape(3.dp)))
                    Box(Modifier.width(5.dp).height(17.dp).background(Card, RoundedCornerShape(3.dp)))
                }
                Text(
                    "Hulp",
                    fontFamily = Fredoka,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = Card,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }
    }
}
