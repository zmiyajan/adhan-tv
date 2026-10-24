# 05 — Audio sources: Saudi Quran Radio live stream, recorded adhan, playback on Android TV

Research date: 2026-10-24. All network probes ran from one macOS host between 18:03 and 18:25 UTC, with the clock synced to time.apple.com to within ±0.07 s. Scope: an open-source Android TV adhan app that plays either the live Saudi Quran Radio (إذاعة القرآن الكريم) or a recorded adhan at prayer time.

Legend: **[V]** verified by a check run during this research (a curl probe, a capture, or reading primary source code or docs). **[U]** uncertain, secondary-only, or inferred.

---

## TL;DR recommendations

| Topic | Recommendation |
|---|---|
| **Default at prayer time** | Play a **bundled recording** at the computed local time. A live stream cannot be the adhan trigger: it is at least ~6 s late against radiojar's own edge, and ~22–25 s with the official HLS on default settings (§6). The studio-to-radiojar delay is unknown, it needs a network, and its own adhan is not on the user's local schedule (§3). |
| Live radio, official source | Saudi Radio+ (radioplus.sba.sa) channel 4 → HLS `https://live.kwikmotion.com/sbrksaquranradiolive/ksaquranradio/playlist.m3u8` (AAC-LC, 192 kbps, 4 s segments, Akamai) [V]. Resolve it at runtime through `https://saudiradio.faulio.com/api/v1.1/channels/4/player/audio` and keep the URL above as a fallback. |
| Live radio, lowest latency | radiojar `0tpy1h0kxtzuv`. **Its audio matches the official feed (correlation 0.97–0.99), and it is about 8 s *ahead* of the official HLS timestamps** [V]. It is an unofficial relay (`icy-name: Mini2's Broadcast`), so treat it as a "fast" option that can break, not as the primary. |
| Use of the live radio | An optional "continue with Quran Radio after the adhan" feature, or a plain radio player. Do not use it as the adhan itself. |
| Recorded adhan licence | No clean-licensed **studio** recording of the official Haram muezzins was found. The best permissive options are visitor recordings of the live Haram adhan under **CC BY 3.0** (licence-reviewed on Commons) and own-work field recordings under **CC0** (§2). Better still: commission a local muezzin to record a clean adhan and release it as CC0. |
| Player | Media3 ExoPlayer 1.11.1. Manage audio focus **manually** with `AUDIOFOCUS_GAIN_TRANSIENT`, because Media3's automatic focus handling only allows USAGE_MEDIA/GAME and permanent focus [V]. Play from a `mediaPlayback` foreground service. |
| Offline fallback | Always bundled: one full adhan (plus an optional Fajr variant) in Opus/AAC at 48–64 kbps mono, about 1–1.5 MB each. |

---

## 1. Live stream URLs for Saudi Quran Radio

### 1.1 Official (Saudi Broadcasting Authority)

How it was found [V]: the [sba.sa](https://sba.sa/ar) homepage links "إذاعة القرآن الكريم" to [radioplus.sba.sa/channel/4](https://radioplus.sba.sa/channel/4). That page is a Nuxt app on the Faulio CMS (`saudiradio.faulio.com`). I loaded [radioplus.sba.sa/ar/live/4](https://radioplus.sba.sa/ar/live/4) in a headless browser and recorded its network requests. The player calls:

- `GET https://saudiradio.faulio.com/api/v1/channels/4` → title "إذاعة القران الكريم", `has_live_audio: true`, guest access allowed (`access.guest: true`) [V]
- `GET https://saudiradio.faulio.com/api/v1.1/channels/4/player/audio` → returns [V]:
  - HLS: `https://live.kwikmotion.com/sbrksaquranradiolive/ksaquranradio/playlist.m3u8`
  - DASH: `https://live.kwikmotion.com/sbrksaquranradiolive/ksaquranradio/manifest.mpd`
  - Smooth: `https://live.kwikmotion.com/sbrksaquranradiolive/ksaquranradio/Manifest`

Properties of the official stream (probed 2026-10-24 18:05–18:25 UTC) [V]:

| Property | Value |
|---|---|
| CDN | KwikMotion on Akamai (`live.kwikmotion.com.akamaized.net`) |
| Codec | AAC-LC, 48 kHz stereo, ADTS segments with ID3 (`CODECS="mp4a.40.2"`, BANDWIDTH 192 681) |
| Segments | `#EXT-X-TARGETDURATION:5`, real durations 3.96–4.04 s, a 3-segment (~12 s) sliding window, `#EXT-X-PROGRAM-DATE-TIME` on every segment |
| DASH | `type="dynamic"`, 4 s segments, `minBufferTime=PT6S`, `timeShiftBufferDepth=PT45S`, and no `suggestedPresentationDelay`, so ExoPlayer falls back to `DEFAULT_FALLBACK_TARGET_LIVE_OFFSET_MS = 30_000` [V, DashMediaSource] (~40 s total delay). **Prefer HLS over DASH.** No low-latency mode, and no LL-HLS parts or `EXT-X-SERVER-CONTROL`. |
| Access | No token, no Referer or User-Agent check. `access-control-allow-origin: *`. HTTPS end to end. |
| Fetch timing | Master playlist TTFB 0.23 s, one 95 KB segment in 0.36 s |
| Availability lag | A segment could be downloaded 0.8–4.8 s (median 2.2 s) after its end PDT (88 segments over 2 captures) |
| Stability | 25 of 25 and 63 of 63 segments arrived contiguously, with PDT gaps ≤ 1 ms, over captures of 90 s and 240 s. Long-term uptime **[U]**: this was not measured over days. |

Caveats:

- `saudiradio.faulio.com/api/v1.1/...` is an **undocumented internal API** of the SBA vendor and can change without notice [U]. Resolve the URL at runtime, cache the last good value, and ship the kwikmotion URL as a hard-coded fallback.
- The same CDN path pattern serves the other SBA stations (`sbrksariyadhradiolive/ksariyadhradio`, `sbrksajeddahradiolive/…`, `sbrksakhuzamaradiolive/…`). Radio Browser lists those, but **not** the Quran Radio one [V] (see §6.1).
- The program-grid API (`/api/v1/programgrid`) gave channel 4 only two "نور على الدرب" entries for the day, with **no adhan or prayer slots** [V]. The schedule cannot tell you when the radio carries an adhan.

### 1.2 Third-party mirrors

| Stream | Probe result | Identity vs official | Notes |
|---|---|---|---|
| `https://stream.radiojar.com/0tpy1h0kxtzuv` | [V] HEAD → **404**. GET → 302 to **`http://`** nXX.radiojar.com/…?rj-ttl=5&rj-tok=… → 200 `audio/mpeg`, MP3 44.1 kHz joint stereo, ~88–96 kbps, `icy-metaint: 16000`, `icy-name: Mini2's Broadcast` | **Same audio as official** [V]: envelope cross-correlation 0.973 (90 s) and 0.994 (240 s), against a noise floor of about 0.14 | A personal relay on Radiojar, not run by SBA [U]. The redirect target is cleartext HTTP, and the token expires in 5 s, so always start from the `stream.radiojar.com` URL. TuneIn station s37232 "Saudi Quran Station" also tunes to this exact stream (the coordinator checked this with TuneIn's OPML API; I did not re-check it). |
| `https://stream.radiojar.com/4wqre23fytzuv` | [V] Same 302 → HTTP pattern, MP3 ~88 kbps, same `icy-name` | **Does not match** the official Quran Radio audio in a ±4 min window (peak 0.18–0.38, at the window edge, which means no match). It also does not match SBA's Makkah station إذاعة نداء الإسلام (channel 1, `…/sbrksanedaradiolive/ksanedaradio/playlist.m3u8`) within ±2.5 min [V] | Labelled "(مكة المكرمة)" on Radio Browser. Either a different feed or delayed by more than 4 min [U]. Avoid. |
| `https://edge.mixlr.com/channel/rwumx` | [V] 200 `audio/mp3`, ~128 kbps, HTTPS | **Does not match** official Quran Radio, نداء الإسلام, or `4wqre23fytzuv` within ±2.5–4 min [V] | Radio Browser has 1094 votes for it. Unknown operator. Avoid as the "Saudi Quran Radio" source. |

### 1.3 Terms of use for embedding

- No public terms of use or licence for the streams were found on sba.sa, radioplus.sba.sa, or the SBA B2B portal ([b2b.sba.sa](https://b2b.sba.sa/Services.aspx)). The portal footer says "All rights reserved", and the portal sells "Specialized Content" and "Digital Media" services [V]. Contact: `info@sba.sa` (from the sba.sa page) [V].
- Technical openness (CORS `*`, no token) is **not** permission [U]. For the GitHub repo, linking to the official stream with attribution ("إذاعة القرآن الكريم — هيئة الإذاعة والتلفزيون") is low-risk. For Google Play, ask SBA for **written permission** before release. Play's IP policy can lead to takedowns of apps that redistribute broadcasts without authorization (see 02-play-policy-android-tv.md). Never re-host or record the stream.
- Using an unofficial relay (radiojar, mixlr) does not add rights. It only adds a dependency on someone's personal account [U].

---

## 2. Recorded adhan with a clear permissive licence

Provenance rule used here: a CC0 or CC BY tag is trusted only when (a) the uploader plausibly made the recording, or (b) Commons ran a licence review against the original source. Self-asserted CC0 or Public Domain Mark labels on rips of broadcasts or YouTube videos are rejected.

### 2.1 Usable (permissive, provenance acceptable)

| File | Muezzin / place | Duration, format | Licence (exact) | Provenance verdict |
|---|---|---|---|---|
| [Commons: Adhan, Great Mosque of Mecca – Jan 21, 2013.webm](https://commons.wikimedia.org/wiki/File:Adhan,_Great_Mosque_of_Mecca_-_Jan_21,_2013.webm) | Live Haram adhan, recorded by a visitor. Muezzin not named [U]. | 197 s, WebM video, 9.4 MB | **CC BY 3.0**, "YouTube CC-BY" by Seyfula Islam. `{{LicenseReview}}` passed, MZaplotnik, 2018-07-19 [V] | OK. Attribute "Seyfula Islam, CC BY 3.0, trimmed / audio extracted". (CC BY requires marking changes.) Visitor-recorded PA sound with crowd noise, so quality is moderate [U]. Extract the audio track. |
| [Commons: Maghrib Adhan at the Masjid al Haram, Mecca – 25 Feb 2012.webm](https://commons.wikimedia.org/wiki/File:Maghrib_Adhan_at_the_Masjid_al_Haram,_Mecca_-_25_Feb,_2012.webm) | Live Haram Maghrib adhan, recorded by a visitor. Muezzin not named [U]. | 301 s, WebM video, 32 MB (includes waiting time before the adhan) | **CC BY 3.0**, "YouTube CC-BY" by 3omar Faruq. `{{YouTubeReview}}` 2020-02-14 [V] | OK. Attribute "3omar Faruq, CC BY 3.0, trimmed". |
| [Commons: 33937 ejaz215 call-to-prayer-from-the-prophet-s-mo.ogg](https://commons.wikimedia.org/wiki/File:33937_ejaz215_call-to-prayer-from-the-prophet-s-mo.ogg) (from [Freesound 33937](http://www.freesound.org/people/ejaz215/sounds/33937/)) | "From the Prophet's Mosque" (Madinah) | 187 s, Ogg | **CC BY 3.0** (`{{cc-by-3.0}}`), no licence review [V] | Probably the uploader's own recording, but this is not confirmed [U]. |
| [Freesound 144431 "Call to Prayer"](https://freesound.org/people/3bagbrew/sounds/144431/) by 3bagbrew | Jumeirah Mosque, Dubai, Jan 2012 | field recording | **CC0 1.0** [V] | Own field recording. Ambient. |
| [Freesound 168775 "morning_azan.wav"](https://freesound.org/people/Jahffar/sounds/168775/) by Jahffar | Village in Tatarstan, Aug 2012 | 24-bit/48 kHz mono field recording | **CC0 1.0** [V] | Own field recording. Not a Haramain style. |
| [Freesound 683120 "Adhan in Al-Azhar Park – Cairo"](https://freesound.org/people/AhmadAiuby/sounds/683120/) by AhmadAiuby | Cairo, distant, with water ambience | field recording | **CC0 1.0** [V] | Own recording, but distant and ambient, so unsuitable as the main adhan. |
| [Commons: Oración Al-Azzan.ogg](https://commons.wikimedia.org/wiki/File:Oraci%C3%B3n_Al-Azzan.ogg) | Description says "Madeenah", but it is categorised under Tunisia | 30 s, Ogg | **PD-self** ("Libero todos los derechos de autor") [V] | Candidate, **unverified**. 30 s is probably a fragment, not a full adhan [U]. |

None of these is a clean studio recording of an official Haram muezzin (e.g., Sheikh Ali Mullah). Every Haram option is a visitor recording of the PA system [V, from the file descriptions]. The Commons review covers only the *uploader's* licence grant. Whether the muezzin has a separate performer's right in a PA recording of a public adhan is untested [U]. Commons accepts these files, so this is not a blocker, but note it.

### 2.2 Share-alike (permissive but copyleft; does not meet "public domain / CC0 / CC BY")

- [The Adhan – Muslim Call to Prayer – Aaqib Azeez.mp3](https://commons.wikimedia.org/wiki/File:The_Adhan_-_Muslim_Call_to_Prayer_-_Aaqib_Azeez.mp3): 86 s, CC BY-SA 4.0, own work (Atcovi), 2020 [V]. A clean solo voice. Usable if the project accepts BY-SA for this one asset.
- [Athan in Ramadan.wav](https://commons.wikimedia.org/wiki/File:Athan_in_Ramadan.wav) (37 s) and [Athan during Ramadan 2.wav](https://commons.wikimedia.org/wiki/File:Athan_during_Ramadan_2.wav) (84 s): CC BY-SA 4.0, daSupremo, 2026 [V].
- [Llamada a oración Mezquita Hassan II.wav](https://commons.wikimedia.org/wiki/File:Llamada_a_oraci%C3%B3n_Mezquita_Hassan_II.wav): 176 s, CC BY-SA 4.0, ambient with ocean sound [V].
- [Islamic call to worship.oga](https://commons.wikimedia.org/wiki/File:Islamic_call_to_worship.oga): CC BY-SA 4.0. The description ("نظرة عن تعريف الأذان ومشروعيّته") suggests a spoken talk, not an adhan [U].

### 2.3 Avoid (unclear rights)

- [Call to prayer by Sabah Fakhry.mp3](https://commons.wikimedia.org/wiki/File:Call_to_prayer_by_Sabah_Fakhry.mp3): tagged "Public domain", but it is a 1985 performance ripped from YouTube, and the artist died in 2021. The tag is not credible.
- [Freesound 639494](https://freesound.org/people/sonically_sound/sounds/639494/) (CC0): its description says "Extracted from youtube.com/watch?v=4dpqrKYI-n4", so the uploader is not the rights holder.
- [Freesound 570199 "Athan Of Muslims"](https://freesound.org/people/Senan_2009/sounds/570199/) (CC0): no provenance.
- archive.org items `MakkahAzan-ramadan`, `28MayFajrAdhanMakkah`, `athan_202604` (CC0), `MakkahAzan_201711`, `adhan.recordings.from.doha.qatar`, `azaan-of-world-mosques_202608` (Public Domain Mark): self-asserted by anonymous uploaders, and almost certainly rips of broadcasts or compilations.
- The Cambridge "World Oral Literature" Cairo adhan recordings are CC BY-NC-ND 3.0, which is non-commercial and no-derivatives. That is not acceptable for a Play app with trimming or encoding.
- Rips of SBA Quran TV or radio broadcasts: rights belong to SBA.

**Recommendation:** for v1, bundle a trimmed audio extract of the licence-reviewed **CC BY 3.0 Haram recording** (2013 file) and show the attribution in an "About / الشكر" screen. In parallel, commission or obtain a clean recording from a muezzin who signs a CC0 release, and swap it in later. Choose the final file by listening; quality was not checked by ear in this research [U].

---

## 3. Does the live radio carry the Haram adhan at prayer time?

- **Arabic Wikipedia** ([إذاعة القرآن الكريم (السعودية)](https://ar.wikipedia.org/wiki/إذاعة_القرآن_الكريم_(السعودية))) says the station carries "النقل الحي للصلوات من المسجد الحرام، ورفع الأذان **مسجلاً أو على الهواء من مسجد الإمام تركي بن عبد الله بالرياض**" [V that the article says this; U whether it is current. The article is unsourced and may be dated]. If true, the radio's adhan is on **Riyadh** time and comes from a Riyadh mosque, not the Haram. Riyadh is roughly 20–25 min ahead of Makkah.
- The official program grid lists no adhan slots (§1.1) [V].
- No primary SBA source confirming the schedule was found [U].
- Not tested live. The capture ran at ~18:05–18:25 UTC (21:05 AST), after Isha.

**Design conclusion (holds whatever the answer):** any adhan that the radio carries happens on *one Saudi city's* schedule (Riyadh or Makkah), plus 6–25 s of stream delay. For users anywhere else, including other Saudi cities, it will not coincide with local prayer time. **The app must play its own recording at the locally computed time.** The live radio can follow the adhan ("بعد الأذان: إذاعة القرآن الكريم"), or the user can play it on demand.

**Verification recipe (for whoever builds it):** capture the official HLS from 2 min before to 5 min after a Riyadh prayer time and a Makkah prayer time on the same day. Each segment has `PROGRAM-DATE-TIME`, so the capture script in §6.3 can record it. Then listen for whether an adhan airs, which voice it is, and when.

---

## 4. Playback on Android TV

### 4.1 Media3 ExoPlayer vs MediaPlayer

- **Use Media3 ExoPlayer.** The latest release is **1.11.1 (2026-09-11)** [V, GitHub releases API]. It plays HLS (`media3-exoplayer-hls`), DASH, and progressive Icecast MP3 (`ProgressiveMediaSource`, with ICY metadata support). It also gives control over buffering and live offset, which matters here.
- `MediaPlayer` (the platform player) can play HLS and MP3 streams, but it has no buffer or live-offset control and weaker ICY handling. Its behaviour also varies between TV vendor builds [U: general knowledge, not tested in this research].

### 4.2 Fast start

- `DefaultLoadControl` defaults in 1.11.1 [V, from source]: `DEFAULT_BUFFER_FOR_PLAYBACK_MS = 1000`, `DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 2000`, `DEFAULT_MIN_BUFFER_MS = DEFAULT_MAX_BUFFER_MS = 50_000`. Playback already starts after 1 s of buffer, so lowering `bufferForPlaybackMs` gains little.
- Measured network start cost for the official HLS [V]: master playlist 0.23 s, then the media playlist, then one 4 s segment (0.36 s). A **time-to-first-audio of about 1–1.5 s** should be achievable with defaults [U: estimated from the curl timings, not measured on a TV].
- **Pre-warm:** call `prepare()` on the player about 10–20 s before the prayer time, then `play()` at the exact time. This hides DNS, TLS, and playlist setup. Only do this for the radio option; the recording is local.
- **Icecast/radiojar specifics** [V for the redirect; V from source for the flag]: `stream.radiojar.com` redirects **HTTPS → HTTP**. `DefaultHttpDataSource.Factory` blocks cross-protocol redirects unless `setAllowCrossProtocolRedirects(true)` is set, and Android 9+ blocks cleartext unless the network security config allows `*.radiojar.com`. Without both, the radiojar URL fails. Also, HEAD returns 404, so do not probe the URL with HEAD.

### 4.3 Live offset (latency) on HLS

- Without `EXT-X-SERVER-CONTROL` hold-back (this stream has none), ExoPlayer targets **3 × target duration = 15 s** behind the live edge [V, `HlsMediaSource.getTargetLiveOffsetUs`]. It then clamps the offset to the playlist duration plus the live-edge offset [V, `createTimelineForLive`]. This playlist only holds ~12 s, so the effective default is ~12–15 s. The short window also leaves little headroom for a low `targetOffsetMs`: one stall puts playback at the window edge [U]. You can override it with `MediaItem.LiveConfiguration.Builder().setTargetOffsetMs(…)`, e.g. 6000–8000 ms (between 1.5 and 2 segments behind). Lower values increase rebuffering risk [U: not soak-tested].

### 4.4 Audio focus when YouTube or Netflix is playing

- **Media3 constraint** [V, `androidx.media3.common.audio.AudioFocusManager`]: automatic focus handling (`setAudioAttributes(attrs, handleAudioFocus = true)`) throws an error unless usage is `USAGE_MEDIA` or `USAGE_GAME`, both of which request **`AUDIOFOCUS_GAIN`** (permanent). That would make YouTube or Netflix lose focus *permanently*. On Android 12+ the system fades them out, and they stay muted until they request focus again [V, [Android audio-focus docs](https://developer.android.com/media/optimize/audio-focus)]. That is wrong for a 2–3 min adhan.
- **Do this instead:** set `handleAudioFocus = false`, then request focus yourself with `AudioManagerCompat.requestAudioFocus` and an `AudioFocusRequestCompat` (both in Media3 common):
  - `AUDIOFOCUS_GAIN_TRANSIENT` (Media3 maps `USAGE_ALARM` to this) → the other app is asked to **pause** and resumes when you abandon focus. **This is the recommended default for the adhan.**
  - `AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK` → the other app keeps playing, quieter. Android 8+ ducks automatically **unless** the other app plays `CONTENT_TYPE_SPEECH` or set `setWillPauseWhenDucked(true)`, in which case it pauses [V, docs]. Offer this as an "خفض صوت التطبيق الآخر" option.
  - Abandon focus as soon as the adhan ends so the other app resumes.
- Whether YouTube and Netflix on Android TV **duck or pause** on a MAY_DUCK request is their own choice. Video apps usually pause on any transient loss [U: not tested on a device]. Test on the target TVs.
- **Android 15+ (targetSdk 35):** focus requests **fail** unless the app is the top app or is running a foreground service [V, docs]. On a TV, the adhan fires in the background, so a **`mediaPlayback` foreground service is mandatory** (started from an exact alarm; see 02-play-policy-android-tv.md for alarm and FGS rules).
- Android ≤ 11 does not enforce focus at all. A misbehaving other app may keep playing over the adhan [V, docs].
- Audio attributes for the player: `USAGE_ALARM` + `CONTENT_TYPE_SONIFICATION` (or `USAGE_MEDIA` with manual transient focus). Note that `USAGE_ALARM` follows the alarm volume stream, which on some TVs is not controlled by the remote's volume keys [U]. Test this, and consider `USAGE_MEDIA` + manual `GAIN_TRANSIENT` to keep the remote's volume working.

---

## 5. Offline fallback

- **Bundle the recording in the APK, always.** The recording is the primary source, so offline is the normal path rather than the fallback. The radio is the optional extra.
- Size budget [U: arithmetic, not encoded yet]: a 2.5–3 min adhan in **Opus 48 kbps mono** is about 1.0 MB, or about 1.5 MB in AAC-LC 64 kbps mono. A separate Fajr adhan (with "الصلاة خير من النوم") adds about 1 MB. A short pre-adhan chime or iqama tone adds under 100 KB. **Total ≤ 3 MB.** Opus in Ogg/WebM is generally decodable on Android TV via platform MediaCodec [U]. AAC is the safest choice on all TVs.
- If the radio option is selected and the stream fails to start within about 3 s of the prayer time, or errors, **switch immediately to the bundled recording**. Do not wait out ExoPlayer's retry loop.
- Optional downloadable "adhan packs" (other muezzins) can be added later, each with its licence shown.

---

## 6. Latency

### 6.1 Every Saudi Quran Radio stream found

| # | Source | URL (url_resolved) | Codec / bitrate | Directory status |
|---|---|---|---|---|
| A | **Official SBA**, Saudi Radio+ channel 4 | `https://live.kwikmotion.com/sbrksaquranradiolive/ksaquranradio/playlist.m3u8` (+ `manifest.mpd`, `Manifest`) | AAC-LC 192 kbps, HLS/DASH, 4 s segments | **Not in Radio Browser** [V] (SBA's Riyadh, Jeddah, Saudia, and Khuzama stations on the same CDN are) |
| B | radiojar relay, also the TuneIn stream (s37232, per coordinator) | `http://stream.radiojar.com/0tpy1h0kxtzuv` | MP3 ~92 kbps measured (Radio Browser bitrate 0) | Radio Browser "Saudi Quran" 6dd3dc65, 3657 votes, lastcheckok 1 (2026-10-22). Duplicates carrying stale tokenised node URLs: "Saudi Quran1" 807f5b26 (`n01.radiojar.com/0tpy1h0kxtzuv?rj-tok=…`), ".Quran radio" 7084ad06 / 552ad30a (`n09…`), all lastcheckok 1 [V] |
| C | radiojar | `https://n05.radiojar.com/4wqre23fytzuv?rj-ttl=5&rj-tok=…` | MP3 ~88 kbps | Radio Browser "إذاعة القرآن الكريم (مكة المكرمة)" ce30edd2, 421 votes, ok 1. **Content does not match A** [V] |
| D | mixlr | `https://edge.mixlr.com/channel/rwumx` | MP3 ~128 kbps | Radio Browser "إذاعة القرآن الكريم (مكة المكرمة)" 5c2e9722, 1094 votes, ok 1. **Content does not match A** [V] |
| — | Not this station | `quraanfm-radio.mbc.net/…m3u8` ("QURAN KAREEM", MBC, lastcheckok 0), `qurango.net/radio/*` (Mp3Quran) | — | Different broadcasters [V] |

Radio Browser data came from `de1`/`all.api.radio-browser.info` on 2026-10-24 [V]. Home Assistant's Radio Browser integration would most likely pick B (6dd3dc65) when searching "Saudi Quran" [U].

### 6.2 Measured delay

Method [V]: the official HLS and streams B, C, D were captured **at the same time**, twice (90 s and 240 s). Everything was decoded to 8 kHz mono with macOS `afconvert`, and 10 ms amplitude envelopes were cross-correlated. Each HLS segment's `PROGRAM-DATE-TIME` dates its audio. Each Icecast byte was dated by its arrival time; a stream reaches real-time once the initial burst has been sent.

| Measurement | Run 1 (90 s) | Run 2 (240 s) |
|---|---|---|
| radiojar B ↔ official A correlation | 0.973 | 0.994 → **same programme** |
| radiojar B server edge **vs** official A PDT stamp | B is **8.3 s earlier** | B is **8.2 s earlier** |
| radiojar initial burst (audio in hand at connect) | ≈ 5.4 s | ≈ 6.3 s |
| radiojar TTFB (incl. 302) | 0.7 s | 0.7 s |
| HLS segment available after its end PDT | median 2.3 s (0.9–3.9) | median 2.2 s (0.8–4.8) |

**Clock sanity check:** the 8 s result depends on the Quran Radio packager's `PROGRAM-DATE-TIME` being real wall-clock time. Segments always became downloadable 0.8–4.8 s *after* their PDT end, on a host synced to ±0.07 s. That is only possible if the packager clock is within about 1–3 s of true time: a clock 8 s slow would show ~10 s lag, and one 8 s fast would show negative lag. So the 8 s figure is real, within about ±3 s, and is not clock skew. Caution: SBA's نداء الإسلام packager showed *negative* lag (−0.3 to −3.8 s), meaning its clock runs ~2–4 s fast. PDT accuracy therefore differs per channel [V].

Expected *listener* delay relative to radiojar's server edge, for the same audio. These are derived from the numbers above [V for the inputs, U for the sum]:

| Path | Delay |
|---|---|
| radiojar B in ExoPlayer (progressive) | ≈ **burst 5–6 s** + ~0–1 s start buffer → **~6–7 s** |
| Official A, ExoPlayer default HLS (12–15 s target offset after clamping) | 8.2 + ~2 (availability) + 12–15 → **~22–25 s** |
| Official A, ExoPlayer DASH (30 s fallback offset) | **~40 s** |
| Official A, ExoPlayer with `targetOffsetMs` ≈ 6–8 s | 8.2 + ~2 + 6–8 → **~16–18 s** |
| Official A via a Chromecast / web HLS player | Typically ≥ 3 segments + receiver launch → **25–35 s** [U] |

The true on-air (FM / studio) time could not be measured. Even radiojar, the earliest stream, lags the real studio by some unknown amount. **So the official HLS is the *slower* source, by about 8 s plus the HLS buffering. The radiojar relay is not the cause of the user's delay.** This agrees with the coordinator's TuneIn finding: TuneIn plays the same radiojar stream, and the extra lag on the TV came from Chromecast and HLS buffering, not the source. **No official SBA source with lower delay than radiojar was found.** SBA publishes only HLS, DASH, and Smooth with 4 s segments and no low-latency mode [V].

### 6.3 Recommendation

1. **On-time sound = the bundled recording, triggered at the computed prayer time** (exact alarm → foreground service → local file). Local playback starts in tens of milliseconds, while every live path is 6–35 s late (and §3 shows the radio's adhan is not on local time anyway). **Make this the default.**
2. For live radio, offer two choices:
   - "Fast" = radiojar B (`stream.radiojar.com/0tpy1h0kxtzuv`) with cross-protocol redirects plus cleartext enabled. About 6–7 s delay, but unofficial.
   - "Official" = SBA HLS, resolved through the Faulio API, with `LiveConfiguration.targetOffsetMs` ≈ 8000. About 17–18 s delay, but official.
   - On either one failing, fall back to the other, then to the recording.
3. Do not cast the stream to a Chromecast receiver for the adhan. Play locally on the TV.
4. The capture and correlation scripts used for §6.2 were scratch tools and are **not** in the repo (they were in the session scratchpad). To re-measure, repeat the method above: run concurrent captures, decode, and cross-correlate envelopes against the official HLS's `PROGRAM-DATE-TIME`.

---

## Sources

- SBA: [sba.sa](https://sba.sa/ar), [radioplus.sba.sa/ar/live/4](https://radioplus.sba.sa/ar/live/4), [b2b.sba.sa](https://b2b.sba.sa/Services.aspx). APIs: `saudiradio.faulio.com/api/v1/channels/4`, `/api/v1.1/channels/4/player/audio`, `/api/v1/programgrid`.
- Directories: [Radio Browser API](https://api.radio-browser.info/), [dRadio listing](https://radio.dubbeh.net/stations/saudi-quran-1362-rep-715856).
- Wikipedia: [ar: إذاعة القرآن الكريم (السعودية)](https://ar.wikipedia.org/wiki/إذاعة_القرآن_الكريم_(السعودية)), [en: Saudi Broadcasting Authority](https://en.wikipedia.org/wiki/Saudi_Broadcasting_Authority).
- Commons file pages and Freesound pages, as linked in §2. Licences were read from the Commons API `extmetadata`, page wikitext, and Freesound pages on 2026-10-24.
- Media3 source (release branch): [DefaultLoadControl](https://github.com/androidx/media/blob/release/libraries/exoplayer/src/main/java/androidx/media3/exoplayer/DefaultLoadControl.java), [AudioFocusManager](https://github.com/androidx/media/blob/release/libraries/common/src/main/java/androidx/media3/common/audio/AudioFocusManager.java), [HlsMediaSource](https://github.com/androidx/media/blob/release/libraries/exoplayer_hls/src/main/java/androidx/media3/exoplayer/hls/HlsMediaSource.java), [DefaultHttpDataSource](https://github.com/androidx/media/blob/release/libraries/datasource/src/main/java/androidx/media3/datasource/DefaultHttpDataSource.java). Releases: [androidx/media releases](https://github.com/androidx/media/releases).
- Android docs: [Manage audio focus](https://developer.android.com/media/optimize/audio-focus).
