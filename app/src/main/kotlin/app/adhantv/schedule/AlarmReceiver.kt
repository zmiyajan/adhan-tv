package app.adhantv.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import app.adhantv.container
import app.adhantv.prayer.Event
import app.adhantv.prayer.Prayer
import app.adhantv.ui.adhan.AdhanActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

/** Fires at an adhan or iqama: shows the screen if the TV is on and the adhan is switched on, then arms the next event. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val event = event(intent)
        val screenOn = context.getSystemService(PowerManager::class.java).isInteractive
        val pending = goAsync()
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val c = context.container
                Log.i(TAG, "alarm for ${event?.at} received ${Instant.now()}")
                event?.let { delay(maxOf(0, it.at.toEpochMilli() - System.currentTimeMillis())) }   // woke early
                // A late delivery (inexact alarm, TV just woke) is only worth showing within the first two minutes.
                val show = event != null && screenOn && Duration.between(event.at, Instant.now()) < LATE &&
                    c.settings.current().active
                Log.i(TAG, "show=$show at ${Instant.now()}")
                if (show) {
                    context.startActivity(
                        AdhanActivity.intent(context, event!!, intent.getIntExtra(SECONDS, 0))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    )
                }
                c.scheduler.armNext()
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        private const val TAG = "AdhanAlarm"
        private val LATE = Duration.ofMinutes(2)

        /** Optional: how long the screen stays, overriding the settings (used by the debug test trigger). */
        const val SECONDS = "seconds"

        fun intent(context: Context, e: Event): Intent = Intent(context, AlarmReceiver::class.java)
            .putExtra("prayer", e.prayer.name)
            .putExtra("kind", e.kind.name)
            .putExtra("at", e.at.toEpochMilli())
            .putExtra("friday", e.friday)

        private fun event(i: Intent): Event? = runCatching {
            Event(
                Prayer.valueOf(i.getStringExtra("prayer")!!),
                Event.Kind.valueOf(i.getStringExtra("kind")!!),
                Instant.ofEpochMilli(i.getLongExtra("at", 0)),
                i.getBooleanExtra("friday", false),
            )
        }.getOrNull()
    }
}
