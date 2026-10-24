package app.adhantv.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import app.adhantv.data.AppSettings
import app.adhantv.data.Hijri
import app.adhantv.data.SettingsRepository
import app.adhantv.prayer.Event
import app.adhantv.prayer.Schedule
import app.adhantv.ui.home.HomeActivity
import java.time.Instant

/**
 * Keeps exactly one alarm armed: the next adhan or iqama. Each firing arms the one after it, so the chain also
 * rolls over to the next day by itself.
 */
class AlarmScheduler(
    private val context: Context,
    private val settings: SettingsRepository,
    private val hijri: Hijri,
) {
    private val alarms = context.getSystemService(AlarmManager::class.java)

    fun schedule(s: AppSettings): Schedule = Schedule(s.city.place, s.schedule, hijri::isRamadan)

    /** Whether the system lets us fire at the exact minute ("Alarms & reminders"). */
    fun canBeExact(): Boolean = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarms.canScheduleExactAlarms()

    suspend fun armNext(now: Instant = Instant.now()): Event? {
        val event = schedule(settings.current()).next(now) ?: return null
        val fire = PendingIntent.getBroadcast(
            context, 0, AlarmReceiver.intent(context, event),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val at = event.at.toEpochMilli() - LEAD_MS   // wake early: a cold start takes ~3 s on a TCL TV
        if (canBeExact()) {
            val show = PendingIntent.getActivity(
                context, 0, Intent(context, HomeActivity::class.java), PendingIntent.FLAG_IMMUTABLE,
            )
            alarms.setAlarmClock(AlarmManager.AlarmClockInfo(at, show), fire)
        } else {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, fire)   // may arrive a few minutes late
        }
        return event
    }

    companion object {
        /** The alarm fires this early; [AlarmReceiver] then waits for the exact second. */
        const val LEAD_MS = 5_000L
    }
}
