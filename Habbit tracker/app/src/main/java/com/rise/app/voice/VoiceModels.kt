package com.rise.app.voice

import java.io.File

/**
 * Model identifiers + on-disk layout for the on-device Dutch voice models
 * (downloaded on first use by [VoiceModelManager], stored under filesDir/voice).
 */
object VoiceModels {
    private const val RELEASES = "https://github.com/k2-fsa/sherpa-onnx/releases/download"

    /**
     * Whisper speech-to-text. BASE = 208 MB download, good Dutch. Switch [whisper] to
     * SMALL for noticeably better Dutch at the cost of a 639 MB first-run download.
     */
    enum class Whisper(val dir: String, val archivePath: String, val prefix: String) {
        BASE("sherpa-onnx-whisper-base", "asr-models/sherpa-onnx-whisper-base.tar.bz2", "base"),
        SMALL("sherpa-onnx-whisper-small", "asr-models/sherpa-onnx-whisper-small.tar.bz2", "small"),
    }

    /** The active STT model — change to [Whisper.SMALL] for max Dutch accuracy. */
    val whisper = Whisper.BASE

    // Piper Dutch TTS voice — "ronnie" (warm male, int8 ≈ 21 MB).
    // Other nl_NL voices: pim, alex (medium); miro, dii (high).
    private const val VOICE_DIR = "vits-piper-nl_NL-ronnie-medium-int8"
    private const val VOICE_ARCHIVE = "tts-models/vits-piper-nl_NL-ronnie-medium-int8.tar.bz2"
    private const val VOICE_MODEL = "nl_NL-ronnie-medium.onnx"

    fun whisperArchiveUrl() = "$RELEASES/${whisper.archivePath}"
    fun voiceArchiveUrl() = "$RELEASES/$VOICE_ARCHIVE"

    // Resolved paths after extraction under [base].
    fun whisperEncoder(base: File) = File(base, "${whisper.dir}/${whisper.prefix}-encoder.int8.onnx").absolutePath
    fun whisperDecoder(base: File) = File(base, "${whisper.dir}/${whisper.prefix}-decoder.int8.onnx").absolutePath
    fun whisperTokens(base: File) = File(base, "${whisper.dir}/${whisper.prefix}-tokens.txt").absolutePath
    fun voiceModel(base: File) = File(base, "$VOICE_DIR/$VOICE_MODEL").absolutePath
    fun voiceTokens(base: File) = File(base, "$VOICE_DIR/tokens.txt").absolutePath
    fun voiceDataDir(base: File) = File(base, "$VOICE_DIR/espeak-ng-data").absolutePath

    fun whisperReady(base: File) =
        File(whisperEncoder(base)).exists() && File(whisperDecoder(base)).exists() && File(whisperTokens(base)).exists()
    fun voiceReady(base: File) =
        File(voiceModel(base)).exists() && File(voiceDataDir(base)).exists()
    fun allReady(base: File) = whisperReady(base) && voiceReady(base)
}
