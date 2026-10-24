package app.adhantv.ui

import app.adhantv.prayer.Event
import app.adhantv.prayer.Prayer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Arabic prayer names; Friday Dhuhr is «الجمعة». */
fun Prayer.arabic(jumuah: Boolean = false): String = when (this) {
    Prayer.FAJR -> "الفجر"
    Prayer.DHUHR -> if (jumuah) "الجمعة" else "الظهر"
    Prayer.ASR -> "العصر"
    Prayer.MAGHRIB -> "المغرب"
    Prayer.ISHA -> "العشاء"
}

val Event.title: String
    get() = if (kind == Event.Kind.IQAMA) "حان الآن موعد إقامة صلاة ${prayer.arabic()}" else "حان الآن موعد أذان ${prayer.arabic(jumuah)}"

private val clock = DateTimeFormatter.ofPattern("h:mm", Locale.ENGLISH)   // Western digits, as the household prefers

/** "5:04 ص" / "3:23 م" in the place's own zone. */
fun Instant.clockText(zone: ZoneId): String {
    val t = atZone(zone)
    return "${t.format(clock)} ${if (t.hour < 12) "ص" else "م"}"
}

/** Arabic names of the calculation methods. */
val app.adhantv.prayer.Method.arabic: String
    get() = when (this) {
        app.adhantv.prayer.Method.UMM_AL_QURA -> "أم القرى"
        app.adhantv.prayer.Method.MUSLIM_WORLD_LEAGUE -> "رابطة العالم الإسلامي"
        app.adhantv.prayer.Method.EGYPTIAN -> "الهيئة المصرية العامة للمساحة"
        app.adhantv.prayer.Method.KARACHI -> "جامعة العلوم الإسلامية بكراتشي"
        app.adhantv.prayer.Method.DUBAI -> "دبي"
        app.adhantv.prayer.Method.KUWAIT -> "الكويت"
        app.adhantv.prayer.Method.QATAR -> "قطر"
        app.adhantv.prayer.Method.SINGAPORE -> "سنغافورة"
        app.adhantv.prayer.Method.TURKEY -> "تركيا"
        app.adhantv.prayer.Method.NORTH_AMERICA -> "أمريكا الشمالية"
    }
