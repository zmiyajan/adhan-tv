package app.adhantv.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import app.adhantv.R

/** Black and gold, as on the room's adhan screen. */
object Palette {
    val gold = Color(0xFFBBA27A)
    val ink = Color(0xFFECE7DE)
    val mute = Color(0x96ECE7DE)
    val live = Color(0xFFE5484D)
    val bgCentre = Color(0xFF1A191C)
    val bgEdge = Color(0xFF0A0A0B)
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
