package app.adhantv.audio

import app.adhantv.R

/**
 * The bundled adhan recordings the user can choose from. [durationMs] is the file's length: a recorded adhan
 * stays on screen exactly as long as it plays. Sources and licences are in CREDITS.md.
 */
enum class AdhanVoice(val label: String, val res: Int, val durationMs: Long) {
    HARAM(label = "المسجد الحرام", res = R.raw.adhan_haram, durationMs = 181_500),
}
