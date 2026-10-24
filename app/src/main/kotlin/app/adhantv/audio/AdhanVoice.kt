package app.adhantv.audio

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import app.adhantv.R
import androidx.core.net.toUri
import java.io.File

/**
 * The adhan recordings the user can choose from. A recorded adhan stays on screen exactly as long as it plays.
 * Bundled sources and licences are in CREDITS.md. [CUSTOM] is a file the user puts on the TV themselves
 * (Android/data/app.adhantv/files/adhan.mp3, .m4a, .webm or .ogg); it never ships with the app.
 */
enum class AdhanVoice(val label: String, val res: Int, val durationMs: Long) {
    HARAM(label = "المسجد الحرام", res = R.raw.adhan_haram, durationMs = 181_500),
    CUSTOM(label = "أذانك الخاص", res = 0, durationMs = 0),
    ;

    fun uri(context: Context): Uri =
        if (this == CUSTOM && customFile(context).exists()) Uri.fromFile(customFile(context))
        else "android.resource://${context.packageName}/${HARAM.res}".toUri()

    fun durationMs(context: Context): Long =
        if (this == CUSTOM && customFile(context).exists()) {
            val r = MediaMetadataRetriever()   // not AutoCloseable before API 29
            try {
                r.setDataSource(customFile(context).path)
                r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()
            } catch (e: RuntimeException) {
                null
            } finally {
                r.release()
            } ?: HARAM.durationMs
        } else {
            HARAM.takeIf { this == CUSTOM }?.durationMs ?: durationMs
        }

    companion object {
        /** The user's own file: adhan.mp3, adhan.m4a, adhan.webm, adhan.ogg … (the first one found). */
        fun customFile(context: Context): File {
            val dir = context.getExternalFilesDir(null)
            return dir?.listFiles { f -> f.nameWithoutExtension == "adhan" }?.minByOrNull { it.name } ?: File(dir, "adhan.mp3")
        }

        /** The voices to offer: the user's own file only when it is there. */
        fun available(context: Context) = entries.filter { it != CUSTOM || customFile(context).exists() }
    }
}
