# Credits and licences

| What | Source | Licence | In the repo |
|---|---|---|---|
| Quran font (KFGQPC HAFS Uthmanic Script v3.0) | King Fahd Glorious Quran Printing Complex, [developers platform](https://qurancomplex.gov.sa/quran-dev/) | KFGQPC EULA: free use, copy and distribution; no modification, no sale | `app/src/main/res/font/kfgqpc_hafs_v30.ttf`, unmodified (SHA-256 `9ccd8232…3c57`). Licence: [licenses/KFGQPC-HAFS-EULA.txt](licenses/KFGQPC-HAFS-EULA.txt) |
| Quran text | KFGQPC developer data `kfgqpc_hafs_v30.json` (pairs with the font) | KFGQPC | `app/src/main/assets/verses.json`, built by `tools/verses.py` — never typed by hand |
| Prayer-time calculation | [batoulapps/adhan-kotlin](https://github.com/batoulapps/adhan-kotlin) | MIT | Gradle dependency |
| Recorded adhan | "Maghrib Adhan at the Masjid al Haram, Mecca - 25 Feb, 2012" by 3omar Faruq, [Wikimedia Commons](https://commons.wikimedia.org/wiki/File:Maghrib_Adhan_at_the_Masjid_al_Haram,_Mecca_-_25_Feb,_2012.webm) | [CC BY 3.0](https://creativecommons.org/licenses/by/3.0/) | `app/src/main/res/raw/adhan_haram.m4a` — audio only, trimmed to the adhan, mono, fades, AAC 64 kbps (`tools/adhan_audio.py`) |
| Live radio | Saudi Quran Radio, Saudi Broadcasting Authority | Streamed, never stored or re-hosted | URL in `LiveRadio.kt` |
| City list | [GeoNames](https://www.geonames.org/) `cities15000` + Arabic alternate names | [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/) | `app/src/main/assets/cities.tsv`, built by `tools/cities.py` |
| Amiri Bold | [Amiri](https://github.com/aliftype/amiri) via Google Fonts | SIL OFL 1.1 | [licenses/Amiri-OFL.txt](licenses/Amiri-OFL.txt) |
| IBM Plex Sans Arabic | [IBM Plex](https://github.com/IBM/plex) via Google Fonts | SIL OFL 1.1 | [licenses/IBMPlexSansArabic-OFL.txt](licenses/IBMPlexSansArabic-OFL.txt) |
| AndroidX, Compose for TV, DataStore | Google | Apache 2.0 | Gradle dependencies |

The app shows these credits on its «عن البرنامج» page.
