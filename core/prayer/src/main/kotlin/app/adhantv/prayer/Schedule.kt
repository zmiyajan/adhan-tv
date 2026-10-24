package app.adhantv.prayer

import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate

/** What happens at a moment: the adhan of a prayer, or its iqama. */
data class Event(val prayer: Prayer, val kind: Kind, val at: Instant, val friday: Boolean) {
    enum class Kind { ADHAN, IQAMA }

    /** Friday Dhuhr is Jumu'ah. */
    val jumuah: Boolean get() = friday && prayer == Prayer.DHUHR
}

/** The user's choices that shape the day's events. */
data class ScheduleSettings(
    val method: Method = Method.UMM_AL_QURA,
    val enabled: Set<Prayer> = Prayer.entries.toSet(),
    val iqamaEnabled: Boolean = true,
    val iqamaMinutes: Map<Prayer, Int> = DEFAULT_IQAMA,
) {
    companion object {
        /** Minutes from adhan to iqama: the usual practice in Saudi mosques (editable per prayer). */
        val DEFAULT_IQAMA = mapOf(
            Prayer.FAJR to 25,
            Prayer.DHUHR to 20,
            Prayer.ASR to 20,
            Prayer.MAGHRIB to 10,
            Prayer.ISHA to 20,
        )
    }
}

/**
 * Computes adhan and iqama events for a place. [isRamadan] is supplied by the caller (the Android app uses
 * ICU's Umm al-Qura calendar) so this module stays free of Android.
 */
class Schedule(
    private val place: Place,
    private val settings: ScheduleSettings,
    private val isRamadan: (LocalDate) -> Boolean = { false },
) {
    fun day(date: LocalDate): DayTimes = DayTimes.of(place, settings.method, date, isRamadan(date))

    fun events(date: LocalDate): List<Event> {
        val times = day(date)
        val friday = date.dayOfWeek == DayOfWeek.FRIDAY
        return Prayer.entries.filter { it in settings.enabled }.flatMap { p ->
            val adhan = Event(p, Event.Kind.ADHAN, times[p], friday)
            val minutes = settings.iqamaMinutes[p] ?: 0
            if (settings.iqamaEnabled && !adhan.jumuah && minutes > 0) {
                listOf(adhan, Event(p, Event.Kind.IQAMA, times[p].plus(Duration.ofMinutes(minutes.toLong())), friday))
            } else {
                listOf(adhan)
            }
        }.sortedBy { it.at }
    }

    /** The first event strictly after [now], looking up to two days ahead (covers Isha → next Fajr). */
    fun next(now: Instant): Event? {
        val today = now.atZone(place.zone).toLocalDate()
        return (0L..2L).asSequence()
            .flatMap { events(today.plusDays(it)) }
            .firstOrNull { it.at.isAfter(now) }
    }
}
