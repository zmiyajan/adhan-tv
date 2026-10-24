package app.adhantv.data

import android.content.Context
import java.text.Normalizer
import kotlin.math.cos
import kotlin.math.pow

/** The offline city list, loaded on first use. Rows are sorted by population, largest first. */
class Cities(private val context: Context) {
    val all: List<City> by lazy {
        context.assets.open("cities.tsv").bufferedReader().useLines { lines ->
            lines.mapNotNull(City::fromRow).toList()
        }
    }

    fun byId(id: Long): City? = all.firstOrNull { it.id == id }

    /** Nearest city by equirectangular distance (plenty accurate at city scale). */
    fun nearest(latitude: Double, longitude: Double): City = all.minBy {
        val x = (it.longitude - longitude) * cos(Math.toRadians(latitude))
        (it.latitude - latitude).pow(2) + x.pow(2)
    }

    /** The largest city in a time zone: a reasonable guess before the user picks one. */
    fun largestIn(zone: String): City? = all.firstOrNull { it.zone == zone }

    /** Cities whose Arabic or English name contains [query] (diacritics and hamza forms ignored). */
    fun search(query: String, country: String?, limit: Int = 60): List<City> {
        val q = fold(query)
        if (q.isEmpty()) return all.asSequence().filter { country == null || it.country == country }.take(limit).toList()
        return all.asSequence().filter { fold(it.arabic).contains(q) || fold(it.english).contains(q) }.take(limit).toList()
    }

    private fun fold(s: String): String = Normalizer.normalize(s, Normalizer.Form.NFD)
        .replace(Regex("[\\p{Mn}\\u0640]"), "")            // tashkeel, tatweel, Latin accents
        .replace(Regex("[أإآٱ]"), "ا").replace('ة', 'ه').replace('ى', 'ي')
        .lowercase().trim()
}
