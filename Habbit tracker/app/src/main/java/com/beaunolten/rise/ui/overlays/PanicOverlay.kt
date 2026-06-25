package com.beaunolten.rise.ui.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beaunolten.rise.data.RiseUiState
import com.beaunolten.rise.ui.theme.Card
import com.beaunolten.rise.ui.theme.GreenDark
import com.beaunolten.rise.vm.RiseViewModel

// Stub — implemented in Tasks 9 & 10.
@Composable
fun PanicOverlay(state: RiseUiState, vm: RiseViewModel) {
    Box(Modifier.fillMaxSize().background(GreenDark), contentAlignment = Alignment.Center) {
        Text("Hulp", color = Card)
        Text(
            "Sluit",
            color = Card,
            modifier = Modifier.align(Alignment.TopEnd).padding(22.dp).clickable { vm.closePanic() },
        )
    }
}
