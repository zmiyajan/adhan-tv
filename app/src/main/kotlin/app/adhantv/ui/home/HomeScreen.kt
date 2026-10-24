package app.adhantv.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusGroup
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
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.DenseListItem
import androidx.tv.material3.Text
import app.adhantv.audio.AdhanVoice
import app.adhantv.data.AppSettings
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
import app.adhantv.ui.theme.Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant

private enum class Section(val title: String) {
    TODAY("اليوم"),
    CITY("المدينة"),
    ADHAN("الأذان"),
    IQAMA("الإقامة"),
    LOOK("الشكل"),
    PERMISSIONS("الأذونات"),
    ABOUT("عن البرنامج"),
}

/** Everything the screen can ask the activity to do. */
class HomeActions(
    val change: ((AppSettings) -> AppSettings) -> Unit,
    val locate: () -> Unit,
    val pick: (City) -> Unit,
    val tryEvent: (Event.Kind) -> Unit,
    val board: () -> Unit,
    val radio: () -> Unit,
    val overApps: () -> Unit,
    val exact: () -> Unit,
    val locationSettings: () -> Unit,
    val restart: () -> Unit,
    val reset: () -> Unit,
    val finishOnboarding: () -> Unit,
)

/** Menu on the right (RTL), the chosen section beside it. Every row works with the remote's OK. */
@Composable
fun HomeScreen(
    settings: SettingsRepository,
    scheduler: AlarmScheduler,
    cities: Cities,
    hijri: Hijri,
    status: String?,
    resumes: Int,
    version: String,
    act: HomeActions,
) {
    val s = settings.settings.collectAsState(initial = null).value ?: return
    var picking by remember { mutableStateOf(false) }
    var section by remember { mutableStateOf(Section.TODAY) }
    val list = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val menu = remember { FocusRequester() }
    var inPane by remember { mutableStateOf(false) }
    LaunchedEffect(section) { list.scrollToItem(0) }
    // Back from the settings pane goes to the menu; Back from the menu leaves the app as usual.
    BackHandler(enabled = inPane && s.onboarded) { menu.requestFocus() }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(Modifier.fillMaxSize().background(Palette.bgEdge)) {
            when {
                picking -> CityPicker(cities, s.city.country, onPick = { act.pick(it); picking = false }, onBack = { picking = false })
                !s.onboarded -> Onboarding(s, status, resumes, act) { picking = true }
                else -> Row(Modifier.fillMaxSize()) {
                    Column(
                        // Coming back from the settings pane lands on the section you were in, not the nearest item.
                        Modifier.width(185.dp).fillMaxHeight().background(Palette.panel).padding(vertical = 22.dp, horizontal = 8.dp)
                            .focusRequester(menu).focusRestorer().focusGroup(),
                    ) {
                        Text(
                            "أذان التلفاز",
                            style = TextStyle(fontFamily = Fonts.amiri, fontSize = 22.sp, color = Palette.gold, textAlign = TextAlign.Center),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                        )
                        Box(Modifier.padding(horizontal = 12.dp).fillMaxWidth().height(1.dp).background(Palette.gold.copy(alpha = .35f)))
                        Spacer(Modifier.height(10.dp))
                        Section.entries.forEach { sec ->
                            DenseListItem(
                                selected = section == sec,
                                onClick = {
                                    section = sec
                                    scope.launch { list.animateScrollToItem(0) }   // pressing a section always shows its top
                                },
                                modifier = Modifier.onFocusChanged { if (it.isFocused) section = sec },
                                headlineContent = { Text(sec.title, style = TextStyle(fontFamily = Fonts.plex, fontSize = 16.sp), maxLines = 1) },
                            )
                        }
                        Spacer(Modifier.weight(1f))
                        // RTL: «صُنع في مكة» on the right, a divider, the version number on the left.
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("صُنع في مكة 🕋", style = TextStyle(fontFamily = Fonts.plex, fontSize = 13.sp, color = Palette.mute))
                            }
                            Text("|", style = TextStyle(fontFamily = Fonts.plex, fontSize = 13.sp, color = Palette.mute))
                            Text(version, style = TextStyle(fontFamily = Fonts.plex, fontSize = 13.sp, color = Palette.mute))
                        }
                    }
                    CompositionLocalProvider(LocalScrollTop provides { scope.launch { list.animateScrollToItem(0) } }) { LazyColumn(
                        Modifier.weight(1f).fillMaxHeight().padding(horizontal = 40.dp).onFocusChanged { inPane = it.hasFocus },
                        state = list,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        item { Spacer(Modifier.height(30.dp)) }
                        item {
                            // Title on the right; on «اليوم» the city and Hijri date sit quietly on the left of the same line.
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(section.title, style = TextStyle(fontFamily = Fonts.amiri, fontSize = 30.sp, color = Palette.ink))
                                if (section == Section.TODAY) {
                                    val h = remember(s.city) { hijri.of(java.time.LocalDate.now(s.city.place.zone)) }
                                    Text(
                                        "${s.city.name} · ${h.day} ${Hijri.MONTHS[h.month - 1]} ${h.year}",
                                        style = TextStyle(fontFamily = Fonts.plex, fontSize = 15.sp, color = Palette.mute),
                                    )
                                }
                            }
                        }
                        item { Spacer(Modifier.height(8.dp)) }
                        when (section) {
                            Section.TODAY -> item { Today(s, scheduler, hijri, act) }
                            Section.CITY -> item { CityAndPrayers(s, act) { picking = true } }
                            Section.ADHAN -> item { Adhan(s, act) }
                            Section.IQAMA -> item { Iqama(s, act) }
                            Section.LOOK -> item { Look(s, act) }
                            Section.PERMISSIONS -> item { PermissionsPane(resumes, act) }
                            Section.ABOUT -> item { About(version, act) }
                        }
                        status?.let { msg -> item { Note(msg, Palette.gold) } }
                        item { Spacer(Modifier.height(30.dp)) }
                    } }
                }
            }
        }
    }
}

@Composable
fun Note(text: String, color: Color = Palette.mute, size: Int = 18) {
    Text(text, style = TextStyle(fontFamily = Fonts.plex, fontSize = size.sp, color = color), modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
private fun Today(s: AppSettings, scheduler: AlarmScheduler, hijri: Hijri, act: HomeActions) {
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

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Prayer.entries.forEach { p ->
                val friday = events.firstOrNull { it.prayer == p }?.jumuah == true
                val iqama = events.firstOrNull { it.prayer == p && it.kind == Event.Kind.IQAMA }
                val isNext = next?.prayer == p && next.at.atZone(zone).toLocalDate() == date
                val off = p !in s.enabled
                val shape = RoundedCornerShape(14.dp)
                Column(
                    Modifier.weight(1f).background(Palette.panel, shape)
                        .then(if (isNext) Modifier.border(2.dp, Palette.gold, shape) else Modifier)
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(p.arabic(friday), style = TextStyle(fontFamily = Fonts.plex, fontSize = 17.sp, color = Palette.mute))
                    Text(day[p].clockText(zone), style = TextStyle(fontFamily = Fonts.plex, fontSize = 22.sp, color = if (off) Palette.mute else Palette.ink), maxLines = 1)
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
        when {
            !s.active -> Note("الأذان متوقف. تقدر تشغّله من قسم الأذان.", Palette.live)
            next != null -> {
                val what = if (next.kind == Event.Kind.IQAMA) "إقامة" else "أذان"
                Note("القادم: $what ${next.prayer.arabic(next.jumuah)} ${next.at.clockText(zone)}", Palette.gold)
            }
        }
        Column {
            ActionRow("شاشة المسجد", "الساعة والأوقات والعدّ التنازلي بالشاشة كاملة", top = true, onClick = act.board)
            ActionRow("إذاعة القرآن الكريم", "استمع مباشر، وتوقف وقت الأذان", onClick = act.radio)
            ActionRow("جرّب الأذان", "يطلع الأذان الحين بالشكل والصوت الحالي") { act.tryEvent(Event.Kind.ADHAN) }
        }
    }
}

@Composable
private fun CityAndPrayers(s: AppSettings, act: HomeActions, onChooseCity: () -> Unit) {
    Column {
        ActionRow("المدينة: ${s.city.name}", "اضغط عشان تختار من القائمة", top = true, onClick = onChooseCity)
        ActionRow("حدّد مدينتي تلقائياً", "من الواي فاي، مرة وحدة، والموقع ما يطلع من التلفزيون", onClick = act.locate)
        ChoiceRow("طريقة الحساب", s.method, Method.entries, { it.arabic }, "أم القرى هي التقويم الرسمي في السعودية") { m -> act.change { it.copy(method = m) } }
    }
}

/** What happens at a prayer's adhan time. */
private enum class AdhanMode(val label: String) { SOUND("بصوت"), SILENT("بدون صوت"), OFF("متوقف") }

/** One sound choice: a recording, or the live radio. */
private data class Sound(val label: String, val voiceId: String?)

@Composable
private fun Adhan(s: AppSettings, act: HomeActions) {
    val context = LocalContext.current
    val sounds = remember(s.voiceId, s.source) {
        AdhanVoice.available(context).map { Sound(it.label, it.id) } + Sound("إذاعة القرآن الكريم مباشر", null)
    }
    val current = if (s.useRadio) sounds.last() else sounds.firstOrNull { it.voiceId == s.voiceId } ?: sounds.first()
    Column {
        ToggleRow("الأذان شغّال", s.active, if (s.active) "يطلع الأذان والإقامة في وقتها" else "متوقف: ما يطلع شي لين تشغّله", top = true) { on ->
            act.change { it.copy(active = on) }
        }
        ChoiceRow(
            "الصوت", current, sounds, { it.label },
            if (s.useRadio) "لو انقطع النت يشتغل أذان المسجد الحرام" else "يبقى الأذان على الشاشة طول مدة التسجيل",
        ) { v ->
            act.change { if (v.voiceId == null) it.copy(source = SoundSource.RADIO) else it.copy(source = SoundSource.RECORDED, voiceId = v.voiceId) }
        }
        if (s.useRadio) {
            ChoiceRow("مدة الأذان مع الإذاعة", s.adhanMinutes, listOf(1, 2, 3, 4, 5), { minutes(it) }) { v -> act.change { it.copy(adhanMinutes = v) } }
        }
        Spacer(Modifier.height(10.dp))
        Prayer.entries.forEach { p ->
            val mode = when {
                p !in s.enabled -> AdhanMode.OFF
                p in s.sound -> AdhanMode.SOUND
                else -> AdhanMode.SILENT
            }
            ChoiceRow("أذان ${p.arabic()}", mode, AdhanMode.entries, { it.label }) { m ->
                act.change {
                    it.copy(
                        enabled = if (m == AdhanMode.OFF) it.enabled - p else it.enabled + p,
                        sound = if (m == AdhanMode.SOUND) it.sound + p else it.sound - p,
                    )
                }
            }
        }
    }
}

@Composable
private fun Iqama(s: AppSettings, act: HomeActions) {
    Column {
        ToggleRow("تنبيه الإقامة", s.iqamaEnabled, "شاشة بدون صوت. ما فيه إقامة لصلاة الجمعة", top = true) { on -> act.change { it.copy(iqamaEnabled = on) } }
        Prayer.entries.forEach { p ->
            ChoiceRow("${p.arabic()} بعد الأذان", s.iqamaMinutes.getValue(p), (5..45 step 5).toList(), { "$it دقيقة" }) { v ->
                act.change { it.copy(iqamaMinutes = it.iqamaMinutes + (p to v)) }
            }
        }
        ChoiceRow("مدة شاشة الإقامة", s.iqamaSeconds, listOf(15, 30, 60), { "$it ثانية" }) { v -> act.change { it.copy(iqamaSeconds = v) } }
        ActionRow("جرّب الإقامة") { act.tryEvent(Event.Kind.IQAMA) }
    }
}

@Composable
private fun Look(s: AppSettings, act: HomeActions) {
    Column {
        ChoiceRow("اللون", s.theme, Theme.entries, { it.label }, "للبرنامج كله وشاشة الأذان", swatch = { Color(it.accent) }, top = true) { v -> act.change { it.copy(theme = v) } }
        ChoiceRow("حجم الكلام", s.textSize, TextSize.entries, { it.label }, "حسب بُعد الكنبة عن التلفزيون") { v -> act.change { it.copy(textSize = v) } }
        ToggleRow("خط العدّ التنازلي", s.showBar, "خط يقصر لين تخلص الشاشة") { on -> act.change { it.copy(showBar = on) } }
        ToggleRow("سطر الصلاة القادمة", s.showNext, "تحت الآية في شاشة الأذان") { on -> act.change { it.copy(showNext = on) } }
    }
}

@Composable
private fun PermissionsPane(resumes: Int, act: HomeActions) {
    val context = LocalContext.current
    val overApps = remember(resumes) { Permissions.canShowOverApps(context) }
    val exact = remember(resumes) { Permissions.canBeExact(context) }
    val located = remember(resumes) { Permissions.canLocate(context) }
    Column {
        Note("كل إذن تقدر تشغّله أو توقفه. الضغط يفتح مكانه في إعدادات التلفزيون.", size = 16)
        ToggleRow("الظهور فوق التطبيقات", overApps, "عشان يطلع الأذان فوق اللي شغّال. بدونه يطلع بس والبرنامج مفتوح", top = true) { act.overApps() }
        ToggleRow("المنبهات والتذكيرات", exact, "عشان يطلع الأذان في ثانيته") { act.exact() }
        ToggleRow("الموقع التقريبي", located, "لتحديد المدينة تلقائياً بس") { if (located) act.locationSettings() else act.locate() }
    }
}

@Composable
private fun About(version: String, act: HomeActions) {
    var confirmReset by remember { mutableStateOf(false) }
    var open by remember { mutableStateOf<Credit?>(null) }
    var last by remember { mutableStateOf<Credit?>(null) }
    val rows = remember { CREDITS.associateWith { FocusRequester() } }
    open?.let { c ->
        AboutDetail(c) {
            last = c
            open = null
        }
        return
    }
    // Coming back from a page puts the focus on the entry you opened, not at the top of the menu.
    LaunchedEffect(last) { last?.let { runCatching { rows.getValue(it).requestFocus() } } }
    Column {
        Text(
            "صدقة جارية عن صاحبه وأسرته، وعن كل من ساهم فيه، إلى يوم الدين.",
            style = TextStyle(fontFamily = Fonts.amiri, fontSize = 22.sp, color = Palette.gold),
            modifier = Modifier.padding(bottom = 4.dp),
        )
        Note("الإصدار $version · مجاني وبدون إعلانات", size = 16)
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 10.dp)) {
            Qr(SUGGESTIONS, 130.dp)
            Spacer(Modifier.width(22.dp))
            Column {
                Text("عندك اقتراح أو مشكلة؟", style = TextStyle(fontFamily = Fonts.plex, fontSize = 20.sp, color = Palette.ink))
                Text("صوّر الرمز بجوالك واكتب لنا.", style = TextStyle(fontFamily = Fonts.plex, fontSize = 15.sp, color = Palette.mute))
            }
        }
        CREDITS.forEachIndexed { i, c ->
            ActionRow(c.title, c.summary, top = i == 0, modifier = Modifier.focusRequester(rows.getValue(c))) { open = c }
        }
        Spacer(Modifier.height(10.dp))
        ActionRow("إعادة تشغيل البرنامج", "لو حسّيت إن شي مو طبيعي. ما يغيّر إعداداتك", onClick = act.restart)
        ActionRow(if (confirmReset) "اضغط مرة ثانية للتأكيد" else "رجّع الإعدادات الأصلية", "ويخلي مدينتك مثل ما هي") {
            if (confirmReset) act.reset()
            confirmReset = !confirmReset
        }
        Note("صُنع بإحسان في مكة المكرمة، بواسطة زياد مياجان.", size = 15)
    }
}

/** Where suggestions and problems go (the project's GitHub issues). */
private const val SUGGESTIONS = "$REPO/issues/new/choose"

private fun minutes(n: Int) = when (n) {
    1 -> "دقيقة"
    2 -> "دقيقتين"
    in 3..10 -> "$n دقايق"
    else -> "$n دقيقة"
}
