package com.rise.app.voice

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import java.io.File
import java.net.URL

/**
 * Downloads + extracts the sherpa-onnx model archives into filesDir/voice on first use.
 * Whisper (~208 MB) is the bulk; the Dutch voice is small (~21 MB). Both are .tar.bz2.
 */
class VoiceModelManager(context: Context) {

    val baseDir: File = File(context.filesDir, "voice").apply { mkdirs() }

    fun isReady(): Boolean = VoiceModels.allReady(baseDir)

    /** Downloads + extracts whatever is missing, reporting a human-readable [onStatus]. */
    suspend fun ensureModels(onStatus: (String) -> Unit = {}) = withContext(Dispatchers.IO) {
        if (!VoiceModels.whisperReady(baseDir)) {
            downloadAndExtract("Spraakmodel", VoiceModels.whisperArchiveUrl(), onStatus)
        }
        if (!VoiceModels.voiceReady(baseDir)) {
            downloadAndExtract("Stem", VoiceModels.voiceArchiveUrl(), onStatus)
        }
        onStatus("Klaar")
    }

    private fun downloadAndExtract(label: String, url: String, onStatus: (String) -> Unit) {
        onStatus("$label downloaden...")
        val tmp = File(baseDir, "download.tar.bz2")
        val conn = URL(url).openConnection()
        conn.connect()
        val total = conn.contentLengthLong.coerceAtLeast(1L)
        conn.getInputStream().use { input ->
            tmp.outputStream().use { out ->
                val buf = ByteArray(1 shl 16)
                var read = 0L
                var n: Int
                var lastPct = -1
                while (input.read(buf).also { n = it } >= 0) {
                    out.write(buf, 0, n)
                    read += n
                    val pct = ((read.toFloat() / total) * 100).toInt().coerceIn(0, 100)
                    if (pct != lastPct) { lastPct = pct; onStatus("$label downloaden... $pct%") }
                }
            }
        }
        // Extraction has no byte-level progress and can take a minute on a phone; say so.
        onStatus("$label uitpakken... (kan even duren)")
        extractTarBz2(tmp, baseDir)
        tmp.delete()
    }

    private fun extractTarBz2(archive: File, dest: File) {
        TarArchiveInputStream(BZip2CompressorInputStream(archive.inputStream().buffered())).use { tar ->
            var entry = tar.nextEntry
            while (entry != null) {
                val outFile = File(dest, entry.name)
                if (entry.isDirectory) {
                    outFile.mkdirs()
                } else {
                    outFile.parentFile?.mkdirs()
                    outFile.outputStream().use { tar.copyTo(it) }
                }
                entry = tar.nextEntry
            }
        }
    }
}
