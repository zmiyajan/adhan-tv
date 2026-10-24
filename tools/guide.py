"""Print the guides in docs/guide/*.html to PDF, with every verse taken from the official KFGQPC data.

Verses are written in the HTML as {{surah:ayah:words}}, e.g. {{4:103:14}} (from word 14 to the end) or
{{2:43:1-2}} (words 1–2), so no Quran text is ever typed by hand.

Usage: python3 tools/guide.py <path to kfgqpc_hafs_v30.json>   (macOS, needs Google Chrome)
"""
import json
import pathlib
import re
import subprocess
import sys

CHROME = "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"
data = {(a["sura_no"], a["aya_no"]): a for a in json.load(open(sys.argv[1], encoding="utf-8"))}


def verse(m):
    sura, aya, span = m.group(1), m.group(2), m.group(3)
    words = [w for w in data[(int(sura), int(aya))]["aya_text_unicode"].split() if not w.startswith("۝")]
    first, _, last = span.partition("-")
    return " ".join(words[int(first) - 1:int(last) if last else None])


guides = pathlib.Path(__file__).resolve().parent.parent / "docs/guide"
for src in sorted(guides.glob("*.html")):
    if src.name.startswith("_"):
        continue
    tmp = src.with_name("_" + src.name)
    tmp.write_text(re.sub(r"\{\{(\d+):(\d+):([\d-]+)\}\}", verse, src.read_text(encoding="utf-8")), encoding="utf-8")
    pdf = src.with_suffix(".pdf").with_name({"guide.html": "adhan-tv-guide.pdf", "guide-simple.html": "adhan-tv.pdf"}[src.name])
    subprocess.run([CHROME, "--headless=new", "--disable-gpu", "--allow-file-access-from-files", "--no-pdf-header-footer",
                    f"--print-to-pdf={pdf}", tmp.as_uri()], check=True, capture_output=True)
    tmp.unlink()
    print(pdf.name)
