package app.adhantv.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.CompositionLocalProvider
import androidx.tv.material3.Text
import app.adhantv.ui.theme.Fonts
import app.adhantv.ui.theme.Palette
import kotlinx.coroutines.launch

/** One entry of «عن البرنامج»: a short line in the list, and a full page with its sources and licence. */
class Credit(
    val title: String,
    val summary: String,
    val body: String,
    val links: List<Pair<String, String>> = emptyList(),
    /** A file in assets/legal/ shown in full (licence texts, the changelog). */
    val file: String? = null,
)

/** Where the project lives. */
const val REPO = "https://github.com/zmiyajan/adhan-tv"

val CREDITS = listOf(
    Credit(
        "ما الجديد", "سجل التغييرات في كل إصدار",
        "كل تعديل على البرنامج ينكتب هنا تلقائياً مع رقم الإصدار.",
        listOf("سجل التغييرات" to "$REPO/blob/main/CHANGELOG.md"), file = "CHANGELOG.md",
    ),
    Credit(
        "الكود المصدري", "مفتوح للجميع على GitHub",
        "كل كود البرنامج منشور، وأي أحد يقدر يقرأه ويعدّل عليه ويقترح تحسينات. انبنى بمساعدة أدوات الذكاء الاصطناعي، " +
            "والكود مرتّب وموثّق، فالتعديل عليه سهل.",
        listOf("الريبو" to REPO),
    ),
    Credit(
        "الرخصة", "MIT",
        "تقدر تستخدم الكود وتعدّله وتنشره بحرية. للخطوط والمحتوى رخصها الخاصة، وهي تحت.",
        listOf("نص الرخصة" to "$REPO/blob/main/LICENSE"), file = "LICENSE",
    ),
    Credit(
        "خط المصحف ونص الآيات", "مجمع الملك فهد لطباعة المصحف الشريف",
        "خط حفص (الإصدار 3) ونص الآيات من منصة المطورين في المجمع. الخط موجود في البرنامج بملفه الأصلي بدون أي تعديل، " +
            "والآيات تنسحب برمجياً من ملفات المجمع، ما تنكتب باليد. رخصة المجمع تسمح بالاستخدام والنسخ والتوزيع مجاناً، " +
            "وما تسمح بتعديل الخط ولا بيعه.",
        listOf("منصة المطورين" to "https://qurancomplex.gov.sa/quran-dev/"), file = "KFGQPC-HAFS-EULA.txt",
    ),
    Credit(
        "أوقات الصلاة", "تقويم أم القرى",
        "الأوقات تنحسب في التلفزيون نفسه بدون نت، بمكتبة adhan المفتوحة (رخصة MIT)، على تقويم أم القرى الرسمي " +
            "(العشاء في رمضان بعد المغرب بساعتين). مختبرة ضد أوقات التقويم الرسمية بفرق دقيقة كحد أقصى.",
        listOf("تقويم أم القرى" to "https://www.ummulqura.org.sa/", "مكتبة adhan" to "https://github.com/batoulapps/adhan-kotlin"),
    ),
    Credit(
        "الأذان المسجّل", "المسجد الحرام، CC BY 3.0",
        "أذان المغرب في المسجد الحرام، تسجيل 3omar Faruq على Wikimedia Commons، برخصة CC BY 3.0. " +
            "أخذنا الصوت بس، وقصّينا الأذان من التسجيل، ورفعنا الصوت.",
        listOf(
            "التسجيل" to "https://commons.wikimedia.org/wiki/File:Maghrib_Adhan_at_the_Masjid_al_Haram,_Mecca_-_25_Feb,_2012.webm",
            "الرخصة" to "https://creativecommons.org/licenses/by/3.0/",
        ),
    ),
    Credit(
        "إذاعة القرآن الكريم", "هيئة الإذاعة والتلفزيون",
        "البث المباشر لإذاعة القرآن الكريم من المملكة العربية السعودية. البرنامج يشغّل البث بس، وما يسجّله ولا يحفظه.",
        listOf("هيئة الإذاعة والتلفزيون" to "https://sba.sa/"),
    ),
    Credit(
        "قائمة المدن", "GeoNames، CC BY 4.0",
        "أكثر من 34 ألف مدينة بأسمائها العربية ومناطقها الزمنية، من GeoNames برخصة CC BY 4.0.",
        listOf("GeoNames" to "https://www.geonames.org/", "الرخصة" to "https://creativecommons.org/licenses/by/4.0/"),
    ),
    Credit(
        "خط أميري", "SIL Open Font License",
        "خط العناوين، من مشروع أميري، برخصة SIL OFL 1.1.",
        listOf("أميري" to "https://github.com/aliftype/amiri"), file = "Amiri-OFL.txt",
    ),
    Credit(
        "خط IBM Plex Sans Arabic", "SIL Open Font License",
        "خط النصوص، من IBM، برخصة SIL OFL 1.1.",
        listOf("IBM Plex" to "https://github.com/IBM/plex"), file = "IBMPlexSansArabic-OFL.txt",
    ),
)

/** The full page of one credit. Up and down scroll; Back returns to the list. */
@Composable
fun AboutDetail(c: Credit, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val text = remember(c) { c.file?.let { f -> runCatching { context.assets.open("legal/$f").bufferedReader().readText() }.getOrNull() } }
    val scroll = rememberScrollState()
    val scope = rememberCoroutineScope()
    val focus = remember { FocusRequester() }
    var focused by remember { mutableStateOf(false) }
    LaunchedEffect(c) { focus.requestFocus() }
    Column(
        Modifier.fillMaxWidth().heightIn(max = 470.dp)
            .focusRequester(focus).onFocusChanged { focused = it.isFocused }.focusable()
            .onKeyEvent {
                if (it.type != KeyEventType.KeyDown) return@onKeyEvent false
                val by = when (it.key) {
                    Key.DirectionDown -> 160f
                    Key.DirectionUp -> -160f
                    else -> return@onKeyEvent false
                }
                scope.launch { scroll.animateScrollBy(by) }
                true
            }
            .verticalScroll(scroll),
    ) {
        Text(c.title, style = TextStyle(fontFamily = Fonts.amiri, fontSize = 26.sp, color = Palette.gold))
        Spacer(Modifier.height(8.dp))
        Text(c.body, style = TextStyle(fontFamily = Fonts.plex, fontSize = 18.sp, color = Palette.ink, lineHeight = 30.sp))
        if (c.links.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(26.dp)) {
                c.links.forEach { (label, url) ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(150.dp)) {
                        Qr(url, 110.dp)
                        Text(label, style = TextStyle(fontFamily = Fonts.plex, fontSize = 14.sp, color = Palette.mute), modifier = Modifier.padding(top = 6.dp))
                    }
                }
            }
            Text(
                "صوّر الرمز بجوالك عشان تفتح الرابط.",
                style = TextStyle(fontFamily = Fonts.plex, fontSize = 14.sp, color = Palette.mute),
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        text?.let {
            Spacer(Modifier.height(18.dp))
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(
                    it,
                    style = TextStyle(fontSize = 13.sp, color = Palette.mute, lineHeight = 20.sp),
                    modifier = Modifier.fillMaxWidth().border(1.dp, Palette.panel, RoundedCornerShape(10.dp)).padding(16.dp),
                )
            }
        }
        Text(
            if (focused) "▲ ▼ للتمرير · رجوع للقائمة" else "",
            style = TextStyle(fontFamily = Fonts.plex, fontSize = 13.sp, color = Palette.mute),
            modifier = Modifier.padding(top = 10.dp),
        )
    }
}
