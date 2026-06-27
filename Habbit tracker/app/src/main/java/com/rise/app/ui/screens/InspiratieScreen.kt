package com.rise.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rise.app.data.RiseUiState
import com.rise.app.ui.theme.Fredoka
import com.rise.app.ui.theme.Ink
import com.rise.app.ui.theme.InkSoft
import com.rise.app.ui.theme.Nunito
import com.rise.app.vm.RiseViewModel

/** Inspiratie tab — routine cards + non-diagnostic self-checks. Grid added in Task 5. */
@Composable
fun InspiratieScreen(state: RiseUiState, vm: RiseViewModel) {
    Column(Modifier.fillMaxWidth()) {
        Spacer(Modifier.height(6.dp))
        Text(
            "Inspiratie",
            fontFamily = Fredoka,
            fontWeight = FontWeight.SemiBold,
            fontSize = 24.sp,
            color = Ink,
        )
        Text(
            "Routines en check-ins die werken",
            fontFamily = Nunito,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 13.sp,
            color = InkSoft,
        )
    }
}
