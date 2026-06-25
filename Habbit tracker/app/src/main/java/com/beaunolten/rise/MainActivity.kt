package com.beaunolten.rise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.beaunolten.rise.ui.theme.Bg
import com.beaunolten.rise.ui.theme.Fredoka
import com.beaunolten.rise.ui.theme.Green
import com.beaunolten.rise.ui.theme.RiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RiseTheme {
                Surface(color = Bg, modifier = Modifier.fillMaxSize()) {
                    // Placeholder probe — replaced by RiseApp() in Task 3.
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "Rise",
                            fontFamily = Fredoka,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 48.sp,
                            color = Green,
                        )
                    }
                }
            }
        }
    }
}
