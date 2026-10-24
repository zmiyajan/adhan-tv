package app.adhantv.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.focusGroup
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.DenseListItem
import androidx.tv.material3.ListItem
import androidx.tv.material3.Text
import app.adhantv.audio.AdhanVoice
import app.adhantv.data.AppSettings
import app.adhantv.data.BarColor
import app.adhantv.data.Cities
import app.adhantv.data.City
import app.adhantv.data.Hijri
import app.adhantv.data.SettingsRepository
import app.adhantv.data.SoundSource
import app.adhantv.data.TextSize
import app.adhantv.prayer.Event
import app.adhantv.prayer.Method
import app.adhantv.prayer.Prayer
import app.adhantv.schedule.AlarmScheduler
import app.adhantv.ui.arabic
import app.adhantv.ui.clockText
import app.adhantv.ui.theme.Fonts
import app.adhantv.ui.theme.Palette
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalTime
import java.time.temporal.ChronoUnit

private enum class Section(val title: String) {
    TODAY("اليوم"),
    PRAYERS("الصلوات"),
    ADHAN("الأذان"),
    IQAMA("الإقامة"),
    LOOK("الشكل"),
    PAUSE("إيقاف مؤقت"),
    CHECK("الأذونات"),
    ABOUT("عن البرنامج"),
}

/** Menu on the right (RTL), the chosen section's settings beside it. Every row works with the remote's OK. */
@Composable
fun HomeScreen(
    settings: SettingsRepository,
    scheduler: AlarmScheduler,
    cities: Cities,
    hijri: Hijri,
    status: String?,
    resumes: Int,
    onChange: ((AppSettings) -> AppSettings) -> Unit,
    onLocate: () -> Unit,
    onPick: (City) -> Unit,
    onTry: (Event.Kind) -> Unit,
    onOverApps: () -> Unit,
    onExact: () -> Unit,
    onBoard: () -> Unit,
    onRadio: () -> Unit,
    onRestart: () -> Unit,
    onReset: () -> Unit,
) {
    val s = settings.settings.collectAsState(initial = null).value ?: return
    var picking by remember { mutableStateOf(false) }
    var section by remember { mutableStateOf(Section.TODAY) }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(Modifier.fillMaxSize().background(Palette.bgEdge)) {
            if (picking) {
                CityPicker(cities, s.city.country, onPick = { onPick(it); picking = false }, onBack = { picking = false })
                return@Box
            }
            Row(Modifier.fillMaxSize()) {
                Column(
                    // Coming back from the settings pane lands on the section you were in, not the nearest item.
                    Modifier.width(250.dp).fillMaxHeight().background(Palette.bgCentre).padding(vertical = 26.dp, horizontal = 14.dp)
                        .focusRestorer().focusGroup(),
                ) {
                    Text(
                        "أذان التلفاز",
                        style = TextStyle(fontFamily = Fonts.amiri, fontSize = 28.sp, color = Palette.gold),
                        modifier = Modifier.padding(start = 14.dp, bottom = 10.dp),
                    )
                    Section.entries.forEach { sec ->
                        DenseListItem(
                            selected = section == sec,
                            onClick = { section = sec },
                            modifier = Modifier.onFocusChanged { if (it.isFocused) section = sec },
                            headlineContent = { Text(sec.title, style = TextStyle(fontFamily = Fonts.plex, fontSize = 18.sp)) },
                        )
                    }
                }
                LazyColumn(
                    Modifier.weight(1f).fillMaxHeight().padding(horizontal = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    item { Spacer(Modifier.height(34.dp)) }
                    item { Text(section.title, style = TextStyle(fontFamily = Fonts.amiri, fontSize = 30.sp, color = Palette.ink)) }
                    item { Spacer(Modifier.height(10.dp)) }
                    when (section) {
                        Section.TODAY -> item { Today(s, scheduler, hijri, onTry, onBoard, onRadio) }
                        Section.PRAYERS -> item { Prayers(s, onChange, onLocate) { picking = true } }
                        Section.ADHAN -> item { Adhan(s, onChange) }
                        Section.IQAMA -> item { Iqama(s, onChange) }
                        Section.LOOK -> item { Look(s, onChange) }
                        Section.PAUSE -> item { Pause(s, onChange) }
                        Section.CHECK -> item { Check(resumes, onOverApps, onExact, onRestart, onReset) }
                        Section.ABOUT -> item { About() }
                    }
                    status?.let { msg ->
                        item { Text(msg, style = TextStyle(fontFamily = Fonts.plex, fontSize = 18.sp, color = Palette.gold), modifier = Modifier.padding(top = 14.dp)) }
                    }
                    item { Spacer(Modifier.height(34.dp)) }
                }
            }
        }
    }
}

@Composable
private fun Today(
    s: AppSettings,
    scheduler: AlarmScheduler,
    hijri: Hijri,
    onTry: (Event.Kind) -> Unit,
    onBoard: () -> Unit,
    onRadio: () -> Unit,
) {
    var now by remember { mutableStateOf(Instant.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(20_000)
            now = Instant.now()
        }
    }
    val zone = s.city.place.zone
    val schedule = remember(s) { scheduler.schedule(s) }
    val date = now.atZone(zone).toLocalDate()
    val day = remember(s, date) { schedule.day(date) }
    val events = remember(s, date) { schedule.events(date) }
    val next = remember(s, now) { schedule.next(now) }
    val h = hijri.of(date)

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            "${s.city.name}${if (s.cityConfirmed) "" else " (تقديري)"} · ${h.day} ${Hijri.MONTHS[h.month - 1]} ${h.year}",
            style = TextStyle(fontFamily = Fonts.plex, fontSize = 18.sp, color = Palette.mute),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Prayer.entries.forEach { p ->
                val friday = events.firstOrNull { it.prayer == p }?.jumuah == true
                val iqama = events.firstOrNull { it.prayer == p && it.kind == Event.Kind.IQAMA }
                val isNext = next?.prayer == p && next.at.atZone(zone).toLocalDate() == date
                val off = p !in s.enabled
                Column(
                    Modifier.weight(1f).background(Palette.bgCentre, RoundedCornerShape(14.dp))
                        .then(if (isNext) Modifier.border(2.dp, Palette.gold, RoundedCornerShape(14.dp)) else Modifier)
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val c = if (off) Palette.mute else Palette.ink
                    Text(p.arabic(friday), style = TextStyle(fontFamily = Fonts.plex, fontSize = 17.sp, color = Palette.mute))
                    Text(day[p].clockText(zone), style = TextStyle(fontFamily = Fonts.plex, fontSize = 22.sp, color = c), maxLines = 1)
                    Text(
                        when {
                            off -> "متوقف"
                            iqama != null -> "الإقامة ${iqama.at.clockText(zone)}"
                            else -> " "
                        },
                        style = TextStyle(fontFamily = Fonts.plex, fontSize = 13.sp, color = Palette.mute),
                        maxLines = 1,
                    )
                }
            }
        }
        if (s.paused(now)) {
            Text("الأذان موقوف مؤقتاً لين ${s.pausedUntil!!.clockText(zone)}", style = TextStyle(fontFamily = Fonts.plex, fontSize = 18.sp, color = Palette.live))
        } else {
            next?.let {
                val what = if (it.kind == Event.Kind.IQAMA) "إقامة" else "أذان"
                Text("القادم: $what ${it.prayer.arabic(it.jumuah)} ${it.at.clockText(zone)}", style = TextStyle(fontFamily = Fonts.plex, fontSize = 18.sp, color = Palette.gold))
            }
        }
        Column {
            ActionRow("شاشة المسجد", "الساعة وأوقات اليوم والعدّ التنازلي بالشاشة كاملة، تقدر تخليها شغّالة طول اليوم", onBoard)
            ActionRow("استمع لإذاعة القرآن الكريم", "مباشر، وتوقف وقت الأذان", onRadio)
            ActionRow("جرّب الأذان", "يطلع الأذان الحين بالشكل والصوت الحالي") { onTry(Event.Kind.ADHAN) }
            ActionRow("جرّب الإقامة") { onTry(Event.Kind.IQAMA) }
        }
    }
}

@Composable
private fun Prayers(s: AppSettings, onChange: ((AppSettings) -> AppSettings) -> Unit, onLocate: () -> Unit, onChooseCity: () -> Unit) {
    Column {
        Prayer.entries.forEach { p ->
            ToggleRow("أذان ${p.arabic()}", p in s.enabled) { on -> onChange { it.copy(enabled = if (on) it.enabled + p else it.enabled - p) } }
        }
        ActionRow("المدينة: ${s.city.name}", "اختر من القائمة", onChooseCity)
        ActionRow("حدّد مدينتي تلقائياً", "يطلب إذن الموقع التقريبي مرة وحدة، والموقع ما يطلع من التلفزيون", onLocate)
        ChoiceRow("طريقة الحساب", s.method, Method.entries, { it.arabic }, "أم القرى هي التقويم الرسمي في السعودية") { m -> onChange { it.copy(method = m) } }
    }
}

@Composable
private fun Adhan(s: AppSettings, onChange: ((AppSettings) -> AppSettings) -> Unit) {
    Column {
        ChoiceRow("صوت الأذان المسجّل", s.voice, AdhanVoice.entries, { it.label }, "الأذان المسجّل يبقى على الشاشة طول مدة التسجيل") { v ->
            onChange { it.copy(voice = v) }
        }
        ChoiceRow("مدة الأذان مع الإذاعة", s.adhanMinutes, listOf(1, 2, 3, 4, 5), { minutes(it) }) { v -> onChange { it.copy(adhanMinutes = v) } }
        ChoiceRow(
            "مصدر الصوت", s.source, SoundSource.entries, { it.label },
            "الآن: ${if (s.useRadio) "إذاعة القرآن الكريم مباشر" else "أذان الحرم المسجّل"}. لو ما فيه نت يشتغل المسجّل",
        ) { v -> onChange { it.copy(source = v) } }
        Prayer.entries.forEach { p ->
            ToggleRow("صوت أذان ${p.arabic()}", p in s.sound) { on ->
                onChange { it.copy(sound = if (on) it.sound + p else it.sound - p) }
            }
        }
    }
}

@Composable
private fun Iqama(s: AppSettings, onChange: ((AppSettings) -> AppSettings) -> Unit) {
    Column {
        ToggleRow("تنبيه الإقامة", s.iqamaEnabled, "صورة بدون صوت. ما فيه إقامة لصلاة الجمعة") { on -> onChange { it.copy(iqamaEnabled = on) } }
        Prayer.entries.forEach { p ->
            ChoiceRow("إقامة ${p.arabic()} بعد الأذان", s.iqamaMinutes.getValue(p), (5..45 step 5).toList(), { minutesShort(it) }) { v ->
                onChange { it.copy(iqamaMinutes = it.iqamaMinutes + (p to v)) }
            }
        }
        ChoiceRow("مدة شاشة الإقامة", s.iqamaSeconds, listOf(15, 30, 60), { "$it ثانية" }) { v -> onChange { it.copy(iqamaSeconds = v) } }
    }
}

@Composable
private fun Look(s: AppSettings, onChange: ((AppSettings) -> AppSettings) -> Unit) {
    Column {
        ToggleRow("خط العدّ التنازلي", s.showBar, "خط يقصر لين تخلص الشاشة") { on -> onChange { it.copy(showBar = on) } }
        ChoiceRow("لون الخط", s.barColor, BarColor.entries, { it.label }, swatch = { Color(it.argb) }) { v -> onChange { it.copy(barColor = v) } }
        ChoiceRow("حجم الكلام", s.textSize, TextSize.entries, { it.label }, "حسب بُعد الكنبة عن التلفزيون") { v -> onChange { it.copy(textSize = v) } }
        ToggleRow("سطر الصلاة القادمة", s.showNext, "مثل «القادم: أذان العصر 3:23 م» تحت الآية") { on -> onChange { it.copy(showNext = on) } }
    }
}

@Composable
private fun Pause(s: AppSettings, onChange: ((AppSettings) -> AppSettings) -> Unit) {
    val zone = s.city.place.zone
    Column {
        Text(
            if (s.paused()) "موقوف لين ${s.pausedUntil!!.clockText(zone)}" else "الأذان شغّال",
            style = TextStyle(fontFamily = Fonts.plex, fontSize = 18.sp, color = if (s.paused()) Palette.live else Palette.mute),
            modifier = Modifier.padding(bottom = 8.dp),
        )
        ActionRow("أوقفه ساعة") { onChange { it.copy(pausedUntil = Instant.now().plus(1, ChronoUnit.HOURS)) } }
        ActionRow("أوقفه ساعتين") { onChange { it.copy(pausedUntil = Instant.now().plus(2, ChronoUnit.HOURS)) } }
        ActionRow("أوقفه لين بكرة", "يرجع يشتغل الساعة 12 بالليل") {
            val midnight = Instant.now().atZone(zone).toLocalDate().plusDays(1).atTime(LocalTime.MIDNIGHT).atZone(zone).toInstant()
            onChange { it.copy(pausedUntil = midnight) }
        }
        if (s.paused()) ActionRow("رجّعه الحين") { onChange { it.copy(pausedUntil = null) } }
    }
}

@Composable
private fun Check(resumes: Int, onOverApps: () -> Unit, onExact: () -> Unit, onRestart: () -> Unit, onReset: () -> Unit) {
    var confirmReset by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val overApps = remember(resumes) { Permissions.canShowOverApps(context) }
    val exact = remember(resumes) { Permissions.canBeExact(context) }
    Column {
        PermissionRow(overApps, "الظهور فوق التطبيقات", "عشان تطلع شاشة الأذان فوق أي شي شغّال. بدونه يطلع الأذان بس لما يكون البرنامج مفتوح.", onOverApps)
        PermissionRow(exact, "المنبهات والتذكيرات", "عشان يطلع الأذان في دقيقته بالضبط.", onExact)
        Spacer(Modifier.height(10.dp))
        ActionRow("إعادة تشغيل البرنامج", "لو حسّيت إن شي مو طبيعي. ما يغيّر إعداداتك", onRestart)
        ActionRow(
            if (confirmReset) "اضغط مرة ثانية للتأكيد" else "رجّع الإعدادات الأصلية",
            "يرجّع كل الإعدادات مثل أول مرة، ويخلي مدينتك",
        ) {
            if (confirmReset) onReset()
            confirmReset = !confirmReset
        }
    }
}

@Composable
private fun PermissionRow(ok: Boolean, title: String, why: String, onFix: () -> Unit) {
    ListItem(
        selected = false,
        onClick = { if (!ok) onFix() },
        headlineContent = { Text(title, style = TextStyle(fontFamily = Fonts.plex, fontSize = 20.sp)) },
        supportingContent = { Text(if (ok) why else "$why اضغط عشان تفعّله.", style = TextStyle(fontFamily = Fonts.plex, fontSize = 14.sp)) },
        trailingContent = {
            Text(if (ok) "مفعّل ✓" else "غير مفعّل", style = TextStyle(fontFamily = Fonts.plex, fontSize = 18.sp, color = if (ok) Palette.gold else Palette.live))
        },
    )
}

@Composable
private fun About() {
    val credits = listOf(
        "أذان التلفاز" to "برنامج مجاني ومفتوح المصدر بدون إعلانات، برخصة GPL-3.0",
        "خط المصحف ونص الآيات" to "مجمع الملك فهد لطباعة المصحف الشريف (خط حفص، الملف الأصلي بدون تعديل)",
        "أوقات الصلاة" to "تقويم أم القرى، بمكتبة adhan من Batoul Apps (رخصة MIT)",
        "الأذان المسجّل" to "أذان المغرب في المسجد الحرام، تسجيل 3omar Faruq، رخصة CC BY 3.0. قصّينا الأذان من التسجيل وحوّلناه لصوت فقط",
        "إذاعة القرآن الكريم" to "هيئة الإذاعة والتلفزيون السعودية",
        "قائمة المدن" to "GeoNames، رخصة CC BY 4.0",
        "خطوط أميري و IBM Plex" to "رخصة SIL Open Font License",
    )
    Column {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
            Qr(SUGGESTIONS, 140.dp)
            Spacer(Modifier.width(22.dp))
            Column {
                Text("عندك اقتراح أو مشكلة؟", style = TextStyle(fontFamily = Fonts.plex, fontSize = 20.sp, color = Palette.gold))
                Text("صوّر الرمز بجوالك واكتب لنا.", style = TextStyle(fontFamily = Fonts.plex, fontSize = 15.sp, color = Palette.mute))
            }
        }
        credits.forEach { (title, body) -> ActionRow(title, body) {} }   // focusable, so the remote scrolls the page
    }
}

/** Where suggestions and problems go (the project's GitHub issues). */
private const val SUGGESTIONS = "https://github.com/zmiyajan/adhan-tv/issues/new/choose"

private fun minutes(n: Int) = when (n) {
    1 -> "دقيقة"
    2 -> "دقيقتين"
    in 3..10 -> "$n دقايق"
    else -> "$n دقيقة"
}

private fun minutesShort(n: Int) = "$n د"
