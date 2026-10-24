package app.adhantv.prayer

import com.batoulapps.adhan2.CalculationMethod
import com.batoulapps.adhan2.CalculationParameters

/** Calculation methods offered in the app; each maps to the batoulapps adhan definition. */
enum class Method(internal val source: CalculationMethod) {
    UMM_AL_QURA(CalculationMethod.UMM_AL_QURA),
    MUSLIM_WORLD_LEAGUE(CalculationMethod.MUSLIM_WORLD_LEAGUE),
    EGYPTIAN(CalculationMethod.EGYPTIAN),
    KARACHI(CalculationMethod.KARACHI),
    DUBAI(CalculationMethod.DUBAI),
    KUWAIT(CalculationMethod.KUWAIT),
    QATAR(CalculationMethod.QATAR),
    SINGAPORE(CalculationMethod.SINGAPORE),
    TURKEY(CalculationMethod.TURKEY),
    NORTH_AMERICA(CalculationMethod.NORTH_AMERICA),
    ;

    /** Umm al-Qura puts Isha 120 min after Maghrib in Ramadan instead of 90 (official KACST timetable). */
    internal fun parameters(ramadan: Boolean): CalculationParameters {
        val p = source.parameters
        return if (this == UMM_AL_QURA && ramadan) p.copy(ishaInterval = RAMADAN_ISHA_MINUTES) else p
    }

    companion object {
        private const val RAMADAN_ISHA_MINUTES = 120

        /** A sensible default per ISO country code; the user can always change it. */
        fun defaultFor(country: String): Method = when (country.uppercase()) {
            "SA", "YE", "BH", "OM" -> UMM_AL_QURA
            "EG", "SD", "LY", "SY", "LB", "IQ", "JO", "PS" -> EGYPTIAN
            "PK", "IN", "BD", "AF" -> KARACHI
            "AE" -> DUBAI
            "KW" -> KUWAIT
            "QA" -> QATAR
            "SG", "MY", "ID", "BN" -> SINGAPORE
            "TR" -> TURKEY
            "US", "CA" -> NORTH_AMERICA
            else -> MUSLIM_WORLD_LEAGUE
        }
    }
}
