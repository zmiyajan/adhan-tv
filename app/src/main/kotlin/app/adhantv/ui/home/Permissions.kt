package app.adhantv.ui.home

import android.Manifest
import android.app.AlarmManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri

/** The optional system permissions, and the settings screens where the user turns them on or off. */
object Permissions {
    fun canShowOverApps(c: Context): Boolean = Settings.canDrawOverlays(c)

    fun canBeExact(c: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || c.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    fun canLocate(c: Context): Boolean =
        c.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

    /** The app's page in the TV settings, where location can be turned off again. */
    fun openAppSettings(c: Context): Boolean =
        open(c, Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${c.packageName}".toUri()))

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
