# Contributing

Thank you for helping. A few rules keep the app trustworthy:

1. **Quran text is never typed by hand.** Add a verse by adding its key to `tools/verses.py` and regenerating `verses.json` from the official KFGQPC data.
2. **The KFGQPC font file stays byte-for-byte unmodified** (no subsetting, no conversion).
3. **Every bundled asset needs a clear licence** recorded in `CREDITS.md`, and a script in `tools/` that rebuilds it from its source.
4. **No ads, no tracking, no accounts, no servers of our own.**
5. Prayer-time changes need tests in `core/prayer` against official timetables (±1 minute).

Before a pull request:

```sh
./gradlew :core:prayer:test assembleDebug lint
```

Test on a real TV with the debug trigger:

```sh
adb shell am broadcast -a app.adhantv.TEST -n app.adhantv/.schedule.TestReceiver --ei delay 60 --ei seconds 30
```

Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/) — `feat: …`, `fix: …`,
`docs: …` — because the version number and CHANGELOG.md are generated from them (release-please). Never edit
`version.txt` or the changelog by hand.

Code: Kotlin, Compose for TV, no DI framework, comments explain *why*. UI text is Arabic first.
By contributing you agree your work is licensed under the MIT licence.
