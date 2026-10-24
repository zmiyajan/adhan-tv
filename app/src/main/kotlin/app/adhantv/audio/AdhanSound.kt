package app.adhantv.audio

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import app.adhantv.R

/**
 * The adhan's sound: the live Quran radio, or the bundled recording. If the radio has not started within
 * [RADIO_TIMEOUT_MS] (no internet, stream down), the recording plays instead, so there is always an adhan.
 */
class AdhanSound(private val context: Context, radio: Boolean, private val onLive: (Boolean) -> Unit) {
    private var live: LiveRadio? = null
    private var recorded: ExoPlayer? = null
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
        // ExoPlayer decodes to PCM itself. MediaPlayer hands long AAC files to the TV's "offload" output, which on
        // some TVs (TCL with HDMI audio) plays nothing at all.
        recorded = ExoPlayer.Builder(context).build().apply {
            setAudioAttributes(
                AudioAttributes.Builder().setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_SPEECH).build(),
                false,   // the adhan screen already took audio focus
            )
            setMediaItem(MediaItem.fromUri(RawResourceDataSource.buildRawResourceUri(R.raw.adhan_haram)))
            prepare()
            play()
        }
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
