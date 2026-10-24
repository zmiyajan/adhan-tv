package app.adhantv.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import app.adhantv.R

/**
 * The adhan's sound: the live Quran radio, or the bundled recording. If the radio has not started within
 * [RADIO_TIMEOUT_MS] (no internet, stream down), the recording plays instead, so there is always an adhan.
 */
class AdhanSound(private val context: Context, radio: Boolean, private val onLive: (Boolean) -> Unit) {
    private var live: LiveRadio? = null
    private var recorded: MediaPlayer? = null
    private val fallback = Runnable { if (live?.playing != true) playRecording() }
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    init {
        if (radio) {
            live = LiveRadio(LiveRadio.QURAN_RADIO)
            onLive(true)
            handler.postDelayed(fallback, RADIO_TIMEOUT_MS)
        } else {
            playRecording()
        }
    }

    private fun playRecording() {
        live?.release()
        live = null
        onLive(false)
        recorded = MediaPlayer.create(
            context, R.raw.adhan_haram,
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_SPEECH).build(),
            0,
        )?.apply { start() }
    }

    fun release() {
        handler.removeCallbacks(fallback)
        live?.release()
        recorded?.release()
    }

    private companion object {
        const val RADIO_TIMEOUT_MS = 6_000L
    }
}
