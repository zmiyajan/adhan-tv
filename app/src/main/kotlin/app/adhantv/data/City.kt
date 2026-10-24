package app.adhantv.data

import app.adhantv.prayer.Place
import java.time.ZoneId
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/** A row of the bundled GeoNames list (assets/cities.tsv, built by tools/cities.py). */
data class City(
    val id: Long,
    val arabic: String,
    val english: String,
    val country: String,
    val latitude: Double,
    val longitude: Double,
    val zone: String,
) {
    /** Arabic name when GeoNames has one, otherwise the English name. */
    val name: String get() = arabic.ifEmpty { english }

    val place: Place get() = Place(latitude, longitude, ZoneId.of(zone))

    /** Within [km] of a (latitude, longitude) point, by great-circle distance. */
    fun near(point: Pair<Double, Double>, km: Double): Boolean {
        val (lat, lon) = point
        val dLat = Math.toRadians(lat - latitude)
        val dLon = Math.toRadians(lon - longitude)
        val h = sin(dLat / 2).pow(2) + cos(Math.toRadians(latitude)) * cos(Math.toRadians(lat)) * sin(dLon / 2).pow(2)
        return 2 * 6371 * asin(sqrt(h)) <= km
    }
}
