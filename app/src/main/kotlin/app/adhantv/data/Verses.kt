package app.adhantv.data

import android.content.Context
import org.json.JSONArray

/** Verses shown on the adhan screen, copied from the official KFGQPC data by tools/verses.py. */
class Verses(private val context: Context) {
    data class Verse(val surah: Int, val ayah: Int, val name: String, val text: String)

    val adhan: Verse by lazy {
        val a = JSONArray(context.assets.open("verses.json").bufferedReader().readText())
        (0 until a.length()).map { a.getJSONObject(it) }.first { it.getString("use") == "adhan" }.let {
            Verse(it.getInt("surah"), it.getInt("ayah"), it.getString("name"), it.getString("text"))
        }
    }
}
