package app.adhantv.ui.adhan

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.foundation.text.BasicText
import app.adhantv.data.AppSettings
import app.adhantv.data.Verses
import app.adhantv.prayer.Event
import app.adhantv.ui.theme.Fonts
import app.adhantv.ui.theme.Palette
import app.adhantv.ui.arabic
import app.adhantv.ui.clockText
import app.adhantv.ui.title

/**
 * The adhan screen, laid out on a 1920 × 1080 design grid and scaled to the TV: title, a short gold rule, the
 * verse in the KFGQPC font, its reference, optionally the next prayer, the radio label (when the radio plays) and a
 * line that shrinks to the centre over the whole time on screen. Colour, size and the line follow the settings.
 */
@Composable
fun AdhanScreen(event: Event, verse: Verses.Verse, showMs: Long, settings: AppSettings, next: Event?, live: Boolean) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val u = maxWidth / 1920f                                     // one design pixel
        val density = LocalDensity.current
        val scale = settings.textSize.scale
        fun text(px: Float): TextUnit = with(density) { (u * px * scale).toSp() }
        val barColor = Palette.gold
        val zone = settings.city.place.zone
        val adhan = event.kind == Event.Kind.ADHAN
        val widthPx = with(density) { maxWidth.toPx() }

        Box(
            Modifier.fillMaxSize().background(
                Brush.radialGradient(
                    listOf(Palette.bgCentre, Palette.bgEdge),
                    center = Offset(widthPx * .5f, with(density) { maxHeight.toPx() } * .45f),
                    radius = widthPx * 1100f / 1920f,
                ),
            ),
        )

        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            Line(event.title, Fonts.amiri, text(104f), Palette.ink)
            Spacer(Modifier.height(u * 34f))
            Box(Modifier.width(u * 300f).height(maxOf(u * 1.5f, Dp.Hairline)).background(Palette.gold.copy(alpha = .55f)))
            Spacer(Modifier.height(u * 30f))
            Line("﴿${verse.text}﴾", Fonts.hafs, text(76f), Palette.gold)
            Spacer(Modifier.height(u * 6f))
            Line("${verse.name} · ${isolateLtr(verse.ayah.toString())}", Fonts.plex, text(28f), Palette.mute)
        }

        if (live) {
            Row(
                Modifier.align(Alignment.TopCenter).offset(y = u * 945f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(u * 14f),
            ) {
                Box(Modifier.size(u * 12f).clip(CircleShape).background(Palette.live))
                Line("إذاعة القرآن الكريم · مباشر", Fonts.plex, text(30f), Palette.mute)
            }
        }

        if (!settings.showBar) return@BoxWithConstraints
        val left = remember { Animatable(1f) }
        LaunchedEffect(Unit) { left.animateTo(0f, tween(showMs.toInt(), easing = LinearEasing)) }
        val bar = Modifier.align(Alignment.TopCenter).offset(y = u * (if (adhan) 895f else 960f))
            .width(u * 900f).height(u * 8f).clip(RoundedCornerShape(u * 4f))
        Box(bar.background(barColor.copy(alpha = .16f)))
        Box(
            bar.graphicsLayer {
                scaleX = left.value
                transformOrigin = TransformOrigin.Center
            }.background(barColor),
        )
    }
}

@Composable
private fun Line(text: String, font: FontFamily, size: TextUnit, color: Color) {
    BasicText(text, style = TextStyle(fontFamily = font, fontSize = size, color = color, textAlign = TextAlign.Center))
}

/** Keeps a number left-to-right inside Arabic text (Unicode LRI … PDI). */
private fun isolateLtr(s: String) = Char(0x2066) + s + Char(0x2069)
