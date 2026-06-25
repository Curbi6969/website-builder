package com.beaunolten.rise.ui.overlays

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.beaunolten.rise.data.RiseUiState
import com.beaunolten.rise.ui.theme.Bg
import com.beaunolten.rise.vm.RiseViewModel

// Stub — implemented in Task 11.
@Composable
fun BoredSheet(state: RiseUiState, vm: RiseViewModel) {
    Box(Modifier.fillMaxSize().background(Color(0x66142819)).clickable { vm.closeBored() }) {
        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(280.dp)
                .background(Bg, RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .padding(22.dp),
        ) {
            Text("Vul de leegte 🎯")
        }
    }
}
