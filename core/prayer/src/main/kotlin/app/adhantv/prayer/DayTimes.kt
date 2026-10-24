package app.adhantv.prayer

import com.batoulapps.adhan2.Coordinates
import com.batoulapps.adhan2.PrayerTimes
import com.batoulapps.adhan2.data.DateComponents
import java.time.Instant
import java.time.LocalDate

/** The five adhan times of one local day. */
data class DayTimes(val date: LocalDate, val adhan: Map<Prayer, Instant>) {
    operator fun get(prayer: Prayer): Instant = adhan.getValue(prayer)

    companion object {
        fun of(place: Place, method: Method, date: LocalDate, ramadan: Boolean): DayTimes {
            val t = PrayerTimes(
                Coordinates(place.latitude, place.longitude),
                DateComponents(date.year, date.monthValue, date.dayOfMonth),
                method.parameters(ramadan),
            )
            fun j(i: kotlin.time.Instant) = Instant.ofEpochSecond(i.epochSeconds)
            return DayTimes(
                date,
                mapOf(
                    Prayer.FAJR to j(t.fajr),
                    Prayer.DHUHR to j(t.dhuhr),
                    Prayer.ASR to j(t.asr),
                    Prayer.MAGHRIB to j(t.maghrib),
                    Prayer.ISHA to j(t.isha),
                ),
            )
        }
    }
}
