# 01 — ترخيص المحتوى القرآني والحديثي والأذكار (Quran / Hadith / Adhkar content licensing)

> Research date: 2026-10-24. Not legal advice.
> **Legend:** ✅ **Verified** = I read the primary source myself (file bytes, official page, or API response). ⚠️ **Secondary** = taken from a search-engine summary or a third party. ❓ **Unverified / open**.
> Scope: an open-source Android TV adhan/iqama app with an Arabic UI, published on Google Play and in a public GitHub repo.

---

## الخلاصة التنفيذية (TL;DR)

| Item | Recommendation | Confidence |
|---|---|---|
| Quran font | Bundle the **official KFGQPC HAFS Uthmanic Script TTF, byte-for-byte unmodified**, with its EULA. Keep the app free of charge. Do **not** subset it or convert it to WOFF. | ✅ EULA grants Use/Copy/Distribute. ❓ The EULA contradicts itself on the word "Reproduced". |
| Quran text | Bundle the **KFGQPC developer JSON** (`kfgqpc_hafs_v30.zip`), which is designed to pair with that font. Tanzil is the fallback. Quran Foundation API is **not suitable** as the primary source. | ✅ Data and pairing verified. ❓ The data carries no explicit licence file. |
| Fallback font | **Amiri Quran** (OFL-1.1), but only paired with **Tanzil Uthmani** text. | ✅ OFL verified. ❓ Rendering not tested. |
| Verses | 22 primary keys, all matched to the root ص-ل-و or to the call/mosque vocabulary (§3). | ✅ Keys verified in KFGQPC data. The selection itself is mine and needs scholarly review. |
| Hadith | Show only the Arabic *matn* of Bukhari/Muslim (public domain), cited with sunnah.com numbering. 10+ references are verified in §4. | ✅ Numbering verified. ❓ sunnah.com/Dorar terms are blocked (403). |
| Adhkar | *Hisn al-Muslim* as a compilation is **under copyright until about 2068**. Source each dhikr from its hadith reference instead of copying the book. | ✅/⚠️ (see §5) |

---

## 1. خط KFGQPC "Uthmanic Script HAFS"

### 1.1 Licence text (✅ verified from the font file itself)

I downloaded the official developer package [`kfgqpc_hafs_v30.zip`](https://download.qurancomplex.gov.sa/resources_dev/kfgqpc_hafs_v30.zip). It is linked from the [KFGQPC Developers Platform](https://qurancomplex.gov.sa/quran-dev/). I then parsed the TTF `name` table, which reports `KFGQPC HAFS Uthmanic Script Regular`, `Version 3.0`, designer "Ashfaq Ahmad Niazi".

**nameID 13 (License Description)**, verbatim excerpt:

> "ELECTRONIC END-USER LICENSE AGREEMENT … Copyright (c) 2010 by King Fahd Glorious Quran Printing Complex (KFGQPC) … All Rights Reserved. KFGQPC retains full title and ownership of this Typeface both as artwork and font software. This Agreement does not grant you any intellectual property rights in the Font.
> **Permission is hereby granted, Free of Cost, to any person obtaining a copy of this Font accompanying this license, the rights to Use, Copy, Distribute**, subject to the following conditions:
> 1. The Font Software cannot be **Sold, Modified, Altered, Translated, Reverse Engineered, Decompiled, Disassembled, Reproduced** or Attempted to discover the Source Code of this Font in no means.
> 2. The Font Software is provided "AS IS" …"

**nameID 0 (Copyright)**, verbatim:

> "… All rights reserved. This Font is the property of King Fahd Glorious Quran Printing Complex, and **may not be reproduced, modified without the express written approval** of King Fahd Glorious Quran Printing Complex."

The same restrictive sentence is catalogued by ScanCode as [`LicenseRef-scancode-kfgqpc-uthmanic-script-hafs`](https://scancode-licensedb.aboutcode.org/kfgqpc-uthmanic-script-hafs.html), category "Proprietary Free". The "Hafs Smart" font in [`kfgqpc_hafs_smart_4.zip`](https://download.qurancomplex.gov.sa/resources_dev/kfgqpc_hafs_smart_4.zip) carries the identical EULA (✅ verified).

**The text is internally contradictory (❓).** It grants "Copy, Distribute" but forbids "Reproduced", and nameID 0 asks for written approval to reproduce. The most reasonable reading is that the explicit grant covers distributing **unmodified copies with the licence attached**, and that "reproduce" targets re-creating or re-drawing the typeface. That reading is my interpretation, not a ruling.

### 1.2 Answers to the questions

| Question | Answer |
|---|---|
| Bundle in an APK on Google Play? | **Yes, under the explicit grant "Use, Copy, Distribute, Free of Cost"**, with these conditions: (a) **"cannot be Sold"**: keep the app free and never put the font or Quran feature behind a payment. Whether ads or IAP elsewhere in the app count as "selling" is ❓, so the safest choice is a free app with no ads. (b) **"cannot be Modified"**: ship the TTF **byte-exact** in `res/font` or `assets`. Do not subset it, convert it to WOFF/WOFF2, rename it, or let any build step rewrite its tables. Verify the SHA-256 in CI. (c) The grant applies to a copy "**accompanying this license**", so ship the EULA text. |
| Public GitHub repo? | **Yes on the same basis** (unmodified copy plus EULA). The repo's own licence (MIT/Apache/GPL) **must exclude** the font. Put it in e.g. `third_party/kfgqpc/` with `LICENSE-KFGQPC.txt` (the nameID 13 text) and a NOTICE saying the font is KFGQPC property and not covered by the project licence. ❓ F-Droid would likely reject a non-free font asset (not checked). |
| Attribution? | The EULA does not demand on-screen credit, but it requires the licence to accompany the copy. Recommended: an "Open-source licences" screen showing the EULA, plus the credit "الخط: مجمع الملك فهد لطباعة المصحف الشريف — KFGQPC" with a link to https://qurancomplex.gov.sa/quran-dev/. |
| Download at runtime from the official source? | **Feasible but not recommended.** The direct URL works and the platform publishes a SHA-256 that ✅ matches the zip (`227E6B15…F245`). However, the zip was updated on 2026-09-30 (`Last-Modified` header, "تحديث 15.0"), so a pinned hash breaks on the next update. The zip is about 10 MB and contains DOCX/PDF/SQL files the app does not need. Several older official URLs now return 404 (`dm.qurancomplex.gov.sa/en/?p=138`, `qurancomplex.gov.sa/techquran/dev`), which shows link rot. No hot-linking terms are published. Bundling is simpler and also allowed. |

### 1.3 Two official builds with different bytes (✅)

| Source | File | SHA-256 |
|---|---|---|
| Developers platform `kfgqpc_hafs_v30.zip` (recommended) | `kfgqpc_hafs_v30.ttf` (301,204 B) | `9ccd82324319bfe9e57efd34c830770ce86655f01661ca06b50e3b0c20073c57` |
| fonts.qurancomplex.gov.sa [`/ten-readings`](https://fonts.qurancomplex.gov.sa/ten-readings) → `KFGQPC-Hafs-V30.zip` | `KFGQPC Hafs V30.ttf` | `c9dd7e71a454565b13d89e5e6f2663a918acb0a20a23eea31eccb32a3aeec47a` |

Use the **developers-platform build**. It is versioned and shipped together with the matching text data (§2.3). Note that the font's internal version ("Version 3.0") and the platform's update counter ("15.0") are different numbering schemes.

### 1.4 Legal alternatives (OFL), ✅ licence verified via Google Fonts `METADATA.pb`

| Font | Licence | Quranic suitability |
|---|---|---|
| [Amiri Quran](https://github.com/google/fonts/tree/main/ofl/amiriquran) (Khaled Hosny, Sebastian Kosch) | OFL | Purpose-built for Quran; best OFL fallback. [Amiri README](https://github.com/aliftype/amiri) (OFL-1.1). |
| [Scheherazade New](https://software.sil.org/scheherazade/) (SIL) | OFL 1.1, Reserved Font Names "Scheherazade"/"SIL" | Near-complete Unicode Arabic coverage. Its own page describes a "simplified" style with few ligatures, so it is less mushaf-like. |
| [Noto Naskh Arabic](https://github.com/google/fonts/tree/main/ofl/notonaskharabic) | OFL | General UI Naskh, **not** a Quranic font. Use it for UI or hadith text, not for verses. |

- **"KFGQPC derivatives on Google Fonts":** ❓ I found none. Any derivative would breach "cannot be Modified".
- **Important pairing constraint:** Uthmani text and font are encoded and tested **as pairs**. The KFGQPC data column is documented as "Text of the Aya in kfgqpc_hafs_v3 font" (✅ [quran-dev page](https://qurancomplex.gov.sa/quran-dev/)). Tanzil's options reference the `me_quran` font ([tanzil.net/download](https://tanzil.net/download/)). **Do not mix sources.** If you fall back to Amiri Quran, use Tanzil Uthmani text. ❓ Rendering of KFGQPC v3 text with Amiri Quran, Scheherazade New or Noto is **not tested**; check it on a real device.

---

## 2. النص القرآني (Quran text)

### 2.1 Tanzil (✅ [tanzil.net/docs/text_license](https://tanzil.net/docs/text_license))
- Copyright © 2007-2021 Tanzil Project, "Creative Commons Attribution 3.0".
- "Permission is granted to copy and distribute verbatim copies of this text, but **CHANGING IT IS NOT ALLOWED**." In practice this is BY plus no-derivatives, despite the CC BY label.
- Use in apps is allowed if you "clearly indicate the source (Tanzil Project) and **link to tanzil.net**".
- Keep the copyright notice in every verbatim copy and in derived files. Watch https://tanzil.net/updates/.
- Download requires agreeing to the terms ([download page](https://tanzil.net/download/)).

### 2.2 Quran Foundation / quran.com API (✅ [developer terms](https://api-docs.quran.foundation/legal/developer-terms/), "Last updated: 2026-10-11")
- QF content must not be cached "longer than 1 week" unless you use **Content Sync**, which must re-sync every 7 days.
- Content Sync "does not allow you to ship a prepackaged database or build-time bundle".
- Fonts and Mushaf images may be bundled "as an integrated part of the Application", **with credit to Quran Foundation**.
- Credentials must be kept confidential. You cannot ship a client secret in an open-source APK, so you would need your own backend.
- Ads and IAP are allowed. Redistributing raw data is not.
- **Verdict:** this is the wrong fit for an offline TV screen with a public repo. Do not use it as the primary source.

### 2.3 KFGQPC official text (✅ files inspected)
- The [Developers Platform](https://qurancomplex.gov.sa/quran-dev/) says the data "يمكن استخدامه في تطوير التطبيقات والبرمجيات" (can be used in developing apps and software). It is offered as Excel/CSV/HTML5/SQL/XML/JSON/TXT.
- `kfgqpc_hafs_v30.json` has 6,236 ayahs with the fields `sura_no`, `aya_no`, `aya_text_unicode` (for the v3 font) and `aya_text_emlaey` (plain spelling, for search). The `aya_text_unicode` value ends with the ayah-end marker and number (e.g. `… ۝١`). Strip it or render it deliberately.
- The data files contain **no licence or copyright text** (grep found 0 hits). The permission rests on the platform page above and on the KFGQPC statement about its digital Mushaf. That statement ([qurancomplex.gov.sa/en/?p=4713](https://qurancomplex.gov.sa/en/?p=4713), Warsh vector edition) says it "يَسمَحُ باستخدام النسخة المذكورة مجاناً … في المجالات الشخصية والتجارية … ومواقع الإنترنت والبرامج الحاسوبية". That statement concerns a different product, so applying it here is ❓.
- The [2021 announcement](https://qurancomplex.gov.sa/en/?p=5536) states the goal of publishing "نسخ سليمة ومدققة وجاهزة للاستخدام" for software developers.

### 2.4 Which source is safest?

1. **KFGQPC JSON plus the KFGQPC font**: an official Saudi source, audited, bundleable, and paired with the font. Attribution: "النص القرآني والخط: مجمع الملك فهد لطباعة المصحف الشريف، منصة مطوري برمجيات القرآن الكريم — qurancomplex.gov.sa/quran-dev". Ship the text unaltered and record the package version and SHA-256 in the repo.
2. **Tanzil Uthmani plus Amiri Quran**: the fallback if you want fully OFL fonts. Attribution: "Quran text: Tanzil Project — https://tanzil.net" plus the verbatim notice.
3. **Quran Foundation API**: avoid.

❓ **Open item:** for certainty, request written confirmation from info@qurancomplex.gov.sa that an open-source app may bundle the unmodified font and data. Use your own address, not a shared or official mailbox.

---

## 3. آيات عن الصلاة (verse keys only; the app fetches the exact text)

**Method and source:**
- I drew candidates from the Quranic Arabic Corpus concordance for root **ص-ل-و** ([corpus.quran.com, root Slw](https://corpus.quran.com/qurandictionary.jsp?q=Slw)), the University of Leeds academic morphology project. Caveat: my paging requests all returned the same first page, so I only saw occurrences up to 21:73.
- I then ✅ checked every key below in the **KFGQPC data** (`aya_text_emlaey`) for prayer vocabulary: صل/أقم/نودي/ناديتم/مساجد/ركع/سجد.
- The *selection* for an adhan screen is my curation, not a published list, so have it reviewed by a qualified person (e.g. the masjid imam).
- Third-party topical lists exist ([surahquran.com topic 114](https://surahquran.com/ayat_Topic-114-en.html), [inquran.com/topic/salah](https://www.inquran.com/topic/salah)), but they are not authoritative.

### 3.1 Primary list (22 keys, all root- or term-matched ✅)

| # | Key | Theme (suggested screen) |
|---|---|---|
| 1 | 2:3 | Establishing prayer as a trait of the God-fearing |
| 2 | 2:43 | Establish prayer; bow with those who bow (congregation, iqama) |
| 3 | 2:45 | Seek help through patience and prayer |
| 4 | 2:110 | Establish prayer; good is stored with Allah |
| 5 | 2:153 | Patience and prayer |
| 6 | 2:238 | Guard the prayers and the middle prayer |
| 7 | 4:103 | Prayer is prescribed at fixed times (adhan screen) |
| 8 | 5:6 | Wudu before prayer (between adhan and iqama) |
| 9 | 5:58 | "When you call to prayer" (adhan) |
| 10 | 11:114 | Prayer at the two ends of the day (times) |
| 11 | 14:40 | Supplication: make me one who establishes prayer |
| 12 | 17:78 | Prayer from the sun's decline; Qur'an al-Fajr (Fajr screen) |
| 13 | 20:14 | Establish prayer for My remembrance |
| 14 | 20:132 | Enjoin prayer on your family |
| 15 | 23:2 | Humility in prayer (pair with 23:1) |
| 16 | 23:9 | Those who guard their prayers |
| 17 | 24:37 | Men whom trade does not distract from prayer (pair with 24:36, mosques) |
| 18 | 24:56 | Establish prayer, give zakat, obey the Messenger |
| 19 | 29:45 | Prayer restrains from indecency |
| 20 | 31:17 | Luqman: establish prayer |
| 21 | 62:9 | Call to prayer on Friday (Jumu'ah screen) |
| 22 | 108:2 | So pray to your Lord |

### 3.2 Optional (✅ term-matched)
9:18 (maintaining mosques plus prayer), 72:18 (mosques belong to Allah), 22:77 (bow and prostrate), 62:10 (after the prayer is concluded), 70:23, 70:34, 98:5, 4:102 (fear prayer), 87:15.
- 107:4–5 is a warning about heedlessness in prayer. It matches the terms but is probably unsuitable as a welcome screen.

### 3.3 Contextual only (❌ no root match; include only if a scholar approves)
- 23:1 (opening of the passage before 23:2).
- 30:17, 20:130, 50:39–40 (tasbih at set times). These match "سبح" only, and their reading as the prayer times comes from tafsir, which I did not fetch.
- 30:18, 2:239.
- 41:33: some mufassirun linked it to muezzins (❓ not verified).

---

## 4. الأحاديث (Hadith)

### 4.1 Sources and terms
- **sunnah.com API**: per the [GitHub repo](https://github.com/sunnah-com/api), you request a key by opening a GitHub issue with the ["Request for API access" template](https://github.com/sunnah-com/api/blob/master/.github/ISSUE_TEMPLATE/request-for-api-access.md). The template asks for name, **email (public)**, purpose, rate limits, and "offline dump vs API". Docs are at [sunnah.stoplight.io](https://sunnah.stoplight.io/docs/api/).
  - The repo has **no LICENSE** (GitHub API: `license: null`, ✅).
  - ❓ I could not read the sunnah.com/developers page (HTTP 403). The [search summary](https://sunnah.com/developers) (⚠️) says a key is required and the offline dump is "not yet available".
  - No content licence was found.
  - Use a dedicated contact address in the public issue, not a personal or official mailbox.
- **Dorar.net (الدرر السنية)**: [dorar.net/article/389](https://dorar.net/article/389) describes a JSONP "API الموسوعة الحديثية" for embedding search results (⚠️ search summary only; the page returned 403 to me). ❓ No terms, rate limits or licence were found. It is useful as a **manual verification and grading reference** (حكم المحدث), not as a bundled dataset.
- **fawazahmed0/hadith-api**: GitHub licence "Unlicense" (✅). I used it only to verify numbering. The Unlicense **cannot relicense** third-party English translations such as Khan or Siddiqui, which are presumably copyrighted. ❓
- **Practical rule:** the Arabic *matn* of Sahih al-Bukhari and Sahih Muslim is classical and in the public domain. Translations and curated databases are not. For an Arabic UI, store a short Arabic *matn* plus a citation, and verify each text against a printed edition or sunnah.com/dorar.net.

### 4.2 Ten-plus references
Numbering follows the sunnah.com convention: Bukhari uses the Fath al-Bari (Fu'ad 'Abd al-Baqi) numbering, and Muslim uses 'Abd al-Baqi numbers (sunnah.com may show a suffix such as "667a"). ✅ I verified each one by matching the content in the hadith-api dataset (Muslim by its `arabicnumber` field, not its sequential index).

| # | Reference | Subject |
|---|---|---|
| 1 | Bukhari 8 · Muslim 16 | Islam is built on five, including establishing prayer |
| 2 | Bukhari 527 · Muslim 85 | The most beloved deed: prayer at its time |
| 3 | Bukhari 528 · Muslim 667 | Five prayers like a river at the door |
| 4 | Bukhari 645 · Muslim 650 | Congregation is 27 degrees better |
| 5 | Bukhari 611 | When you hear the adhan, say what the muezzin says |
| 6 | Muslim 384 | Repeat after the muezzin, then send salat on the Prophet ﷺ and ask for al-wasilah |
| 7 | Bukhari 614 | The supplication after the adhan ("اللهم رب هذه الدعوة التامة…") |
| 8 | Bukhari 615 · Muslim 437 | If people knew the reward of the adhan and the first row |
| 9 | Bukhari 636 · Muslim 602 | When the iqama is called, walk with tranquillity (iqama screen) |
| 10 | Bukhari 574 · Muslim 635 | Whoever prays the two cool prayers enters Paradise |
| 11 | Muslim 657 | Whoever prays Fajr is under Allah's protection |
| 12 | Bukhari 662 · Muslim 669 | Going to the mosque morning and evening, a feast prepared |
| 13 | Muslim 82 | Between a man and shirk/kufr is abandoning prayer |
| 14 | Bukhari 631 | "صلوا كما رأيتموني أصلي" (inside the Malik ibn al-Huwayrith hadith) |

Links follow the pattern `https://sunnah.com/bukhari:528` and `https://sunnah.com/muslim:667`. ❓ I could not open them directly because of the 403. I dropped "Muslim 387 (the muezzins' long necks)" because I could not verify it in the dataset.

---

## 5. الأذكار — حصن المسلم

- **Author:** Sa'id ibn 'Ali ibn Wahf al-Qahtani, died 2018 ([Arabic Wikisource author page](https://ar.wikisource.org/wiki/مؤلف:سعيد_بن_علي_بن_وهف_القحطاني), ✅). The page carries the template **{{أعمال محمية}}** ("protected works").
- **Saudi copyright term:** life plus 50 years (Royal Decree M/41, 2003, Art. 19). Sources are secondary (⚠️): [Wikipedia](https://en.wikipedia.org/wiki/Copyright_law_of_Saudi_Arabia) and [Saudipedia](https://saudipedia.com/en/copyright-law-in-saudi-arabia). Expiry is therefore about **2068**.
- ❓ A "free to print for free distribution" waiver is widely rumoured, but I **did not find** it in any source I could read. The [IslamHouse edition](https://enc.islamhouse.com/en/books/819/1) and its [about page](https://d1.islamhouse.com/data/ar/ih_other/about/about.html) carry no rights statement. Next check: the copyright page of a printed or Ministry edition.
- **hisnmuslim.com API** (✅ reachable): `http://www.hisnmuslim.com/api/ar/husn_ar.json` is the index (e.g. ID 27 "أذكار الصباح والمساء"), and `…/api/ar/27.json` returns the items with `ARABIC_TEXT`, `REPEAT` and `AUDIO`. It is served over **HTTP** only.
  - ❓ No terms, owner identity or licence were found. `/api` returned 403.
  - The Quranic parts of the adhkar, such as Ayat al-Kursi, are in **imla'i** spelling, not Uthmani. Render Quranic segments from KFGQPC data instead (§2.3).
- **Recommendation:** the individual adhkar are Quran and hadith texts (public domain). The book's selection, arrangement, headings and notes are the protected part. Build your own small JSON of adhkar, for example after the adhan and morning/evening. Give each item its primary-source reference (e.g. the dua after the adhan = Bukhari 614; repeating after the muezzin = Muslim 384) and its count, verified against sunnah.com/dorar.net. You may mention "انظر: حصن المسلم" as a bibliography credit. Do not bulk-import the hisnmuslim.com JSON until its terms are confirmed.
- Precedent, not a licence: the open-source F-Droid app [Hisn Elmoslem](https://f-droid.org/de/packages/com.hassaneltantawy.hisnelmoslem) (⚠️ not inspected).

---

## 6. Open items

1. Written confirmation from KFGQPC (font and data bundling in an open-source app). This decision needs the owner.
2. Whether ads or IAP anywhere in the app conflict with "cannot be Sold". The recommendation is a free app with no ads.
3. A sunnah.com API key or offline dump, only if dynamic hadith is wanted. A static curated list is enough.
4. Scholarly review of the verse and hadith selection, and of the Arabic hadith wording before shipping.
5. On-device rendering test of the KFGQPC font on Android TV, including ayah markers and the 08F0–08F2 tanween codepoints.
