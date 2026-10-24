package app.adhantv.audio

import android.media.AudioAttributes
import android.media.MediaDataSource
import android.media.MediaPlayer
import java.net.HttpURLConnection
import java.net.URL

/**
 * Live MP3 radio with almost no delay (measured on a TCL Google TV: sound ~1.5 s after the screen, ~1 s behind
 * live). MediaPlayer on a URL waits ~5 s to fill its buffer, and the server first sends ~6 s of old audio, so the
 * sound was ~11 s late. Here the stream is downloaded directly, the old burst is dropped (keeping [CUSHION] bytes
 * so playback never starves), and MediaPlayer reads the bytes as a local source, which it starts at once.
 */
class LiveRadio(url: String) : MediaDataSource() {
    private val player = MediaPlayer()
    private val lock = Object()
    private var data = ByteArray(1 shl 20)
    private var size = 0

    @Volatile private var closed = false

    /** True once sound is actually coming out. */
    @Volatile var playing = false
        private set

    init {
        player.setAudioAttributes(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build(),
        )
        player.setOnPreparedListener {
            it.start()
            playing = true
        }
        player.setOnErrorListener { _, _, _ -> true }   // no internet: the screen still shows
        Thread({ download(url) }, "radio").start()
    }

    private fun download(url: String) {
        runCatching {
            val c = URL(url).openConnection() as HttpURLConnection
            c.connectTimeout = 8000
            c.readTimeout = 15000
            c.inputStream.use { input ->
                val b = ByteArray(4096)
                val start = System.currentTimeMillis()
                var live = false
                while (!closed) {
                    val n = input.read(b)
                    if (n <= 0) break
                    val goLive = !live && System.currentTimeMillis() - start >= BURST_MS
                    synchronized(lock) {
                        append(b, n)
                        if (goLive) {   // drop the old audio the server sent first
                            val from = frameStart(maxOf(0, size - CUSHION))
                            System.arraycopy(data, from, data, 0, size - from)
                            size -= from
                        }
                        lock.notifyAll()
                    }
                    if (goLive) {
                        live = true
                        player.setDataSource(this)
                        player.prepareAsync()
                    }
                }
            }
        }
    }

    private fun append(b: ByteArray, n: Int) {
        if (size + n > data.size) data = data.copyOf(data.size * 2)
        System.arraycopy(b, 0, data, size, n)
        size += n
    }

    /** First MP3 frame header (sync 0xFFE, MPEG-1 layer III) at or after [from], so decoding starts cleanly. */
    private fun frameStart(from: Int): Int {
        for (i in from until size - 1) {
            if (data[i].toInt() and 0xFF == 0xFF && data[i + 1].toInt() and 0xFE == 0xFA) return i
        }
        return 0
    }

    override fun readAt(position: Long, buffer: ByteArray, offset: Int, length: Int): Int = synchronized(lock) {
        while (!closed && position >= size) lock.wait(500)   // live: wait for the next bytes
        if (closed) return -1
        val n = minOf(length.toLong(), size - position).toInt()
        System.arraycopy(data, position.toInt(), buffer, offset, n)
        n
    }

    override fun getSize(): Long = -1   // live: no end

    override fun close() {
        closed = true
        synchronized(lock) { lock.notifyAll() }
    }

    fun release() {
        close()
        player.release()
    }

    companion object {
        /** Saudi Quran Radio (same stream TuneIn plays; ~8 s ahead of the official HLS feed). */
        const val QURAN_RADIO = "http://stream.radiojar.com/0tpy1h0kxtzuv"
        private const val BURST_MS = 700L
        private const val CUSHION = 12000
    }
}
