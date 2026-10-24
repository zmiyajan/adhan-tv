package app.adhantv.data

import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import java.time.LocalDate
import java.time.ZoneOffset

/** Umm al-Qura Hijri dates from Android's ICU (matches the official KACST table). */
class Hijri {
    data class Date(val year: Int, val month: Int, val day: Int)

    fun of(date: LocalDate): Date {
        val cal = IslamicCalendar(ULocale("ar@calendar=islamic-umalqura"))
        cal.calculationType = IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
        cal.timeInMillis = date.atTime(12, 0).toInstant(ZoneOffset.UTC).toEpochMilli()
        return Date(cal.get(IslamicCalendar.YEAR), cal.get(IslamicCalendar.MONTH) + 1, cal.get(IslamicCalendar.DAY_OF_MONTH))
    }

    fun isRamadan(date: LocalDate): Boolean = of(date).month == 9

    companion object {
        val MONTHS = listOf(
            "محرم", "صفر", "ربيع الأول", "ربيع الآخر", "جمادى الأولى", "جمادى الآخرة",
            "رجب", "شعبان", "رمضان", "شوال", "ذو القعدة", "ذو الحجة",
        )
    }
}
