package com.rise.app

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.collectAsState
import com.rise.app.data.Game
import com.rise.app.data.Panic
import com.rise.app.data.Tab
import com.rise.app.ui.common.RiseToast
import com.rise.app.ui.overlays.BoredSheet
import com.rise.app.ui.overlays.CheckinOverlay
import com.rise.app.ui.overlays.PanicOverlay
import com.rise.app.ui.overlays.PlayerOverlay
import com.rise.app.ui.overlays.RoutineDetailOverlay
import com.rise.app.ui.overlays.SelfCheckOverlay
import com.rise.app.ui.overlays.SettingsOverlay
import com.rise.app.ui.screens.HomeScreen
import com.rise.app.ui.screens.InspiratieScreen
import com.rise.app.ui.screens.MoodScreen
import com.rise.app.ui.screens.StatsScreen
import com.rise.app.ui.theme.Bg
import com.rise.app.ui.theme.Card
import com.rise.app.ui.theme.Fredoka
import com.rise.app.ui.theme.Green
import com.rise.app.ui.theme.GreenDark
import com.rise.app.ui.theme.NavIdle
import com.rise.app.ui.theme.Nunito
import com.rise.app.ui.theme.OrangeA
import com.rise.app.ui.theme.OrangeB
import com.rise.app.vm.RiseViewModel

@Composable
fun RiseApp(vm: RiseViewModel = viewModel()) {
    val state by vm.state.collectAsState()

    // A tapped reminder notification routes here to open the daily check-in.
    LaunchedEffect(CheckinBus.pending) {
        if (CheckinBus.pending) {
            vm.openCheckin()
            CheckinBus.pending = false
        }
    }

    // No nav graph: handle the system back gesture/button ourselves so it dismisses
    // the top overlay (or steps back inside the panic flow), then falls back to HOME,
    // and only closes the app from HOME with nothing open. Z-order: player → panic →
    // settings → bored.
    BackHandler(
        enabled = state.showCheckin || state.player != null || state.panic != Panic.NONE ||
            state.showSettings || state.bored || state.openRoutine != null ||
            state.openSelfCheck != null || state.tab != Tab.HOME,
    ) {
        when {
            state.showCheckin -> vm.closeCheckin()
            state.player != null -> vm.closePlayer()
            state.panic != Panic.NONE -> when {
                state.panic == Panic.GAME && state.game != Game.NONE -> vm.gameBack()
                state.panic == Panic.MENU -> vm.closePanic()
                else -> vm.panicBack()
            }
            state.showSettings -> vm.closeSettings()
            state.bored -> vm.closeBored()
            state.openRoutine != null -> vm.closeRoutine()
            state.openSelfCheck != null -> vm.closeSelfCheck()
            state.tab != Tab.HOME -> vm.goTab(Tab.HOME)
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF1FAF5), Bg, Color(0xFFE4F1EA))))
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 110.dp),
                ) {
                    AnimatedContent(
                        targetState = state.tab,
                        transitionSpec = {
                            // Sequenced (not overlapping) so the two screens' card
                            // shadows never composite together mid-transition.
                            fadeIn(tween(220, delayMillis = 90, easing = EaseOutQuart)) togetherWith
                                fadeOut(tween(90, easing = EaseOutQuart))
                        },
                        label = "tab",
                    ) { tab ->
                        when (tab) {
                            Tab.HOME -> HomeScreen(state, vm)
                            Tab.MOOD -> MoodScreen(state, vm)
                            Tab.STATS -> StatsScreen(state, vm)
                            Tab.INSPIRATIE -> InspiratieScreen(state, vm)
                        }
                    }
                }
            }
        }

        BottomNav(
            active = state.tab,
            onPanic = vm::openPanic,
            onTab = vm::goTab,
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        // Overlays, drawn over nav/FAB, lowest z first.
        if (state.bored) BoredSheet(state, vm)
        if (state.openRoutine != null) RoutineDetailOverlay(state, vm)
        if (state.openSelfCheck != null) SelfCheckOverlay(state, vm)
        if (state.showSettings) SettingsOverlay(state, vm)
        if (state.panic != Panic.NONE) PanicOverlay(state, vm)
        if (state.player != null) PlayerOverlay(state, vm)
        if (state.showCheckin) CheckinOverlay(state, vm)

        RiseToast(state.toast, Modifier.align(Alignment.BottomCenter).padding(bottom = 130.dp))
    }
}

/**
 * Floating expandable-tabs pill: icons by default, the active tab expands to show its label.
 * The Hulp (panic) button is docked in the centre of the row so it can never overlap a tab.
 */
@Composable
private fun BottomNav(active: Tab, onPanic: () -> Unit, onTab: (Tab) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 14.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Card)
            .border(1.dp, Color(0x141B3A2E), RoundedCornerShape(28.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Two equal-weight halves keep the central Hulp button dead-centre with an odd tab count.
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ExpandableTab(Icons.Rounded.Home, "Start", active == Tab.HOME) { onTab(Tab.HOME) }
            ExpandableTab(Icons.Rounded.AutoAwesome, "Inspiratie", active == Tab.INSPIRATIE) { onTab(Tab.INSPIRATIE) }
        }
        HulpButton(onPanic)
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ExpandableTab(Icons.Rounded.Mood, "Stemming", active == Tab.MOOD) { onTab(Tab.MOOD) }
        }
    }
}

@Composable
private fun ExpandableTab(icon: ImageVector, label: String, active: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        if (active) Green.copy(alpha = 0.15f) else Color.Transparent,
        tween(300, easing = EaseOutQuart), label = "navBg",
    )
    val fg by animateColorAsState(if (active) GreenDark else NavIdle, tween(300, easing = EaseOutQuart), label = "navFg")
    Row(
        Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }
            .padding(horizontal = 10.dp, vertical = 9.dp)
            .animateContentSize(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(if (active) 6.dp else 0.dp),
    ) {
        Icon(icon, contentDescription = label, tint = fg, modifier = Modifier.size(22.dp))
        if (active) {
            Text(
                label,
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.5.sp,
                color = fg,
                maxLines = 1,
            )
        }
    }
}

/** Docked panic button, orange, centre of the nav, gently breathing. */
@Composable
private fun HulpButton(onClick: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "hulp")
    val pulse by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(1300), RepeatMode.Reverse),
        label = "pulse",
    )
    Box(
        Modifier
            .size(54.dp)
            .graphicsLayer { scaleX = pulse; scaleY = pulse }
            .background(Brush.linearGradient(listOf(OrangeA, OrangeB)), CircleShape)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onClick() }
            .semantics { contentDescription = "Hulp bij drang"; role = Role.Button },
        contentAlignment = Alignment.Center,
    ) {
        Text("Hulp", fontFamily = Fredoka, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Card)
    }
}

