package app.adhantv.ui.home

import android.Manifest
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import app.adhantv.container
import app.adhantv.data.City
import app.adhantv.prayer.Event
import app.adhantv.prayer.Prayer
import app.adhantv.ui.adhan.AdhanActivity
import app.adhantv.ui.board.BoardActivity
import app.adhantv.ui.radio.RadioActivity
import app.adhantv.ui.theme.AdhanTvTheme
import kotlinx.coroutines.launch
import java.time.Instant

/** Opened from the TV's app list: today's times and all settings, driven by the remote. */
class HomeActivity : ComponentActivity() {
    private var status by mutableStateOf<String?>(null)
    private var resumes by mutableIntStateOf(0)   // re-reads permissions after the user returns from Settings

    private val askLocation = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) locate() else status = "ما أعطيت إذن الموقع. اختر المدينة من القائمة."
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            container.settings.migrate()
            container.scheduler.armNext()
        }
        setContent {
            AdhanTvTheme {
                HomeScreen(
                    settings = container.settings,
                    scheduler = container.scheduler,
                    cities = container.cities,
                    hijri = container.hijri,
                    status = status,
                    resumes = resumes,
                    version = packageManager.getPackageInfo(packageName, 0).versionName.orEmpty(),
                    act = HomeActions(
                        change = { change ->
                            lifecycleScope.launch {
                                container.settings.update(change)
                                container.scheduler.armNext()
                            }
                        },
                        locate = {
                            if (Permissions.canLocate(this)) locate() else askLocation.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                        },
                        pick = ::choose,
                        tryEvent = { kind -> startActivity(AdhanActivity.intent(this, Event(Prayer.MAGHRIB, kind, Instant.now(), false))) },
                        board = { startActivity(Intent(this, BoardActivity::class.java)) },
                        radio = { startActivity(Intent(this, RadioActivity::class.java)) },
                        overApps = {
                            if (!Permissions.openOverAppsSettings(this)) status = "افتح الإعدادات ← التطبيقات ← أذان التلفاز ← الأذونات"
                        },
                        exact = {
                            if (!Permissions.openExactAlarmSettings(this)) status = "افتح الإعدادات ← التطبيقات ← الوصول الخاص ← المنبهات والتذكيرات"
                        },
                        locationSettings = {
                            if (!Permissions.openAppSettings(this)) status = "افتح الإعدادات ← التطبيقات ← أذان التلفاز ← الأذونات"
                        },
                        restart = ::restart,
                        reset = {
                            lifecycleScope.launch {
                                container.settings.reset()
                                container.scheduler.armNext()
                                status = "رجعت الإعدادات الأصلية."
                            }
                        },
                        finishOnboarding = {
                            status = null
                            lifecycleScope.launch { container.settings.update { it.copy(onboarded = true) } }
                        },
                    ),
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resumes++
        lifecycleScope.launch { container.scheduler.armNext() }
    }

    /** Re-arms the alarm and starts the app afresh in a new process. */
    private fun restart() {
        lifecycleScope.launch {
            container.scheduler.armNext()
            startActivity(Intent(this@HomeActivity, HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            Runtime.getRuntime().exit(0)
        }
    }

    private fun locate() {
        status = "أبحث عن موقع التلفزيون…"
        lifecycleScope.launch {
            val fix = approximateLocation(this@HomeActivity)
            status = if (fix == null) {
                "التلفزيون ما يعرف موقعه. اختر المدينة من القائمة."
            } else {
                choose(container.cities.nearest(fix.latitude, fix.longitude))
                null
            }
        }
    }

    private fun choose(city: City) {
        lifecycleScope.launch {
            container.settings.setCity(city)
            container.scheduler.armNext()
            status = "المدينة: ${city.name}"
        }
    }
}
