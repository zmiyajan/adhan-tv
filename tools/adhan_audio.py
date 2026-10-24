"""Build app/src/main/res/raw/adhan_haram.m4a from a CC BY 3.0 recording on Wikimedia Commons.

Source: "Maghrib Adhan at the Masjid al Haram, Mecca - 25 Feb, 2012.webm" by 3omar Faruq, CC BY 3.0
https://commons.wikimedia.org/wiki/File:Maghrib_Adhan_at_the_Masjid_al_Haram,_Mecca_-_25_Feb,_2012.webm
Changes (as CC BY requires us to state): audio only, trimmed to the adhan (113.5 s – 295 s), mono, short fades,
AAC 64 kbps. The attribution is shown in the app.

Usage (macOS, uses afconvert):
  1. download the Commons ".360p.mpeg4.mov" transcode of that file
  2. python3 tools/adhan_audio.py <file.mov>
"""
import array
import pathlib
import subprocess
import sys
import tempfile
import wave

START, END, FADE_IN, FADE_OUT, RATE = 113.5, 295.0, 0.5, 2.0, 44100

src = pathlib.Path(sys.argv[1])
out = pathlib.Path(__file__).resolve().parent.parent / "app/src/main/res/raw/adhan_haram.m4a"
with tempfile.TemporaryDirectory() as tmp:
    full, cut = pathlib.Path(tmp, "full.wav"), pathlib.Path(tmp, "cut.wav")
    subprocess.run(["afconvert", "-f", "WAVE", "-d", f"LEI16@{RATE}", "-c", "1", str(src), str(full)], check=True)
    with wave.open(str(full)) as w:
        a = array.array("h", w.readframes(w.getnframes()))
    a = a[int(START * RATE):int(END * RATE)]
    n, fi, fo = len(a), int(FADE_IN * RATE), int(FADE_OUT * RATE)
    for i in range(fi):
        a[i] = int(a[i] * i / fi)
    for i in range(fo):
        a[n - 1 - i] = int(a[n - 1 - i] * i / fo)
    with wave.open(str(cut), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(a.tobytes())
    out.parent.mkdir(parents=True, exist_ok=True)
    subprocess.run(["afconvert", "-f", "m4af", "-d", "aac", "-b", "64000", str(cut), str(out)], check=True)
print(f"{n / RATE:.1f} s -> {out} ({out.stat().st_size // 1024} KB)")
