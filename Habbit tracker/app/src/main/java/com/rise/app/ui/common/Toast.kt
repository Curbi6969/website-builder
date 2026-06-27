package com.rise.app.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rise.app.ui.theme.Card
import com.rise.app.ui.theme.Ink
import com.rise.app.ui.theme.Nunito

/** Affirmation toast, slides up + fades in, auto-dismissed by the ViewModel. */
@Composable
fun RiseToast(message: String?, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = message != null,
        enter = slideInVertically { it / 3 } + fadeIn(),
        exit = slideOutVertically { it / 3 } + fadeOut(),
        modifier = modifier,
    ) {
        Text(
            text = message ?: "",
            color = Card,
            fontFamily = Nunito,
            fontWeight = FontWeight.Black,
            fontSize = 14.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(max = 300.dp)
                .background(Ink, RoundedCornerShape(18.dp))
                .padding(horizontal = 22.dp, vertical = 14.dp),
        )
    }
}
