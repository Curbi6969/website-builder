package com.rise.app.voice

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * TEMPORARY smoke test for the on-device Dutch voice pipeline, remove once the real
 * voice-journal UI lands. Proves: model download+extract, Piper Dutch TTS playback,
 * and Whisper Dutch transcription, end-to-end on the device.
 */
@Composable
fun VoiceLab(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val manager = remember { VoiceModelManager(context) }
    var engineRef by remember { mutableStateOf<RiseSpeech?>(null) }
    fun engine(): RiseSpeech = engineRef ?: RiseSpeech(manager.baseDir).also { engineRef = it }

    var status by remember { mutableStateOf(if (manager.isReady()) "Modellen klaar" else "Nog niet gedownload") }
    var transcript by remember { mutableStateOf("") }
    var hasMic by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val micLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { hasMic = it }

    Column(
        modifier.fillMaxWidth().padding(top = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("🎙️ Stemtest (tijdelijk), $status")

        Button(onClick = {
            scope.launch {
                status = "Starten..."
                runCatching { manager.ensureModels { s -> status = s } }
                    .onSuccess { status = "Modellen klaar" }
                    .onFailure { status = "Download faalde: ${it.message}" }
            }
        }) { Text("1 · Download Nederlandse modellen") }

        Button(onClick = {
            scope.launch {
                if (!manager.isReady()) {
                    status = "Eerst downloaden"
                } else {
                    status = "Spreken..."
                    runCatching {
                        withContext(Dispatchers.IO) {
                            val s = engine()
                            val pcm = s.synthesize("Hallo, dit is je stem in Rise. Je bent goed bezig vandaag.")
                            AudioIO.playPcmFloat(pcm, s.ttsSampleRate())
                        }
                    }.onSuccess { status = "Gesproken ✓" }
                        .onFailure { status = "TTS faalde: ${it.message}" }
                }
            }
        }) { Text("2 · Spreek Nederlands") }

        Button(onClick = {
            if (!hasMic) {
                micLauncher.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                scope.launch {
                    if (!manager.isReady()) {
                        status = "Eerst downloaden"
                    } else {
                        status = "Opnemen (4s)..."
                        runCatching {
                            val samples = withContext(Dispatchers.IO) { AudioIO.recordSeconds(4) }
                            status = "Transcriberen..."
                            withContext(Dispatchers.IO) { engine().transcribe(samples) }
                        }.onSuccess { transcript = it; status = "Transcript klaar" }
                            .onFailure { status = "STT faalde: ${it.message}" }
                    }
                }
            }
        }) { Text("3 · Neem 4s op → tekst") }

        if (transcript.isNotEmpty()) Text("« $transcript »")
    }
}
