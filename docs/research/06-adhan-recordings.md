# 06 — Adhan recordings by style: licences, provenance and measured quality

Research date: 2026-10-24. This follows [05-audio-sources.md](05-audio-sources.md) §2. The question here: can the app offer a choice of adhan **style** (المكي، المدني، القدسي، المصري, and others), each with a clean recording whose licence allows redistribution inside a GPL-3.0 app on GitHub and Google Play, plus a Fajr variant (with «الصلاة خير من النوم») for each style?

Legend: **[V]** verified in this research (an API response, a page or file read, or a measurement I ran). **[U]** unverified, inferred, or not checkable without listening.

**Nobody listened to these files.** Every statement about *content* is [U]: whether a file is an adhan, which style it is, who the muezzin is, whether it is Sunni or Shia, and whether it contains «الصلاة خير من النوم». The numbers are objective measurements; the content judgements are inferred from the file descriptions plus phrase counts. **Listen to the shortlist (paths in §6) before you choose anything.**

Licence filter used: CC0, credible public domain, CC BY, and CC BY-SA (for the audio file only, with attribution) are accepted. NC, ND, unknown uploads and rips of broadcasts or YouTube are rejected. A CC tag counts only if the uploader plausibly made the recording, or Commons ran a licence review against the source (same rule as 05 §2).

---

## TL;DR

| Style | Best licensed recording found | Licence | Measured quality | Verdict |
|---|---|---|---|---|
| **المدني** (Prophet's Mosque) | Commons `33937 ejaz215 call-to-prayer-from-the-prophet-s-mo.ogg` (from Freesound 33937, 2007) | CC BY 3.0 [V] | SNR **42.6 dB**, floor −54 dBFS, 15.7 kHz bandwidth, 12 phrases, ends cleanly [V] | **Usable, and the best Haramain-style file found.** It is still a visitor recording of the PA system [V, from the uploader's description]. |
| **المكي** (Masjid al-Haram) | Commons `Adhan, Great Mosque of Mecca – Jan 21, 2013.webm` (Seyfula Islam) | CC BY 3.0, LicenseReview passed [V] | SNR 29.2 dB, 10.3 kHz bandwidth. **Cut off:** the final phrase starts at 195.3 s and the file ends at 197.0 s while still loud [V] | **No complete clean recording exists.** The 2013 file is much clearer than the bundled 2012 one, but it is missing most of the final «لا إله إلا الله». |
| **القدسي** (Al-Aqsa) | Commons `Al Aqsa Mosque December 2019.webm` | CC BY-SA 2.5, own work [V] | Only ~60 s of adhan, cut off at the end (last 1 s is −2.6 dB from the loud level) [V] | **None.** Nothing complete or clean was found. |
| **المصري** (Cairo) | Freesound 511003 "Call to prayer (Khan El Khalili rooftop)" by 3bagbrew | CC0 [V] | SNR 20.6 dB, several mosques overlap (35 detected segments) [V] | **None clean.** All Egyptian candidates are street or rooftop field recordings. |
| **Fajr variant** | Commons `Eid al-Fitr Fajr azan at Malmö Mosque – 19 August 2012.webm` (Islamic Center Malmö's own channel) | CC BY 3.0, YouTubeReview passed [V] | SNR 38.2 dB, 14.7 kHz; the adhan (~4.9–233 s) appears complete, then a second item starts and is cut off [V/U] | Balkan (Albanian) style, not one of the four. **No Fajr recording of any of the four styles was found.** |
| Cleanest Sunni adhan of any style | Freesound 459704 "Islamic Call to Prayer – Mosque Marylebone" by Emanuele_Correani | **CC0** [V] | SNR **43.8 dB**, floor −66 dBFS, 13 kHz, close-miked lavalier, 12 phrases, ends cleanly [V] | Technically the cleanest. Style unknown [U]. |

**Bottom line:**

1. A clean, licensed recording exists only for **المدني**: a single visitor recording, but a good one.
2. **المكي** has an improvement over the current file, but that file is truncated.
3. **القدسي** and **المصري** have nothing usable.
4. No official body publishes adhan recordings under terms that allow redistribution in an app (§3).
5. The realistic path to "every style, clean, with Fajr" is to **commission recordings released as CC0** (§7), or to get **written permission** from the official bodies (§7.2).

**Why the bundled file sounds muffled** [V]: the current `adhan_haram.m4a` (built from the 2012 Haram file, 113.5–295 s) has an SNR of **8.9 dB**: the quiet gaps between phrases are only ~9 dB below the voice. Crowd and PA reverb fill the gaps. Its 60 dB bandwidth is only **4.2 kHz**. The 2012 source measures the same, 8.8 dB and 4.3 kHz, so trimming did not cause this. For comparison, every candidate below that sounds "clean" on paper has an SNR of 25–45 dB.

---

## 1. Method

- **Searches** [V]:
  - Wikimedia Commons: category `Adhan` and its subcategories, `Muezzins`, plus CirrusSearch with `filetype:audio` and `filetype:video` for adhan, azan, athan, أذان, اذان, call to prayer, muezzin, ezan, adzan, azaan, fajr, Haram, Nabawi, Aqsa, Madinah, Prophet's Mosque, Cairo/Egypt mosque, Dome of the Rock, Kaaba, Quba, Al-Azhar, and others.
  - The audio files used on the en/ar/fr/tr/de Wikipedia "Adhan" articles.
  - Freesound: site search, 3 pages each for adhan, azan, athan, call to prayer, muezzin, adzan, ezan, fajr, and more (89 unique sounds; every sound page was read for its licence and description).
  - archive.org advancedsearch for CC-licensed items (347 hits, filtered by hand).
  - GitHub: open-source adhan apps.
  - Official sites: alharamain.gov.sa, moia.gov.sa, awqaf.gov.jo, pal-wakf.ps, awqaf.gov.eg, azhar.eg, alaqsa.ps.
- **Downloads:** everything went to `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/adhan-candidates/` (`commons/`, `freesound/`). Nothing from that folder was executed.
- **Measurement** [V]: `python3 -I`, stdlib only, with `afconvert` decoding to 44.1 kHz mono 16-bit. Scripts: `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/tools/measure.py`, `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/tools/phrases.py`, `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/tools/envelope.py`, `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/tools/fine.py`. Metrics:
  - `peak` / `RMS`: dBFS.
  - `loud`: 95th percentile of 50 ms frame levels (the voice level).
  - `floor`: 10th percentile (the level between phrases).
  - **SNR = loud − floor**: the main "clean vs. muffled" number.
  - `voiced`: the share of frames more than 15 dB above the floor. This is an SNR proxy, not voice detection.
  - `clip`: samples at or above |32700|.
  - `tail`: the level of the last 1 s relative to `loud`. Above about −8 dB means the file probably ends mid-phrase.
  - `bw`: the highest frequency within 60 dB of the spectral peak (Hann-windowed FFT over the loudest 30% of 2048-sample blocks). A low value means a band-limited or muffled source.
  - `phrases`: breath groups separated by gaps of at least 0.7 s. Heuristic [U]: if each pair of takbirs is sung in one breath, a Sunni adhan has about 12 phrases and Fajr about 14. Muezzins vary, so a matching count is *consistent with* a complete adhan, not proof of one.
- **Caveats:**
  - Commons videos were measured on Commons' **360p MP4 transcode** (MP3 128 kbps stereo 48 kHz), because `afconvert` cannot open WebM.
  - Freesound files were measured on the public **HQ previews** (MP3 ~130–190 kbps). The originals need a login.
  - So bandwidth figures are lower bounds, and SNR is not affected much.
  - `tools/adhan_audio.py` in the repo also builds from the 360p transcode. The bundled file is therefore two lossy generations away from the WebM. Any final asset should be built from the **original** file (WebM/Ogg/WAV) with a real decoder (e.g., ffmpeg in CI) [V for the transcode path; U for how much it matters].

---

## 2. Candidates

### 2.1 Measured, ranked

Ranking order: licence tier and provenance first, then a complete adhan with a clean ending, then SNR, then match to a requested style. All numbers are [V], measured on the segment shown. All content, style and muezzin claims are [U] unless the source says so.

| # | File (link) | Place / muezzin | Style match | Licence (exact template) | Uploader / provenance | Seg. measured | SNR dB | Floor dBFS | bw kHz | Phrases / ending | Format measured |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | [Freesound 459704](https://freesound.org/people/Emanuele_Correani/sounds/459704/) "Islamic Call to Prayer – Mosque Marylebone" | Marylebone mosque, London. Muezzin not named [V] | Unknown [U] | **CC0 1.0** [V] | Emanuele_Correani, sound recordist: "Recorded in the Marylebone Mosque … Wisycom wireless, Sanken COS11 mic, Sound Devices 633" (own work) [V] | 0–119 s of 136 | **43.8** | −66.2 | 13.0 | 12, ends cleanly (−36) | MP3 preview 149 kbps, 48 kHz stereo |
| 2 | [Commons: 33937 ejaz215 call-to-prayer-from-the-prophet-s-mo.ogg](https://commons.wikimedia.org/wiki/File:33937_ejaz215_call-to-prayer-from-the-prophet-s-mo.ogg) | **Prophet's Mosque, Madinah**, April 2007. Muezzin not named [V] | **المدني** [V by description] | **CC BY 3.0** (`{{cc-by-3.0}}`) [V] | ejaz215 on Freesound. The 2013 Wayback page reads: "This specific sample was recorded from the Prophet's Mosque … the PA system is quite powerful", licensed under "Attribution", originally an MP3 at 320 kbps [V]. **The Freesound original is now "Deleted sound"** [V]. Commons has a 126 kbps Ogg. No Commons licence review [V]. The uploader asked: "Please do NOT use this sample in any sort of musical production" (a request, not a licence term). | full 186.6 s | **42.6** | −54.2 | 15.7 | 12, ends cleanly (−24.6) | Ogg Vorbis 126 kbps, 44.1 kHz stereo |
| 3 | [Commons: Adhan in Shalqar mosque.webm](https://commons.wikimedia.org/wiki/File:Adhan_in_Shalqar_mosque.webm) | Matey Qoqanuly mosque, Shalkar, Kazakhstan, 2014 [V] | Central Asian (Hanafi) [U] | **CC BY-SA 4.0** (`{{self\|cc-by-sa-4.0}}`) [V] | Esetok, own work [V] | 0–150 s of 155 | **46.7** | −74.7 | 14.5 | 12 + short blips, ends cleanly | 360p transcode MP3 128 kbps |
| 4 | [Commons: Eid al-Fitr Fajr azan at Malmö Mosque – 19 August 2012.webm](https://commons.wikimedia.org/wiki/File:Eid_al-Fitr_Fajr_azan_at_Malm%C3%B6_Mosque_-_19_August_2012.webm) | Malmö Mosque, Sweden. Description names Besim Azemi [V] | **Fajr**, Balkan/Albanian [U] | **CC BY 3.0** (`{{YouTube CC-BY\|Islamic Center Malmö}}`), **`{{YouTubeReview}}` passed 2020-02-12** [V] | Published by the mosque's own channel (islamiccentermalmo) [V]. This is the strongest provenance found. | 3–246 s of 248 | **38.2** | −52.5 | 14.7 | ~14 phrases from 4.9 to 233.4 s (Fajr count), then a new segment at 241 s cut at file end [V]. Trim at about 234 s [U]. | 360p transcode |
| 5 | [Commons: Adhan, Great Mosque of Mecca – Jan 21, 2013.webm](https://commons.wikimedia.org/wiki/File:Adhan,_Great_Mosque_of_Mecca_-_Jan_21,_2013.webm) | Masjid al-Haram, visitor recording. Muezzin not named [V] | **المكي** [V by description] | **CC BY 3.0** (`{{YouTube CC-BY\|Seyfula Islam}}`), `{{LicenseReview}}` passed [V] | Seyfula Islam, YouTube [V] | 6–197 s | 29.2 | −41.3 | 10.3 | **11 full phrases + the final phrase cut after 1.7 s** (file ends at −2.8 dB) [V] | 360p transcode |
| 6 | [Freesound 861625](https://freesound.org/people/jrosin/sounds/861625/) "Morning call to prayer on Bodufolhudhoo, Maldives" | Island mosque, Maldives. Described as Fajr [V] | **Fajr**, Maldivian [U] | **CC BY 4.0** [V] | jrosin, own field recording [V] | full 284.6 s | 27.4 | −39.1 | 7.9 | 14 + blip (Fajr count), ends cleanly | MP3 preview |
| 7 | [Commons: Beautiful adhan.ogg](https://commons.wikimedia.org/wiki/File:Beautiful_adhan.ogg) | Unknown [V] | Unknown, long-drawn phrases [U] | **CC0** (`{{self\|Cc-zero}}`) [V] | Adam-synagda, "own work", 2022. The uploader's other files are Russian Islamic talks [V]. Own-work claim **unverified** [U]. | full 154 s | 26.4 | −35.4 | 8.3 | 12, ends cleanly | Ogg 63 kbps |
| 8 | [Commons: Azan.ogg](https://commons.wikimedia.org/wiki/File:Azan.ogg) | Unknown [V] | Unknown. 14 phrases may mean a **Fajr** adhan [U] | **CC BY-SA 4.0** [V] | Andrewler, "own work", 2022. Other uploads are SVG drawings [V]. Own-work claim **unverified** [U]. Not the same recording as #7 (envelope correlation 0.39) [V]. | full 183.5 s | 24.2 | −33.1 | 8.4 | 14, ends cleanly | Ogg 64 kbps |
| 9 | [Commons: The Adhan – Muslim Call to Prayer – Aaqib Azeez.mp3](https://commons.wikimedia.org/wiki/File:The_Adhan_-_Muslim_Call_to_Prayer_-_Aaqib_Azeez.mp3) | Aaqib Azeez (solo voice) [V] | Short, fast delivery (86 s), not a Haramain style [U] | **CC BY-SA 4.0** [V] | Atcovi, own work 2020 [V] | full 86.5 s | 36.6 | −48.9 | 18.3 | 12, ends cleanly | MP3 134 kbps |
| 10 | [Freesound 133832](https://freesound.org/people/felix.blume/sounds/133832/) | Diafarabé, Mali: "without sound reinforcement, recorder close to the muezzin" [V] | West African [U] | **CC0** [V] | felix.blume, professional sound recordist [V] | full 65.9 s | 35.6 | −52.6 | 9.8 | 13, starts with a 15 s pause | MP3 preview |
| 11 | [Freesound 508441](https://freesound.org/people/bagustris/sounds/508441/) "adzan-forest" | The uploader performing the adhan on a forest path in Kaga, Japan [V] | Long-drawn [U] | **CC0** [V] | bagustris, own performance [V] | full 227 s | 34.2 | −45.9 | 18.8 | 15 segments, ends cleanly. Includes river, birds and footsteps [V by description]. | MP3 preview |
| 12 | [Commons: Time of Adhan, Great Mosque of Mecca – Jan 6, 2018.webm](https://commons.wikimedia.org/wiki/File:Time_of_Adhan,_Great_Mosque_of_Mecca_-_Jan_6,_2018_.webm) | Masjid al-Haram, visitor [V] | المكي [V by description] | CC BY 3.0, `{{YouTubeReview}}` 2019-12-07 [V] | M SOHAIL Ahmad, YouTube [V] | full 189 s | **14.4** | −36.8 | 15.8 | 12–13, first phrase only 6.8 s (start probably cut), end unclear | 360p transcode |
| 13 | Freesound 511003 (Khan El Khalili, Cairo) | Rooftop café, Khan el-Khalili, Cairo [V] | **المصري** (several mosques) [U] | CC0 [V] | 3bagbrew [V] | full 336 s | 20.6 | −32.8 | 9.5 | 35 segments (overlapping mosques) | MP3 preview |
| 14 | [Freesound 555065](https://freesound.org/people/AhmadAiuby/sounds/555065/) (Quseir) | Quseir, Egypt, 2018 [V] | Egyptian [U] | CC0 [V] | AhmadAiuby, own field recording [V] | full 126.7 s | 15.7 | −43.1 | 15.6 | 25 fragments | MP3 preview |
| 15 | [Freesound 457264](https://freesound.org/people/Agim/sounds/457264/) / [457265](https://freesound.org/people/Agim/sounds/457265/) (Giza) | Al Mansoureya Rd, Giza, busy street [V] | Egyptian [U] | CC0 [V] | Agim [V] | full | **8.2 / 8.6** | −28.7 | 18 | traffic-dominated | MP3 preview |
| 16 | [Freesound 191866](https://freesound.org/people/Zabuhailo/sounds/191866/) | Old City of Jerusalem, square in front of the Holy Sepulchre, phone recording [V] | Jerusalem (not necessarily Al-Aqsa) [U] | CC BY 4.0 [V] | Zabuhailo, "Posted with permission" of the author Denis Nelubin [V] | full 108.6 s | 16.5 | −24.4 | 9.8 | 7 long overlapping segments | MP3 preview |
| 17 | [Freesound 239921](https://freesound.org/people/nikitralala/sounds/239921/) | East Jerusalem, "call to prayer from several mosques" [V] | Jerusalem [U] | CC0 [V] | nikitralala, binaural [V] | full 243.6 s | 24.0 | −43.3 | 16.1 | overlapping, no clean phrase structure | MP3 preview |
| 18 | [Commons: Al Aqsa Mosque December 2019.webm](https://commons.wikimedia.org/wiki/File:Al_Aqsa_Mosque_December_2019.webm) | Al-Aqsa compound, 2019 [V] | **القدسي** [U] | CC BY-SA 2.5 (`{{cc-by-sa-2.5}}`, Wikimedia Levant project) [V] | براء, own work [V] | 36–97 s | 27.0 | −45.0 | 12.4 | **~3 phrases, cut at file end** (−2.6) [V] | 360p transcode |
| 19 | [Freesound 331519](https://freesound.org/people/nikitralala/sounds/331519/) "muezzin_5am_windy" | Abu Dhabi, 5 am (Fajr) [V] | Gulf [U] | CC0 [V] | nikitralala [V] | full 202 s | 24.2 | −51.5 | **4.5** (wind) | 13 | MP3 preview |
| 20 | [Commons: Maghrib Adhan at the Masjid al Haram – 25 Feb 2012.webm](https://commons.wikimedia.org/wiki/File:Maghrib_Adhan_at_the_Masjid_al_Haram,_Mecca_-_25_Feb,_2012.webm) (**currently bundled**) | Haram, visitor [V] | المكي | CC BY 3.0, `{{YouTubeReview}}` 2020-02-14 [V] | 3omar Faruq [V] | 113.5–295 s | **8.8** | −26.0 | **4.3** | 12, ends cleanly | 360p transcode |
| — | Bundled `app/src/main/res/raw/adhan_haram.m4a` | (#20 trimmed) | — | — | — | full 181.5 s | **8.9** | −23.8 | **4.2** | — | AAC 62 kbps mono [V] |

Other measured files, not ranked [V for numbers]:

- Commons `Adhan.ogg` (Aishatu98, CC0, 42 s): 4.2 kHz bandwidth, probably a fragment.
- `Iqamah.ogg` (CC0, an iqama, not an adhan; 2.6 kHz).
- `Adhan wiki.oga` (CC BY-SA 3.0, 26.6 s, "a simple lecture of the adhan", i.e. a recitation sample).
- `Oración Al-Azzan.ogg` (PD-self, 30 s, two phrases only).
- `Athan in Ramadan.wav` (CC BY-SA 4.0, 2 phrases).
- `Athan during Ramadan 2.wav` (CC BY-SA 4.0, 84 s, SNR 28, 13 phrases).
- `Llamada a oración Mezquita Hassan II.wav` (CC BY-SA 4.0, SNR 19, ocean noise).
- `Call to prayer.ogg` (CC BY-SA 4.0, **14 520 clipped samples**).
- `Adhan in Istanbul.webm` (CC0, SNR 9).
- Somaliland (Leiden, CC BY-SA 4.0, SNR 13.5).
- Isfahan tour guide (CC BY 3.0, SNR 12.8, 48 s).
- `Al-Masjid Al-Aqsa(Bayt al-Muqaddas).webm` (CC BY 3.0 reviewed, but the audio is a flat background track with no phrase structure, SNR 11.7, and the uploader is an aggregator channel). **Not an adhan recording** [U].
- Freesound 807484 (Aroumd, CC BY 4.0, 2.9 kHz bandwidth).
- Freesound 331518 (Abu Dhabi, CC0, SNR 14).
- Freesound 342165 (CC0, SNR 18).
- Freesound 256192 (Alexandria, CC BY 4.0, 19 s fragment, "beginning missing").
- Freesound 36997 (Azemour, CC BY 3.0): **download truncated** at 106 KB, not measured.

### 2.2 Shia adhans: listed only as a warning

- [Commons: AzaanMaahur.ogg](https://commons.wikimedia.org/wiki/File:AzaanMaahur.ogg) (Saeed Hatamzadeh-Varmazyar, CC BY-SA 4.0, own voice, Persian dastgah Mahur) measures as a studio recording: **SNR 69 dB**, floor −80.8 dBFS [V].
- [Commons: اذان حسین صبحدل.webm](https://commons.wikimedia.org/wiki/File:%D8%A7%D8%B0%D8%A7%D9%86_%D8%AD%D8%B3%DB%8C%D9%86_%D8%B5%D8%A8%D8%AD%D8%AF%D9%84.webm) (Hussein Sobhdel, CC BY-SA 4.0) measures SNR 31.5 dB [V].
- Both are Iranian, and their phrase counts (16+) exceed the 12 of a Sunni adhan [V]. They very likely contain «أشهد أن علياً ولي الله» and «حي على خير العمل» [U, not listened]. **Do not offer them in a Saudi-oriented app** unless the app adds a separate, clearly labelled Shia option.

### 2.3 Rejected (licence or provenance)

| Source | Why rejected |
|---|---|
| archive.org: `IshaAzan14thOct2010ByAliAhmedMullah` (CC BY 3.0), `17thAug2010MaghribAzan` and `18thAug2010MaghribAzan` (CC BY 3.0), `AdzanKotaMadinah` (CC BY 4.0), dozens of "Public Domain"/CC0 items credited to `www.HaramainRecordings.co`, `MakkahAzan*`, `MadinahFajrAzan*`, `is-4iq-cfrk-3c-arabvid-org` ("اذان علي ملا 1415ه"), `zied_hadj_hassen1` (السريحي, Quba) | Anonymous uploaders with self-asserted licences on recordings of Saudi TV/radio broadcasts or commercial releases. HaramainRecordings is a site that records the broadcast. The uploader is not the rights holder [V for the metadata; U for each item's exact origin]. |
| archive.org `April202012-SheikhFarooqAlHadraawi-MaghribAdhan`, `Mp3CollectionAdzan`, `AdzanAroundTheWorld`, `jamendo-225970` (Al-Aqsa), `aporee_55249…` (Wadi Rum) and other `by-nc-*` / `by-nd` items | NC or ND licences are not allowed [V]. |
| Freesound 178382, 179605, 330597, 613933, 768362 (Palestine), 89738, 69643, 401917, 725099 | CC BY-NC [V]. |
| Freesound 639494 | "Extracted from youtube.com/…" [V]. |
| Freesound 570199 | No provenance ("Very Nice Athan Of Muslims") [V]. |
| Cambridge World Oral Literature (Cairo) | CC BY-NC-ND 3.0 (see 05 §2.3). |
| `Call to prayer by Sabah Fakhry.mp3` | "Public domain" tag on a 1985 commercial performance (see 05 §2.3). |
| Open-source apps | See §4. |

---

## 3. Official bodies: published terms

| Body | Adhan recordings offered? | Terms (quoted) | Verdict |
|---|---|---|---|
| **الهيئة العامة للعناية بشؤون المسجد الحرام والمسجد النبوي** ([alharamain.gov.sa, policies page](https://alharamain.gov.sa/public/?page=authority-terms-ar)) | No download library found on the portal's homepage links [V]. Live broadcast is the official channel [U]. | IP policy: «جميع المحتويات … ملكًا للهيئة ومحميّة بموجب قوانين حقوق الملكية الفكرية المحلية والدولية. **يُحظر إعادة إنتاج، توزيع، أو تعديل أي من هذه المحتويات دون إذن كتابي مسبق من الهيئة**» [V]. Terms of use §10: «جميع المحتويات المنشورة على البوابة محمية بموجب حقوق النشر، ولا يجوز استخدامها دون إذن خطي من الهيئة» [V]. The customer charter §5 is looser: «يُمنع استخدامها لأغراض تجارية دون إذن خطي … **ويُسمح بالاستخدام الشخصي وغير الربحي مع ضرورة الإشارة إلى مصدر المحتوى**» [V]. | **No redistribution licence.** The policies contradict each other, and the strictest one forbids reproduction, distribution and modification without written permission. A free, ad-free app is non-profit, but bundling and trimming a file is distribution and modification, so **ask for written permission** (§7.2). |
| **وزارة الشؤون الإسلامية والدعوة والإرشاد** ([moia.gov.sa terms](https://www.moia.gov.sa/Pages/Terms-Of-Use.aspx)) | None found [V for the homepage links] | «تحظر إعادة إنتاج هذه المواد فيما عدا بموجب شروط إشعار حقوق الملكية المحفوظة» [V] | No licence. |
| **وزارة الأوقاف والشؤون والمقدسات الإسلامية (الأردن)**, the custodian of the Jerusalem Awqaf, i.e. Al-Aqsa ([copyright page](https://www.awqaf.gov.jo/AR/Pages/%D8%AD%D9%82%D9%88%D9%82_%D8%A7%D9%84%D9%86%D8%B4%D8%B1)) | None found [V for the homepage] | «جميع محتويات الموقع … (النصوص والرسومات والصور والملفات والروابط **وملفات الصوت**) ملكا للوزارة … تسمح … بالتنزيل … للاستخدام الشخصي … **ولأغراض غير تجارية** … إعادة نشر أجزاء بسيطة … **دون التعديل** … في حالة وجود حاجة للاستخدام لغير الغايات الموضحة أعلاه يرجى مخاطبة الوزارة» via `awqaf@awqaf.gov.jo` [V] | Non-commercial and no modification, so not compatible. Written permission is needed. |
| Palestinian Awqaf ([pal-wakf.ps](https://www.pal-wakf.ps/)) | Photo and video libraries only [V] | No licence found [U] | — |
| Egyptian Ministry of Awqaf (awqaf.gov.eg) | Runs «الأذان الموحد», a radio-transmitted unified adhan in Greater Cairo since 2010–2019 ([Al Arabiya 2010](https://www.alarabiya.net/articles/2010/07/04/113023.html), [Akhbar El Yom 2021](https://m.akhbarelyom.com/news/newdetails/3533796/1/%D8%A7%D9%84%D8%A3%D9%88%D9%82%D8%A7%D9%81-%D8%A7%D9%84%D8%A3%D8%B0%D8%A7%D9%86-%D8%A7%D9%84%D9%85%D9%88%D8%AD%D8%AF-%D9%8A%D8%B9%D8%AA%D9%85%D8%AF-%D8%B9%D9%84%D9%89-%D8%AC%D9%87%D8%A7%D8%B2-%D8%A7%D8%B3%D8%AA)) [V that the articles say this] | **Site unreachable** from this host (connection failed) [V]. Terms not checked [U]. | Unknown. Contact them (§7.2). |
| Al-Azhar (azhar.eg) | Splash page only [V] | Not found [U] | — |
| alaqsa.ps | Unreachable [V] | — | — |

No official body publishes adhan recordings under an open licence [V for the sites checked; U for those that were unreachable].

---

## 4. Open-source adhan apps: bundled audio

| Project | Bundled audio | Stated provenance | Counts? |
|---|---|---|---|
| [meypod/al-azan-compose](https://github.com/meypod/al-azan-compose) (AGPL-3.0, on F-Droid) | `res/raw/masjid_an_nabawi.mp3`, `abdul_basit_abdus_samad.mp3`, `ragheb_mustafa_ghalwash.mp3`, `moazen_zade.mp3` [V] | None: `licenses/README.md` covers only the fonts [V] | **No** |
| [meypod/al-azan](https://github.com/meypod/al-azan) (AGPL-3.0) | `src/assets/sounds/masjid_an_nabawi.mp3` [V] | None [V] | **No** |
| [DBChoco/Salawat](https://github.com/DBChoco/Salawat) (MIT) | `Adhan - Mecca.mp3`, `Adhan - al-Aqsa.mp3`, `Adhan - Ahmed Al-Nufais.mp3` [V] | README credits YouTube URLs (`watch?v=MaEzj5eRmjc`, `watch?v=9k0wQtYu2nU`) [V] | **No** (YouTube rips) |
| [Kiwifu/adhan-mp3](https://github.com/Kiwifu/adhan-mp3) | 224 MP3s: Ali Mulla, Haram muezzins, Najee Qazaz (Al-Aqsa), Abdul Basit, and others [V] | No licence (`license: null`) [V] | **No** |

An F-Droid listing does not mean the app's media assets were licence-cleared. F-Droid reviews code licences, not audio provenance [U].

---

## 5. Per-style verdicts

### المكي (Masjid al-Haram)

- **No clean, complete, licensed recording exists** [V for everything searched].
- Choose between:
  - **(a)** the 2013 Seyfula Islam file, about **+20 dB** better SNR than the current file (29.2 vs 8.8 dB) but missing the final phrase. It could be "repaired" by fading out after the last takbir; the result would be an incomplete adhan, which is liturgically unacceptable [U, a judgement].
  - **(b)** the current 2012 file, complete but muffled (SNR 8.8 dB).
- **Recommendation:** keep (b) as the bundled default for now. Rebuild it from the original WebM rather than the 360p transcode, and apply a gentle expander or noise gate between phrases. Ask the user to compare by ear. Pursue §7 for a real fix.
- Makki Fajr: **none found.**

### المدني (Prophet's Mosque)

- **Usable:** the ejaz215 2007 recording, CC BY 3.0 [V]. Among Haramain-style files it is the best by far: SNR 42.6 dB, 12 complete phrases, clean ending [V].
- Attribution: "Call to Prayer from the Prophet's Mosque – ejaz215 (Freesound 33937), CC BY 3.0, via Wikimedia Commons; trimmed and re-encoded." Honour the "no musical production" request (the app is not one).
- Provenance is "plausible own work" (a first-person description from 2007), **not** a Commons licence review [V]. The Freesound original was deleted at some point. The reason is unknown, and could be the user's choice or a takedown [U]. Archived Freesound copies exist only as low-quality previews [V].
- Madani Fajr: **none found.**

### القدسي (Al-Aqsa)

- **None.** The only Al-Aqsa-labelled own-work file holds ~60 s of a partial adhan, cut off [V].
- Jerusalem street recordings (#16, #17) are overlapping, multi-mosque, phone or binaural recordings [V], and cannot be attributed to Al-Aqsa [U].
- Path: §7, either the Jerusalem Awqaf through the Jordanian ministry (`awqaf@awqaf.gov.jo`) or a muezzin who records in this style.

### المصري (Cairo)

- **None clean.** The best is Khan el-Khalili, CC0, at SNR 20.6 dB, with several mosques overlapping [V].
- The famous Egyptian adhans (Muhammad Refaat, Al-Hussary, Abdul Basit, Naqshbandi) are commercial or state-radio recordings, and no licensed copies were found [V for the searches; U for each work's copyright status].
  - Note [U]: Sheikh Muhammad Refaat died in 1950, so his 1940s radio recordings *might* now be out of copyright in some countries. The term rules for performances and sound recordings differ by country, and the rights of Egyptian Radio as the producer are a separate question. None of this was checked. Do not rely on it without legal advice.

### Other styles worth offering (cleanest licensed Sunni recordings)

| Option | File | Licence | Why |
|---|---|---|---|
| "London" / clean neutral | Freesound 459704 (Marylebone) | CC0 | SNR 43.8, professional close-mic, nothing to attribute |
| Central Asian | Commons Shalqar | CC BY-SA 4.0 | SNR 46.7, own work; a quiet file, so normalise (peak −0.4 dBFS but loud level −28) |
| Balkan, **with Fajr** | Commons Malmö Fajr | CC BY 3.0, reviewed, published by the mosque | SNR 38.2; trim at about 234 s |
| Fajr, alternative | Freesound 861625 (Maldives) | CC BY 4.0 | SNR 27.4, 14 phrases, clean ending |

CC BY-SA on a single audio asset inside a GPL-3.0 app is fine for this project. The file stays BY-SA, the GPL covers the code, and the app must show attribution plus the licence link. Note in `CREDITS.md` that BY-SA applies to that file only [U: standard reading, not legal advice].

---

## 6. Shortlist: scratch file paths

Base: `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/adhan-candidates`

| Style | File |
|---|---|
| المدني | `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/adhan-candidates/commons/a07_orig.ogg` |
| المكي (2013, truncated) | `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/adhan-candidates/commons/v01.mov` |
| المكي (2012, bundled source) | `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/adhan-candidates/commons/v02.mov` |
| المكي (2018, noisy) | `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/adhan-candidates/commons/v00.mov` |
| القدسي (partial) | `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/adhan-candidates/commons/v04.mov` |
| المصري (Khan el-Khalili) | `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/adhan-candidates/freesound/fs_511003_571436-hq.mp3` |
| Clean neutral (Marylebone) | `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/adhan-candidates/freesound/fs_459704_2808561-hq.mp3` |
| Fajr (Malmö) | `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/adhan-candidates/commons/v03.mov` |
| Fajr (Maldives) | `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/adhan-candidates/freesound/fs_861625_2713464-hq.mp3` |
| Central Asian (Shalqar) | `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/adhan-candidates/commons/v06.mov` |
| Unknown style, CC0 (Beautiful adhan) | `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/adhan-candidates/commons/a05_orig.ogg` |
| Unknown style, 14 phrases (Azan.ogg) | `/private/tmp/claude-501/-Users-zmiyajan-Desktop-home-assistant/a991e66b-387d-41c4-8c8c-44a4d3c17ed6/scratchpad/adhan-candidates/commons/a04_orig.ogg` |

The other `aNN` and `vNN` files follow the order of the measurement lists above. Their mapping: a00 Adhan.ogg, a01 Iqamah, a02 Adhan wiki, a03 AzaanMaahur, a04 Azan, a05 Beautiful adhan, a06 Call to prayer, a07 ejaz215, a08/a09 daSupremo, a10 Oración, a11 Aaqib Azeez, a12 Hassan II; v00 Haram 2018, v01 Haram 2013, v02 Haram 2012, v03 Malmö, v04 Al-Aqsa 2019, v05 Al-Masjid Al-Aqsa (Masajida Allah), v06 Shalqar, v07 Istanbul, v08 Sobhdel, v09 Isfahan, v10 Somaliland. The `.mov` files are Commons' 360p transcodes, used for measurement only.

---

## 7. The realistic path to clean recordings for every style

1. **Commission recordings released as CC0.** This is the only route that yields clean, complete Makki, Madani, Qudsi and Egyptian adhans with Fajr variants and no legal doubt. Ask a muezzin who performs in the style (many local muezzins imitate the Haramain styles closely) to record in a quiet room, and to sign a one-paragraph release dedicating the recording to CC0.
   - Recording spec: 48 kHz/24-bit WAV, about 30–50 cm from a cardioid or lavalier mic, no PA, no reverb, 3 s of room silence at both ends, a full adhan plus a Fajr adhan, ideally two takes each.
   - Note: the right to the *performance* belongs to the muezzin, so the release must come from him, not only from the person holding the recorder.
2. **Ask the official bodies for written permission**, at the same time. The Haramain authority and the Jordanian Awqaf both say plainly that use outside their terms needs written permission (§3). Ask for permission to bundle a specified adhan recording, trimmed and re-encoded, in a free, ad-free GPL app distributed on Google Play and GitHub, with attribution. A permission limited to "this app" makes the file non-free for forks. Record that limitation in `CREDITS.md`, and keep such files out of the GPL source tree or mark them as an exception [U, a judgement].
3. **Meanwhile:** ship المدني (ejaz215) and keep the current المكي file. Optionally add the CC0 Marylebone file and the reviewed Malmö Fajr as "other styles". Label honestly in the app, e.g. «تسجيل من زائر للمسجد النبوي».

### 7.1 Draft request to a muezzin (Arabic; nothing has been sent)

> السلام عليكم ورحمة الله وبركاته،
>
> أعمل على تطبيق مجاني بلا إعلانات اسمه «أذان التلفاز»، يرفع الأذان على شاشات التلفاز في البيوت عند دخول وقت الصلاة، وشيفرته مفتوحة المصدر وسيُنشر على Google Play وGitHub.
>
> أرجو أن تتكرم بتسجيل الأذان بصوتك على الطريقة [المكية / المدنية / المقدسية / المصرية]: أذانٌ كامل، وأذان الفجر بـ«الصلاة خير من النوم»، في غرفة هادئة بدون مكبّر صوت ولا صدى (وسنتكفّل بالتسجيل إن شئت).
>
> ولكي يُستخدم التسجيل في التطبيق ويبقى متاحاً لكل المسلمين دون قيود، نرجو موافقتك المكتوبة على إتاحة التسجيل للعموم (رخصة CC0: يجوز لأي أحد استخدامه ونسخه وتعديله ونشره دون مقابل). وسنذكر اسمك في التطبيق إن رغبت بذلك، أو نُغفله إن شئت.
>
> نص الموافقة المقترح: «أنا [الاسم]، مؤدّي هذا التسجيل وصاحب الحق فيه، أتنازل عن جميع حقوقي فيه وأُتيحه للملك العام وفق رخصة CC0 1.0، بتاريخ [...]، التوقيع: [...]».
>
> جزاك الله خيراً، وجعله في ميزان حسناتك.

### 7.2 Draft request to an official body (Arabic; nothing has been sent)

Addressees found: the Haramain authority (alharamain.gov.sa, «اتصل بنا» page); the Jordanian Ministry of Awqaf, `awqaf@awqaf.gov.jo` [V, from their copyright page]; the Egyptian Ministry of Awqaf (site unreachable, contact not verified [U]).

> الموضوع: طلب إذن كتابي باستخدام تسجيل أذان في تطبيق مجاني غير ربحي
>
> السلام عليكم ورحمة الله وبركاته،
>
> أطلب إذنكم الكتابي باستخدام تسجيل [أذان المسجد الحرام / المسجد النبوي / المسجد الأقصى] [اسم المؤذن والصلاة والتاريخ إن أمكن] داخل تطبيق «أذان التلفاز»: تطبيق مجاني، بلا إعلانات ولا مشتريات، مفتوح المصدر (GPL-3.0)، يرفع الأذان على أجهزة التلفاز عند دخول وقت الصلاة، ويُنشر على متجر Google Play وموقع GitHub.
>
> الاستخدام المطلوب: إدراج التسجيل داخل التطبيق بعد قصّه إلى الأذان فقط وضغطه صوتياً، دون أي تغيير في الأداء، مع نسبته إليكم بوضوح داخل التطبيق: «التسجيل بإذن من [الجهة]».
>
> وقد اطّلعنا على سياسة الملكية الفكرية لديكم التي تشترط الإذن الكتابي المسبق لإعادة الإنتاج أو التوزيع، ولذلك نتقدّم بهذا الطلب. ونرحّب بأي شروط تضعونها، ويسعدنا إن تكرّمتم بإتاحة التسجيل برخصة مفتوحة (مثل CC BY 4.0) لينتفع به المسلمون في تطبيقات أخرى.
>
> وتقبّلوا خالص الشكر والتقدير.
>
> [الاسم — وسيلة التواصل — رابط المشروع]

---

## 8. What could not be verified

- **No file was listened to.** Style, muezzin, Sunni/Shia formula and the presence of «الصلاة خير من النوم» are all inferred [U].
- Original WebM audio was not decoded (no ffmpeg). Videos were measured on the 360p transcodes [V that this was the path used].
- Freesound originals were not downloaded (login required). Previews were measured instead.
- The reason the ejaz215 original was deleted from Freesound is unknown [U].
- The Egyptian Awqaf and alaqsa.ps sites were unreachable, so their terms were not checked [U].
- Whether a muezzin holds a separate performer's right in a visitor's PA recording of a public adhan is untested, as noted in 05 §2.1 [U]. This applies to every Haramain file above, including the recommended المدني file.
- Commons and Wikipedia search can miss files with unusual titles. Seven Commons category queries were rate-limited and returned nothing: `Videos of mosques`, `Videos of Mecca`, `Videos of Medina`, `Videos of Jerusalem`, `Videos of Cairo`, `Adhan in Egypt`, `Videos of mosques in Egypt`. They were partly covered by the full-text searches [V for what ran].

## Sources

- Wikimedia Commons file pages linked in §2 (licence templates read via the MediaWiki API, `action=raw`) [V]
- Freesound sound pages linked in §2; Wayback capture of Freesound 33937 from 2013-07-09 (`web.archive.org/web/20130709091448/http://www.freesound.org/people/ejaz215/sounds/33937/`) [V]
- [alharamain.gov.sa policies](https://alharamain.gov.sa/public/?page=authority-terms-ar), [moia.gov.sa terms of use](https://www.moia.gov.sa/Pages/Terms-Of-Use.aspx), [awqaf.gov.jo copyright](https://www.awqaf.gov.jo/AR/Pages/%D8%AD%D9%82%D9%88%D9%82_%D8%A7%D9%84%D9%86%D8%B4%D8%B1) [V]
- [Saudipedia: الأذان في المسجد الحرام](https://saudipedia.com/الأذان-في-المسجد-الحرام), [al-madina.com on the Haram muezzins](https://www.al-madina.com/article/632667/محليات/24-صوتا-تعانق-عنان-السماء-داخل-مكبرية-الحرم) (background only)
- GitHub repositories linked in §4 (trees and READMEs read via the GitHub API) [V]
- archive.org advancedsearch (`licenseurl:*creativecommons*`, 347 hits) [V]
