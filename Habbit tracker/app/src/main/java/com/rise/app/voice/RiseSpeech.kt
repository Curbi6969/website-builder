package com.rise.app.voice

import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineTts
import com.k2fsa.sherpa.onnx.OfflineTtsConfig
import com.k2fsa.sherpa.onnx.OfflineTtsModelConfig
import com.k2fsa.sherpa.onnx.OfflineTtsVitsModelConfig
import com.k2fsa.sherpa.onnx.OfflineWhisperModelConfig
import java.io.File

/**
 * On-device Dutch speech engine: Whisper (speech→text) + Piper (text→speech) via sherpa-onnx.
 * Models must already be on disk (see [VoiceModelManager]); the recognizer/tts are built lazily
 * on first use, so construct/call this off the main thread.
 */
class RiseSpeech(private val baseDir: File) {

    private val recognizer: OfflineRecognizer by lazy {
        val model = OfflineModelConfig(
            whisper = OfflineWhisperModelConfig(
                encoder = VoiceModels.whisperEncoder(baseDir),
                decoder = VoiceModels.whisperDecoder(baseDir),
                language = "nl",
                task = "transcribe",
            ),
            tokens = VoiceModels.whisperTokens(baseDir),
            numThreads = 2,
            modelType = "whisper",
        )
        OfflineRecognizer(
            config = OfflineRecognizerConfig(
                featConfig = FeatureConfig(sampleRate = 16000, featureDim = 80),
                modelConfig = model,
            ),
        )
    }

    private val tts: OfflineTts by lazy {
        val model = OfflineTtsModelConfig(
            vits = OfflineTtsVitsModelConfig(
                model = VoiceModels.voiceModel(baseDir),
                tokens = VoiceModels.voiceTokens(baseDir),
                dataDir = VoiceModels.voiceDataDir(baseDir),
            ),
            numThreads = 2,
        )
        OfflineTts(config = OfflineTtsConfig(model = model))
    }

    /** Sample rate the Piper voice produces — drives the playback AudioTrack. */
    fun ttsSampleRate(): Int = tts.sampleRate()

    /** Transcribe 16 kHz mono float samples (range −1..1) to Dutch text. */
    fun transcribe(samples: FloatArray): String {
        val stream = recognizer.createStream()
        stream.acceptWaveform(samples, sampleRate = 16000)
        recognizer.decode(stream)
        val text = recognizer.getResult(stream).text
        stream.release()
        return text.trim()
    }

    /** Synthesize Dutch [text] → PCM float samples (mono, [ttsSampleRate]). */
    fun synthesize(text: String, speed: Float = 1.0f): FloatArray =
        tts.generate(text = text, sid = 0, speed = speed).samples

    fun release() {
        runCatching { tts.release() }
        runCatching { recognizer.release() }
    }
}
