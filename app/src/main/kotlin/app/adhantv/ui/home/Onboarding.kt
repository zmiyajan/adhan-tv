package app.adhantv.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import app.adhantv.data.AppSettings
import app.adhantv.ui.theme.Fonts
import app.adhantv.ui.theme.Palette

/** Two short slides on first launch: what the app does, then the two things it needs (city, showing over apps). */
@Composable
fun Onboarding(s: AppSettings, status: String?, resumes: Int, act: HomeActions, onChooseCity: () -> Unit) {
    var page by remember { mutableIntStateOf(0) }
    val primary = remember { FocusRequester() }
    LaunchedEffect(page) { runCatching { primary.requestFocus() } }   // the main button has focus on every slide
    BackHandler(enabled = page == 1) { page = 0 }
    val context = LocalContext.current
    val overApps = remember(resumes) { Permissions.canShowOverApps(context) }
    // On the first slide, left or right also moves on; Back on the second slide goes back.
    val swipe = Modifier.onPreviewKeyEvent {
        val arrow = it.key == Key.DirectionLeft || it.key == Key.DirectionRight
        if (page == 0 && arrow && it.type == KeyEventType.KeyDown) { page = 1; true } else false
    }
    Box(Modifier.fillMaxSize().then(swipe).padding(horizontal = 120.dp, vertical = 50.dp)) {
        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
            if (page == 0) {
                Text("أذان التلفاز", style = TextStyle(fontFamily = Fonts.amiri, fontSize = 52.sp, color = Palette.gold))
                Spacer(Modifier.height(14.dp))
                Body("وقت كل صلاة، يوقف اللي شغّال على التلفزيون ويرفع الأذان على الشاشة.\nوبعدها بوقت يذكّرك بالإقامة.")
                Spacer(Modifier.height(26.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Chip("مجاني وبدون إعلانات")
                    Chip("تقويم أم القرى")
                    Chip("كل شي بالريموت")
                }
                Spacer(Modifier.height(36.dp))
                Button(onClick = { page = 1 }, modifier = Modifier.focusRequester(primary)) { Text("التالي", style = TextStyle(fontFamily = Fonts.plex, fontSize = 18.sp)) }
            } else {
                Text("خطوتين وتبدأ", style = TextStyle(fontFamily = Fonts.amiri, fontSize = 44.sp, color = Palette.gold))
                Spacer(Modifier.height(18.dp))
                Step("1", "مدينتك: ${s.city.name}${if (s.cityConfirmed) "" else " (تقديري)"}")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = act.locate, modifier = if (s.cityConfirmed) Modifier else Modifier.focusRequester(primary)) { Text("حدّد مدينتي تلقائياً", style = TextStyle(fontFamily = Fonts.plex, fontSize = 16.sp)) }
                    Button(onClick = onChooseCity) { Text("اختر من القائمة", style = TextStyle(fontFamily = Fonts.plex, fontSize = 16.sp)) }
                }
                Spacer(Modifier.height(22.dp))
                Step("2", if (overApps) "الظهور فوق التطبيقات: مفعّل ✓" else "اسمح له يظهر فوق التطبيقات، عشان يطلع الأذان فوق اللي شغّال")
                if (!overApps) Button(onClick = act.overApps) { Text("تفعيل", style = TextStyle(fontFamily = Fonts.plex, fontSize = 16.sp)) }
                status?.let { Note(it, Palette.gold, 16) }
                Spacer(Modifier.height(30.dp))
                Button(onClick = act.finishOnboarding, modifier = if (s.cityConfirmed) Modifier.focusRequester(primary) else Modifier) { Text("ابدأ", style = TextStyle(fontFamily = Fonts.plex, fontSize = 18.sp)) }
            }
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(2) { Box(Modifier.size(8.dp).background(if (it == page) Palette.gold else Palette.mute, CircleShape)) }
            }
        }
    }
}

@Composable
private fun Body(text: String) {
    Text(text, style = TextStyle(fontFamily = Fonts.plex, fontSize = 22.sp, color = Palette.ink, textAlign = TextAlign.Center, lineHeight = 36.sp))
}

@Composable
private fun Chip(text: String) {
    Text(
        text,
        style = TextStyle(fontFamily = Fonts.plex, fontSize = 15.sp, color = Palette.mute),
        modifier = Modifier.background(Palette.panel, CircleShape).padding(horizontal = 16.dp, vertical = 6.dp),
    )
}

@Composable
private fun Step(n: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
        Box(Modifier.size(30.dp).background(Palette.gold, CircleShape), contentAlignment = Alignment.Center) {
            Text(n, style = TextStyle(fontFamily = Fonts.plex, fontSize = 15.sp, color = Palette.bgEdge))
        }
        Spacer(Modifier.width(12.dp))
        Text(text, style = TextStyle(fontFamily = Fonts.plex, fontSize = 19.sp, color = Palette.ink))
    }
}
