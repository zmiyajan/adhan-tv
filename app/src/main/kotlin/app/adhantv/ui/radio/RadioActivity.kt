package app.adhantv.ui.radio

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import app.adhantv.audio.LiveRadio
import app.adhantv.container
import app.adhantv.prayer.Event
import app.adhantv.ui.FullScreen
import app.adhantv.ui.Label
import app.adhantv.ui.arabic
import app.adhantv.ui.clockText
import app.adhantv.ui.keepScreenOn
import app.adhantv.ui.theme.Fonts
import app.adhantv.ui.theme.Palette
import kotlinx.coroutines.delay
import java.time.Instant

/**
 * Listen to the Quran Radio any time. It plays while this screen is showing: at prayer time the adhan screen
 * covers it and the radio stops; Back stops it too.
 */
class RadioActivity : ComponentActivity() {
    private var radio: LiveRadio? = null
    private val audio by lazy { getSystemService(AudioManager::class.java) }
    private val focus by lazy {
        AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build())
            .setOnAudioFocusChangeListener { if (it <= 0) stopRadio() }
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keepScreenOn()
        val c = container
        setContent {
            val s = c.settings.settings.collectAsState(initial = null).value ?: return@setContent
            var now by remember { mutableStateOf(Instant.now()) }
            LaunchedEffect(Unit) {
                while (true) {
                    delay(20_000)
                    now = Instant.now()
                }
            }
            val zone = s.city.place.zone
            val next = remember(s, now) { c.scheduler.schedule(s).next(now) }
            FullScreen { g ->
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(g.u * 22f)) {
                        Box(Modifier.size(g.u * 22f).clip(CircleShape).background(Palette.live))
                        Label("مباشر", Fonts.plex, g.text(36f), Palette.mute)
                    }
                    Spacer(Modifier.height(g.u * 30f))
                    Label("إذاعة القرآن الكريم", Fonts.amiri, g.text(130f), Palette.gold)
                    Label("من المملكة العربية السعودية", Fonts.plex, g.text(34f), Palette.mute)
                    Spacer(Modifier.height(g.u * 90f))
                    next?.let {
                        val what = if (it.kind == Event.Kind.IQAMA) "إقامة صلاة ${it.prayer.arabic()}" else "أذان ${it.prayer.arabic(it.jumuah)}"
                        Label("القادم: $what ${it.at.clockText(zone)}", Fonts.plex, g.text(36f), Palette.ink)
                    }
                    Spacer(Modifier.height(g.u * 24f))
                    Label("زر الرجوع يوقف الإذاعة", Fonts.plex, g.text(26f), Palette.mute)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        audio.requestAudioFocus(focus)
        if (radio == null) radio = LiveRadio(LiveRadio.QURAN_RADIO)
    }

    override fun onStop() {
        stopRadio()
        audio.abandonAudioFocusRequest(focus)
        super.onStop()
    }

    private fun stopRadio() {
        radio?.release()
        radio = null
    }
}
