package app.adhantv.data

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.adhantv.prayer.Method
import app.adhantv.prayer.Prayer
import app.adhantv.prayer.ScheduleSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.util.TimeZone

private val Context.store by preferencesDataStore("settings")

/** Colour of the countdown line on the adhan screen. */
enum class BarColor(val argb: Long, val label: String) {
    GOLD(0xFFBBA27A, "ذهبي"),
    WHITE(0xFFE8E6E1, "أبيض"),
    GREEN(0xFF6CC08F, "أخضر"),
    BLUE(0xFF7FA8E6, "أزرق"),
}

/** Text size on the adhan screen, for how far the sofa is from the TV. */
enum class TextSize(val scale: Float, val label: String) {
    SMALL(.85f, "صغير"),
    MEDIUM(1f, "متوسط"),
    LARGE(1.15f, "كبير"),
}

/** Where the adhan sound comes from. */
enum class SoundSource(val label: String) {
    /** Quran Radio in Makkah and Riyadh (it carries their adhan), the recording everywhere else. */
    AUTO("تلقائي حسب المدينة"),
    RADIO("إذاعة القرآن الكريم"),
    RECORDED("أذان مسجّل"),
}

/** Everything the user can change. [cityConfirmed] is false while the city is only a guess from the time zone. */
data class AppSettings(
    val city: City,
    val cityConfirmed: Boolean,
    val method: Method,
    val enabled: Set<Prayer> = Prayer.entries.toSet(),
    val sound: Set<Prayer> = Prayer.entries.toSet(),
    val adhanMinutes: Int = 4,
    val source: SoundSource = SoundSource.AUTO,
    val iqamaEnabled: Boolean = true,
    val iqamaMinutes: Map<Prayer, Int> = ScheduleSettings.DEFAULT_IQAMA,
    val iqamaSeconds: Int = 30,
    val barColor: BarColor = BarColor.GOLD,
    val showBar: Boolean = true,
    val textSize: TextSize = TextSize.MEDIUM,
    val showNext: Boolean = false,
    val pausedUntil: Instant? = null,
) {
    val schedule: ScheduleSettings
        get() = ScheduleSettings(method, enabled, iqamaEnabled, iqamaMinutes)

    fun paused(now: Instant = Instant.now()): Boolean = pausedUntil?.isAfter(now) == true

    /** True when the adhan should play the live radio rather than the recording. */
    val useRadio: Boolean
        get() = when (source) {
            SoundSource.RADIO -> true
            SoundSource.RECORDED -> false
            SoundSource.AUTO -> city.near(MAKKAH, 40.0) || city.near(RIYADH, 50.0)
        }

    private companion object {
        val MAKKAH = 21.4225 to 39.8262
        val RIYADH = 24.7136 to 46.6753
    }
}

class SettingsRepository(private val context: Context, private val cities: Cities) {
    val settings: Flow<AppSettings> = context.store.data.map(::read)

    suspend fun current(): AppSettings = settings.first()

    /** Applies [change] to the current settings and saves the result. */
    suspend fun update(change: (AppSettings) -> AppSettings) {
        context.store.edit { p -> write(p, change(read(p))) }
    }

    suspend fun setCity(city: City) = context.store.edit {
        it[K.city] = city.id
        it.remove(K.method)   // follow the new country's default until the user picks a method
    }

    private fun read(p: Preferences): AppSettings {
        val chosen = p[K.city]?.let(cities::byId)
        val city = chosen ?: guess()
        val d = AppSettings(city, chosen != null, Method.defaultFor(city.country))
        return d.copy(
            method = p[K.method]?.let { enumOr(it, d.method) } ?: d.method,
            enabled = p[K.enabled]?.let(::prayers) ?: d.enabled,
            sound = p[K.sound]?.let(::prayers) ?: d.sound,
            adhanMinutes = p[K.adhanMinutes] ?: d.adhanMinutes,
            source = p[K.source]?.let { enumOr(it, d.source) } ?: d.source,
            iqamaEnabled = p[K.iqamaOn] ?: d.iqamaEnabled,
            iqamaMinutes = Prayer.entries.associateWith { p[K.iqama(it)] ?: d.iqamaMinutes.getValue(it) },
            iqamaSeconds = p[K.iqamaSeconds] ?: d.iqamaSeconds,
            barColor = p[K.barColor]?.let { enumOr(it, d.barColor) } ?: d.barColor,
            showBar = p[K.showBar] ?: d.showBar,
            textSize = p[K.textSize]?.let { enumOr(it, d.textSize) } ?: d.textSize,
            showNext = p[K.showNext] ?: d.showNext,
            pausedUntil = p[K.pausedUntil]?.let(Instant::ofEpochMilli),
        )
    }

    private fun write(p: MutablePreferences, s: AppSettings) {
        if (s.method != Method.defaultFor(s.city.country)) p[K.method] = s.method.name else p.remove(K.method)
        p[K.enabled] = s.enabled.map { it.name }.toSet()
        p[K.sound] = s.sound.map { it.name }.toSet()
        p[K.adhanMinutes] = s.adhanMinutes
        p[K.source] = s.source.name
        p[K.iqamaOn] = s.iqamaEnabled
        s.iqamaMinutes.forEach { (prayer, min) -> p[K.iqama(prayer)] = min }
        p[K.iqamaSeconds] = s.iqamaSeconds
        p[K.barColor] = s.barColor.name
        p[K.showBar] = s.showBar
        p[K.textSize] = s.textSize.name
        p[K.showNext] = s.showNext
        s.pausedUntil?.let { p[K.pausedUntil] = it.toEpochMilli() } ?: p.remove(K.pausedUntil)
    }

    private fun prayers(names: Set<String>) = names.mapNotNull { n -> Prayer.entries.firstOrNull { it.name == n } }.toSet()

    private inline fun <reified E : Enum<E>> enumOr(name: String, fallback: E): E =
        enumValues<E>().firstOrNull { it.name == name } ?: fallback

    /** Largest city in the TV's time zone, else Makkah. */
    private fun guess(): City = cities.largestIn(TimeZone.getDefault().id) ?: cities.byId(MAKKAH)!!

    private object K {
        val city = longPreferencesKey("city_id")
        val method = stringPreferencesKey("method")
        val enabled = stringSetPreferencesKey("enabled")
        val sound = stringSetPreferencesKey("sound")
        val adhanMinutes = intPreferencesKey("adhan_minutes")
        val source = stringPreferencesKey("sound_source")
        val iqamaOn = booleanPreferencesKey("iqama_on")
        val iqamaSeconds = intPreferencesKey("iqama_seconds")
        val barColor = stringPreferencesKey("bar_color")
        val showBar = booleanPreferencesKey("show_bar")
        val textSize = stringPreferencesKey("text_size")
        val showNext = booleanPreferencesKey("show_next")
        val pausedUntil = longPreferencesKey("paused_until")
        fun iqama(p: Prayer) = intPreferencesKey("iqama_${p.name}")
    }

    private companion object {
        const val MAKKAH = 104515L
    }
}
