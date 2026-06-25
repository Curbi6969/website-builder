package com.beaunolten.rise.ui.overlays

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beaunolten.rise.data.RiseUiState
import com.beaunolten.rise.ui.theme.Card
import com.beaunolten.rise.vm.RiseViewModel

// Stub — implemented in Task 10 (bubbles, grounding, tap-green).
@Composable
fun PanicGames(state: RiseUiState, vm: RiseViewModel) {
    Column(Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Text("Kies je afleiding", color = Card)
        BackPill { vm.panicBack() }
    }
}
