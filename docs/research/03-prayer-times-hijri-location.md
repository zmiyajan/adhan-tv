# 03 — Prayer times, Hijri date, location, iqama defaults

Research date: 2026-10-24. Scope: an open-source Android TV adhan/iqama app in Kotlin with an Arabic UI. The default is Saudi Arabia (Umm al-Qura) but it should work anywhere.

Legend: **[V]** verified against a primary source or by running a check during this research. **[U]** uncertain, secondary-only, or inferred.

---

## TL;DR recommendations

| Topic | Recommendation |
|---|---|
| Prayer-time engine | `com.batoulapps.adhan:adhan2:0.0.7` (Kotlin Multiplatform, MIT). Use `CalculationMethod.UMM_AL_QURA.parameters` and add **+30 min to Isha in Ramadan** yourself. 0.0.7 lacks the polar-circle guards that are on `main`, so validate times at high latitudes. |
| Expected accuracy vs official | ±1 min against the official Umm al-Qura times for Makkah, with a rare ±2 min (measured on 60 days, below). Treat ±1 min as a pass in tests. |
| Hijri date | `android.icu.util.IslamicCalendar` with `CalculationType.ISLAMIC_UMALQURA` (API 24+). Its table matched the official KACST month lengths exactly for 1300–1500 AH. Also offer a ±1/±2 day user offset. |
| Location | An **offline city picker** built from GeoNames `cities15000` (with Arabic names from `alternateNamesV2`), using the row's IANA `timezone`. Advanced: manual lat/lon + tz. No IP geolocation by default. |
| Time zone | Compute times in the **city's IANA zone** (from the dataset), not the TV's default zone. Warn when they differ. |
| Iqama defaults | Make them configurable per prayer. Ship Saudi-style defaults (see §5) and label them "editable", because no current official national table was found. |
| Ramadan Isha | Officially Maghrib + 120 min in Ramadan (MOIA plus the official KACST timetable, verified). |

---

## 1. batoulapps Adhan libraries

### 1.1 Repos and status

| Repo | Language | License | Latest | Last push | Notes |
|---|---|---|---|---|---|
| [batoulapps/adhan-kotlin](https://github.com/batoulapps/adhan-kotlin) (`adhan-java` redirects here) | Kotlin MP (+ legacy `java` branch) | MIT [V] | **adhan2 v0.0.7**, released 2026-06-13 [V] | 2026-08-21 [V] | Active. In Jul 2026 it merged polar-circle guards (PR #94, "invalidate all times" when a calculation fails near the poles) and fixed README docs (PR #95). |
| Legacy Java artifact | Java | MIT | `com.batoulapps.adhan:adhan:1.2.1` (2021-03-06) [V] | — | Frozen. The README says "All primary development is for the Kotlin Multiplatform version". |
| [batoulapps/adhan-swift](https://github.com/batoulapps/adhan-swift) | Swift | MIT | 1.5.0 | 2026-08-21 | — |
| [batoulapps/adhan-js](https://github.com/batoulapps/adhan-js) | JS/TS | MIT | v4.4.6 | 2026-10-20 | Used below for cross-checks. |
| [batoulapps/adhan-testdata](https://github.com/batoulapps/adhan-testdata) | JSON | — | — | 2021 | Shared reference times, including `Times/Makkah-UmmAlQura.json` (source ummulqura.org.sa, "adjusted +/- 1 minute"). |
| [batoulapps/adhan](https://github.com/batoulapps/adhan) | umbrella | MIT | — | — | Algorithms from Jean Meeus, *Astronomical Algorithms*. |

**Maven coordinates [V]** (checked in [Maven Central metadata](https://repo1.maven.org/maven2/com/batoulapps/adhan/adhan2/maven-metadata.xml), updated 2026-06-13):

```kotlin
implementation("com.batoulapps.adhan:adhan2:0.0.7")      // KMP root (Gradle resolves -jvm)
// legacy, frozen: implementation("com.batoulapps.adhan:adhan:1.2.1")
```

Note: the search.maven.org Solr index still shows 0.0.5, which is stale. Trust `maven-metadata.xml`.

**Android requirement [V]:** the library uses kotlinx-datetime (0.8.0) on top of `java.time`. It therefore needs **minSdk 26 or core library desugaring** (from the README). This is no problem for Android TV, where minSdk 26 or higher is reasonable.

Extras in the library: a `Qibla` class (direction) and `SunnahTimes` (middle and last third of the night). No external dependencies.

### 1.2 Calculation methods (from `CalculationMethod.kt` on `main`, Aug 2026) [V]

| Enum | Fajr | Isha | Built-in method adjustments |
|---|---|---|---|
| `MUSLIM_WORLD_LEAGUE` | 18° | 17° | dhuhr +1 |
| `EGYPTIAN` | 19.5° | 17.5° | dhuhr +1 |
| `KARACHI` | 18° | 18° | dhuhr +1 |
| **`UMM_AL_QURA`** | **18.5°** | **90 min after Maghrib** | none |
| `DUBAI` | 18.2° | 18.2° | sunrise −3, dhuhr/asr/maghrib +3 |
| `MOON_SIGHTING_COMMITTEE` | 18° | 18° | seasonal; dhuhr +5, maghrib +3; special rules at lat ≥ 55 |
| `NORTH_AMERICA` (ISNA) | 15° | 15° | dhuhr +1 (README: "not recommended") |
| `KUWAIT` | 18° | 17.5° | — |
| `QATAR` | 18° | 90 min | — |
| `SINGAPORE` | 20° | 18° | dhuhr +1, with rounding up |
| `TURKEY` | 18° | 17° | sunrise −7, dhuhr +5, asr +4, maghrib +7 (Diyanet approximation) |
| `OTHER` | 0 | 0 | custom |

(All of the above is read from `CalculationMethod.kt` on `main` and is [V]. The Kotlin port has **no Tehran method**, although adhan-js has one. `UMM_AL_QURA` has no method adjustments and no rounding override.)

**Ramadan rule [V]:** the library does **not** apply it automatically. The KDoc and README both say: *"you should add a +30 minute custom adjustment for Isha during Ramadan."* The app must decide when it is Ramadan (see §3) and set `adjustments.isha += 30`.

**Madhab / Asr [V]:** `Madhab.SHAFI` uses shadow ratio 1 and is the default; Saudi Arabia (Umm al-Qura) uses it. `Madhab.HANAFI` uses ratio 2. Expose this as a setting for other countries.

**High-latitude rules [V]** (`HighLatitudeRule`):
- `MIDDLE_OF_THE_NIGHT`: Fajr is never before the middle of the night, and Isha never after it.
- `SEVENTH_OF_THE_NIGHT`: Fajr/Isha are clamped to the last and first seventh of the night.
- `TWILIGHT_ANGLE`: the fraction of the night is angle/60.
- `HighLatitudeRule.recommendedFor(coords)` returns SEVENTH_OF_THE_NIGHT above 48° latitude and MIDDLE_OF_THE_NIGHT otherwise.
- **Polar guards are on `main` only.** PR #94, which invalidates all times when a calculation fails near the polar circle, merged on 2026-07-04, after the 0.0.7 release (2026-06-13). The v0.0.7 tag's `PrayerTimes.kt` has 0 "polar" matches [V]. With 0.0.7, expect NaN or garbage times at extreme latitudes until the next release. Guard in app code (validate every time, and show "no time today" instead of crashing), or pin a commit.

Other parameters: per-prayer `adjustments` (user offsets), `methodAdjustments`, `Rounding`, and `Shafaq` (for Moonsighting Committee).

### 1.3 Accuracy check I ran [V]

I ran adhan-js 4.4.6 (the same algorithm family and shared test data as adhan-kotlin) with UmmAlQura, Shafi, +30 Isha in Ramadan, at the official Makkah coordinates. I compared it with the **official KACST Umm al-Qura timetable** over 60 days (Ramadan 1447 and Jumada I 1448). Result: the difference in minutes (adhan − official) per prayer:

| Prayer | 0 | −1 | +1 | −2 |
|---|---|---|---|---|
| Fajr | 27 | 31 | 2 | 0 |
| Sunrise | 32 | 1 | 27 | 0 |
| Dhuhr | 23 | 37 | 0 | 0 |
| Asr | 33 | 27 | 0 | 0 |
| Maghrib | 32 | 27 | 0 | 1 |
| Isha | 32 | 27 | 0 | 1 |

So the error is bounded at ±1 minute, with one −2 outlier. It comes from rounding (the official tables appear to round differently, roughly towards later times, which is safer for prayers) and from coordinate and ephemeris differences. adhan-testdata itself records the official source as "adjusted +/- 1 minute". **Use a 1-minute tolerance in unit tests, or 2 minutes for Maghrib/Isha.** [U] The exact official rounding rule is not published, so one option is a "match official" mode that rounds up to the next minute. Test that against the vectors before adopting it.

I could not run adhan-kotlin itself, because there is no JVM on the research machine. Its algorithm and test data are shared with adhan-js, but **re-run these vectors in the Kotlin test suite** [U].

### 1.4 Alternatives

| Option | License | Pros | Cons |
|---|---|---|---|
| [PrayTimes.org / zarrabi/praytime](https://github.com/zarrabi/praytime) v3.2 | MIT (LICENSE file, © 2007–2025 Hamid Zarrabi-Zadeh) [V] | Long-standing reference with a "Makkah: 18.5°, Isha 90 min" method | JS only today, so it would need a hand port to Kotlin. No built-in Ramadan rule. Older ports to other languages exist under various licenses [U]. |
| [Aladhan API](https://aladhan.com/prayer-times-api) (method=4 Umm al-Qura) | Free web API | No math on the device | Needs network and has a third-party dependency, which conflicts with offline/privacy goals. Suitable only as an optional cross-check. |
| Official KACST API (`umqserv.kacst.gov.sa`, see §6) | Government service, no published ToS for third-party use [U] | Authoritative for Saudi cities | Undocumented and could change. Network only. Usable for **test-vector generation**, not as a runtime dependency. |

**Pick: adhan-kotlin (adhan2).** It is MIT, offline, maintained in 2026, has the Umm al-Qura method built in, has high-latitude handling and Qibla, and is used widely in Muslim apps.

---

## 2. Official Umm al-Qura prayer definition

- **Fajr at sun depression 18.5°; Isha = Maghrib + 90 min; Ramadan Isha = Maghrib + 120 min.**
- The 90 and 120 minute values are **[V]** from the official timetable. The KACST API returns Makkah Isha exactly 90 min after Maghrib on normal days (e.g., 2026-10-24 Maghrib 17:50, Isha 19:20). Throughout Ramadan 1447 it returns exactly 120 min (e.g., 1 Ramadan 1447 = 2026-02-18: Maghrib 18:20, Isha 20:20). See §6.
- **[V] Ministry of Islamic Affairs (MOIA)**, 2021 news: the time between Maghrib and Isha in Ramadan is "(ساعتين) حسب توقيت أم القرى" (two hours, according to Umm al-Qura timing). <https://www.moia.gov.sa/MediaCenter/News/Pages/30081442_7.aspx>
- **18.5° Fajr [U, strongly corroborated]:** the official site does not state the angle. Its FAQ (i18n `PRAYER_TIMES.FAQ.ITEMS.DATA_SOURCE`) says only "astronomical calculations according to Umm Al-Qura calendar standards used in Saudi Arabia". The angle comes from batoulapps (source-checked against ummulqura.org.sa), PrayTimes and others. Its fit to the official times within ±1 min over 60 days (above) is strong empirical confirmation.
- The official portal is **https://www.ummulqura.org.sa** ("Developed and maintained by King Abdulaziz City for Science and Technology (KACST)", registered with the Digital Government Authority). Its prayer page is <https://www.ummulqura.org.sa/en/prayer-times> (also `/ar/prayer-times`).
- [U] Ramadan *start* in Saudi Arabia is decided by moon-sighting announcements, which can differ from the Umm al-Qura calendar by a day. Drive the +30 min rule from the app's Hijri date, including the user's Hijri offset, so a user can correct it.

---

## 3. Hijri date

### 3.1 `android.icu.util.IslamicCalendar` (recommended)

- Available since **API 24** (`IslamicCalendar` and `IslamicCalendar.CalculationType`, both "Added in API level 24") [V] — <https://developer.android.com/reference/android/icu/util/IslamicCalendar.CalculationType>.
- Usage: `IslamicCalendar().apply { calculationType = IslamicCalendar.CalculationType.ISLAMIC_UMALQURA }`, or locale `ar-SA-u-ca-islamic-umalqura`. ICU4J docs: <https://unicode-org.github.io/icu-docs/apidoc/released/icu4j/com/ibm/icu/util/IslamicCalendar.html>.
- Implementation [V] (ICU source, `IslamicCalendar.java`): a month-length table for **AH 1300–1600** (`UMALQURA_YEAR_START = 1300`, `UMALQURA_YEAR_END = 1600`). **Outside that range it silently falls back to the arithmetic civil algorithm.** The ICU class Javadoc calls the Islamic calculations "approximations". For UMALQURA inside the range, that is misleading, because the result comes from the table.
- **Accuracy check I ran [V]:** I compared the official KACST month lengths (`/api/v1/DateConversion/GetHijriMonthLengths`, years 1–1500) with ICU 77.1's islamic-umalqura (via Node `Intl`) and with the JDK table. **AH 1301–1500 matched with 0 mismatching years in both.** Also, all 60 days of the official prayer timetables (Ramadan 1447 and Jumada I 1448) matched ICU's Hijri day and month.
- [U] Android ships its own ICU version per OS release, and I tested ICU 77.1, not a specific Android build. The UMALQURA table has been stable for many ICU releases, but **add a unit or instrumented test on the oldest supported Android TV version** using the vectors in §6. Also: KACST occasionally revises future months, and the official table ends at 1500 AH (about 2077).
- Provide a **user Hijri offset (−2…+2 days)**, which is common practice for sighting-based countries.

### 3.2 `java.time.chrono.HijrahDate` (alternative)

- `HijrahChronology.INSTANCE` is "Hijrah-umalqura" (<https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/chrono/HijrahChronology.html>). It is driven by `hijrah-config-Hijrah-umalqura_islamic-umalqura.properties`, with `iso-start=1882-11-12` and years **1300–1600** [V]. It throws `DateTimeException` outside that range, which is better than a silent fallback.
- Its table matched the official one with 0 mismatches for 1300–1500 [V].
- Android: `java.time` arrives at API 26. [U] I did not verify whether core-library desugaring includes `HijrahChronology` data. Do not rely on it below API 26.
- Either API is fine. ICU gives better Arabic month names and formatting through `android.icu.text.DateFormat` with the `ar` locale.

---

## 4. Location on a TV without GPS

The Android TV guidance [V] (<https://developer.android.com/training/tv/get-started/hardware>) says TVs "don't have built-in global positioning system (GPS) receivers". Apps should "let users search for a location or use a static location provider such as a postal code configured during the TV device setup" (`LocationManager.getLastKnownLocation("static")`, which may return null). Declare `<uses-feature android:name="android.hardware.location.gps" android:required="false"/>`, and do the same for any implied features. Marking a feature `required="true"` hides the app from TV launchers.

| Option | Pros | Cons |
|---|---|---|
| **Offline city picker (GeoNames)** | Works offline, private, gives the IANA tz | Needs a bundled dataset and search UI built for a D-pad |
| Static/network location provider | Zero input when it works | Often null on TVs. Network location needs Play Services and Wi-Fi scanning, and it is unreliable [U]. Needs a runtime permission prompt. |
| IP geolocation | Zero input | Sends the IP to a third party (privacy, and needs a disclosure in the Play Data safety form). City-level error can be tens of km, and is wrong behind VPNs or carrier NAT. Many free tiers are HTTP-only or non-commercial (e.g., ip-api.com free tier) [U]. |
| Manual lat/lon + tz | Exact (e.g., a mosque's coordinates) | Tedious with a remote, so best as an "advanced" option |

### GeoNames data [V]

- License: **CC BY 4.0** ("This work is licensed under a Creative Commons Attribution 4.0 License", <https://download.geonames.org/export/dump/readme.txt>). **Attribution is required**: show "Contains data from GeoNames (geonames.org), CC BY 4.0" in About/Licenses and in the repo NOTICE.
- Files (2026-10-24 dump, <https://download.geonames.org/export/dump/>):
  - `cities15000.zip` is **3.2 MB zipped and 8.5 MB unzipped, with 34,180 rows**. It has **98 Saudi rows**, all `Asia/Riyadh`.
  - `cities5000.zip` is **5.5 MB zipped and 15.3 MB unzipped, with 69,943 rows**. It has **144 Saudi rows**.
  - `cities1000.zip` is 11 MB, `cities500.zip` 13 MB, and `alternateNamesV2.zip` 196 MB.
- Columns used: name, asciiname, lat, lon, country code, admin1, population, **`timezone` (IANA id)**.
  - Example: `104515 Makkah 21.42664 39.82563 pop 1578722 Asia/Riyadh` [V].
- **Arabic names:** the `alternatenames` column has no language tags. Build-time filter `alternateNamesV2` on `isolanguage=ar` (prefer `isPreferredName`) for the selected geonameIds. Ship `countryInfo.txt` and `admin1CodesASCII.txt` (plus Arabic alternates) for country and region grouping.
- Build step: preprocess at build time into a compact SQLite/Room DB, a proto, or CSV. Keep only the needed columns and Arabic and English names. Expect roughly 1.5–3 MB in the APK for cities15000 [U, an estimate].

### Recommendation

1. On first run, show a **country, then city** picker (Saudi Arabia preselected), backed by bundled GeoNames cities15000. Add `cities5000` for SA and the Gulf if small towns matter, or allow manual coordinates. Search should match both Arabic and Latin names.
2. Store `lat`, `lon`, and the **IANA tz id from the dataset**. Compute prayer times with that `TimeZone`/`ZoneId` and render in it. If `ZoneId.systemDefault()` differs from the city's zone, show a warning, because a wrong TV clock or zone is a common real-world fault. Also warn if the system clock seems unsynchronised [U].
3. Optional: "use device location", only if `getLastKnownLocation("static"/network)` returns a value. Snap it to the nearest dataset city to get the tz.
4. **No IP geolocation by default.** If it is ever added, make it opt-in with a clear disclosure.
5. Recommended method per country: map the country code to a default method (SA→UMM_AL_QURA, EG→EGYPTIAN, PK→KARACHI, AE→DUBAI, QA→QATAR, KW→KUWAIT, TR→TURKEY, SG/MY/ID→SINGAPORE, US/CA→ISNA or MWL; otherwise MWL) [U, a common convention, not an official mapping].

---

## 5. Iqama defaults in Saudi Arabia

**Finding:** I could **not find a currently valid official MOIA national table** of adhan→iqama waiting times for every prayer. What exists:

| Source | Fajr | Dhuhr | Asr | Maghrib | Isha | Status |
|---|---|---|---|---|---|---|
| MOIA directive, Feb 2021 (COVID precautions) — [Okaz 2021-02-04](https://www.okaz.com.sa/news/local/2057022), [MOIA news 2021](https://www.moia.gov.sa/MediaCenter/News/Pages/30081442_7.aspx), [MOIA on X](https://x.com/Saudi_Moia/status/1357329635130892289) | 20 | 10 | 10 | 10 | 10 | [V] issued. [U] whether still in force. |
| MOIA Ramadan guidance (1446 / reported again 1447): Isha and Fajr 15 min, others "per the approved duration" — [Youm7 2025-02-20](https://www.youm7.com/story/2025/2/20/السعودية-15-دقيقة-فقط-بين-الأذان-والإقامة-لصلاتى-العشاء-والفجر/6890574), [X post](https://x.com/saudistuff/status/2013669592225231119) | 15 | — | — | — | 15 | [U] press reports only |
| Hajj-season Makkah reduction — [Okaz](https://www.okaz.com.sa/news/local/2199104) | 15 | 10 | 10 | 5 | 10 | Seasonal, Makkah only |
| Traditional pre-2020 mosque practice (widely used in Saudi apps; [Okaz 2007](https://www.okaz.com.sa/article/86801) mentions 25–30 min gaps) | 25 | 20 | 20 | 10 | 20 | [U] convention, not an official document |
| Official ummulqura.org.sa front-end constant `prayerDurations={fajr:30,dhuhr:20,asr:20,maghrib:10,isha:20}` | 30 | 20 | 20 | 10 | 20 | [V] in code, but it is **not iqama**. It controls how long the "حان وقت صلاة {prayer}" banner shows after the adhan. |

**Recommendation for app defaults** (all user-editable per prayer, with a Ramadan profile):
- Normal days: **Fajr 25, Dhuhr 20, Asr 20, Maghrib 10, Isha 20.** This matches common Saudi mosque practice; the user asked for Fajr 25 / Maghrib 10. Label it "قابل للتعديل حسب المسجد" (editable to match the mosque).
- Offer a preset button for the "MOIA 2021: 10 min (Fajr 20)" values.
- Ramadan profile: Fajr 15, Isha 15 (per the MOIA Ramadan guidance), others as normal [U].
- Every mosque ultimately sets its own times, so a per-prayer override plus "fixed iqama time" mode (e.g., Isha iqama at 20:30 every day) is valuable.

**Friday (Jumu'ah):**
- In KSA, Dhuhr on Friday becomes Jumu'ah. The adhan stays at the computed Dhuhr time, and the khutbah begins right after it.
- MOIA 2021 rule [V]: jāmiʿ mosques open 30 min before the adhan, and the khutbah plus prayer should not exceed 15 min (a COVID-era rule).
- App behaviour: on Fridays, rename Dhuhr to **الجمعة**. Replace the iqama countdown with a configurable "khutbah/prayer" duration (default about 15–20 min, or "no iqama countdown"). Allow an optional early reminder (e.g., 30–60 min before) [U, a design choice].
- Some countries hold Jumu'ah at a fixed time, so allow a fixed-time override.

---

## 6. Test vectors (official, Makkah)

**Source [V]:** the official Umm al-Qura portal, <https://www.ummulqura.org.sa/en/prayer-times> (KACST). It loads data from a public JSON API that answered plain unauthenticated GETs during this research:

```
https://umqserv.kacst.gov.sa/api/v1/Prayer/GetPrayers?lang=en&format=24&yg=2026&mg=10&dg=24&lat=21.426666&lon=39.831666&zone=3
https://umqserv.kacst.gov.sa/api/v1/Prayer/GetPrayerHijriMonth?lang=en&format=24&yh=1447&mh=9&lat=21.426666&lon=39.831666&zone=3
https://umqserv.kacst.gov.sa/api/v1/DateConversion/GetHijriMonthLengths
```

The site's own Makkah city point is **21.426666, 39.831666, UTC+3**. Querying the requested Kaaba point **(21.4225, 39.8262)** for 2026-10-24 returned the **identical** times [V]. The API is undocumented and may change [U], so **commit the vectors into the repo as JSON fixtures**. Do not call the API from tests.

Page text for 2026-10-24 (official) [V]: *"Today's prayer times in Mecca … on 13/5/1448 AH, corresponding to October 24, 2026 are: Fajr 05:04, Sunrise 06:19, Dhuhr 12:05, Asr 03:23, Maghrib 05:50, and Isha 07:20."*

Vectors for Makkah (Asia/Riyadh, 24 h). Ramadan rows show Isha = Maghrib + 120; other rows show +90:

| Gregorian | Hijri (official) | Fajr | Sunrise | Dhuhr | Asr | Maghrib | Isha |
|---|---|---|---|---|---|---|---|
| 2026-02-18 | 1 رمضان 1447 | 05:33 | 06:49 | 12:35 | 15:53 | 18:20 | 20:20 |
| 2026-02-27 | 10 رمضان 1447 | 05:27 | 06:43 | 12:34 | 15:55 | 18:25 | 20:25 |
| 2026-03-08 | 19 رمضان 1447 | 05:20 | 06:35 | 12:32 | 15:55 | 18:28 | 20:28 |
| 2026-03-19 | 30 رمضان 1447 | 05:10 | 06:25 | 12:29 | 15:54 | 18:32 | 20:32 |
| 2026-10-12 | 1 جمادى الأولى 1448 | 05:00 | 06:15 | 12:08 | 15:29 | 17:59 | 19:29 |
| 2026-10-16 | 5 جمادى الأولى 1448 | 05:01 | 06:16 | 12:07 | 15:27 | 17:56 | 19:26 |
| 2026-10-24 | 13 جمادى الأولى 1448 | 05:04 | 06:19 | 12:05 | 15:23 | 17:50 | 19:20 |
| 2026-11-10 | 30 جمادى الأولى 1448 | 05:11 | 06:28 | 12:05 | 15:17 | 17:41 | 19:11 |

These rows also serve as **Hijri test vectors**: Ramadan 1447 = 2026-02-18 … 2026-03-19, 30 days, and 1 Jumada I 1448 = 2026-10-12.

Suggested test assertions:
- Each prayer within ±1 min of the official time (±2 min for Maghrib/Isha).
- Isha − Maghrib == 90 outside Ramadan and 120 inside, compared using your own computed values.
- The ICU Hijri date equals the official one.
- More historical vectors: `adhan-testdata/Times/Makkah-UmmAlQura.json` (2015–2016).

---

## Verification log (what I actually ran)

- I queried GitHub and Maven Central for repo status, tags and versions, and cloned `adhan-kotlin` to read `CalculationMethod.kt`, `HighLatitudeRule.kt`, `PrayerTimes.kt` and the README.
- I loaded the official ummulqura.org.sa prayer page in a browser, read the API calls it made, then fetched the Ramadan 1447 and Jumada I 1448 monthly JSON plus the month-length table.
- I compared adhan-js 4.4.6 with the official data over 60 days (results in §1.3), and Node ICU 77.1 and the OpenJDK Umm al-Qura table with the official month lengths (0 mismatches, 1300–1500 AH).
- I downloaded GeoNames `cities15000` and `cities5000` and counted rows and Saudi time zones.
- **Not done:** I did not run adhan-kotlin on a JVM or Android, because no JVM was available. I did not test Android-device ICU. I found no official current MOIA iqama table.
