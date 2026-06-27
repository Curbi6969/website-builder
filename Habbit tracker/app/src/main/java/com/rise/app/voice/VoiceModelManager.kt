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

    /** Downloads + extracts whatever is missing. [onProgress] reports 0f..1f (rough). */
    suspend fun ensureModels(onProgress: (Float) -> Unit = {}) = withContext(Dispatchers.IO) {
        if (!VoiceModels.whisperReady(baseDir)) {
            downloadAndExtract(VoiceModels.whisperArchiveUrl()) { onProgress(it * 0.9f) }
        }
        if (!VoiceModels.voiceReady(baseDir)) {
            downloadAndExtract(VoiceModels.voiceArchiveUrl()) { onProgress(0.9f + it * 0.1f) }
        }
        onProgress(1f)
    }

    private fun downloadAndExtract(url: String, onProgress: (Float) -> Unit) {
        val tmp = File(baseDir, "download.tar.bz2")
        val conn = URL(url).openConnection()
        conn.connect()
        val total = conn.contentLengthLong.coerceAtLeast(1L)
        conn.getInputStream().use { input ->
            tmp.outputStream().use { out ->
                val buf = ByteArray(1 shl 16)
                var read = 0L
                var n: Int
                while (input.read(buf).also { n = it } >= 0) {
                    out.write(buf, 0, n)
                    read += n
                    onProgress((read.toFloat() / total).coerceIn(0f, 1f))
                }
            }
        }
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
