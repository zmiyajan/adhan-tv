package app.adhantv.ui.board

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.adhantv.container
import app.adhantv.data.Hijri
import app.adhantv.prayer.Event
import app.adhantv.prayer.Prayer
import app.adhantv.ui.FullScreen
import app.adhantv.ui.Label
import app.adhantv.ui.arabic
import app.adhantv.ui.clockText
import app.adhantv.ui.keepScreenOn
import app.adhantv.ui.theme.Fonts
import app.adhantv.ui.theme.Palette
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * A mosque-style board that can stay on all day: the clock, today's adhan and iqama times, and a countdown to the
 * next one. The adhan screen still comes up over it at prayer time. Back closes it.
 */
class BoardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        keepScreenOn()
        val c = container
        setContent {
            val s = c.settings.settings.collectAsState(initial = null).value ?: return@setContent
            var now by remember { mutableStateOf(Instant.now()) }
            LaunchedEffect(Unit) {
                while (true) {
                    delay(1000 - System.currentTimeMillis() % 1000)
                    now = Instant.now()
                }
            }
            val zone = s.city.place.zone
            val schedule = remember(s) { c.scheduler.schedule(s) }
            val local = now.atZone(zone)
            val date = local.toLocalDate()
            val day = remember(s, date) { schedule.day(date) }
            val events = remember(s, date) { schedule.events(date) }
            val next = remember(s, now.epochSecond / 30) { schedule.next(now) }
            val h = remember(date) { c.hijri.of(date) }

            FullScreen { g ->
                Column(Modifier.fillMaxWidth().padding(top = g.u * 90f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Label(local.format(CLOCK), Fonts.plex, g.text(190f), Palette.ink)
                        Label(if (local.hour < 12) " ص" else " م", Fonts.plex, g.text(60f), Palette.mute, Modifier.padding(bottom = g.u * 40f))
                    }
                    Label(
                        "${s.city.name} · ${h.day} ${Hijri.MONTHS[h.month - 1]} ${h.year} · ${local.format(GREGORIAN)}",
                        Fonts.plex, g.text(34f), Palette.mute,
                    )
                    Spacer(Modifier.height(g.u * 80f))
                    Row(horizontalArrangement = Arrangement.spacedBy(g.u * 26f)) {
                        Prayer.entries.forEach { p ->
                            val friday = events.firstOrNull { it.prayer == p }?.jumuah == true
                            val iqama = events.firstOrNull { it.prayer == p && it.kind == Event.Kind.IQAMA }
                            val isNext = next?.prayer == p && next.at.atZone(zone).toLocalDate() == date
                            val shape = RoundedCornerShape(g.u * 28f)
                            Column(
                                Modifier.width(g.u * 300f).background(Palette.bgCentre.copy(alpha = .8f), shape)
                                    .then(if (isNext) Modifier.border(g.u * 3f, Palette.gold, shape) else Modifier)
                                    .padding(vertical = g.u * 34f),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Label(p.arabic(friday), Fonts.amiri, g.text(52f), if (isNext) Palette.gold else Palette.ink)
                                Label(day[p].clockText(zone), Fonts.plex, g.text(56f), Palette.ink)
                                Label(iqama?.let { "الإقامة ${it.at.clockText(zone)}" } ?: " ", Fonts.plex, g.text(28f), Palette.mute)
                            }
                        }
                    }
                    Spacer(Modifier.height(g.u * 80f))
                    next?.let { e ->
                        val left = Duration.between(now, e.at)
                        val what = if (e.kind == Event.Kind.IQAMA) "إقامة صلاة ${e.prayer.arabic()}" else "أذان ${e.prayer.arabic(e.jumuah)}"
                        Label("$what بعد ${countdown(left)}", Fonts.plex, g.text(48f), Palette.gold)
                    }
                }
            }
        }
    }

    private companion object {
        val CLOCK: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm", Locale.ENGLISH)
        val GREGORIAN: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("ar-u-nu-latn"))

        fun countdown(d: Duration): String {
            val s = d.seconds.coerceAtLeast(0)
            return if (s >= 3600) "%d:%02d:%02d".format(Locale.ENGLISH, s / 3600, s / 60 % 60, s % 60)
            else "%d:%02d".format(Locale.ENGLISH, s / 60, s % 60)
        }
    }
}
