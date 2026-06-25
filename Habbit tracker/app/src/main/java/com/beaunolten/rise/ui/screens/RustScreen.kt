package com.beaunolten.rise.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.beaunolten.rise.data.RiseUiState
import com.beaunolten.rise.vm.RiseViewModel

// Stub — implemented in Task 6.
@Composable
fun RustScreen(state: RiseUiState, vm: RiseViewModel) {
    Text("Rust", modifier = Modifier.padding(top = 12.dp))
}
