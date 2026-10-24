package app.adhantv.prayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class ScheduleTest {
    private val riyadhZone = ZoneId.of("Asia/Riyadh")
    private val makkah = Place(21.426666, 39.831666, riyadhZone)
    private val ramadan1447 = LocalDate.of(2026, 2, 18)..LocalDate.of(2026, 3, 19)
    private val schedule = Schedule(makkah, ScheduleSettings()) { it in ramadan1447 }

    /** Official Umm al-Qura times for Makkah (ummulqura.org.sa, KACST): date → Fajr, Dhuhr, Asr, Maghrib, Isha. */
    private val official = mapOf(
        "2026-02-18" to "05:33 12:35 15:53 18:20 20:20",
        "2026-02-27" to "05:27 12:34 15:55 18:25 20:25",
        "2026-03-08" to "05:20 12:32 15:55 18:28 20:28",
        "2026-03-19" to "05:10 12:29 15:54 18:32 20:32",
        "2026-10-12" to "05:00 12:08 15:29 17:59 19:29",
        "2026-10-16" to "05:01 12:07 15:27 17:56 19:26",
        "2026-10-24" to "05:04 12:05 15:23 17:50 19:20",
        "2026-11-10" to "05:11 12:05 15:17 17:41 19:11",
    )

    @Test fun matchesOfficialMakkahTimesWithinOneMinute() {
        for ((date, row) in official) {
            val day = schedule.day(LocalDate.parse(date))
            Prayer.entries.zip(row.split(" ")).forEach { (prayer, hhmm) ->
                val expected = LocalDateTime.of(day.date, LocalTime.parse(hhmm)).atZone(riyadhZone).toInstant()
                val diff = Duration.between(expected, day[prayer]).abs()
                assertTrue("$date $prayer: off by $diff", diff <= Duration.ofMinutes(1))
            }
        }
    }

    @Test fun ishaIs120MinutesAfterMaghribInRamadanAnd90Otherwise() {
        fun gap(date: String) = schedule.day(LocalDate.parse(date)).let {
            Duration.between(it[Prayer.MAGHRIB], it[Prayer.ISHA]).toMinutes()
        }
        assertEquals(120, gap("2026-03-01"))
        assertEquals(90, gap("2026-10-24"))
    }

    @Test fun fridayDhuhrIsJumuahWithNoIqama() {
        val friday = LocalDate.of(2026, 10, 23)
        val events = schedule.events(friday)
        val dhuhr = events.filter { it.prayer == Prayer.DHUHR }
        assertEquals(listOf(Event.Kind.ADHAN), dhuhr.map { it.kind })
        assertTrue(dhuhr.single().jumuah)
        assertEquals(9, events.size)
    }

    @Test fun iqamaFollowsTheConfiguredMinutes() {
        val events = schedule.events(LocalDate.of(2026, 10, 24))
        val fajr = events.filter { it.prayer == Prayer.FAJR }
        assertEquals(25, Duration.between(fajr[0].at, fajr[1].at).toMinutes())
        val maghrib = events.filter { it.prayer == Prayer.MAGHRIB }
        assertEquals(10, Duration.between(maghrib[0].at, maghrib[1].at).toMinutes())
    }

    @Test fun nextAfterIshaIqamaIsTomorrowsFajr() {
        val day = LocalDate.of(2026, 10, 24)
        val lastToday = schedule.events(day).last()
        val next = schedule.next(lastToday.at)!!
        assertEquals(Prayer.FAJR, next.prayer)
        assertEquals(Event.Kind.ADHAN, next.kind)
        assertEquals(day.plusDays(1), next.at.atZone(riyadhZone).toLocalDate())
    }

    @Test fun disabledPrayersProduceNoEvents() {
        val noFajr = Schedule(makkah, ScheduleSettings(enabled = Prayer.entries.toSet() - Prayer.FAJR))
        assertFalse(noFajr.events(LocalDate.of(2026, 10, 24)).any { it.prayer == Prayer.FAJR })
    }
}
