package app.adhantv.ui

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import app.adhantv.ui.theme.Palette

/** Keeps the TV screen on while this activity is showing. */
fun Activity.keepScreenOn() = window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

/** Sizes on the 1920 × 1080 design grid of the full-screen pages. */
class Grid(val u: Dp, private val px: Float, private val density: androidx.compose.ui.unit.Density) {
    fun text(size: Float): TextUnit = with(density) { (px * size).toSp() }
}

/** The black-and-gold radial background of the adhan screen, right-to-left, with content on the design grid. */
@Composable
fun FullScreen(content: @Composable BoxWithConstraintsScope.(Grid) -> Unit) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) { BoxWithConstraints(Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val w = with(density) { maxWidth.toPx() }
        val h = with(density) { maxHeight.toPx() }
        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(listOf(Palette.bgCentre, Palette.bgEdge), center = Offset(w * .5f, h * .45f), radius = w * 1100f / 1920f),
            ),
        )
        content(Grid(maxWidth / 1920f, w / 1920f, density))
    } }
}

@Composable
fun Label(text: String, font: FontFamily, size: TextUnit, color: Color, modifier: Modifier = Modifier) {
    BasicText(text, modifier, style = TextStyle(fontFamily = font, fontSize = size, color = color, textAlign = TextAlign.Center))
}
