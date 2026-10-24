package app.adhantv.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.adhantv.prayer.Event
import app.adhantv.prayer.Prayer
import java.time.Instant

/**
 * Debug only. Arms a real exact alarm through [AlarmReceiver], exactly like a prayer time, e.g.:
 *   adb shell am broadcast -a app.adhantv.TEST -n app.adhantv/.schedule.TestReceiver \
 *     --ei delay 60 --ei seconds 30 --es prayer MAGHRIB --es kind ADHAN
 */
class TestReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val at = Instant.now().plusSeconds(intent.getIntExtra("delay", 30).toLong())
        val event = Event(
            Prayer.valueOf(intent.getStringExtra("prayer") ?: Prayer.MAGHRIB.name),
            Event.Kind.valueOf(intent.getStringExtra("kind") ?: Event.Kind.ADHAN.name),
            at,
            false,
        )
        val fire = AlarmReceiver.intent(context, event).putExtra(AlarmReceiver.SECONDS, intent.getIntExtra("seconds", 0))
        val pi = PendingIntent.getBroadcast(context, 1, fire, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val show = PendingIntent.getBroadcast(context, 2, Intent(), PendingIntent.FLAG_IMMUTABLE)
        val wake = at.toEpochMilli() - AlarmScheduler.LEAD_MS
        context.getSystemService(AlarmManager::class.java).setAlarmClock(AlarmManager.AlarmClockInfo(wake, show), pi)
    }
}
