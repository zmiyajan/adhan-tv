# 04 — Architecture and repository structure

*Research date: 2026-10-24. Every version number gives the date it was checked. Items marked **[verify]** could not be confirmed from a primary source during this research.*

Scope: an open-source Android TV app in Kotlin that schedules prayer-time events, shows a full-screen adhan/iqama screen with a countdown and optional audio, and has a D-pad settings UI. It must be light on RAM and APK size and ship on Google Play (TV), GitHub Releases and F-Droid.

---

## 0. The constraint that shapes the design: showing a screen over other apps on TV

Firing a timer at the right second is the easy part. The hard part is **bringing a full-screen Activity to the front while the user is in another app** (YouTube, live TV, and so on).

- Android's background-activity-start rules have **no exemption for alarms**. The exemptions are: a visible window, IME, a system-sent `PendingIntent` (for example a notification tap), `SYSTEM_ALERT_WINDOW` granted, `START_ACTIVITIES_FROM_BACKGROUND` (privileged), a bound service with that right, the launcher, and core OS components. — [Restrictions on starting activities from the background](https://developer.android.com/guide/components/activities/background-starts)
- The phone answer, a full-screen-intent notification, does not carry over to TV. Android TV / Google TV has no notification shade like a phone's, and `USE_FULL_SCREEN_INTENT` is restricted on Android 14+. **[verify on target devices]**
- `SYSTEM_ALERT_WINDOW` ("Display over other apps") is a valid exemption, but **many TV builds have no settings screen to grant it**. Users of Fire TV and NVIDIA Shield report needing `adb shell appops set <pkg> SYSTEM_ALERT_WINDOW allow` ([Floating Apps on Shield](https://blog.floatingapps.net/2018/02/using-floating-apps-on-nvidia-shield-and-android-tv/), [PlaySignage on Fire TV](https://playsignage.com/setup/amazon-firetv-launch-on-startup/)). Whether Google TV has the toggle varies by build. **[verify per device]**

So the app needs **explicit operating modes**:

| Mode | How the adhan screen appears | Needs | Idle cost | Recommended for |
|---|---|---|---|---|
| **A. Resident (default)** | The app stays in the foreground as a prayer-times display. At prayer time, the running Activity switches state to the adhan screen. | Nothing special | The resident Compose Activity, roughly tens of MB **[measure]** | Mosque/home "prayer clock" TVs, which is the primary use case |
| **B. Overlay (opt-in)** | An alarm fires, the receiver starts `AdhanActivity` over the current app | `SYSTEM_ALERT_WINDOW` granted (Settings or adb) + exact alarm | About 0 MB between alarms (process can die) | Living-room TV used for other things |
| **C. Screensaver (optional, experimental)** | Register a `DreamService` so that the prayer display is the screensaver. When the TV idles, the app is the visible window. | User picks it as the screensaver | Only while dreaming | Hybrid. **[unverified: Google TV may restrict third-party screensavers]** — [DreamService](https://developer.android.com/reference/android/service/dreams/DreamService) |

**TV standby.** All modes assume the panel is on. If the TV is in standby, an alarm can still wake the CPU and play audio **[verify]**, but apps generally cannot turn a TV panel on. `setTurnScreenOn`/`FLAG_TURN_SCREEN_ON` behaviour on TV hardware is device-dependent, and HDMI-CEC is out of scope. **[verify per device]** Promise only "shows the adhan while the TV is on".

**The trade-off.** "Idle RAM small" and "always able to show the adhan" pull in opposite directions on stock TV:

- With no resident process, idle memory is close to 0 MB, but the screen cannot be guaranteed to appear without overlay permission.
- Mode A's realistic idle footprint is that of one lean Compose Activity.

Ways to keep Mode A small:

- a minimal composition tree, with the clock ticking once per second (or per minute when no countdown is shown);
- no WorkManager, Room, Hilt or Retrofit;
- no `android:largeHeap`;
- fonts subset to the glyphs actually used;
- release the ExoPlayer instance after each adhan.

Mode-independent rule: **a pure-Kotlin `PrayerEngine` produces a timeline. Mode A consumes it with a coroutine ticker, and Modes B and C consume it through AlarmManager.** Both paths share the same `AdhanScreen` composable.

---

## 1. UI toolkit

| | Compose for TV (`androidx.tv:tv-material`) | Leanback | Plain Views |
|---|---|---|---|
| Status (Oct 2026) | **tv-material 1.1.0 stable (2026-05-06)**, tv-foundation 1.0.0 stable (2026-05-06) — [release notes](https://developer.android.com/jetpack/androidx/releases/tv) | **Deprecated**: "Use Compose for TV instead" — [Leanback releases](https://developer.android.com/jetpack/androidx/releases/leanback), [Leanback guide](https://developer.android.com/training/tv/playback/leanback) | Supported, but no TV-specific components |
| Official recommendation | Recommended — [Get started with TV](https://developer.android.com/training/tv/get-started/create) | Migration guide away from it | — |
| APK size (after R8) | Compose runtime + foundation + tv-material: a few MB **[measure]** | Smaller than Compose, but brings fragments + Material 1 | Smallest |
| RAM | Slightly higher baseline than Views | Similar to Views | Lowest |
| D-pad focus | `Modifier.focusable/onFocusChanged`, `FocusRequester`, `focusRestorer()`, `focusProperties { enter/exit }`, tv-material `Surface`/`Card`/`Button` with built-in focus scale/glow. TV focus guide: [Focus in Compose for TV](https://developer.android.com/training/tv/playback/compose/focus) **[verify path]** | Built-in focus for browse rows | Manual `nextFocus*` attributes |
| RTL | Automatic through `LocalLayoutDirection` when `android:supportsRtl="true"` and locale is `ar`. D-pad left/right are mirrored by the focus system. | Partial | Good with `start/end` attributes |

Notes:

- The tv-foundation lazy layouts (`TvLazyColumn/Row`) were deprecated in 1.0.0-alpha11 and removed later. **Use `androidx.compose.foundation.lazy.LazyColumn/LazyRow`** — [release notes](https://developer.android.com/jetpack/androidx/releases/tv).
- The release notes list no RTL-specific fixes. Treat "focus order and side-panel/drawer behaviour in an Arabic locale" as a **test task**, not as solved.
- Plain Views would save perhaps 2–4 MB of APK **[measure]**. They would cost hand-rolled focus visuals and theming, and that code is harder to maintain for contributors.

**Recommendation: Compose for TV (tv-material 1.1.x) + Compose foundation, with no Leanback.** It is the only actively developed, officially recommended TV toolkit. With R8 and a single Activity, the size cost is acceptable. Pull in only `tv-material` and `compose-foundation`. **Do not add `material3` or `material-icons-extended`**; the icons package alone adds a large amount before shrinking. Vector-draw the handful of icons the app needs.

Required TV manifest entries ([source](https://developer.android.com/training/tv/get-started/create)):

```xml
<uses-feature android:name="android.software.leanback" android:required="true"/>
<uses-feature android:name="android.hardware.touchscreen" android:required="false"/>
<application android:banner="@drawable/banner" android:supportsRtl="true" ...>
  <activity android:name=".MainActivity" android:exported="true">
    <intent-filter>
      <action android:name="android.intent.action.MAIN"/>
      <category android:name="android.intent.category.LEANBACK_LAUNCHER"/>
    </intent-filter>
  </activity>
```

The banner is 320×180 xhdpi with the text baked into the image, one per language (so ar + en). TV quality checklist: [TV app quality](https://developer.android.com/docs/quality-guidelines/tv-app-quality).

---

## 2. Module layout

The app has one screen family (clock/adhan), one settings screen, one scheduler and one calculation library. A full `:core:*`/`:feature:*` split ([Now in Android style](https://developer.android.com/topic/modularization)) would add build-logic overhead, cross-module DI plumbing and slower config, with no team-scale benefit.

**Recommendation: two modules.**

| Module | Type | Contents | Why separate |
|---|---|---|---|
| `:core:prayer` | **Pure Kotlin/JVM** (`kotlin("jvm")`) | Wraps `com.batoulapps.adhan:adhan2`, holds the `PrayerTimeline` model, iqama offsets, hijri/day rollover, the city database model | Fast JVM unit tests with no emulator; enforces "no Android in the maths"; reusable (for example a future phone/Wear app) |
| `:app` | Android application | UI (Compose for TV), DataStore, scheduling, audio, DI container | Everything that touches the Android framework |

Inside `:app`, organise by **feature package**, not by layer: `ui/clock`, `ui/adhan`, `ui/settings`, `schedule`, `audio`, `data/settings`, `di`. This keeps a future split to modules mechanical if the app grows.

Add `:baselineprofile` **only** if the F-Droid conflict in §6 is resolved. Do not add `build-logic`/convention plugins until there are three or more Android modules.

---

## 3. Settings storage and dependency injection

**Storage: Jetpack DataStore** — [DataStore guide](https://developer.android.com/topic/libraries/architecture/datastore)

- **Preferences DataStore** is the right default. The settings are flat key/values: per-prayer enabled, adhan duration, iqama offset minutes, theme id, font scale, verse-overlay interval, city id/lat/lng/method/madhab, audio source URL. Coroutines/Flow-native, no extra runtime.
- **Proto DataStore** gives typed, versioned schemas but adds the protobuf-lite runtime and the protobuf Gradle plugin. It is not worth it here.
- Middle option if typed per-prayer structs become messy: a **typed DataStore with a kotlinx-serialization JSON `Serializer`**. It is type-safe with no protobuf. Add it only if needed.
- Bundle the city list (name ar/en, lat, lng, tz, default method) as a **compact asset file** (CSV or JSON) read on demand, not as a Room database.

Illustrative only:

```kotlin
// data/settings/SettingsRepository.kt
class SettingsRepository(private val store: DataStore<Preferences>) {
    val settings: Flow<AppSettings> = store.data.map { it.toAppSettings() }
    suspend fun setIqamaOffset(p: Prayer, min: Int) =
        store.edit { it[intPreferencesKey("iqama_${p.name}")] = min }
}
```

**DI: manual, through an `AppContainer`** — [Manual dependency injection](https://developer.android.com/training/dependency-injection/manual)

The graph has about 6 objects (settings repo, prayer engine, scheduler, audio player factory, city repo, clock). Hilt adds KSP/kapt, generated code, build time and APK size. Koin adds a runtime and reflection-ish lookups. Neither is justified.

```kotlin
class AdhanApp : Application() { val container by lazy { AppContainer(this) } }
class AppContainer(ctx: Context) {
    val settings = SettingsRepository(ctx.settingsDataStore)
    val engine   = PrayerEngine(clock = Clock.System)
    val scheduler = AlarmScheduler(ctx, engine, settings)
}
```

ViewModels get dependencies through a `viewModelFactory { initializer { … } }` ([ViewModel factories](https://developer.android.com/topic/libraries/architecture/viewmodel/viewmodel-factories)).

---

## 4. Scheduling design

Docs: [Schedule alarms](https://developer.android.com/develop/background-work/services/alarms/schedule), [Exact alarm changes (Android 14)](https://developer.android.com/about/versions/14/changes/schedule-exact-alarms).

### 4.1 Alarm chain (Modes B and C, and a backstop for Mode A)

- **Schedule only the next event**, never the whole day. Each firing computes and schedules the one after it.
- Events per prayer: `PRE_ADHAN` (optional), `ADHAN`, `IQAMA`. These are the only alarm events.
- The **periodic verse overlay is a Mode A ticker feature only**, not an alarm. In Mode B it would need an overlay every N minutes and would use up the exact-alarm budget.
- Use **`setAlarmClock()`** for ADHAN and IQAMA. The docs say the system never adjusts its delivery time, and the TV shows it as an upcoming alarm. Use `setExactAndAllowWhileIdle()` for PRE_ADHAN.
- **Mode A (resident):** an in-process coroutine ticker drives the UI from the same timeline, with no exact-alarm dependency. The alarm still exists so that the app recovers if its process was killed.

```kotlin
// schedule/AlarmScheduler.kt — illustrative
fun scheduleNext(now: Instant) {
    val next = engine.nextEvent(now, settings.snapshot()) ?: return
    val pi = PendingIntent.getBroadcast(ctx, next.requestCode,
        Intent(ctx, PrayerAlarmReceiver::class.java).putExtra("event", next.id),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    if (alarmManager.canScheduleExactAlarms())
        alarmManager.setAlarmClock(AlarmClockInfo(next.at.toEpochMilliseconds(), showPi), pi)
    else
        alarmManager.setAndAllowWhileIdle(RTC_WAKEUP, next.at.toEpochMilliseconds(), pi) // degraded
}
```

### 4.2 Rescheduling triggers (one `RescheduleReceiver`)

| Broadcast | Why |
|---|---|
| `ACTION_BOOT_COMPLETED` (+ `LOCKED_BOOT_COMPLETED` optional) | Alarms are cleared on reboot. Needs `RECEIVE_BOOT_COMPLETED`, and only works after the user has launched the app once. |
| `ACTION_TIME_CHANGED` (`TIME_SET`) | The user or NTP changed the clock |
| `ACTION_TIMEZONE_CHANGED` | DST/zone change: prayer times shift |
| `ACTION_MY_PACKAGE_REPLACED` | Re-arm after an app update. **[verify whether alarms survive updates on target devices; re-arming is cheap insurance]** |
| `AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED` | Permission granted, so re-arm in exact mode |
| Settings changed (in-app) | Any change to city, method or offsets calls `scheduleNext()` |
| Date rollover | Covered by the chain itself: after Isha, the next event is the next day's Fajr |

The Android docs suggest declaring the boot receiver `enabled="false"` and enabling it when the user turns alarms on. That fits Modes B and C.

### 4.3 Permissions

- API 31+ requires `SCHEDULE_EXACT_ALARM` or `USE_EXACT_ALARM` for exact alarms. This includes `setAlarmClock`; without one, a `SecurityException` is thrown.
- On **Android 14+, `SCHEDULE_EXACT_ALARM` is denied by default** for new installs targeting 33+. The grant screen ("Alarms & reminders") may also be missing on some TV builds **[verify]**.
- `USE_EXACT_ALARM` is granted automatically but is a Play-restricted permission, for apps whose core function is an alarm, timer or calendar. An adhan app is a credible "alarm clock" use case, but this needs a **Play Console permissions declaration (often with a demo video)**. The current policy wording was not retrieved. **[verify: Play Console Help → "Exact alarm permission"]**
- **Recommendation:**
  - declare `USE_EXACT_ALARM` (Play/GitHub/F-Droid) with `SCHEDULE_EXACT_ALARM` as `maxSdkVersion="32"`;
  - check `canScheduleExactAlarms()` and fall back gracefully to inexact alarms;
  - Mode A does not depend on any of this.
- Mode B also needs `SYSTEM_ALERT_WINDOW`. Show an in-app explanation screen with the exact adb command as a fallback.

### 4.4 When to use WorkManager

**Not at all for v1.** Nothing here is deferrable or long-running. Prayer times are computed offline in microseconds, and WorkManager pulls in Room and startup initialisers ([WorkManager](https://developer.android.com/develop/background-work/background-tasks/persistent)). If an optional feature such as downloading a city list or an online timetable is added later, use a one-off `WorkManager` job for **that** feature only.

---

## 5. Audio

Docs: [Media3 ExoPlayer](https://developer.android.com/media/media3/exoplayer), [Media3 releases](https://developer.android.com/jetpack/androidx/releases/media3), [Audio focus](https://developer.android.com/media/optimize/audio-focus).

| | `MediaPlayer` (framework) | Media3 ExoPlayer |
|---|---|---|
| APK cost | 0 | About 1–1.5 MB after R8 for `media3-exoplayer` (+ `-hls` if needed) **[measure]** |
| Bundled adhan MP3/OGG | Perfect | Overkill |
| Internet radio (Icecast/MP3/AAC/HLS) | Fragile: weak HLS, poor error/retry, ICY metadata handling | Robust: HLS, retries, buffering control, ICY metadata |
| Audio focus | Manual `AudioManager.requestAudioFocus(AudioFocusRequest)` | `setAudioAttributes(attrs, /* handleAudioFocus = */ true)` handles it |

Media3 versions: **1.10.1 is the last confirmed stable (2026-05-12)**. 1.11.0 was at RC by July 2026 **[check the release page for 1.11 final]**.

**Recommendation:**

- If streaming radio (for example Makkah/Madinah live) is in scope, use **Media3 ExoPlayer for everything** (one code path) with `handleAudioFocus = true`.
- If v1 only plays a bundled adhan, use `MediaPlayer` with a manual `AudioFocusRequest(AUDIOFOCUS_GAIN_TRANSIENT)`, and add Media3 when radio arrives.
- Either way:
  - use `AudioAttributes` with `USAGE_ALARM` (so volume follows the alarm stream) **[verify TV volume routing; `USAGE_MEDIA` may be preferable on TVs where users only control media volume]**;
  - create the player only for the adhan window and release it right after;
  - have no MediaSession for a 3-minute alarm sound.

---

## 6. Build

Current versions (checked 2026-10-24):

| Tool | Version | Source |
|---|---|---|
| Kotlin | **2.4.20** (2026-09-07) | [What's new in 2.4.20](https://kotlinlang.org/docs/whatsnew2420.html), [blog](https://blog.jetbrains.com/kotlin/2026/09/kotlin-2-4-20-released/) |
| AGP | **9.x**; the Android docs' examples use **9.2.0** **[verify the exact latest stable]**. AGP 9.0 was a breaking major release (built-in Kotlin, new DSL defaults). | [AGP release notes](https://developer.android.com/build/releases/gradle-plugin), [9.0 upgrade notes](https://drafts.msfjarvis.dev/notes/android-gradle-plugin-9.0.0-upgrade-notes/index.txt) |
| Gradle | 9.x, matching AGP | [Gradle releases](https://gradle.org/releases/) |
| Compose | Use the current Compose BOM + Kotlin `org.jetbrains.kotlin.plugin.compose` | [Compose BOM](https://developer.android.com/develop/ui/compose/bom) |
| tv-material / tv-foundation | 1.1.0 / 1.0.0 | §1 |
| adhan-kotlin | `com.batoulapps.adhan:adhan2` 0.0.x, **MIT** | [GitHub](https://github.com/batoulapps/adhan-kotlin), [Maven Central](https://central.sonatype.com/artifact/com.batoulapps.adhan/adhan2) |
| JDK | 17 or 21 toolchain, pinned via `kotlin { jvmToolchain(17) }` | |

**SDK levels:**

- `minSdk` 23–26 covers practically all Android TV hardware. Pick 26 to simplify notification channels and adaptive icons.
- `targetSdk`: **Play requires TV apps to target API 34+** for the Aug 31, 2026 cycle (phones need 36) — [Target API requirements](https://support.google.com/googleplay/android-developer/answer/11926878), [target-sdk](https://developer.android.com/google/play/requirements/target-sdk). Target **35 or 36** anyway, to stay ahead.

**Version catalog** (`gradle/libs.versions.toml`) — [docs](https://developer.android.com/build/migrate-to-catalogs). Put every dependency and plugin in it, with no versions in `build.gradle.kts`.

**R8 / size** — [Shrink, obfuscate, optimize](https://developer.android.com/build/shrink-code):

```kotlin
buildTypes { release { isMinifyEnabled = true; isShrinkResources = true
    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro") } }
androidResources { localeFilters += listOf("en", "ar") }   // drop unused library translations
```

Ship a universal APK for GitHub/F-Droid and an AAB for Play. TV devices are mostly `armeabi-v7a`/`arm64-v8a`, but with no native code there are no ABI splits to worry about.

**Baseline profiles conflict with F-Droid reproducibility.** F-Droid lists `baseline.prof`/`baseline.profm` as a known source of non-reproducible builds ([F-Droid Reproducible Builds](https://f-droid.org/docs/Reproducible_Builds/)). Resolution:

1. **Recommended for v1:** add no `:baselineprofile` module. The app is small, startup happens once per boot (Mode A), and it is not a bottleneck.
2. Skipping the module is **not enough**. Compose and other AndroidX AARs ship their own `baseline-prof.txt`, and AGP merges them into `assets/dexopt/baseline.prof(m)` by default. If F-Droid's rebuild mismatches on those files, disable the ArtProfile tasks in `:app`. This is the workaround the F-Droid doc names:
   ```kotlin
   tasks.configureEach { if (name.contains("ArtProfile")) enabled = false }
   ```
3. Before every tag, confirm that two clean builds give identical APKs (`diffoscope`). If profiles are wanted later, commit a hand-checked `src/main/baseline-prof.txt` and **never generate it in CI**.

**F-Droid reproducible-build checklist** (from the F-Droid doc):

- build the release from a clean checkout of the tag with `./gradlew assembleRelease`, not from Android Studio;
- pin the JDK, AGP and build-tools versions;
- sign with `apksigner` **from build-tools 34**, because 35.0.0-rc1+ produces signatures `apksigcopier` can't verify **[re-check; this may be fixed by now]**;
- `vectorDrawables.generatedDensities = []`; commit optimised PNGs and disable crunching;
- add no build timestamps, `BuildConfig` dates or git-hash-dependent output beyond what AGP's `vcsInfo` writes (keep it unless it causes a mismatch);
- avoid AboutLibraries' `generated` field, or exclude it;
- metadata: `Binaries:` + `AllowedAPKSigningKeys:` so that F-Droid publishes **your** signed APK once it verifies.

**Signing kept out of the repo:**

- `keystore.properties` and `*.jks` are gitignored.
- Locally, `signingConfigs` reads from `keystore.properties` if it exists. In CI, it reads from environment variables populated from GitHub **encrypted secrets**, with the keystore base64-decoded at job time.
- Enrol in **Play App Signing by uploading your own release key as the app-signing key**, rather than letting Google generate one ([docs](https://support.google.com/googleplay/android-developer/answer/9842756)).
- Sign the GitHub and F-Droid APKs with that **same key**. F-Droid's `Binaries:`/`AllowedAPKSigningKeys:` flow then publishes your APK.
- Result: one key across three channels, and users can switch between stores without reinstalling.

```kotlin
val ks = rootProject.file("keystore.properties").takeIf { it.exists() }?.let { Properties().apply { load(it.inputStream()) } }
signingConfigs { create("release") {
    storeFile = (ks?.getProperty("storeFile") ?: System.getenv("SIGNING_STORE_FILE"))?.let(::file)
    storePassword = ks?.getProperty("storePassword") ?: System.getenv("SIGNING_STORE_PASSWORD")
    keyAlias = ks?.getProperty("keyAlias") ?: System.getenv("SIGNING_KEY_ALIAS")
    keyPassword = ks?.getProperty("keyPassword") ?: System.getenv("SIGNING_KEY_PASSWORD") } }
```

---

## 7. Quality

| Area | Choice | Notes |
|---|---|---|
| Formatting | **ktlint** through the `org.jlleitschuh.gradle.ktlint` plugin or Spotless + ktlint | `.editorconfig` with the `ktlint_code_style = android_studio` style — [ktlint](https://pinterest.github.io/ktlint/) |
| Static analysis | **detekt** with a baseline file + `compose-rules` (mrmans0n) detekt ruleset | [detekt](https://detekt.dev/), [compose-rules](https://mrmans0n.github.io/compose-rules/) |
| Android Lint | `lint { warningsAsErrors = true; baseline = file("lint-baseline.xml") }` | Includes the TV-specific lint checks (banner, touchscreen) — [Lint](https://developer.android.com/studio/write/lint) |
| Unit tests | JUnit 5 (or kotlin.test) in `:core:prayer` | **Prayer-time test vectors**: reuse batoulapps' published JSON `Times` fixtures (method × location × date → expected times, with tolerance ±1 min). Add local vectors (Riyadh, Makkah, Kuwait, high-latitude) checked against Umm al-Qura and official ministry timetables. Also test DST days, the Isha→Fajr rollover, the Umm al-Qura Ramadan +30 min Isha rule and iqama offsets. |
| Scheduler tests | Robolectric `ShadowAlarmManager` in `:app` | Asserts that the next event is scheduled after boot, time-set and settings-change |
| Compose UI tests | `createAndroidComposeRule`; D-pad simulated with `performKeyInput { pressKey(Key.DirectionDown) }` | Keep to a **smoke suite**: settings focus traversal, test button → adhan screen → back, RTL (`ar`) focus order — [Compose testing](https://developer.android.com/develop/ui/compose/testing) |
| TV emulator in CI | `reactivecircus/android-emulator-runner` with a **TV system image** (`target: android-tv`/`google-tv`, x86_64) on `ubuntu-latest` (KVM available) | Run on `main` and release tags only, to keep PR CI fast **[verify TV image availability for chosen API]** |
| Screenshots (optional) | Roborazzi or Paparazzi for the adhan screen in themes × ar/en × font sizes | JVM-only, no emulator |
| Dependency updates | **Renovate** (preferred: groups AndroidX/Compose/Kotlin updates and understands version catalogs well), or Dependabot (`package-ecosystem: gradle` also supports catalogs) | [Renovate Gradle](https://docs.renovatebot.com/modules/manager/gradle/), [Dependabot](https://docs.github.com/en/code-security/dependabot) |

**GitHub Actions** — [setup-java](https://github.com/actions/setup-java), [gradle/actions](https://github.com/gradle/actions):

- `ci.yml` (PR + push):
  - checkout → `actions/setup-java` (temurin 17) → `gradle/actions/setup-gradle` (cache) → `./gradlew ktlintCheck detekt lint test assembleDebug`;
  - upload reports and the debug APK as artifacts;
  - run `gradle/actions/wrapper-validation`.
- `ui-tests.yml`: TV emulator smoke tests, on push to main and nightly.
- `release.yml` (on `v*` tag):
  - decode the keystore from secrets → `./gradlew assembleRelease bundleRelease`;
  - attach the APK + SHA-256 to a GitHub Release;
  - optionally upload the AAB to the Play internal track through `r0adkll/upload-google-play` or Gradle Play Publisher.
  - Release notes come from `CHANGELOG.md` / fastlane changelogs.

---

## 8. Repository hygiene and licensing

### 8.1 License choice

Third-party pieces and their constraints:

- **adhan-kotlin: MIT.** Compatible with everything; keep its copyright notice in NOTICE.
- **AndroidX/Compose/Media3: Apache-2.0.** Compatible with Apache-2.0 and GPL-3.0 (not GPL-2.0).
- **OFL-1.1 fonts** (for example Noto Naskh/Kufi Arabic, Amiri, Cairo): may be bundled in any app, cannot be sold alone, and the font files stay under OFL. List them in NOTICE.
- **KFGQPC Hafs font: NOT OFL.** See §8.3.

| License | Effect | Fit |
|---|---|---|
| **GPL-3.0-or-later** | Forks must stay open. Common on F-Droid. Compatible with Apache-2.0/MIT deps. | Pick this if you want to stop closed-source rebrands, for example ad-laden clones on Play |
| **Apache-2.0** | Permissive with an explicit patent grant. Same license as AndroidX. | Pick this if you want maximum reuse, for example by mosque-display vendors |
| MIT | Permissive, shortest, no patent clause | Fine, but Apache-2.0 is generally preferred for apps |

**Recommendation:** GPL-3.0-or-later if protecting against closed clones matters to you, otherwise Apache-2.0. **This is your decision.** Both work with all the listed dependencies and are both F-Droid-acceptable.

### 8.2 Files

| File | Purpose / notes |
|---|---|
| `LICENSE` | Full text of the chosen license |
| `NOTICE` (or `THIRD_PARTY_NOTICES.md`) | Every bundled third-party component and asset, with license + copyright + source URL. Covers libraries (adhan-kotlin MIT, AndroidX Apache-2.0), fonts (OFL-1.1, with the `OFL.txt` copies in `licenses/`), audio files (each adhan recording's licence or permission) and the city dataset (for example GeoNames CC-BY-4.0, which needs attribution). Also show it on an in-app "Licences" screen. |
| `README.md` + `README.ar.md` | English and Arabic (`<div dir="rtl">` on GitHub), each linking to the other at the top. Covers screenshots, install (Play / F-Droid / APK), D-pad usage, Mode A vs B, the adb overlay instructions, and build steps. |
| `CONTRIBUTING.md` | Build, code style (ktlint/detekt), tests and test vectors, commit convention (Conventional Commits suits automated changelogs), how to add a translation or a city |
| `CODE_OF_CONDUCT.md` | Contributor Covenant 2.1 — [link](https://www.contributor-covenant.org/version/2/1/code_of_conduct/) |
| `SECURITY.md` | How to report vulnerabilities (GitHub private advisories) |
| `PRIVACY.md` | **Required for Play**: every app needs a privacy policy URL and a completed **Data safety** form, even if it collects nothing ([Play User Data policy](https://support.google.com/googleplay/android-developer/answer/10144311), [Data safety](https://support.google.com/googleplay/android-developer/answer/10787469)). State: no accounts, no analytics, no ads, location (if used) stays on device, network only for user-chosen radio streams. Host it through GitHub Pages so it has a stable URL. |
| `CHANGELOG.md` | [Keep a Changelog](https://keepachangelog.com/) format + SemVer; mirrored to `fastlane/metadata/android/*/changelogs/<versionCode>.txt` |
| `.github/ISSUE_TEMPLATE/` | `bug_report.yml` (device model, Android TV OS version, mode A/B, city/method, expected vs actual time), `feature_request.yml`, `wrong_prayer_time.yml` (method, coordinates, date, official source), `config.yml` |
| `.github/PULL_REQUEST_TEMPLATE.md` | Checklist: tests, ktlint/detekt, screenshots in ar + en |
| `.gitignore` | Android/Gradle standard + `keystore.properties`, `*.jks`, `*.keystore`, `local.properties`, `.idea/` (except shared code style), `/build`, `.DS_Store` |
| `fastlane/metadata/android/{en-US,ar}/` | F-Droid and Play listing text, `images/tvBanner.png`, TV screenshots. F-Droid reads this layout directly ([F-Droid metadata](https://f-droid.org/docs/All_About_Descriptions_Graphics_and_Screenshots/)). |
| `.editorconfig`, `config/detekt/detekt.yml` | Tooling config |

### 8.3 KFGQPC Quran font: a licensing blocker to resolve

The project preference is to render Quran verses **only** in the King Fahd Complex (KFGQPC) Hafs font. However:

- ScanCode LicenseDB classifies the "KFGQPC Uthmanic Script Hafs" licence as **proprietary-free**, with redistribution or modification requiring KFGQPC's written approval ([ScanCode entry](https://scancode-licensedb.aboutcode.org/kfgqpc-uthmanic-script-hafs.html)).
- Other font sites describe it as "personal use" / "non-commercial".
- No official licence page from [fonts.qurancomplex.gov.sa](https://fonts.qurancomplex.gov.sa/) was retrieved. **[verify]**

Consequences:

- **F-Droid:** a bundled non-free font would get the `NonFreeAssets` anti-feature, or be rejected.
- **GPL/Apache repo:** the font file cannot be relicensed. It needs its own licence entry and a separate `licenses/KFGQPC.txt`.
- **Play:** generally OK if KFGQPC terms allow redistribution in a free app.

Options, for you to decide:

1. Get written permission from KFGQPC, then bundle the font with that licence in NOTICE.
2. Ship without the font. On first enable of the verse overlay, offer to download it from the official KFGQPC URL into app-private storage. The user fetches it from the source, so the APK stays clean. The app then needs the `INTERNET` permission, and the PRIVACY.md must mention it.
3. Use a separate F-Droid flavour that falls back to an OFL Quranic font (for example Amiri Quran) while keeping KFGQPC on Play. This departs from the KFGQPC-only preference.

Fetch the verse text itself from an authoritative source (for example the KFGQPC/Tanzil text) and ship it with its own licence notice. **Tanzil's text licence requires attribution and forbids modification** ([tanzil.net/docs/text_license](https://tanzil.net/docs/text_license)) **[verify current terms]**.

---

## 9. Proposed directory tree

```
adhan-tv/
├── .editorconfig
├── .gitignore
├── .github/
│   ├── ISSUE_TEMPLATE/
│   │   ├── bug_report.yml
│   │   ├── feature_request.yml
│   │   ├── wrong_prayer_time.yml
│   │   └── config.yml
│   ├── PULL_REQUEST_TEMPLATE.md
│   ├── renovate.json            # or dependabot.yml
│   └── workflows/
│       ├── ci.yml               # lint + detekt + unit tests + assembleDebug
│       ├── ui-tests.yml         # TV emulator smoke tests (main/nightly)
│       └── release.yml          # tag → signed APK + AAB → GitHub Release (+ Play internal)
├── LICENSE
├── NOTICE                       # all third-party code, fonts, audio, data
├── README.md                    # English
├── README.ar.md                 # Arabic (RTL)
├── CONTRIBUTING.md
├── CODE_OF_CONDUCT.md
├── SECURITY.md
├── PRIVACY.md                   # also published via GitHub Pages for the Play listing
├── CHANGELOG.md
├── settings.gradle.kts
├── build.gradle.kts             # root: plugin aliases only (apply false)
├── gradle.properties            # org.gradle.caching=true, configuration-cache, nonTransitiveRClass
├── gradle/
│   ├── libs.versions.toml       # single source of versions
│   └── wrapper/ (gradle-wrapper.jar, gradle-wrapper.properties)
├── gradlew, gradlew.bat
├── config/
│   └── detekt/detekt.yml (+ baseline.xml)
├── licenses/                    # full texts: OFL.txt, MIT-adhan.txt, Apache-2.0.txt, (KFGQPC.txt if permitted)
├── fastlane/metadata/android/
│   ├── en-US/ { title.txt, short_description.txt, full_description.txt, changelogs/, images/tvBanner.png, images/tvScreenshots/ }
│   └── ar/    { … same … }
├── docs/
│   ├── research/                # 01-…, 04-architecture-repo.md (this file)
│   ├── adr/                     # 0001-compose-for-tv.md, 0002-operating-modes.md, …
│   └── privacy/index.md         # GitHub Pages source for privacy URL (optional)
├── core/
│   └── prayer/                  # PURE KOTLIN/JVM — no Android
│       ├── build.gradle.kts
│       └── src/
│           ├── main/kotlin/…/prayer/
│           │   ├── PrayerEngine.kt        # wraps adhan2 → PrayerTimeline
│           │   ├── PrayerTimeline.kt      # events: PRE_ADHAN, ADHAN, IQAMA, VERSE
│           │   ├── IqamaPolicy.kt
│           │   ├── CalculationConfig.kt   # method, madhab, high-lat rule, adjustments
│           │   └── City.kt
│           └── test/
│               ├── kotlin/…/PrayerEngineTest.kt, TimelineRolloverTest.kt, DstTest.kt
│               └── resources/vectors/     # batoulapps JSON fixtures + local official timetables
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    ├── lint-baseline.xml
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml        # LEANBACK_LAUNCHER, banner, receivers, permissions
        │   ├── assets/
        │   │   ├── cities.csv             # bundled city DB (attributed in NOTICE)
        │   │   └── quran/verses.json      # curated verse set, fetched from source, with licence
        │   ├── res/
        │   │   ├── drawable/banner.xml|png, values/, values-ar/, font/, raw/adhan_*.ogg
        │   │   └── xml/locales_config.xml # per-app language (ar/en)
        │   └── kotlin/…/adhantv/
        │       ├── AdhanApp.kt            # Application; holds AppContainer
        │       ├── MainActivity.kt        # single Activity, Compose NavHost (or simple state switch)
        │       ├── di/AppContainer.kt     # manual DI
        │       ├── data/settings/         # AppSettings, SettingsRepository (Preferences DataStore)
        │       ├── data/cities/           # CityRepository (asset reader, search)
        │       ├── schedule/
        │       │   ├── AlarmScheduler.kt
        │       │   ├── PrayerAlarmReceiver.kt
        │       │   ├── RescheduleReceiver.kt  # BOOT / TIME_SET / TZ / PACKAGE_REPLACED / EXACT_ALARM_PERM
        │       │   └── Ticker.kt              # in-process coroutine clock for resident mode
        │       ├── audio/AdhanPlayer.kt   # MediaPlayer or Media3, audio focus, release-after-use
        │       ├── ui/theme/              # colours, themes, typography scale (font size setting)
        │       ├── ui/clock/              # resident prayer-times display
        │       ├── ui/adhan/              # AdhanScreen, IqamaCountdown, VerseOverlay
        │       ├── ui/settings/           # D-pad settings: per-prayer toggles, durations, offsets,
        │       │                          #   theme, font size, verse overlay, city picker, Test button
        │       ├── ui/onboarding/         # mode choice, overlay-permission + adb help, exact-alarm help
        │       └── ui/licences/           # renders NOTICE
        ├── test/                          # Robolectric: scheduler, settings mapping
        └── androidTest/                   # Compose TV smoke tests (D-pad, RTL)
```

Not in the tree on purpose: `local.properties`, `keystore.properties`, `*.jks` (gitignored), `build-logic/` (premature), `:baselineprofile` (see §6), and Room/WorkManager/Hilt.

---

## 10. Summary of recommendations

1. **Compose for TV** (tv-material 1.1.0), with no Leanback and no material3/icons-extended.
2. **Two modules**: `:core:prayer` (pure JVM, test vectors) + `:app` (package-by-feature).
3. **Preferences DataStore** + **manual DI** (`AppContainer`).
4. **Operating modes**: Resident (default), Overlay (SAW, opt-in), optional Screensaver. Use an alarm chain with `setAlarmClock` and re-arm on boot, time, timezone, package-replaced and permission-changed broadcasts. Declare `USE_EXACT_ALARM` with a Play declaration. **No WorkManager.**
5. **Audio**: `MediaPlayer` for bundled adhan only; **Media3 ExoPlayer** with `handleAudioFocus=true` once radio streaming is added.
6. **Build**: Kotlin 2.4.20, AGP 9.x, version catalog, R8 + resource shrinking + locale filters, **no generated baseline profiles** (to protect F-Droid reproducibility), signing from gitignored properties or CI secrets, targetSdk 35–36 (Play TV minimum 34).
7. **Quality**: ktlint + detekt + compose-rules + Lint as errors; JVM prayer-vector tests; Robolectric scheduler tests; TV-emulator Compose smoke tests; GitHub Actions ci/ui/release; Renovate.
8. **Hygiene**: GPL-3.0-or-later or Apache-2.0 (your call); NOTICE + `licenses/`; bilingual README; PRIVACY.md (Play-mandatory); fastlane metadata for both stores; **KFGQPC font licence must be resolved before bundling**.

### Open items to verify

- Whether "Display over other apps" exists on your target Google TV build.
- Whether "Alarms & reminders" exists on it too.
- Current Play wording for `USE_EXACT_ALARM`.
- Media3 1.11 status.
- The exact latest stable AGP.
- KFGQPC and Tanzil licence terms.
- Whether alarms survive app updates.
- `USAGE_ALARM` vs `USAGE_MEDIA` volume behaviour on TV.
- Real APK size and idle RSS (measure with `adb shell dumpsys meminfo <pkg>`).
