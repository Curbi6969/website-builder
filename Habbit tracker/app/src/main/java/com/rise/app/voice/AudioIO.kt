package com.rise.app.voice

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import kotlin.math.sqrt

/** Mic capture + PCM playback for the on-device voice engine. Both calls block, run on IO. */
object AudioIO {
    private const val SAMPLE_RATE = 16000

    /**
     * Records [seconds] of 16 kHz mono audio → normalized float samples (−1..1).
     * Requires RECORD_AUDIO already granted. [onLevel] reports a 0..1 loudness for the orb.
     */
    fun recordSeconds(seconds: Int, onLevel: (Float) -> Unit = {}): FloatArray {
        val minBuf = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
        )
        val record = AudioRecord(
            MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
            maxOf(minBuf, SAMPLE_RATE),
        )
        val total = SAMPLE_RATE * seconds
        val out = FloatArray(total)
        val chunk = ShortArray(1600)
        var written = 0
        try {
            record.startRecording()
            while (written < total) {
                val n = record.read(chunk, 0, chunk.size)
                if (n <= 0) continue
                var sum = 0.0
                for (i in 0 until n) {
                    if (written >= total) break
                    val v = chunk[i] / 32768f
                    out[written++] = v
                    sum += (v * v).toDouble()
                }
                onLevel(sqrt(sum / n).toFloat().coerceIn(0f, 1f))
            }
        } finally {
            runCatching { record.stop() }
            record.release()
        }
        return out
    }

    /** Plays mono PCM float [samples] at [sampleRate]. */
    fun playPcmFloat(samples: FloatArray, sampleRate: Int) {
        if (samples.isEmpty()) return
        val minBuf = AudioTrack.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_FLOAT,
        )
        val track = AudioTrack.Builder()
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_FLOAT)
                    .setSampleRate(sampleRate)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build(),
            )
            .setBufferSizeInBytes(maxOf(minBuf, samples.size * 4))
            .setTransferMode(AudioTrack.MODE_STREAM)
            .build()
        try {
            track.play()
            track.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
            track.stop()
        } finally {
            track.release()
        }
    }
}
