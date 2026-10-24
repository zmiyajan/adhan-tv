package app.adhantv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import app.adhantv.R

/** The app's colour themes: an accent on a dark background, used by every screen. */
enum class Theme(val label: String, val accent: Long, val centre: Long, val edge: Long, val panel: Long) {
    GOLD("أسود وذهبي", 0xFFBBA27A, 0xFF1A191C, 0xFF0A0A0B, 0xFF1C1C1F),
    EMERALD("زمردي", 0xFF7CC4A0, 0xFF0F201A, 0xFF060D0A, 0xFF13251E),
    NAVY("كحلي", 0xFF8FB4E8, 0xFF111A2B, 0xFF070B13, 0xFF152034),
    SILVER("فضي", 0xFFD8D6D0, 0xFF1C1C1E, 0xFF0B0B0C, 0xFF202022),
}

/** Current colours. Reading them inside a composable recomposes it when the theme changes. */
object Palette {
    var theme by mutableStateOf(Theme.GOLD)

    val gold get() = Color(theme.accent)
    val bgCentre get() = Color(theme.centre)
    val bgEdge get() = Color(theme.edge)
    val panel get() = Color(theme.panel)
    val ink = Color(0xFFECE7DE)
    val mute = Color(0x96ECE7DE)
    val live = Color(0xFFE5484D)
}

object Fonts {
    val amiri = FontFamily(Font(R.font.amiri_bold))
    val hafs = FontFamily(Font(R.font.kfgqpc_hafs_v30))   // official KFGQPC file, unmodified
    val plex = FontFamily(Font(R.font.plex_arabic))
}

@Composable
fun AdhanTvTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Palette.gold,
            onPrimary = Color.Black,
            background = Palette.bgEdge,
            surface = Palette.bgCentre,
            onSurface = Palette.ink,
            onBackground = Palette.ink,
        ),
        content = content,
    )
}
