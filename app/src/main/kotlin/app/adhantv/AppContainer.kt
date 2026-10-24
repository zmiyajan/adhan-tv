package app.adhantv

import android.content.Context
import app.adhantv.data.Cities
import app.adhantv.data.Hijri
import app.adhantv.data.SettingsRepository
import app.adhantv.data.Verses
import app.adhantv.schedule.AlarmScheduler

/** The app's few long-lived objects, built by hand (no DI framework needed for this size). */
class AppContainer(context: Context) {
    private val app = context.applicationContext
    val cities = Cities(app)
    val settings = SettingsRepository(app, cities)
    val verses = Verses(app)
    val hijri = Hijri()
    val scheduler = AlarmScheduler(app, settings, hijri)
}

val Context.container: AppContainer get() = (applicationContext as AdhanApp).container
