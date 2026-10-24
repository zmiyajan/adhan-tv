# 02 — Play policy & platform constraints for an Android TV adhan app

Researched 2026-10-24. Scope: an Android TV / Google TV app that, at prayer time, takes over the
screen (over YouTube/Netflix etc.), plays the adhan, closes and returns to the previous app; plus a
small periodic verse banner in a corner. Target: Google Play (TV), open source.

Legend: **[V]** = verified against the linked official page (or AOSP source) on the date above.
**[U]** = uncertain / community evidence only / must be tested on real devices.

---

## TL;DR — recommended architecture

1. **Scheduling:** `AlarmManager.setAlarmClock()` (or `setExactAndAllowWhileIdle()`) → `BroadcastReceiver`.
   Needs `SCHEDULE_EXACT_ALARM` (user-granted, revocable) — or `USE_EXACT_ALARM` if Play accepts the
   app as "alarm-like" (risky, see §2).
2. **Taking over the screen:** the only exemption a third-party TV app can realistically use is
   **`SYSTEM_ALERT_WINDOW` ("Display over other apps")** granted by the user. AlarmManager itself
   grants **no** background-activity-launch privilege (verified in AOSP). Full-screen-intent
   notifications are not a reliable path on TV.
3. **Audio:** play the adhan from the full-screen activity (foreground — no FGS needed). If audio must
   continue after the activity is dismissed, use a `mediaPlayback` FGS started while the activity is
   visible. A scheduler FGS is **not** needed; AlarmManager + receiver is the right pattern.
4. **Verse banner:** `TYPE_APPLICATION_OVERLAY` window, same `SYSTEM_ALERT_WINDOW` grant.
5. **Fallback when the overlay grant is impossible** (Go/low-RAM devices, TVs without the settings
   screen, TCL auto-start blocker): document an `adb shell appops` path and degrade gracefully
   (e.g. audio-only adhan + notification).
6. **Play target SDK for TV in 2026: API 34** (not 36 like phones). Recommend targeting 35/36 anyway
   for forward compatibility — but note API 35 makes the SAW→FGS exemption stricter (§3).

---

## 1. Background activity launch (BAL) restrictions, Android 10+

**Official exemption list** [V] — <https://developer.android.com/guide/components/activities/background-starts>

A background app may start an activity only if one of these holds (abridged): it has a visible
window; it is the current IME; the launch comes from a system-sent `PendingIntent` (e.g. notification
tap); **the user has granted `SYSTEM_ALERT_WINDOW`**; it holds the privileged
`START_ACTIVITIES_FROM_BACKGROUND`; it is bound by a service with BAL rights; launcher-initiated; or a
core OS component (e.g. telephony in-call UI).

What this means for each candidate exemption:

| Exemption | Usable by our app? | Notes |
|---|---|---|
| `SYSTEM_ALERT_WINDOW` (user grant) | **Yes — primary path** | [V] listed exemption. Grant flow on TV is the weak point (below). |
| Alarm-triggered start | **No, not by itself** | [V] The BAL doc lists no alarm exemption. [V] AOSP `AlarmManagerService` explicitly sends every alarm `PendingIntent` with `setPendingIntentBackgroundActivityLaunchAllowed(false)` (incl. `mOptsWithFgsForAlarmClock`, `mActivityOptsRestrictBal`) — source: <https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/apex/jobscheduler/service/java/com/android/server/alarm/AlarmManagerService.java>. So the alarm only wakes the app; the activity start must ride on the app's own privilege (SAW). |
| Full-screen intent notification | **Unreliable on TV** | See §4. |
| `START_ACTIVITIES_FROM_BACKGROUND` | No | Privileged/system only. |

**PendingIntent opt-ins (Android 14/15)** [V] — same page + <https://developer.android.com/about/versions/14/behavior-changes-14>, <https://developer.android.com/about/versions/15/behavior-changes-15>
- targetSdk 34+: a *sender* must opt in with `ActivityOptions.setPendingIntentBackgroundActivityStartMode(...)`.
- targetSdk 35+: a *creator* must opt in with `setPendingIntentCreatorBackgroundActivityStartMode(MODE_BACKGROUND_ACTIVITY_START_ALLOWED)`; otherwise BAL privileges are not delegated.
- Android 15 also: "Block non-visible windows from being considered for background activity launches" and changes to how finishing the top activity returns to the last task.
- Simplest robust design: the alarm fires a **broadcast** to our receiver, and the receiver calls
  `startActivity()` directly (our process holds SAW) — avoids PendingIntent delegation entirely. [U — test on API 34/35/36 TV images]
- Diagnostics: logcat `ActivityTaskManager` "Background activity launch blocked!"; Android 16 `StrictMode...detectBlockedBackgroundActivityLaunch()`. [V]

**"Return to previous app"**: [V] Android 15 doc: "If the top activity finishes a task, Android will go back to whichever task was last active." So launching the adhan activity in its own task (`FLAG_ACTIVITY_NEW_TASK`, own `taskAffinity`, `excludeFromRecents`) and calling `finishAndRemoveTask()` should return to YouTube/Netflix. [U] Whether the streaming app resumes playback vs. stays paused is per-app — test.

### Is SYSTEM_ALERT_WINDOW grantable on Google TV?

- [V] Generic flow: `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` + `Settings.canDrawOverlays()`; Play policy says to "direct users to the system settings page for approval of special permissions (for example, SYSTEM_ALERT_WINDOW)" — <https://support.google.com/googleplay/android-developer/answer/13986130>.
- [V] **Android Go / low-RAM devices cannot grant it at all** (Android 10+): `ACTION_MANAGE_OVERLAY_PERMISSION` is auto-denied and `canDrawOverlays()` always returns false — <https://developer.android.com/about/versions/10/behavior-changes-all>. [U] Whether a given cheap Google TV (1–1.5 GB RAM) is configured `isLowRamDevice` is device-specific; Android TV 14 lowered the minimum RAM to 1 GB (press report), so expect some low-RAM TVs.
- [U] On many current Google TV builds the screen exists at **Settings → Apps → Special app access → Display over other apps** (documented by signage vendors, e.g. <https://help.displaynow.io/help/resolve-autostart-issues-on-your-google-tv-devices>, <https://playsignage.com/setup/google-chromecast-digital-signage/>).
- [U] On older/OEM Android TV builds the intent may not resolve (ActivityNotFoundException) or the screen is missing — reports: Google issue 37121932 (login-gated, unread), <https://b4x.com/android/forum/threads/overlay-permission-with-android-tv.141487/latest>, <https://qna.habr.com/q/1033512>. Fire TV OS 6+ has no UI for it (<https://appchoose.blogspot.com/p/time-netspeed-overlay-adb-help-for.html>; not Google TV, but relevant if you ever sideload/ship to Amazon).
- Workaround used widely: `adb shell appops set <pkg> SYSTEM_ALERT_WINDOW allow`. [U — community]
- **No official developer.android.com page documents SAW on Android TV specifically.** [U] Guard the intent with `resolveActivity()` and ship an in-app help screen.

### Play policy on SYSTEM_ALERT_WINDOW (TV)

- [V] Not a restricted/declaration permission; the only Play policy text is "direct users to the system settings page" (answer 13986130 above).
- [V] TV quality guideline **TV-TR**: "The app fills the entire screen, has a non-transparent background, and does not partially obscure other apps" — <https://developer.android.com/docs/quality-guidelines/tv-app-quality>. **Risk for the corner verse banner** (see §5). [U] How strictly TV review applies TV-TR to overlays is not documented.
- [U] General Play rules against disruptive/unexpected UI (e.g. Disruptive Ads, Device & Network Abuse) apply if the overlay looks like ads or interferes with other apps; keep it user-initiated, clearly explained, easy to disable.

---

## 2. Exact alarms

Sources [V]: <https://developer.android.com/develop/background-work/services/alarms/schedule>,
<https://developer.android.com/about/versions/14/changes/schedule-exact-alarms>,
<https://support.google.com/googleplay/android-developer/answer/13986130> (policy text; the dev doc
links <https://support.google.com/googleplay/android-developer/answer/12253906#exact_alarm_preview>).

| | `SCHEDULE_EXACT_ALARM` | `USE_EXACT_ALARM` |
|---|---|---|
| Grant | User, via "Alarms & reminders" (`ACTION_REQUEST_SCHEDULE_EXACT_ALARM`) | Auto at install |
| Revocable | Yes (all exact alarms cancelled on revoke) | No |
| Play | No declaration | **Restricted**: Play Console declaration + review; "Apps will not be able to publish a version … unless they qualify" |
| Allowed use (policy) | Any user-facing precise timing | Only "an alarm or timer app" or "a calendar app that shows event notifications" |

Version behaviour [V]:
- **Android 12 (31):** exact alarms require one of these permissions, else `SecurityException`. Inexact `set()` may drift up to ~1 h; windows min 10 min.
- **Android 13 (33):** `USE_EXACT_ALARM` introduced.
- **Android 14 (34):** `SCHEDULE_EXACT_ALARM` **denied by default** for fresh installs of apps targeting 33+ (kept on upgrade; denied after backup-restore). Exempt: platform-signed, privileged, apps on the **battery-optimisation allowlist**, `SYSTEM_WELLBEING` role.
- **15:** no further exact-alarm changes found.
- Listen for `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`, re-check `canScheduleExactAlarms()`, reschedule; also reschedule on `BOOT_COMPLETED`, `TIME_SET`, `TIMEZONE_CHANGED`.
- `setAlarmClock()`: never deferred, exits low-power modes; it is an exact alarm and still needs the permission.

**What a prayer-time app qualifies for** — [U] judgement:
- An adhan is functionally a user-configured recurring alarm, (whether phone adhan apps were accepted with
  `USE_EXACT_ALARM` was not verified), but the policy wording lists only "alarm or timer" and "calendar" apps.
  Rejection risk is real. **Recommendation:** ship with `SCHEDULE_EXACT_ALARM` + onboarding that opens
  the Alarms & reminders screen (verify that screen exists on Google TV — [U]), with an inexact
  fallback (`setAndAllowWhileIdle`) and a clear "adhan may be late" warning. Optionally attempt a
  `USE_EXACT_ALARM` declaration later, framed as "user-set prayer alarm clock".
- TVs rarely Doze while on, but go to standby; alarms during standby wake the CPU, not the panel. [U]

---

## 3. Foreground services (Android 14+)

Sources [V]: <https://developer.android.com/develop/background-work/services/fgs/service-types>,
<https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start>,
<https://developer.android.com/about/versions/15/behavior-changes-15>,
<https://support.google.com/googleplay/android-developer/answer/13392821>.

- Target 34+: every FGS must declare a type; each used type is declared in **Play Console → App content** (description, user impact, demo video).
- **Audio:** `mediaPlayback` + `FOREGROUND_SERVICE_MEDIA_PLAYBACK`. Can't be started from `BOOT_COMPLETED` (target 35+).
- **Scheduler:** no FGS type fits "wait until prayer time" and none is needed — AlarmManager + receiver is the documented pattern. `systemExempted` is allowed for apps holding `SCHEDULE_EXACT_ALARM`/`USE_EXACT_ALARM` but is described as "reserved for system applications and specific system integrations" — avoid. [V for eligibility; U for Play acceptance]
- **Starting an FGS from the alarm receiver** is allowed: exemption "Your app invokes an exact alarm to complete an action that the user requests" [V]. Also allowed if the user disabled battery optimisation [V].
- **SAW as FGS exemption narrowed (target 35+)** [V]: the app must hold SAW **and** have a *visible* `TYPE_APPLICATION_OVERLAY` window at that moment.
- **Is an FGS needed at all?** If the adhan plays inside the full-screen activity, the app is foreground and no FGS is needed. Use an FGS only for an audio-only fallback (when the screen takeover isn't possible) — start it from the exact-alarm receiver.
- TV quality **TV-NP** [V]: if audio continues after the user leaves the app, show media controls in system UI (Now Playing). Applies to the audio-only fallback.
- `shortService` (~3 min) is an alternative for a ~3-min adhan, but it times out → ANR risk; adhan + dua can exceed 3 min. [V limits]

---

## 4. Full-screen intent (`USE_FULL_SCREEN_INTENT`)

Sources [V]: <https://developer.android.com/about/versions/14/behavior-changes-14>,
<https://support.google.com/googleplay/android-developer/answer/13986130>,
<https://support.google.com/googleplay/android-developer/answer/13392821>.

- Target 34+: only **calling and alarm** apps keep it by default; Play revokes it otherwise. Play Console declaration from **31 May 2024**; from **22 Jan 2025** only calling/alarm apps are default-granted, others must ask the user (`NotificationManager.canUseFullScreenIntent()`, `Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT`) and "gracefully degrade".
- [V AOSP doc comment, general Android] From Android 13, an FSI shows as a **heads-up notification instead of launching** when the user is actively using the device; launching normally requires screen off/locked and an `IMPORTANCE_HIGH` channel. A TV playing Netflix is "in use" → FSI would not take over the screen.
- [U] Android TV / Google TV SystemUI has limited notification UI (side panel; heads-up support varies by OEM). No official doc found on FSI on TV. **Conclusion: don't rely on FSI on TV**; it adds a Play declaration for little gain. Test once on a Google TV device if desired.

---

## 5. Overlay window for the corner verse banner

- [V] `TYPE_APPLICATION_OVERLAY` requires `SYSTEM_ALERT_WINDOW` (same grant as §1); same Go/low-RAM restriction.
- [V] TV-TR "does not partially obscure other apps" is the closest quality rule — a persistent overlay on top of Netflix is exactly what TV-TR describes. Mitigations: off by default, user opt-in with explanation, short display duration, low frequency, non-focusable (`FLAG_NOT_FOCUSABLE | FLAG_NOT_TOUCHABLE`) so D-pad input still reaches the playing app, easy toggle in settings. [U] Whether TV review will reject it — undocumented; consider making the banner a later feature or a separately reviewed update.
- [U] Some streaming apps use `FLAG_SECURE`/protected video surfaces; overlays still draw over them on most devices, but HDR/tunnelled playback may show artefacts — test.
- Consent flow on TV = the same Special app access screen (or adb). See §1.

---

## 6. Play TV listing requirements & target SDK

Source [V]: <https://developer.android.com/docs/quality-guidelines/tv-app-quality>,
<https://developer.android.com/training/tv/start/hardware>,
<https://support.google.com/googleplay/android-developer/answer/11926878>.

- **TV-ML**: `ACTION_MAIN` + `CATEGORY_LEANBACK_LAUNCHER` intent filter.
- **TV-MT**: `android.hardware.touchscreen` (and faketouch etc.) `required="false"`; avoid permissions implying unsupported hardware (e.g. `ACCESS_FINE_LOCATION` implies location — mark `uses-feature` optional; better: manual city / coarse lookup).
- **TV-LB / TV-BN**: 320×180 banner containing the app name; icon ≥160×160.
- **TV-DP**: everything navigable by D-pad; **TV-DM** no reliance on Menu key; **TV-DB** Back from main screen goes to TV home.
- **TV-LO / TV-OV**: landscape, overscan-safe.
- **TV-PS**: `minSdkVersion` ≤ 31. **TV-G1/G6**: AAB; from **1 Aug 2026** support 32- and 64-bit and **16 KB page sizes**.
- Touch is optional (Tier-1 TV-TO only).
- **Target SDK (2026)**: Android TV new apps and updates must target **API 34+**; the Aug-31-2026 bump to API 36 applies to phones/tablets, *not* TV. Existing TV apps must target 33+ to stay discoverable. [V]
- Opt in to the TV form factor in Play Console; TV releases get a separate TV review. [V general knowledge of Play Console; specific page not fetched — U]

---

## 7. Vendor quirks (TCL / Hisense etc.)

- [U, community] **TCL** ships `com.tcl.guard` ("Safety Guard" / TGuard) and a TCL-only app-op `APP_AUTO_START` / `AUTO_START` defaulting to `ignore`; logs show `callee doesn't have OP_AUTO_START permission`. Effect: boot receivers don't run, background services get killed — would break alarm rescheduling after reboot and possibly the receiver itself.
  - Fix via UI: Safety Guard → Permission shield → Auto launch permission (turn "Auto manager" off, then enable the app) — described by <https://help.displaynow.io/help/resolve-autostart-issues-on-your-google-tv-devices>.
  - Fix via adb: `adb shell cmd appops set <pkg> APP_AUTO_START allow` (name per `appops get`), then `cmd appops write-settings` before reboot on Android 14 — <https://github.com/Gathin23/cleanyourtv/issues/1331>, <https://github.com/simbasjungles/free-my-tv/pull/7>, <https://github.com/spocky/miproja1/issues/415>, <https://github.com/mojothemonkey2/TCL_Guide>. Firmware updates may reset it.
- [U] Hisense (VIDAA is not Android; Hisense Google TV models) and Xiaomi/Sony have similar "app management"/autostart managers; no authoritative docs found. dontkillmyapp.com style documentation is the common pattern.
- **How other apps document it:** signage/kiosk vendors (Display NOW, Play Signage) and launcher projects (Projectivy) publish step-by-step pages per brand with screenshots plus adb commands. Recommendation: an in-app "Reliability check" screen (SAW granted? exact alarm granted? battery-opt ignored? last alarm fired on time?) linking to a per-brand docs page in the repo.

---

## 8. Existing prayer/adhan apps on Android TV

- **MAWAQIT for TV** (`com.mawaqit.androidtv`, Play) — mosque-display style app, Android TV 9+; full-screen timings, adhan/iqama. Works as the *foreground* "smart display" app with screen on/off scheduling, not as an overlay over Netflix as far as found. <https://help.mawaqit.net/en/articles/5768847-mawaqit-for-tv-installation-guide> [U on overlay behaviour; source repo not public as of today]
- **Masjid TV** (`id.dreamfighter.android.masjid.tv`, Play) — masjid display app. <https://play.google.com/store/apps/details?id=id.dreamfighter.android.masjid.tv> [U on overlay]
- **my-masjid Android TV** — full-screen adhan media playback, custom sounds. <https://my-masjid.com/android-tv/> [U]
- **Smart Azan** — different approach: casts adhan audio to Google Home / Chromecast / Android TV instead of drawing over apps. <https://smartazan.com/>
- Open-source references (GitHub, unreviewed): `hani-28/thikrallah` (phone, Play-published, uses SAW overlay for prayer reminders), `meypod/al-azan-compose`, `Five-Prayers/five-prayers-android` (phone, SAW), and small TV projects `obaid/masjid-tv-tcl`, `UAEHN/salah_tv`, `Ahmwajih/salat-screen`. [U]
- **Gap:** no found Play-listed TV app that interrupts *other* apps with a full-screen adhan — this is both the differentiator and the review risk.

---

## Open questions / must test on hardware

1. Does "Display over other apps" exist on the target Google TV devices (Chromecast w/ Google TV, Google TV Streamer, TCL, Hisense, Sony)? Does `ACTION_MANAGE_OVERLAY_PERMISSION` resolve?
2. Is the device `isLowRamDevice`? (then SAW impossible).
3. Does "Alarms & reminders" (`ACTION_REQUEST_SCHEDULE_EXACT_ALARM`) exist in TV Settings?
4. Receiver → `startActivity()` with SAW on API 34/35/36 targets: allowed? (logcat BAL messages)
5. `finishAndRemoveTask()` returns to YouTube/Netflix, and does playback resume?
6. TCL Safety Guard: does it block the alarm receiver or only boot/background persistence?
7. Will TV review accept the overlay banner (TV-TR) and `USE_EXACT_ALARM`?
