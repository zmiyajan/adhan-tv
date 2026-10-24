package app.adhantv.ui.adhan

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.KeyEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import app.adhantv.audio.AdhanSound
import app.adhantv.audio.AdhanVoice
import app.adhantv.container
import app.adhantv.prayer.Event
import app.adhantv.prayer.Prayer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant

/** Full-screen adhan (live Quran radio or the recording) or iqama (silent), timed by the settings. Pauses what was playing; Back closes it. */
class AdhanActivity : ComponentActivity() {
    private var sound: AdhanSound? = null
    private var live by mutableStateOf(false)
    private val audio by lazy { getSystemService(AudioManager::class.java) }

    /**
     * Permanent focus: the video that was playing pauses, and stays paused after the adhan (the user resumes it
     * when ready). Transient focus would make it resume on its own.
     */
    private val focus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build())
        .setOnAudioFocusChangeListener { }
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val event = Event(
            Prayer.valueOf(intent.getStringExtra(PRAYER) ?: Prayer.MAGHRIB.name),
            Event.Kind.valueOf(intent.getStringExtra(KIND) ?: Event.Kind.ADHAN.name),
            Instant.now(),
            intent.getBooleanExtra(FRIDAY, false),
        )
        val adhan = event.kind == Event.Kind.ADHAN
        pausePlaying()
        lifecycleScope.launch {
            val c = container
            val s = c.settings.current()
            val override = intent.getIntExtra(SECONDS, 0) * 1000L
            val voice = AdhanVoice.of(this@AdhanActivity, s.voiceId)
            val showMs = if (override > 0) override else if (adhan) s.adhanMs(event.prayer, voice.durationMs(this@AdhanActivity)) else s.iqamaSeconds * 1000L
            Log.i("AdhanSound", "source=${s.source} city=${s.city.english} radio=${s.useRadio} sound=${event.prayer in s.sound}")
            if (adhan && event.prayer in s.sound) sound = AdhanSound(this@AdhanActivity, s.useRadio, voice) { live = it }
            val next = if (s.showNext) c.scheduler.schedule(s).next(Instant.now().plusMillis(showMs)) else null
            setContent { AdhanScreen(event, c.verses.adhan, showMs, s, next, live) }
            delay(showMs)
            finish()
        }
    }

    /**
     * Pauses whatever is playing. A media "pause" key reaches apps that ignore audio focus (YouTube on TV), and
     * permanent focus covers the rest. Nothing is resumed afterwards.
     */
    private fun pausePlaying() {
        val now = SystemClock.uptimeMillis()
        audio.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_MEDIA_PAUSE, 0))
        audio.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, KeyEvent.KEYCODE_MEDIA_PAUSE, 0))
        audio.requestAudioFocus(focus)
    }

    override fun onDestroy() {
        sound?.release()
        audio.abandonAudioFocusRequest(focus)
        super.onDestroy()
    }

    companion object {
        private const val PRAYER = "prayer"
        private const val KIND = "kind"
        private const val FRIDAY = "friday"
        private const val SECONDS = "seconds"

        /** [seconds] > 0 overrides how long the screen stays (tests only). */
        fun intent(context: Context, e: Event, seconds: Int = 0): Intent = Intent(context, AdhanActivity::class.java)
            .putExtra(PRAYER, e.prayer.name)
            .putExtra(KIND, e.kind.name)
            .putExtra(FRIDAY, e.friday)
            .putExtra(SECONDS, seconds)
    }
}
