package app.adhantv.prayer

import java.time.ZoneId

/** Where the times are for. Times are always computed in the place's own time zone, not the device's. */
data class Place(
    val latitude: Double,
    val longitude: Double,
    val zone: ZoneId,
)
