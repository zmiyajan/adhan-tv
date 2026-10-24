package app.adhantv.ui.home

import android.app.AlarmManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri

/** The two optional system permissions, and the settings screens where the user grants them. */
object Permissions {
    fun canShowOverApps(c: Context): Boolean = Settings.canDrawOverlays(c)

    fun canBeExact(c: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || c.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    /** Opens the system screen; false if this TV has none (some builds hide it). */
    fun openOverAppsSettings(c: Context): Boolean =
        open(c, Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, "package:${c.packageName}".toUri()))

    fun openExactAlarmSettings(c: Context): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        open(c, Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, "package:${c.packageName}".toUri()))

    private fun open(c: Context, i: Intent): Boolean = try {
        c.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}
