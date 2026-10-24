# Changelog

All notable changes are listed here. Versions follow [Semantic Versioning](https://semver.org/); entries are added
automatically by [release-please](https://github.com/googleapis/release-please) from
[Conventional Commit](https://www.conventionalcommits.org/) messages.

## 0.2.0 (2026-10-24)

### Features

* Mosque board: full-screen clock, today's adhan and iqama times, and a live countdown.
* Quran Radio player you can open any time; it stops for the adhan.
* Colour themes (black and gold, emerald, navy, silver) for the whole app and the adhan screen.
* Two welcome slides on first launch: what the app does, then city and permission.
* Permissions page: every permission shows its state and can be turned on or off; location sits with the city.
* A recorded adhan stays on screen exactly as long as it plays; the radio follows the chosen minutes.
* «عن البرنامج»: every source opens a page with its full licence, links and QR codes; changelog; version;
  suggestions QR code; restart and reset-to-defaults.
* Narrower menu with the version at its foot; pressing a section returns to its top; Back goes to the menu.
* Licence changed to MIT.
* «أذانك الخاص»: play your own adhan file from the TV (Android/data/app.adhantv/files/adhan.mp3, .m4a, .webm or .ogg).

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
