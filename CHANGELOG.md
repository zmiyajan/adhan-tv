# Changelog

All notable changes are listed here. Versions follow [Semantic Versioning](https://semver.org/); entries are added
automatically by [release-please](https://github.com/googleapis/release-please) from
[Conventional Commit](https://www.conventionalcommits.org/) messages.

## [0.3.0](https://github.com/zmiyajan/adhan-tv/compare/adhan-tv-v0.2.0...adhan-tv-v0.3.0) (2026-10-24)


### Features

* «أذانك الخاص» — play an adhan file the user puts on the TV ([468f0f4](https://github.com/zmiyajan/adhan-tv/commit/468f0f4778c5de36f158cc51cb0e456583030527))
* a single «الأذان شغّال» switch replaces the pause page ([2269341](https://github.com/zmiyajan/adhan-tv/commit/22693418490f189fa7bf9f3f322e255b9cf2f459))
* Kaaba icon beside «صُنع في مكة» ([4b154ad](https://github.com/zmiyajan/adhan-tv/commit/4b154ad61c244966c99233b56ab8f72b8bdaaca2))
* link to the Grand Mosque adhans channel (opens in YouTube) ([73c7f45](https://github.com/zmiyajan/adhan-tv/commit/73c7f45b6a758b501257cc5449e0dc50f19d7937))
* one sound row (recordings or radio), Grand Mosque by default; own files replace the bundled one ([93f4d25](https://github.com/zmiyajan/adhan-tv/commit/93f4d252a07571b0ebaf0a6d996d2c767197e990))
* own adhan file may be mp3, m4a, webm or ogg ([621d392](https://github.com/zmiyajan/adhan-tv/commit/621d3924b196cbb3f28f4a8a3c90bae74e6fc8e7))
* several own adhans, each file a named choice (Android/data/app.adhantv/files/adhan/) ([43abb1f](https://github.com/zmiyajan/adhan-tv/commit/43abb1f1840b2d35a40432f2fc30d0708cd9613c))
* simpler settings — one row per prayer (sound, silent, off); location permission asked in place, managed in Permissions ([cab3e0a](https://github.com/zmiyajan/adhan-tv/commit/cab3e0a0af2bc9225c1d2ba86c292a44fbdc9042))
* themes, welcome slides, permissions page, detailed About, MIT ([c532d79](https://github.com/zmiyajan/adhan-tv/commit/c532d79771b8cc29cbf48becb4f4eef86c0cf490))


### Bug Fixes

* city and Hijri date on the Today title line, not a row of their own ([6e18fc9](https://github.com/zmiyajan/adhan-tv/commit/6e18fc9699140f0b7680f29799c21f032db0ac95))
* correct the maker's name; «صُنع في مكة» next to the version ([d733601](https://github.com/zmiyajan/adhan-tv/commit/d7336010f76b14cfd156dc7425afe4da220b7a7d))
* divider between «صُنع في مكة» and the version ([6f3b808](https://github.com/zmiyajan/adhan-tv/commit/6f3b8088dfe28c63f9535c5803ad25c7ea7eddf9))
* menu footer — «صُنع في مكة» right, version number left ([08bf691](https://github.com/zmiyajan/adhan-tv/commit/08bf691d5e2ef4a035111a94257d743920b0a627))
* move the maker's line to the foot of About ([8d04b97](https://github.com/zmiyajan/adhan-tv/commit/8d04b9710f21f058e2f15a83f5bcf4de844804f8))
* return to the opened entry in About; centre the menu title ([fa63997](https://github.com/zmiyajan/adhan-tv/commit/fa63997b1a05a22538376dfd0c6bf6ccf4fe8611))
* use the standard Kaaba symbol in the menu footer ([3c79524](https://github.com/zmiyajan/adhan-tv/commit/3c79524c68daa95c4592727b30fc9a7dde9625f3))

## 0.2.0 (2026-10-24)

### Features

* Mosque board: full-screen clock, today's adhan and iqama times, and a live countdown.
* Quran Radio player you can open any time; it stops for the adhan.
* Colour themes (black and gold, emerald, navy, silver) for the whole app and the adhan screen.
* Two welcome slides on first launch: what the app does, then city and permission.
* Permissions page: every permission shows its state and can be turned on or off there.
* «الأذان شغّال» switch at the top of «الأذان» replaces the pause page.
* Simpler settings: «المدينة» holds the city, automatic location (asks for permission on first use) and the
  method; «الأذان» holds the sound and one row per prayer: with sound, silent, or off.
* A recorded adhan stays on screen exactly as long as it plays; the radio follows the chosen minutes.
* «عن البرنامج»: every source opens a page with its full licence, links and QR codes; changelog; version;
  suggestions QR code; restart and reset-to-defaults.
* Narrower menu with the version at its foot; pressing a section returns to its top; Back goes to the menu.
* Licence changed to MIT.
* One «الصوت» row for the adhan sound: each recording, or the live Quran Radio. The default is the Grand Mosque
  recording; the radio falls back to it when offline.
* Your own adhans: every audio file put in Android/data/app.adhantv/files/ becomes a choice named after the file,
  and replaces the bundled recording (which stays as the offline fallback).

### Fixes

* The recorded adhan was silent on some TVs (offload audio output); it now plays through the normal mixer.
* The mosque board countdown sometimes stayed on a second twice.
* Start-up is about five times faster (about 0.4 s instead of 2 s).

## 0.1.0 (2026-10-24)

### Features

* Adhan and iqama full-screen at the exact second, over any app, pausing what was playing.
* Prayer times offline (Umm al-Qura and other methods), Ramadan Isha, Friday Jumu'ah.
* Sound: live Quran Radio in Makkah and Riyadh, a recorded Haram adhan elsewhere and offline.
* Settings for every option with the remote; automatic or manual city.
