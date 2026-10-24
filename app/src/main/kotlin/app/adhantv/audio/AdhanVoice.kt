package app.adhantv.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.core.net.toUri
import app.adhantv.R
import java.io.File

/**
 * An adhan recording the user can choose: the bundled one (sources and licences in CREDITS.md), or the user's own
 * audio files put on the TV in Android/data/app.adhantv/files/ — each file is one choice, named after the file
 * (e.g. «المدني.webm» shows as «المدني»). The user's files never ship with the app.
 * A recorded adhan stays on screen exactly as long as it plays.
 */
data class AdhanVoice(val id: String, val label: String) {
    val bundled: Boolean get() = id == HARAM.id

    private fun file(context: Context): File? = if (bundled) null else File(ownDir(context), id).takeIf { it.exists() }

    fun uri(context: Context): Uri = file(context)?.let(Uri::fromFile) ?: "android.resource://${context.packageName}/${R.raw.adhan_haram}".toUri()

    fun durationMs(context: Context): Long {
        val f = file(context) ?: return HARAM_MS
        val r = MediaMetadataRetriever()   // not AutoCloseable before API 29
        return try {
            r.setDataSource(f.path)
            r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: HARAM_MS
        } catch (e: RuntimeException) {
            HARAM_MS
        } finally {
            r.release()
        }
    }

    companion object {
        val HARAM = AdhanVoice("HARAM", "المسجد الحرام")
        private const val HARAM_MS = 181_500L

        /** The app's own folder (created by the app, so it can list files that adb pushes into it). */
        fun ownDir(context: Context): File? = context.getExternalFilesDir(null)

        private val AUDIO = setOf("mp3", "m4a", "aac", "ogg", "oga", "opus", "webm", "wav", "flac")

        /**
         * The user's own files when there are any (the bundled recording then stays only as the offline fallback),
         * otherwise the bundled one. A file named «المسجد الحرام…» comes first and is the default.
         */
        fun available(context: Context): List<AdhanVoice> {
            val own = ownDir(context)?.listFiles { f -> f.isFile && f.extension.lowercase() in AUDIO }
                ?.sortedWith(compareBy({ !it.name.startsWith(HARAM.label) }, { it.name }))
                ?.map { AdhanVoice(it.name, it.nameWithoutExtension) }
            return own?.takeIf { it.isNotEmpty() } ?: listOf(HARAM)
        }

        /** A saved choice; falls back to the first available adhan if the file is gone. */
        fun of(context: Context, id: String?): AdhanVoice = available(context).let { list -> list.firstOrNull { it.id == id } ?: list.first() }
    }
}
