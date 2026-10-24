package app.adhantv.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** A small Kaaba: a black cube with the gold band of the kiswa and its door, in the theme's accent. */
@Composable
fun KaabaIcon(size: Dp = 14.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        drawRoundRect(Color(0xFF111111), Offset(w * .08f, h * .08f), Size(w * .84f, h * .84f), CornerRadius(w * .06f))
        drawRoundRect(Palette.mute, Offset(w * .08f, h * .08f), Size(w * .84f, h * .84f), CornerRadius(w * .06f), style = androidx.compose.ui.graphics.drawscope.Stroke(w * .05f))
        drawRect(Palette.gold, Offset(w * .08f, h * .28f), Size(w * .84f, h * .1f))                 // the band
        drawRect(Palette.gold, Offset(w * .6f, h * .52f), Size(w * .16f, h * .3f))                  // the door
    }
}
