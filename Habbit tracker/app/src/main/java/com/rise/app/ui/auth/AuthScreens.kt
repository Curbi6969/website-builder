package com.rise.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rise.app.RiseApp
import com.rise.app.data.RiseRepository
import com.rise.app.data.supabase
import com.rise.app.ui.onboarding.WizardScreen
import com.rise.app.ui.theme.Bg
import com.rise.app.ui.theme.Card
import com.rise.app.ui.theme.Fredoka
import com.rise.app.ui.theme.Green
import com.rise.app.ui.theme.Ink
import com.rise.app.ui.theme.InkSoft
import com.rise.app.ui.theme.Nunito
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.exceptions.HttpRequestException
import kotlinx.coroutines.launch

/** Shows the login screen until the user has a valid session, then the app. */
@Composable
fun AuthGate() {
    val status by supabase.auth.sessionStatus.collectAsState()
    when (status) {
        is SessionStatus.Authenticated -> {
            var onboarded by remember { mutableStateOf<Boolean?>(null) }
            LaunchedEffect(Unit) {
                onboarded = runCatching { RiseRepository().isOnboarded() }.getOrDefault(true)
            }
            when (onboarded) {
                null -> LoadingScreen()
                true -> RiseApp()
                else -> WizardScreen(onDone = { onboarded = true })
            }
        }
        SessionStatus.Initializing -> LoadingScreen()
        else -> LoginScreen()
    }
}

@Composable
private fun LoadingScreen() {
    Box(Modifier.fillMaxSize().background(Bg), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Green)
    }
}

private enum class Step { EMAIL, CODE }

@Composable
private fun LoginScreen() {
    val scope = rememberCoroutineScope()
    var step by remember { mutableStateOf(Step.EMAIL) }
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("🌱", fontSize = 48.sp)
        Text(
            "Welkom bij Rise",
            fontFamily = Fredoka,
            fontWeight = FontWeight.SemiBold,
            fontSize = 28.sp,
            color = Ink,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            if (step == Step.EMAIL) "Log in met je e-mail, je krijgt een code."
            else "Vul de 6-cijferige code in die we naar $email stuurden.",
            fontFamily = Nunito,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = InkSoft,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )

        Spacer24()

        if (step == Step.EMAIL) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; error = null },
                singleLine = true,
                label = { Text("E-mailadres", fontFamily = Nunito) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6) code = it.filter(Char::isDigit); error = null },
                singleLine = true,
                label = { Text("Code", fontFamily = Nunito) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        if (error != null) {
            Text(error!!, fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFD15B4A), modifier = Modifier.padding(top = 10.dp))
        }

        Spacer24()

        CtaButton(
            label = if (step == Step.EMAIL) "Stuur code" else "Inloggen",
            enabled = !loading && (if (step == Step.EMAIL) email.contains("@") else code.length == 6),
            loading = loading,
        ) {
            scope.launch {
                loading = true
                error = null
                try {
                    if (step == Step.EMAIL) {
                        supabase.auth.signInWith(OTP) { this.email = email.trim() }
                        step = Step.CODE
                    } else {
                        supabase.auth.verifyEmailOtp(type = OtpType.Email.EMAIL, email = email.trim(), token = code)
                        // AuthGate switches to the app once the session is Authenticated.
                    }
                } catch (e: HttpRequestException) {
                    error = "Geen internetverbinding. Probeer het opnieuw."
                } catch (e: Exception) {
                    error = if (step == Step.CODE) {
                        "Die code klopt niet of is verlopen. Vraag een nieuwe aan."
                    } else {
                        "Versturen lukte niet. Controleer je e-mailadres en probeer opnieuw."
                    }
                } finally {
                    loading = false
                }
            }
        }

        if (step == Step.CODE) {
            Text(
                "Ander e-mailadres",
                fontFamily = Nunito,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = InkSoft,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { step = Step.EMAIL; code = ""; error = null }
                    .padding(8.dp),
            )
        }
    }
}

@Composable
private fun CtaButton(label: String, enabled: Boolean, loading: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) Green else Green.copy(alpha = 0.4f))
            .clickable { if (enabled) onClick() }
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(color = Card, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
        } else {
            Text(label, fontFamily = Nunito, fontWeight = FontWeight.Black, fontSize = 15.sp, color = Card)
        }
    }
}

@Composable
private fun Spacer24() = Box(Modifier.height(24.dp))
